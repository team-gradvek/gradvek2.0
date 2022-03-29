package com.semis.gradvek.springdb;

import com.semis.gradvek.entity.Entity;
import com.semis.gradvek.entity.EntityType;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The abstraction of the access to the Neo4j database, delegating methods to the Cypher queries
 * @author ymachkasov, ychen
 *
 */
public class Neo4jDriver implements DBDriver {
	private static final Logger mLogger = Logger.getLogger (Neo4jDriver.class.getName ());

	private final Driver mDriver;

	private Neo4jDriver (String NeoURI, String user, String password) {
		mDriver = GraphDatabase.driver (NeoURI, AuthTokens.basic (user, password));
	}

	/**
	 * Map of singleton instances keyed by access URI
	 */
	private static final Map<String, Neo4jDriver> mInstances = new HashMap<> ();

	/**
	 * Retrieves a singleton tied to the specified URI
	 * @param uri Neo4j URI
	 * @param user user name
	 * @param password user password
	 * @return the singleton driver instance
	 */
	public static DBDriver instance (String uri, String user, String password) {
		String uriOverride = System.getenv("NEO4JURL");
		if (uriOverride != null) {
			uri = uriOverride;
		}
    	Neo4jDriver ret = mInstances.getOrDefault(uri, null);
    	if (ret == null) {
    		ret = new Neo4jDriver(uri, user, password);
    		mInstances.put(uri, ret);
    	}
    	
    	return (ret);
    }

	/**
	 * Performs the command to add this entity to the database
	 * @param entity
	 */
	@Override
	public <T extends Entity> void add (T entity) {
		entity.addCommands ().forEach (c -> write (c));
	}

	/**
	 * Performs the command to add this list of entities to the database
	 * @param entity
	 */
	@Override
	public <T extends Entity> void add (Set<T> entities, boolean canCombine) {
		if (canCombine) {
			// We can get the entire batch in one long command and execute it in one tx
			String cmd = entities.stream ().map (e -> e.addCommands ().stream ().collect (Collectors.joining (" "))).collect (Collectors.joining ("\n"));
			write (cmd);
		} else {
			// all commands need to be run individually, but no reason to open/close sessions for each
			try (Session session = mDriver.session ()) {
				try (final Transaction tx = session.beginTransaction ()) {
					entities.stream ().map (e -> e.addCommands ())
					.forEach (complexCommand -> {
						complexCommand.forEach (command -> {
							tx.run (command);
						});
					});
					tx.commit ();
				}
			}
		}
	}

	/**
	 * Clears the database
	 */
	@Override
	public void clear () {
		write ("MATCH (n) DETACH DELETE n");
	}

	/**
	 * Executes the command in write mode
	 * @param command
	 */
	@Override
	public void write (String command) {
		mLogger.info (command);
		if (command != null && !command.isEmpty ()) {
			try (Session session = mDriver.session ()) {
				session.writeTransaction (tx -> {
					tx.run (command);
					return "";
				});
			}
		}
	}

	/**
	 * Counts the entities of the given type
	 * @param type the entity type for the query
	 * @return the number of entities of this type in the database
	 */
	@Override
	public int count (EntityType type) {
		mLogger.info ("Counting " + type);
		try (Session session = mDriver.session ()) {
			return session.readTransaction (tx -> {
				Result result = tx.run (EntityType.toCountString (type));
				return (result.next ().get (0).asInt ());
			});
		}

	}

	/**
	 * Creates an index on entries of the specified type, if this type supports indexing
	 * and the index does not yet exist
	 * @param type the type of the entities to index
	 */
	@Override
	public void index (EntityType type) {
		String indexField = type.getIndexField ();
		if (indexField != null) {
			mLogger.info ("Indexing " + type + " on " + indexField);
			try (Session session = mDriver.session ()) {
				session.writeTransaction (tx -> {
					tx.run (
						"CREATE INDEX " + type
						+ "Index IF NOT EXISTS FOR (n:" + type + ") ON (n." + indexField + ")"
					);
					return ("");
				});
			}
		} else {
			mLogger.info ("" + type + " does not support indexing");
		}
	}
	
	public void unique (EntityType type) {
		String indexField = type.getIndexField ();
		if (indexField != null) {
			mLogger.info ("Uniquifying " + type + " on " + indexField);
			try (Session session = mDriver.session ()) {
				session.writeTransaction (tx -> {
					tx.run (
							"MATCH (n:" + type + ")"
							+ " WITH n." + indexField + " AS " + indexField 
							+ " , collect(n) AS nodes WHERE size(nodes) > 1"
							+ " FOREACH (n in tail(nodes) | DELETE n)"
					);
					return ("");
				});
			}
		} else {
			mLogger.info ("" + type + " does not support uniquifying");
		}
	}
	
	public List<AdverseEventIntObj> getAEByTarget (String target) {
		mLogger.info("Getting adverse event by target " +target);
		try (Session session = mDriver.session()) {
			return session.readTransaction (tx -> {
				Result result = tx.run("MATCH ((Target{targetId:'" +target+ "'})-[:TARGETS]-(Drug)-[causes:\'ASSOCIATED_WITH\']-(AdverseEvent)) RETURN DISTINCT AdverseEvent.adverseEventId, AdverseEvent.meddraCode, causes.llr ORDER BY causes.llr DESC");
				List<AdverseEventIntObj> finalMap = new LinkedList<>();
				while ( result.hasNext() ) {
					Record record = result.next();
					String name = record.fields().get(0).value().asString();
					String id = record.fields().get(0).value().asString().replace(' ', '_');
					String code = record.fields().get(1).value().asString();
					AdverseEventIntObj ae = new AdverseEventIntObj(name, id, code);
					ae.setLlr(record.fields().get(2).value().asDouble());
					finalMap.add(ae);
				}
				return finalMap;
			});
		}
	}

	public void loadCsv(String url) {
		// TODO Get properties from column headers
		// TODO log the time taken to import the whole CSV
		// TODO Refactor this with index()
		try (Session session = mDriver.session()) {
			session.writeTransaction(tx -> {
				tx.run("CREATE INDEX imported_label IF NOT EXISTS FOR (n:IMPORTED) ON (n.label)");
				return 1;
			});
		}

		String command = String.format(
				"LOAD CSV FROM '%s' AS line "
						+ "  CALL { "
						+ "  WITH line "
						+ "  CREATE (:IMPORTED {label: line[0], id: line[1], name: line[2]}) "
						+ "} IN TRANSACTIONS",
				url
		);
		mLogger.info(command);
		try (Session session = mDriver.session()) {
			session.run(command);
		}

		List<String> labels = new ArrayList<>();
		try (Session session = mDriver.session ()) {
			session.readTransaction (tx -> {
				Result result = tx.run ("MATCH (n:IMPORTED) RETURN DISTINCT n.label");
				while (result.hasNext()) {
					labels.add(result.next().get(0).asString());
				}
				return labels.size();
			});
		}

		for (String label : labels) {
			String relabelCommand = "MATCH (n:IMPORTED {label:'" + label + "'}) "
					+ "SET n:" + label + " "
					+ "REMOVE n.label "
					+ "REMOVE n:IMPORTED";
			try (Session session = mDriver.session()) {
				session.writeTransaction(tx -> {
					tx.run(relabelCommand);
					return 1;
				});
			}
		}
	}
}

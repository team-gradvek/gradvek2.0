package com.semis.gradvek.springdb;

import java.util.ArrayList;
import java.util.List;

import com.semis.gradvek.entity.Entity;
import com.semis.gradvek.entity.EntityFactory;
import com.semis.gradvek.entity.EntityType;
import com.semis.gradvek.parquet.Parquet;

public class Importer {

	private final DBDriver mDriver;
	
	public Importer (DBDriver driver) {
		mDriver = driver;
	}
	
	public static final List<Entity> readEntities (Parquet parquet, EntityType type) {
		Class<? extends Entity> entityClass = type.getEntityClass ();
		final List<Entity> toImport = new ArrayList<> ();
		parquet.getData ().stream ().forEach (p -> {
			Entity entity = EntityFactory.fromParquet (entityClass, p);
			if (entity != null) {
				toImport.add (entity);
			}
		});
	
		return (toImport);
	}
	
	public final void importParquet (Parquet parquet, EntityType type) {
		final List<Entity> toImport = readEntities (parquet, type);
		
		if (toImport.size () >= 0) {
			mDriver.add (toImport, type.canCombine ());
		}
	}
}

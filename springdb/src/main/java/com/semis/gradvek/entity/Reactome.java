package com.semis.gradvek.entity;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.parquet.example.data.Group;

import com.semis.gradvek.parquet.ParquetUtils;
import com.semis.gradvek.springdb.Importer;
public class Reactome extends NamedEntity {

    private final String mReactomeId;

    public Reactome (String name, String code) {
        super (name);
        mReactomeId = code;
    }

    public Reactome(Importer importer, Group data) {
        super(data.getString ("label", 0));
        mReactomeId = data.getString ("id", 0);
        setDataset ("$" + DB_VERSION_PARAM);
    }

    @Override
    public final List<String> addCommands () {
        return Collections.singletonList("CREATE (:Reactome" + " {" + "label:\'" + StringEscapeUtils.escapeEcmaScript (getName ()) + "\', "
                + getDatasetCommandString () + ", "
                + REACTOME_ID + ":\'" + StringEscapeUtils.escapeEcmaScript (mReactomeId) + "\'})");
    }

    @Override
    public int hashCode () {
        return (mReactomeId.hashCode ());
    }

    @Override
    public String getId() {
        return mReactomeId;
    }


    @Override
    public EntityType getType() {
        return EntityType.Reactome;
    }

    @Override
    public boolean equals (Object otherObj) {
        if (otherObj instanceof Reactome) {
            return ((Reactome) otherObj).mReactomeId.equals (mReactomeId);
        } else {
            return (false);
        }
    }


}
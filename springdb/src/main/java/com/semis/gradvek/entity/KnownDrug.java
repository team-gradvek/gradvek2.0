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
public class KnownDrug extends NamedEntity {

    private final String mKnownDrugId;

    public KnownDrug (String name, String code) {
        super (name);
        mKnownDrugId = code;
    }

    public KnownDrug(Importer importer, Group data) {
        super(data.getString ("url", 0));
        mKnownDrugId = data.getString ("drugId", 0);
        setDataset ("$" + DB_VERSION_PARAM);
    }

    @Override
    public final List<String> addCommands () {
        return Collections.singletonList("CREATE (:KnownDrug" + " {" + "url:\'" + StringEscapeUtils.escapeEcmaScript (getName ()) + "\', "
                + getDatasetCommandString () + ", "
                + KNOWN_DRUG_ID + ":\'" + StringEscapeUtils.escapeEcmaScript (mKnownDrugId) + "\'})");
    }

    @Override
    public int hashCode () {
        return (mKnownDrugId.hashCode ());
    }

    @Override
    public String getId() {
        return mKnownDrugId;
    }


    @Override
    public EntityType getType() {
        return EntityType.KnownDrug;
    }

    @Override
    public boolean equals (Object otherObj) {
        if (otherObj instanceof KnownDrug) {
            return ((KnownDrug) otherObj).mKnownDrugId.equals (mKnownDrugId);
        } else {
            return (false);
        }
    }


}

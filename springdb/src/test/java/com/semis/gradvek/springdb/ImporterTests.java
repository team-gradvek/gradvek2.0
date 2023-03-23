package com.semis.gradvek.springdb;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.semis.gradvek.entity.Entity;
import com.semis.gradvek.entity.EntityType;
import com.semis.gradvek.entity.Target;
import com.semis.gradvek.graphdb.TestDBDriver;
import com.semis.gradvek.parquet.Parquet;
import com.semis.gradvek.parquet.ParquetUtils;

public class ImporterTests {

	private static Parquet mParquet = null;
	
	@BeforeAll
	public static void readFile () throws IOException {	
		Resource r = new ClassPathResource ("targets/part-00001-7c4d21db-d777-42ee-ae66-67426a0369f1-c000.snappy.parquet");
		mParquet = ParquetUtils.readResource (r);
	}
	@Test
	public void testImport () {
		List<Entity> imported = new Importer (new TestDBDriver ()).readEntities (mParquet, EntityType.Target);
		
		Assertions.assertEquals (imported.size (), 331);
		imported.forEach (e -> Assertions.assertTrue (e instanceof Target));
	}
}

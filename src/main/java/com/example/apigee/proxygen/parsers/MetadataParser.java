package com.example.apigee.proxygen.parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class MetadataParser {

	public Map<String, Object> parseMetadata(String metadataFile) {

		Yaml yaml = new Yaml();

		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(new File(metadataFile));
		} catch (FileNotFoundException e) {
			System.out.println(new File(metadataFile).getAbsolutePath());
			e.printStackTrace();
		}

		Map<String, Object> proxyMetadata = yaml.load(inputStream);
		return proxyMetadata;
	}
}
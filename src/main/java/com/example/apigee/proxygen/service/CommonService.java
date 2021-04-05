package com.example.apigee.proxygen.service;

import io.swagger.v3.oas.models.OpenAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.apigee.proxygen.parsers.MetadataParser;
import com.example.apigee.proxygen.parsers.OASParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;

@Service
public class CommonService {

	private Map<String, Object> proxyMetadata;
	private String gitRepoUrl;
	private String proxyName;
	private String proxyPattern;
	private Path workDirectory;
	@Value("${config.gitlab.proxyRepo}")
	private String proxyRepo;
	
	@Autowired
	private UnzipService unzipService;

	Logger logger = LoggerFactory.getLogger(CommonService.class);

	public void initialize(String proxyMetadataFilePath, String oasFilePath, String baseWorkDirectory)
			throws IOException {

		MetadataParser metadataParser = new MetadataParser();
		OASParser oasParser = new OASParser();
		OpenAPI openAPI = new OpenAPI();

		// Read the metadata yaml and get hold of details.
		proxyMetadata = metadataParser.parseMetadata(proxyMetadataFilePath);
		logger.debug("Metadata parsing is complete: " + proxyMetadata);

		// Read the Open API spec and get hold of details
		openAPI = oasParser.parse(oasFilePath);
		proxyMetadata.put("openAPI", openAPI);
		logger.debug("Swagger parsing is complete: " + proxyMetadata);
		logger.info("Metadata and Swagger files are successfully parsed");

		// Setup working directory
		this.workDirectory = setUpWorkingDirectory(baseWorkDirectory);
		unzipService.unzip("templates.zip", this.workDirectory.toAbsolutePath().toString());

		initializeProxyName(proxyMetadata);
		initializeGitRepoUrl(proxyMetadata);
		initializeProxyPattern(proxyMetadata);
		proxyMetadata.put("gitPath", 
				this.gitRepoUrl.replace("https://github.com/", ""));

		logger.info("    Git repo URL           : " + gitRepoUrl);
		logger.info("    Proxy Name             : " + proxyName);
		logger.info("    Proxy Pattern          : " + proxyPattern);
	}

	private void initializeProxyPattern(Map<String, Object> proxyMetadata) {
		this.proxyPattern = (String) proxyMetadata.get("templateSource");
	}

	@SuppressWarnings("unchecked")
	private void initializeProxyName(Map<String, Object> proxyMetadata) {
		this.proxyName = ((Map<String, String>) proxyMetadata.get("metadata")).get("proxyName");
	}

	@SuppressWarnings("unchecked")
	private void initializeGitRepoUrl(Map<String, Object> proxyMetadata) {
		this.gitRepoUrl = this.proxyRepo+this.proxyName+".git";
	}

	private Path setUpWorkingDirectory(String baseWorkDirectory) {
		Path path = Paths.get(baseWorkDirectory);
		logger.info("Setting up workdirectory at: " + path);
		try {
			
			  if (Files.isDirectory(path)) {
				Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			}
			 
			Files.createDirectories(path);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Unable to create working directory " + path.toAbsolutePath().toString());
			System.exit(500);
		}
		return path;
	}

	public Map<String, Object> getProxyMetadata() {
		return proxyMetadata;
	}

	public void setProxyMetadata(Map<String, Object> proxyMetadata) {
		this.proxyMetadata = proxyMetadata;
	}

	public String getGitRepoUrl() {
		return gitRepoUrl;
	}

	public void setGitRepoUrl(String gitRepoUrl) {
		this.gitRepoUrl = gitRepoUrl;
	}

	public String getProxyName() {
		return proxyName;
	}

	public void setProxyName(String proxyName) {
		this.proxyName = proxyName;
	}

	public String getProxyPattern() {
		return proxyPattern;
	}

	public void setProxyPattern(String proxyPattern) {
		this.proxyPattern = proxyPattern;
	}

	public Path getWorkDirectory() {
		return workDirectory;
	}

	public void setWorkDirectory(Path workDirectory) {
		this.workDirectory = workDirectory;
	}
}

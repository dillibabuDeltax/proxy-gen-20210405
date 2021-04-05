package com.example.apigee.proxygen;

import io.swagger.v3.oas.models.OpenAPI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.apigee.proxygen.service.*;

@SpringBootApplication
public class Main implements CommandLineRunner{

	@Autowired
	private CommonService commonService;
	@Autowired
	private ProxyGenService proxyGenService;
	@Autowired
	private GitlabService gitlabService;
	
	Logger logger = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(Main.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		String proxyMetadataFilePath = null;
		String oasFilePath = null;
		String proxyGenWorkDir=null;

		logger.info("\n ======================================================");
		logger.info("Initializing Proxy Generator");

		try {
			proxyMetadataFilePath = args[0];
			oasFilePath = args[1];
			proxyGenWorkDir= args[2];

			logger.info("          Metadata file :" + proxyMetadataFilePath);
			logger.info("          Swagger file  :" + oasFilePath);

			//Initialize "workdir" with templates
			commonService.initialize(proxyMetadataFilePath, oasFilePath, proxyGenWorkDir);
			//Clone the empty remote repo
			gitlabService.initializeGitRepo();
			proxyGenService.generateAPIProxy();
			gitlabService.addCommitAndPushToGit();

			logger.info("Proxy Generation complete");
			logger.info("======================================================");
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Usage: java -jar proxy-gen.jar <<metadata.yaml>> <<oas.yaml>> <<proxyGenWorkDir>> ");
			System.exit(500);
		}
	}
}

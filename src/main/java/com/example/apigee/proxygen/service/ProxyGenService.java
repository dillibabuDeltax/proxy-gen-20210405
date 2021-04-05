package com.example.apigee.proxygen.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;

@Service
public class ProxyGenService {
	
	@Autowired
	private ZipService zipService;
	@Autowired
	private CommonService commonService;	
	@Autowired
	private MessageDigestService digesterService;
	@Autowired
	private GitlabService gitlabService;

	Logger logger = LoggerFactory.getLogger(ProxyGenService.class);

	public void generateAPIProxy() throws Exception {
		logger.info("======================================================");
		logger.info("Starting proxy generator");

		Path workDirectory = commonService.getWorkDirectory();
		String proxyPattern = commonService.getProxyPattern();
		Map<String, Object> proxyMetadata = commonService.getProxyMetadata();
		String proxyName = commonService.getProxyName();
		Path apiProxyDirectory = gitlabService.getApiProxyDirectory();

		Configuration cfg = initTemplateConfig();
		Path templateDir = workDirectory.resolve("templates").resolve(proxyPattern);
		logger.info("Loaded templates for the proxy pattern: " + proxyPattern);
		
		OpenAPI openAPI = (OpenAPI) proxyMetadata.get("openAPI") ;
		proxyMetadata.put("requestURICheck", getAvailableRequestURI(openAPI)) ;
		proxyMetadata.put("requestURIandMethodCheck", getAvailableRequestURIandMethod(openAPI)) ;
		
		if (hasMandatoryParameters(openAPI))	{
			proxyMetadata.put("mandatoryParams", true) ;
		}

		generateProxy(proxyMetadata, proxyName, cfg, templateDir, apiProxyDirectory);

		/*
		 * zipService.compressDirectory(apiProxyDirectory.toAbsolutePath().toString(),
		 * workDirectory.resolve(proxyName + ".zip").toAbsolutePath().toString());
		 */

		logger.info("Proxy generation complete");
		logger.info("====================================================== \n");
	}


	private boolean hasMandatoryParameters(OpenAPI openAPI) {
		return openAPI.getPaths()
				.values()
				.stream()
				.flatMap(path -> path.readOperations().stream())
				.flatMap(operation -> Optional.ofNullable(operation.getParameters())
										.map(Collection::stream)
										.orElseGet(Stream::empty))
				.anyMatch(p -> p.getRequired()) ;
	}
	

	private String getAvailableRequestURI(OpenAPI openAPI) {
		return openAPI.getPaths()
				.entrySet()
				.stream()
				.map(e -> String.format("request.path Matches \"%s\"", e.getKey()))
				.collect(Collectors.joining(" OR \n\t", "(", ")"));
	}

	private String getAvailableRequestURIandMethod(OpenAPI openAPI) {
		return openAPI.getPaths()
					.entrySet()
					.stream()
					.map(e -> String.format("(request.path Matches \"%s\" AND (%s))", e.getKey(), 
							getMethodStringForPath(e.getValue())))
					.collect(Collectors.joining(" OR \n\t", "(", ")"));
	}

	private List<String> getMethods(PathItem path) {
		return path.readOperationsMap()
				.keySet()
				.stream()
				.map(HttpMethod::toString)
				.collect(Collectors.toList());
	}
	
	private String getMethodStringForPath(PathItem path)	{
		return getMethods(path).stream()
			.map(m -> String.format("request.verb == \"%s\"", m))
			.collect(Collectors.joining(" OR "));
	}

	private void generateProxy(final Map<String, Object> proxyMetadata, final String proxyName, Configuration cfg,
			Path templateDir, Path targetDir) {
		logger.info("Processing proxy files at : " + targetDir);
		try {
			Files.walk(templateDir).filter(Files::isRegularFile).sorted(Comparator.reverseOrder()).forEach(src -> {
				final Template template = getTemplate(cfg, src);
				final Path dest = targetDir.resolve(templateDir.relativize(src));

				File destDir = dest.toFile().getParentFile();
				if (!destDir.exists()) {
					destDir.mkdirs();
				}
				String destFile = dest.toFile().getName();

				if (destFile.contains("target-server-template.xml")) {
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> targets = (List<Map<String, Object>>) proxyMetadata
							.get("targetEndpoints");
					Map<String, String> digest = new HashMap<>();
					targets.forEach((target) -> {
						File targetFile = new File(destDir, target.get("name") + ".xml");
						createFromTemplate(target, template, targetFile);
						digest.put((String) target.get("name"),
								digesterService.getChecksum(targetFile.getAbsolutePath()));
					});
					proxyMetadata.put("digest", digest);
				} else {
					if (destFile.contains("proxy-name")) {
						destFile = destFile.replace("proxy-name", proxyName);
					}
					createFromTemplate(proxyMetadata, template, new File(destDir, destFile));
				}
			});
			logger.info("Processing proxy files complete");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Unable to walk template directory " + templateDir.toAbsolutePath());
			System.exit(500);
		}
	}

	private void createFromTemplate(final Map<String, Object> proxyMetadata, Template template, File dest) {
		try (Writer file = new FileWriter(dest)) {
			template.process(proxyMetadata, file);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to create file " + dest.getAbsolutePath());
			System.exit(500);
		}
	}

	private Template getTemplate(Configuration cfg, Path src) {
		try {
			cfg.setDirectoryForTemplateLoading(src.toFile().getParentFile());
			Template template = cfg.getTemplate(src.toFile().getName());
			return template;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Unable to create file " + src.toAbsolutePath());
			System.exit(500);
		}
		return null;
	}

	private Configuration initTemplateConfig() {
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(true);
		cfg.setWrapUncheckedExceptions(true);
		cfg.setFallbackOnNullLoopVariable(false);
		return cfg;
	}
}

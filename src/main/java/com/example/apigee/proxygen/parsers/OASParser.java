package com.example.apigee.proxygen.parsers;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;

public class OASParser {

	public OpenAPI parse(String swaggerSpec) {
		OpenAPI openAPI = new OpenAPIV3Parser().read(swaggerSpec);		
		return openAPI;
	}
}

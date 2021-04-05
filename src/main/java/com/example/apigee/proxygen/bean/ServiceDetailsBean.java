package com.example.apigee.proxygen.bean;

import java.util.Map;

import io.swagger.v3.oas.models.PathItem.HttpMethod;

public class ServiceDetailsBean {
	
	private String path ;
	private HttpMethod method ;
	private Map<String, Object> headers;
	private Map<String, Object> parameters;
	private Object request;	
	private Map<String, Map<String, Object>> responses;
	
	
	public ServiceDetailsBean(String path) {
		super();
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public HttpMethod getMethod() {
		return method;
	}
	public void setMethod(HttpMethod method) {
		this.method = method;
	}
	public Map<String, Object> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, Object> headers) {
		this.headers = headers;
	}
	public Map<String, Object> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
	public Object getRequest() {
		return request;
	}
	public void setRequest(Object request) {
		this.request = request;
	}
	public Map<String, Map<String, Object>> getResponses() {
		return responses;
	}
	public void setResponses(Map<String, Map<String, Object>> responses) {
		this.responses = responses;
	}
	@Override
	public String toString() {
		return "ServiceDetailsBean [path=" + path + ", method=" + method + ", headers=" + headers + ", parameters="
				+ parameters + ", request=" + request + ", responses=" + responses + "]";
	}

}

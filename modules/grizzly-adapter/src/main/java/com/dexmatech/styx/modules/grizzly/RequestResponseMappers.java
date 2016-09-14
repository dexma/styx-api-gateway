package com.dexmatech.styx.modules.grizzly;

import com.dexmatech.styx.core.http.Headers;
import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.http.HttpResponse;
import com.dexmatech.styx.core.http.RequestLine;
import com.dexmatech.styx.core.utils.IOUTils;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by aortiz on 9/08/16.
 */
public class RequestResponseMappers {

	public static HttpRequest asPipelineRequest(Request grizzlyRequest) throws URISyntaxException {
		RequestLine requestLine = RequestLine.from(
				grizzlyRequest.getMethod().getMethodString(), grizzlyRequest.getRequestURI(), grizzlyRequest.getAuthType()
		);
		return HttpRequest.from(
				requestLine, extractAndConvertHeaders(grizzlyRequest), grizzlyRequest.getInputStream()
		);
	}

	public static void mapResponseFields(HttpResponse styxResponse, Response grizzlyResponse) throws URISyntaxException, IOException {
		grizzlyResponse.setStatus(styxResponse.getStatusLine().getStatusCode());
		if (styxResponse.getMessageBody().isPresent()) {
			IOUTils.fastCopy(styxResponse.getMessageBody().get(), grizzlyResponse.getOutputStream());
		}
		copyHeaders(styxResponse, grizzlyResponse);
	}

	public static void copyHeaders(HttpResponse styxResponse, Response grizzlyResponse) {
		styxResponse
				.getHeaders()
				.toMap()
				.entrySet()
				.forEach(entry -> grizzlyResponse.addHeader(entry.getKey(), entry.getValue()));
	}

	public static Headers extractAndConvertHeaders(Request grizzlyRequest) {
		Map<String, String> map = new HashMap<>();
		for (String key : grizzlyRequest.getHeaderNames()) {
			map.put(key, grizzlyRequest.getHeader(key));
		}
		return Headers.from(map);
	}

}

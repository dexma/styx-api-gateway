package com.dexmatech.styx.core.http.client;

import com.dexmatech.styx.core.http.Headers;
import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.http.HttpResponse;
import com.dexmatech.styx.core.http.StatusLine;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;

import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * Created by aortiz on 7/09/16.
 */
public class HttpMappers {

	public static Request asClientRequest(String url,HttpRequest request) {
		RequestBuilder builder = new RequestBuilder()
				.setMethod(request.getRequestLine().getMethod())
				.setUrl(url)
				.setHeaders(asClientHeaders(request.getHeaders()));
		request.getMessageBody().ifPresent(builder::setBody);
		return builder.build();
	}

	public static HttpHeaders asClientHeaders(Headers headers) {

		HttpHeaders result = new DefaultHttpHeaders();
		headers.toMap().entrySet().forEach(entry -> result.add(entry.getKey(), entry.getValue()));
		return result;
	}

	public static Headers asHeaders(HttpHeaders headers) {
		return Headers.from(headers.entries().stream().collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));
	}

	public static Function<Response, HttpResponse> generateResponseMapperFromHttpVersion(String httpVersion) {
		return (response) -> {

			StatusLine statusLine = StatusLine.from(
					httpVersion, response.getStatusCode(), response.getStatusText()
			);
			Headers headers = asHeaders(response.getHeaders());
			if(response.hasResponseBody()) {
				return HttpResponse.from(statusLine, headers, response.getResponseBodyAsStream());
			} else {
				return HttpResponse.from(statusLine, headers);
			}
		};
	}

}

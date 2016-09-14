package com.dexmatech.styx.core.http;

import com.dexmatech.styx.core.http.utils.Uris;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.net.URI;

/**
 * Created by aortiz on 9/08/16.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class RequestLine {

	private final String method;
	private final URI uri;
	private final String httpVersion;

	//TODO: Mthoid enum, URI,HTTP version method
	public static RequestLine from(String method, String requestUri, String httpVersion) {
		return new RequestLine(method, Uris.create(requestUri), httpVersion);
	}

	public static RequestLine getMethod(URI requestUri) {
		return new RequestLine("GET",requestUri,HttpMessage.VERSION_HTTP_1_1);
	}
}

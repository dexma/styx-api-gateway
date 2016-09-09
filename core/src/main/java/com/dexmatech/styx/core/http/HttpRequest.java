package com.dexmatech.styx.core.http;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by aortiz on 9/08/16.
 */
@Slf4j
@Getter
public class HttpRequest extends HttpMessage {

	private final RequestLine requestLine;

	private HttpRequest(RequestLine requestLine, Headers headers, Optional<InputStream> messageBody) {
		super(headers, messageBody);
		this.requestLine = requestLine;
	}

	public static HttpRequest from(RequestLine requestLine, Headers headers, InputStream bytes) {
		return new HttpRequest(requestLine, headers, Optional.ofNullable(bytes));
	}

	public static HttpRequest from(RequestLine requestLine, Headers headers, Optional<InputStream> bytes) {
		return new HttpRequest(requestLine, headers, bytes);
	}

	public static HttpRequest from(RequestLine requestLine, Headers headers) {
		return new HttpRequest(requestLine, headers, Optional.empty());
	}

	public static HttpRequest get(String requestUri, Headers headers) throws URISyntaxException {
		return new HttpRequest(RequestLine.getMethod(new URI(requestUri)), headers, Optional.empty());
	}

	public static HttpRequest get(String requestUri) throws URISyntaxException {
		return new HttpRequest(RequestLine.getMethod(new URI(requestUri)), Headers.empty(), Optional.empty());
	}

	public HttpRequest addHeader(String key, String value) {
		return from(this.requestLine, getHeaders().put(key, value), getMessageBody());
	}

	public HttpRequest computeHeader(String key, BiFunction<String, String, String> ifKeyPresent, Function<String, String> ifKeyAbsent) {
		return from(this.requestLine, getHeaders().compute(key, ifKeyPresent, ifKeyAbsent), getMessageBody());
	}

	public HttpRequest removeHeader(String key) {

		return from(this.requestLine, getHeaders().remove(key), getMessageBody());
	}

	public HttpRequest modifyRequestLineUri(String route) {
		return null;
	}

	@Override
	public String toString(){

		 if(log.isDebugEnabled()) {
			 boolean thereIsBody = isBodyPresent();
			return String.format("HttpRequest(%s,%s, payload?%s)", this.getRequestLine(), this.getHeaders(), thereIsBody);
		} else {
			 return String.format("HttpRequest(%s,%s)", this.getRequestLine(), this.getHeaders());
		 }
	}


}

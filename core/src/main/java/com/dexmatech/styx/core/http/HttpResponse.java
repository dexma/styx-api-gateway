package com.dexmatech.styx.core.http;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by aortiz on 9/08/16.
 */
@Slf4j
@Getter
@ToString(callSuper = true)
public class HttpResponse extends HttpMessage {
	public static final HttpResponse OK = new HttpResponse(StatusLine.ok(), Headers.empty(), Optional.empty());
	public static final HttpResponse INTERNAL_SERVER_ERROR = new HttpResponse(StatusLine.INTERNAL_SERVER_ERROR, Headers.empty(), Optional.empty());
	private final StatusLine statusLine;

	private HttpResponse(StatusLine statusLine, Headers headers, Optional<InputStream> messageBody) {
		super(headers, messageBody);
		this.statusLine = statusLine;
	}

	public static HttpResponse from(StatusLine statusLine, Headers headers, InputStream bytes) {
		return new HttpResponse(statusLine, headers, Optional.ofNullable(bytes));
	}

	public static HttpResponse ok() {
		return OK;
	}

	public static HttpResponse internalServerError() {
		return INTERNAL_SERVER_ERROR;
	}

	public static HttpResponse from(StatusLine statusLine, Headers headers) {
		return new HttpResponse(statusLine, headers, Optional.empty());
	}

	public HttpResponse addHeader(String key, String value) {
		return from(this.statusLine, getHeaders().put(key, value), getMessageBody().orElse(null));
	}

	public HttpResponse computeHeader(String key, BiFunction<String, String, String> ifKeyPresent, Function<String, String> ifKeyAbsent) {
		return from(this.statusLine, getHeaders().compute(key, ifKeyPresent, ifKeyAbsent), getMessageBody().orElse(null));
	}

	public HttpResponse removeHeader(String key) {

		return from(this.statusLine, getHeaders().remove(key), getMessageBody().orElse(null));
	}

	public HttpRequest modifyRequestLineUri(String route) {
		return null;
	}

	@Override
	public String toString(){

		if(log.isDebugEnabled()) {
			boolean thereIsBody = isBodyPresent();
			return String.format("HttpResponse(%s,%s, payload?%s)", this.getStatusLine(), this.getHeaders(), thereIsBody);
		} else {
			return String.format("HttpResponse(%s,%s)",this.getStatusLine(), this.getHeaders());
		}

	}
}

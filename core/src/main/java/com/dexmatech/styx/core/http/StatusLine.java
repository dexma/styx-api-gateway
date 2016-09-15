package com.dexmatech.styx.core.http;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by aortiz on 9/08/16.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class StatusLine {

	public static final StatusLine OK = new StatusLine(HttpMessage.VERSION_HTTP_1_1, 200, "OK");
	public static final StatusLine INTERNAL_SERVER_ERROR = new StatusLine(HttpMessage.VERSION_HTTP_1_1, 500, "Internal Server Error");
	public static final StatusLine FORBIDDEN = new StatusLine(HttpMessage.VERSION_HTTP_1_1, 401, "Forbidden");
	public static final StatusLine UNAUTHORIZED = new StatusLine(HttpMessage.VERSION_HTTP_1_1, 403, "Unauthorized");
	public static final StatusLine NOT_FOUND = new StatusLine(HttpMessage.VERSION_HTTP_1_1, 404, "Not found");

	private final String httpVersion;
	private final int statusCode;
	private final String reasonPhrase;

	//TODO: Mthoid enum, URI,HTTP version method
	public static StatusLine from(String httpVersion, int statusCode, String reasonPhrase) {
		return new StatusLine(httpVersion, statusCode, reasonPhrase);
	}

	public static StatusLine ok() {
		return OK;
	}
}

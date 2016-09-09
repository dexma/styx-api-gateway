package com.dexmatech.styx.core.http;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Created by aortiz on 11/08/16.
 */
@Slf4j
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "messageBody")
public abstract class HttpMessage {

	public static final String VERSION_HTTP_1_1 = "HTTP/1.1";

	private final Headers headers;
	private final Optional<InputStream> messageBody;

	protected boolean isBodyPresent() {
		boolean thereIsBody = false;
		try {
			thereIsBody = this.getMessageBody().isPresent()
					&& (this.getMessageBody().get().available() > 0);
		} catch (IOException e) {
			log.error("Error trying to know if there is a body present", e);
		}
		return thereIsBody;
	}



}

package com.dexmatech.styx.core.http.extractors;

import com.dexmatech.styx.core.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Optional;

/**
 * Created by aortiz on 2/09/16.
 */
@Slf4j
public enum HostExtractor implements RequestFragmentExtractor<String> {

	INSTANCE {
		@Override public Optional<String> extract(HttpRequest request) {
			URI uri = request.getRequestLine().getUri();
			if (uri.isAbsolute()) {
				return Optional.of(uri.getHost());
			} else if (request.getHeaders().contains(HOST_HEADER)) {
				return Optional.of(request.getHeaders().get(HOST_HEADER));
			} else {
				log.warn("Impossible extract host from request => {}", request);
				return Optional.empty();
			}
		}
	};

	public static final String HOST_HEADER = "Host";
}

package com.dexmatech.styx.core.http.extractors;

import com.dexmatech.styx.core.http.HttpRequest;

import java.util.Optional;

/**
 * Created by aortiz on 2/09/16.
 */
public enum PathExtractor implements RequestFragmentExtractor<String> {

	INSTANCE {
		@Override public Optional<String> extract(HttpRequest request) {
			String path = request.getRequestLine().getUri().getPath();
			if (EMPTY_PATH.equals(path)) {
				path = ROOT_PATH;
			}
			return Optional.of(path);
		}
	};

	public static final String ROOT_PATH = "/";
	public static final String EMPTY_PATH = "";
}
package com.dexmatech.styx.core.http.extractors;

import com.dexmatech.styx.core.http.HttpRequest;

import java.util.Optional;

/**
 * Created by aortiz on 2/09/16.
 */
@FunctionalInterface
public interface RequestFragmentExtractor<T> {
	Optional<T> extract(HttpRequest request);
}

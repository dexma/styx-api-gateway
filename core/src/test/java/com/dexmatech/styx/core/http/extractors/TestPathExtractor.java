package com.dexmatech.styx.core.http.extractors;

import com.dexmatech.styx.core.http.Headers;
import com.dexmatech.styx.core.http.HttpRequest;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Created by aortiz on 5/09/16.
 */
public class TestPathExtractor {

	@Test
	public void shouldExtractPathWhenAbsoluteRequestedURIWithoutPath() throws URISyntaxException {
		// given
		HttpRequest httpRequest = HttpRequest.get("https://www.as.com");
		// when
		Optional<String> path = PathExtractor.INSTANCE.extract(httpRequest);
		// then
		assertThat(path.isPresent(), is(true));
		assertThat(path.get(), is("/"));
	}

	@Test
	public void shouldExtractPathWhenAbsoluteRequestedURIWithPath() throws URISyntaxException {
		// given
		HttpRequest httpRequest = HttpRequest.get("https://www.as.com/soccer");
		// when
		Optional<String> path = PathExtractor.INSTANCE.extract(httpRequest);
		// then
		assertThat(path.isPresent(), is(true));
		assertThat(path.get(), is("/soccer"));
	}

	@Test
	public void shouldExtractPathWhenRelativeRequestedURIAndHostHeaderIsProvided() throws URISyntaxException {
		// given
		Map<String,String> headers = new HashMap<>();
		headers.put("Host","www.some_host.com");
		HttpRequest httpRequest = HttpRequest.get("/path", Headers.from(headers));
		// when
		Optional<String> path = PathExtractor.INSTANCE.extract(httpRequest);
		// then
		assertThat(path.isPresent(), is(true));
		assertThat(path.get(), is("/path"));
	}


	@Test
	public void shouldExtractPathWhenAsterikURIAndHostHeaderIsProvided() throws URISyntaxException {
		// given
		Map<String,String> headers = new HashMap<>();
		headers.put("Host","www.some_host.com");
		HttpRequest httpRequest = HttpRequest.get("*", Headers.from(headers));
		// when
		Optional<String> path = PathExtractor.INSTANCE.extract(httpRequest);
		// then
		assertThat(path.isPresent(), is(true));
		assertThat(path.get(), is("*"));
	}

}
package com.dexmatech.styx.core.http.extractors;

import com.dexmatech.styx.core.http.Headers;
import com.dexmatech.styx.core.http.HttpRequest;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by aortiz on 2/09/16.
 */
public class TestHostExtractor {

	@Test
	public void shouldExtractHostWhenAbsoluteRequestedURIWithouthPath() throws URISyntaxException {
		// given
		HttpRequest httpRequest = HttpRequest.get("https://www.as.com");
		// when
		Optional<String> host = HostExtractor.INSTANCE.extract(httpRequest);
		// then
		assertThat("Host was empty", host.isPresent(), is(true));
		assertThat("Host was wrong", host.get(), is("www.as.com"));
	}

	@Test
	public void shouldExtractHostWhenAbsoluteRequestedURIWithPath() throws URISyntaxException {
		// given
		HttpRequest httpRequest = HttpRequest.get("https://www.as.com/soccer");
		// when
		Optional<String> host = HostExtractor.INSTANCE.extract(httpRequest);
		// then
		assertThat("Host was empty", host.isPresent(), is(true));
		assertThat("Host was wrong", host.get(), is("www.as.com"));
	}

	@Test
	public void shouldExtractHostWhenRelativeRequestedURIAndHostHeaderIsProvided() throws URISyntaxException {
		// given
		Map<String, String> headers = new HashMap<>();
		headers.put("Host", "www.some_host.com");
		HttpRequest httpRequest = HttpRequest.get("/path", Headers.from(headers));
		// when
		Optional<String> host = HostExtractor.INSTANCE.extract(httpRequest);
		// then
		assertThat("Host was empty", host.isPresent(), is(true));
		assertThat("Host was wrong", host.get(), is("www.some_host.com"));
	}

	@Test
	public void shouldExtractHostWhenAbsoluteRequestedURIAndHostHeaderIsProvided() throws URISyntaxException {
		// given
		Map<String, String> headers = new HashMap<>();
		headers.put("Host", "https://www.as.com");
		HttpRequest httpRequest = HttpRequest.get("https://www.as.com", Headers.from(headers));
		// when
		Optional<String> host = HostExtractor.INSTANCE.extract(httpRequest);
		// then
		assertThat("Host was empty", host.isPresent(), is(true));
		assertThat("Host was wrong", host.get(), is("www.as.com"));
	}

	@Test
	public void shouldExtractHostWhenAsterikURIAndHostHeaderIsProvided() throws URISyntaxException {
		// given
		Map<String, String> headers = new HashMap<>();
		headers.put("Host", "www.some_host.com");
		HttpRequest httpRequest = HttpRequest.get("*", Headers.from(headers));
		// when
		Optional<String> host = HostExtractor.INSTANCE.extract(httpRequest);
		// then
		assertThat("Host was empty", host.isPresent(), is(true));
		assertThat("Host was wrong", host.get(), is("www.some_host.com"));
	}

	@Test
	public void shouldNotExtractHostWhenNoHostAndNoAbsoluteURI() throws URISyntaxException {
		// given
		HttpRequest httpRequest = HttpRequest.get("/path");
		// when
		Optional<String> host = HostExtractor.INSTANCE.extract(httpRequest);
		// then
		assertThat("Host was present",host.isPresent(), is(false));
	}

}
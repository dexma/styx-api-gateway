package com.dexmatech.styx.core.http.utils;

import com.dexmatech.styx.core.http.QueryParam;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by aortiz on 14/09/16.
 */
public class TestUris {

	@Test
	public void shouldCreateAnUriFromFullUri() {
		// when
		URI uri = Uris.create("http://www.mydomain.com:8080/api?param=1");
		// then
		assertThat(uri, notNullValue());
		assertThat(uri.getHost(), is("www.mydomain.com"));
		assertThat(uri.getAuthority(), is("www.mydomain.com:8080"));
		assertThat(uri.getPath(), is("/api"));
		assertThat(uri.getPort(), is(8080));
		assertThat(uri.getQuery(), is("param=1"));
	}

	@Test
	public void shouldCreateAnUriWithoutPort() {
		// when
		URI uri = Uris.create("http://www.mydomain.com/api?param=1");
		// then
		assertThat(uri, notNullValue());
		assertThat(uri.getHost(), is("www.mydomain.com"));
		assertThat(uri.getAuthority(), is("www.mydomain.com"));
		assertThat(uri.getPath(), is("/api"));
		assertThat(uri.getPort(), is(-1));
		assertThat(uri.getQuery(), is("param=1"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailCreatingAnUriWithInvalidCharactersOnHost() {
		// when
		Uris.create("http://www.my_domain.com/api?param=1");
	}

	@Test
	public void shouldReplaceHostWhenStandardUri() {

		// when
		String uri = Uris.changeHost(Uris.create("http://www.mydomain.com/api"), "internal.domain.dmz");
		// then
		assertThat(uri, is("http://internal.domain.dmz/api"));
	}

	@Test
	public void shouldReplaceHostWhenHostIsNotPresentUri() throws URISyntaxException {

		// when
		String uri = Uris.changeHost(Uris.create("/api"), "internal.domain.dmz");
		// then
		assertThat(uri, is("http://internal.domain.dmz/api"));
	}

	@Test
	public void shouldReplaceHostWhenPortIsPresentInUri() {

		// when
		String uri = Uris.changeHost(Uris.create("http://www.mydomain.com:8081/api"), "internal.domain.dmz");
		// then
		assertThat(uri, is("http://internal.domain.dmz:8081/api"));
	}

	@Test
	public void shouldReplaceHostAndPortWhenPortIsIncludedAsReplacement() {

		// when
		String uri = Uris.changeHost(Uris.create("http://www.mydomain.com:8081/api"), "internal.domain.dmz:9090");
		// then
		assertThat(uri, is("http://internal.domain.dmz:9090/api"));
	}

	@Test
	public void shouldReplaceHostWhenQueryIsPresent() {

		// when
		String uri = Uris.changeHost(Uris.create("http://www.mydomain.com:8081/api?param1=2&param_2=3"), "internal.domain.dmz");
		// then
		assertThat(uri, is("http://internal.domain.dmz:8081/api?param1=2&param_2=3"));
	}

	@Test
	public void shouldReplaceHostWhenQueryIsPresentWithURLEncodedSpecialCharactersInQueryParamValues() {

		// when
		String uri = Uris.changeHost(Uris.create("http://www.mydomain.com:8081/api?param1=2&param_2=3&param_3=2017-02-01T00%3A00%3A00%2B02%3A00"), "internal.domain.dmz");
		// then
		assertThat(uri, is("http://internal.domain.dmz:8081/api?param1=2&param_2=3&param_3=2017-02-01T00%3A00%3A00%2B02%3A00"));
	}

	@Test
	public void shouldExtractQueryParamsWithRepeatedParamNames() {

		// when
		List<QueryParam> params = Uris.extractQueryParams(Uris.create("http://www.mydomain.com:8081/api?param_1=2&param_2=3&param_3=3a&param_3=3b&param_3=3c"));
		// then
		assertThat(params.size(), is(5));
		assertThat(params.stream().filter(param -> param.getName().equals("param_1")).count(), is(1l));
		assertThat(params.stream().filter(param -> param.getName().equals("param_2")).count(), is(1l));
		assertThat(params.stream().filter(param -> param.getName().equals("param_3")).count(), is(3l));
		assertThat(params.stream().filter(param -> param.getName().equals("param_3")).map(param -> param.getValue()).collect(Collectors.toSet()),
				is(new HashSet(Arrays.asList("3a", "3b", "3c"))));

	}


}
package com.dexmatech.styx.core.http.utils;

import org.junit.Test;

import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import java.net.URI;

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
	public void shouldCreateAnUriWithouthPort() {
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
	public void shouldReplaceHostWhenPortIsPresent() {

		// when
		String uri = Uris.changeHost(Uris.create("http://www.mydomain.com:8081/api"), "internal.domain.dmz");
		// then
		assertThat(uri, is("http://internal.domain.dmz:8081/api"));
	}

	@Test
	public void shouldReplaceHostWhenQueryIsPresent() {

		// when
		String uri = Uris.changeHost(Uris.create("http://www.mydomain.com:8081/api?param1=2&param_2=3"), "internal.domain.dmz");
		// then
		assertThat(uri, is("http://internal.domain.dmz:8081/api?param1=2&param_2=3"));
	}


}
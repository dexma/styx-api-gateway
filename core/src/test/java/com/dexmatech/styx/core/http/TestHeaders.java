package com.dexmatech.styx.core.http;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created by aortiz on 8/09/16.
 */
public class TestHeaders {

	@Test
	public void shouldReturnDifferentInstancesWhenAddingHeaders() {
		// given
		Headers headersEmpty = Headers.empty();
		// when
		Headers headerWithSomething = headersEmpty.put("header-key", "y");
		// then
		assertThat("Headers were wrong", headersEmpty.equals(headerWithSomething), is(false));
	}


	@Test
	public void shouldReplaceHeaders() {
		// given
		Headers originalHeaders = Headers.from("header-key", "y");
		// when
		Headers changedHeaders = originalHeaders.put("header-key", "x");
		// then
		assertThat("Original headers header with 'header-key' was wrong", originalHeaders.get("header-key"), is("y"));
		assertThat("Changed headers header with 'header-key' was wrong", changedHeaders.get("header-key"), is("x"));
	}

}
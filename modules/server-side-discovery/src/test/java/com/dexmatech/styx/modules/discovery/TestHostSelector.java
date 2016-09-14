package com.dexmatech.styx.modules.discovery;

import com.dexmatech.styx.core.http.HttpRequest;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by aortiz on 13/09/16.
 */
public class TestHostSelector {


	@Test
	public void shouldRouteToDefaultHost() throws URISyntaxException {

		// given
		HttpRequest request = HttpRequest.get("/");
		HostSelector hostSelector = HostSelector.from(Collections.emptyList(), "default.host");

		// when
		String host = hostSelector.hostToRoute(request);

		//then
		assertThat(host, is("default.host"));
	}

	@Test
	public void shouldRouteToDefaultHostWhenOtherRulesDontApply() throws URISyntaxException {

		// given
		HttpRequest request = HttpRequest.get("/");
		HostSelector hostSelector = HostSelector.from(Arrays.asList(HostRoutingRule.from("/path","first.host")), "default.host");

		// when
		String host = hostSelector.hostToRoute(request);

		//then
		assertThat(host, is("default.host"));
	}

	@Test
	public void shouldRouteToMatchingHostRule() throws URISyntaxException {

		// given
		HttpRequest request = HttpRequest.get("/path");
		HostSelector hostSelector = HostSelector.from(Arrays.asList(HostRoutingRule.from("/path.*","first.host")), "default.host");

		// when
		String host = hostSelector.hostToRoute(request);

		//then
		assertThat(host, is("first.host"));
	}

	@Test
	public void shouldRouteToMatchingHostRuleAndQueryParams() throws URISyntaxException {

		// given
		HttpRequest request = HttpRequest.get("/path?test=true");
		HostSelector hostSelector = HostSelector.from(Arrays.asList(HostRoutingRule.from("/path.*","first.host")), "default.host");

		// when
		String host = hostSelector.hostToRoute(request);

		//then
		assertThat(host, is("first.host"));
	}

}
package com.dexmatech.styx.modules.grizzly;

import com.dexmatech.styx.core.http.HttpRequest;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.Protocol;
import org.glassfish.grizzly.http.server.HttpServerFilter;
import org.glassfish.grizzly.http.server.Request;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by aortiz on 22/09/16.
 */
public class TestRequestResponseMappers {




	@Test
	public void shouldParseGrizzlyRequestToStyxRequest() throws URISyntaxException {

		// given
		Connection connection = mock(Connection.class);
		HttpRequestPacket requestPacket = HttpRequestPacket.builder().uri("/path")
				.method(Method.GET)
				.query("param_1=2&param_2=2")
				.protocol(Protocol.HTTP_1_1)
				.header("Host", "localhost:" + 8080).build();

		Request request = Request.create();
		when(connection.isBlocking()).thenReturn(false);
		request.initialize(requestPacket, FilterChainContext.create(connection), null);


		// when
		HttpRequest result = RequestResponseMappers.asPipelineRequest(request);

		// then
		assertThat("Query params were not parsed correctly",result.getRequestLine().getUri().getQuery(), is("param_1=2&param_2=2"));
		assertThat("Path was not parsed correctly",result.getRequestLine().getUri().getPath(), is("/path"));
		assertThat("Method was not parsed correctly",result.getRequestLine().getMethod(), is("GET"));
		assertThat("Http version was not parsed correctly",result.getRequestLine().getHttpVersion(), is("HTTP/1.1"));
		assertThat("Headers size were not correct",result.getHeaders().toMap().entrySet(), hasSize(1));
	}

}
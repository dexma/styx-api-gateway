package com.dexmatech.styx.core;

import com.dexmatech.styx.core.http.*;
import com.dexmatech.styx.core.pipeline.HttpRequestReplyPipeline;
import com.dexmatech.styx.core.pipeline.stages.routing.DefaultRoutingStage;
import com.dexmatech.styx.testing.SocketUtils;
import com.dexmatech.styx.testing.jetty.LocalTestServer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.dexmatech.styx.testing.jetty.LocalTestServer.setUpLocalServer;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by aortiz on 8/09/16.
 */
public class TestApiPipelineIT {
	@Test
	public void shouldApplyPipelineToAnSimpleRequest() throws Exception {
		// given
		int availablePort = SocketUtils.findRandomPort();
		LocalTestServer server = setUpLocalServer().onPort(availablePort).build();
		HttpRequest request = HttpRequest.get("/", Headers.empty().put(DefaultRoutingStage
				.DEFAULT_HEADER_USED_TO_ROUTE, "http://localhost:" + availablePort));

		ApiPipeline pipeline = ApiPipeline.singlePipeline().using(
				HttpRequestReplyPipeline.pipeline().applyingDefaultRoutingStage().build()
		).build();

		// when
		server.runAndKill(() -> {
			HttpResponse reply = pipeline.reply(request).get();
			// then
			assertThat(reply, notNullValue());
			assertThat(reply.getStatusLine().getStatusCode(), is(200));
		});

	}

	@Test
	public void shouldApplyPipelineWhenStaticRouteGenerationIsUsed() throws Exception {
		// given
		int availablePort = SocketUtils.findRandomPort();
		LocalTestServer server = setUpLocalServer().onPort(availablePort).withVirtualHost("virtual.com").build();
		HttpRequest request = HttpRequest.get("/");
		ApiPipeline pipeline = ApiPipeline.singlePipeline().using(
				HttpRequestReplyPipeline
						.pipeline()
						.applyingStaticHostOnRouteGeneration("virtual.com")
						.applyingDefaultRoutingStage().build()
		).build();

		// when
		server.runAndKill(() -> {
			HttpResponse reply = pipeline.reply(request).get();
			// then
			assertThat(reply, notNullValue());
			assertThat(reply.getStatusLine().getStatusCode(), is(200));
		});

	}

	@Test
	public void shouldReportAnErrorWhenPipelineIsAborted() throws Exception {
		// given

		HttpRequest request = HttpRequest.get("/");
		List<String> reportedErrors = new ArrayList<>();

		ApiPipeline pipeline = ApiPipeline.singlePipeline().using(
				HttpRequestReplyPipeline
						.pipeline()
						.applyingDefaultRoutingStage()
						.handlingNonExpectedErrorsWith((r, t) -> reportedErrors.add(t.getMessage()))
						.build()
		).build();

		// when
		HttpResponse reply = pipeline.reply(request).get();
		// then
		assertThat(reply, notNullValue());
		assertThat(reply.getStatusLine().getStatusCode(), is(500));
		assertThat(reportedErrors.size(), is(1));
		assertThat(reportedErrors.get(0), startsWith("Impossible extract route from header "));
	}

}
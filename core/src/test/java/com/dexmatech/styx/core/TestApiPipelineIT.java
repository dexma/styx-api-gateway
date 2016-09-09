package com.dexmatech.styx.core;

import com.dexmatech.styx.core.http.Headers;
import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.http.HttpResponse;
import com.dexmatech.styx.core.pipeline.HttpRequestReplyPipeline;
import com.dexmatech.styx.core.pipeline.stages.routing.DefaultRoutingStage;
import com.dexmatech.styx.utils.jetty.LocalTestServer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.dexmatech.styx.utils.jetty.LocalTestServer.setUpLocalServer;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by aortiz on 8/09/16.
 */
public class TestApiPipelineIT {
	@Test
	public void shouldApplyPipelineToAnSimpleRequest() throws Exception {
		// given
		LocalTestServer server = setUpLocalServer().build();
		HttpRequest request = HttpRequest.get("/", Headers.empty().put(DefaultRoutingStage
				.DEFAULT_HEADER_USED_TO_ROUTE, "http://localhost:" + server.getRunningPort()));

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
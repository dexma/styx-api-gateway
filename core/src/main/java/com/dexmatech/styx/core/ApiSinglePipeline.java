package com.dexmatech.styx.core;

import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.http.HttpResponse;
import com.dexmatech.styx.core.pipeline.HttpRequestReplyPipeline;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.concurrent.CompletableFuture;

/**
 * Created by aortiz on 12/08/16.
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ApiSinglePipeline extends ApiPipeline {

	private final HttpRequestReplyPipeline replyPipeline;

	public CompletableFuture<HttpResponse> reply(final HttpRequest request) {
		return replyPipeline.applyHttpReplyProtocol(request);
	}
}

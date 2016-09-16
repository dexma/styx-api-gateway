package com.dexmatech.styx.modules.ratelimit;

import java.util.concurrent.CompletableFuture;

/**
 * Created by aortiz on 14/09/16.
 */
@FunctionalInterface
public interface RateLimitProvider {
	CompletableFuture<RateLimitStatus> apply(String key);
}

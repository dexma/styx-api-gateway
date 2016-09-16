package com.dexmatech.styx.modules.ratelimit;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Created by aortiz on 16/09/16.
 */
@Getter
@AllArgsConstructor
public class RateLimitStatus {
	private boolean allowed;
	private List<RateLimit> currentRateLimits;
}

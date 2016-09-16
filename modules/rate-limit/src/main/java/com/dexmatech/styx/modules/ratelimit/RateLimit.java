package com.dexmatech.styx.modules.ratelimit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;

/**
 * Created by gszeliga on 24/04/15.
 */
@Getter
@ToString
@AllArgsConstructor
public class RateLimit {

	private String typePrefix;
	private long limit;
	private Duration reset;
	private long remaining;

}

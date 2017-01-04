package com.dexmatech.styx.modules.discovery;

import org.junit.Test;

import java.util.regex.PatternSyntaxException;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.*;

/**
 * Created by aortiz on 13/09/16.
 */
public class TestHostRoutingRule {

	@Test
	public void shouldCreateHostRoutingRule(){
		// when
		HostRoutingRule rule = HostRoutingRule.from(".*", "abc.dexma.dmz");
		// then
		assertThat("Rule was null", rule, notNullValue());
	}

	@Test(expected = PatternSyntaxException.class)
	public void shouldFailWhenInvalidRegexPath(){
		// when
		HostRoutingRule.from("*", "abc.dexma.dmz");
	}

	@Test
	public void shouldCreateDefaultHostRoutingRule(){
		// when
		HostRoutingRule rule = HostRoutingRule.defaultHostRule("abc.dexma.dmz");
		// then
		assertThat("Rule was null", rule, notNullValue());
	}

	@Test
	public void shouldCreateDefaultHostRoutingRuleasd(){
		// when
		HostRoutingRule rule = HostRoutingRule.defaultHostRule("abc.dexma.dmz");
		// then
		assertThat("Rule was null", rule, notNullValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenMalformedHost(){
		// when
		HostRoutingRule.from("*", "__3");
	}

}
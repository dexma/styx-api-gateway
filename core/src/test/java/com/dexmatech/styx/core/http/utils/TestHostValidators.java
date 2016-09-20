package com.dexmatech.styx.core.http.utils;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Created by aortiz on 5/09/16.
 */
public class TestHostValidators {

	@Test
	public void shouldValidateSuccessfullyAnIPV4() {
		// then
		assertThat(HostValidators.HOST_WITH_OPTIONAL_PORT_VALIDATOR.test("127.0.0.1"), is(true));
	}

	@Test
	public void shouldNonValidateMalformedIPV4() {
		// then
		assertThat(HostValidators.HOST_WITH_OPTIONAL_PORT_VALIDATOR.test("127.0.0.1123123"), is(false));
	}

	@Test
	public void shouldNonValidateMalformedTooLongIPV4() {
		// then
		assertThat(HostValidators.HOST_WITH_OPTIONAL_PORT_VALIDATOR.test("127.0.0.1.1.1.1"), is(false));
	}

	@Test
	public void shouldValidateSuccessfullyAnIPV6() {
		// then
		assertThat(HostValidators.HOST_WITH_OPTIONAL_PORT_VALIDATOR.test("3ffe:1900:4545:3:200:f8ff:fe21:67cf"), is(true));
	}

	@Test
	public void shouldNonValidateMalformedIPV6() {
		// then
		assertThat(HostValidators.HOST_WITH_OPTIONAL_PORT_VALIDATOR.test("3ffe:1900:4545:3:200:f8ff:fe21:67cfsdasd"), is(false));
	}

	@Test
	public void shouldValidateSuccessfullySimpleHost() {
		// then
		assertThat(HostValidators.HOST_WITH_OPTIONAL_PORT_VALIDATOR.test("www.dexcell.com"), is(true));
	}

	@Test
	public void shouldValidateSuccessfullyWhenFullyQualifiedDomainName() {
		// then
		assertThat(HostValidators.HOST_WITH_OPTIONAL_PORT_VALIDATOR.test("sensu00-in.c.divine-aegis-105709.internal"), is(true));
	}

	@Test
	public void shouldValidateSuccessfullyWhenFullyQualifiedDomainNameAndPort() {
		// then
		assertThat(HostValidators.HOST_WITH_OPTIONAL_PORT_VALIDATOR.test("www.dexcell.com:8081"), is(true));
	}


	@Test
	public void shouldNonValidateHostsWithSlash() {
		// then
		assertThat(HostValidators.HOST_WITH_OPTIONAL_PORT_VALIDATOR.test("www.dexcell.com/"), is(false));
	}

}
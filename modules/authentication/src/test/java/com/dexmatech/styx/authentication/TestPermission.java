package com.dexmatech.styx.authentication;

import org.junit.Test;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.*;

/**
 * Created by aortiz on 15/09/16.
 */
public class TestPermission {

	@Test
	public void shouldCreatePermission() {
		// when
		Permission permission = Permission.of("USER", "READ");
		// then
		assertThat(permission, notNullValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenInvalidCharactersInTarget() {
		// when
		Permission.of("USéR", "READ");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenInvalidCharactersInAction() {
		// when
		Permission.of("USER", "READ:");
	}

}
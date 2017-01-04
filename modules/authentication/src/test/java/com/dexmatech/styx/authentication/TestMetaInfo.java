package com.dexmatech.styx.authentication;

import org.junit.Test;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by aortiz on 15/09/16.
 */
public class TestMetaInfo {

	@Test
	public void shouldCreateMetaInfo() {
		// when
		MetaInfo metaInfo = MetaInfo.initWith("id ", "1");
		// then
		assertThat("Meta info was null", metaInfo, notNullValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenNonASCIICharactersInKey() {
		// when
		MetaInfo.initWith("§", "READ");
	}

}
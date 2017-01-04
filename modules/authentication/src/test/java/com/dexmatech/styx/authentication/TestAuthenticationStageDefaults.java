package com.dexmatech.styx.authentication;

import com.dexmatech.styx.core.http.Headers;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static com.dexmatech.styx.authentication.AuthenticationStageDefaults.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * Created by aortiz on 15/09/16.
 */
public class TestAuthenticationStageDefaults {

	public static final Function<List<Permission>, Headers> PERMISSIONS_TO_HEADER = PARSE_PERMISSIONS_TO_ONE_HEADER.apply
			(DEFAULT_PERMISSIONS_HEADER_KEY);

	@Test
	public void shouldConvertPermissionsToOneFinalHeader() {

		// given
		List<Permission> permissions = Arrays.asList(Permission.of("users", "R"), Permission.of("users", "W"), Permission.of("items", "R"));
		// when
		Headers headers = PERMISSIONS_TO_HEADER.apply(permissions);
		//then
		assertThat("Expected headers size was wrong", headers.toMap().entrySet(), hasSize(1));
		assertThat("Headers did not contain permission header", headers.contains(DEFAULT_PERMISSIONS_HEADER_KEY), is(true));
		assertThat("Header permission had wrong value", headers.get(DEFAULT_PERMISSIONS_HEADER_KEY), is("users:R,users:W,items:R"));
	}

	@Test
	public void shouldConvertYoEmptyHeadersWhenEmptyPermissions() {

		// given
		List<Permission> permissions = Collections.emptyList();
		// when
		Headers headers = PERMISSIONS_TO_HEADER.apply(permissions);
		//then
		assertThat("Expected headers was not empty", headers.toMap().entrySet(), hasSize(0));

	}

	@Test
	public void shouldConvertMetaInfoToHeaders() {

		// given
		MetaInfo metaInfo = MetaInfo.initWith("account", "3333").put("client", "wwww");
		// when
		Headers headers = PARSE_METAINFO_TO_HEADERS.apply(metaInfo);
		//then
		assertThat("Expected headers size was wrong", headers.toMap().entrySet(), hasSize(2));
		assertThat("Headers did not contain account header", headers.contains(CUSTOM_HEADER_PREFIX + "account"), is(true));
		assertThat("Headers did not contain client header", headers.contains(CUSTOM_HEADER_PREFIX + "client"), is(true));
	}

	@Test
	public void shouldConvertToEmptyHeadersWhenEmptyMetaInfo() {

		// given
		MetaInfo metaInfo = MetaInfo.empty();
		// when
		Headers headers = PARSE_METAINFO_TO_HEADERS.apply(metaInfo);
		//then
		assertThat("Expected headers was not empty",headers.toMap().entrySet(), hasSize(0));

	}

}
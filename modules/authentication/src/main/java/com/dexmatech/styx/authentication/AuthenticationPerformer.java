package com.dexmatech.styx.authentication;


import java.util.Optional;

/**
 * Created by aortiz on 14/09/16.
 */
@FunctionalInterface
public interface AuthenticationPerformer {
	Optional<Principal> perform(String token) throws Exception;


}

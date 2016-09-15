package com.dexmatech.styx.authentication;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Created by aortiz on 14/09/16.
 */
@FunctionalInterface
public interface Authenticator {
	CompletableFuture<Optional<Principal>> authenticate(String token);

}

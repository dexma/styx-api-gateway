package com.dexmatech.styx.authentication;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
/**
 *  Representation of entity an that requests access
 */
public class Principal {
	private final List<Permission> permissions;
	private final MetaInfo metaInfo;
}

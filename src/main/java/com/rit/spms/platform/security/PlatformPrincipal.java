package com.rit.spms.platform.security;

/** Authenticated Super Admin identity for the current request -- deliberately not an
 * {@code AppUser}/{@code UserPrincipal}; see {@link PlatformJwtTokenProvider}'s javadoc. */
public record PlatformPrincipal(Long id, String email) {
}

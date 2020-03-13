package io.netlibs.zzz.jersey;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class JwkWellKnownResolver implements JwkAuthorityResolver {

	@Override
	public URL resolve(JwkKeyId key) {
		URI authority = key.authority();
		String scheme = authority.getScheme();
		try {
			if ((scheme != null) && !scheme.equals("https")) {
				return null;
			} else if (!authority.getPath().isEmpty() && !authority.getPath().contentEquals("/")) {
				return key.authority().toURL();
			}
			return new URL("https", key.authority().getAuthority(), "/.well-known/jwks.json");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

}

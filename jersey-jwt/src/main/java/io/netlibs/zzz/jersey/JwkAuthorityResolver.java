package io.netlibs.zzz.jersey;

import java.net.URL;

public interface JwkAuthorityResolver {

	URL resolve(JwkKeyId key);

}

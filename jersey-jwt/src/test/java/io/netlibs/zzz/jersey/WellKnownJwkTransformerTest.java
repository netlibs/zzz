package io.netlibs.zzz.jersey;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.PublicKey;

import org.junit.jupiter.api.Test;

import com.auth0.jwk.InvalidPublicKeyException;

import io.netlibs.zzz.jersey.WellKnownJwkTransformer;
import io.netlibs.zzz.jersey.WellKnownJwkTransformer.KeyResponse;

class WellKnownJwkTransformerTest {

	@Test
	void test() throws InvalidPublicKeyException {

		KeyResponse jwks = new WellKnownJwkTransformer().transform("{\n" + "  \"keys\": [\n" + "    {\n" + "      \"alg\": \"RS256\",\n"
				+ "      \"kty\": \"RSA\",\n" + "      \"use\": \"sig\",\n"
				+ "      \"n\": \"zDqejKcyx6kGXcdN_hgRVeX0aKvgs8Pav_zSXuC4KbphkAIjys-r6-JhIxDUuciAjL_j8-9CFD0TO9kj6Sw9lvtMv4WqmvRzul7S0CGMkilJKLgg-P6bzmi8H9a5B5AXazHACC0Xv4tYdGG10XP5H1__ZWBq4PO_taeZ0PAjPdNmIt22d2pgwcpkoz7-4KTmw7-phJbDmhTQXzMSCzh7Mv9zvigWVOygt-lRUo2G8jJ4QLfZ6mtJ_YU0w4O2bLMTTe1UzQXVY8lEOFTErCmBv8sKtOMRVik-tR34QRODDHQY8L4Q0ibayDbwx-NtzAu9Dk58I7GCEA8KyEKcuz3-qQ\",\n"
				+ "      \"e\": \"AQAB\",\n"
				+ "      \"kid\": \"NEQ2NEM2MDA1RkYzOTNFODlGMzQ3QzhGOUE5RTA2NjJCOUZEMTBDOA\",\n"
				+ "      \"x5t\": \"NEQ2NEM2MDA1RkYzOTNFODlGMzQ3QzhGOUE5RTA2NjJCOUZEMTBDOA\",\n" + "      \"x5c\": [\n"
				+ "        \"MIIDBzCCAe+gAwIBAgIJVOcyWdDH0F00MA0GCSqGSIb3DQEBCwUAMCExHzAdBgNVBAMTFmZsdWVudHN0cmVhbS5hdXRoMC5jb20wHhcNMjAwMjA2MTgxODIwWhcNMzMxMDE1MTgxODIwWjAhMR8wHQYDVQQDExZmbHVlbnRzdHJlYW0uYXV0aDAuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzDqejKcyx6kGXcdN/hgRVeX0aKvgs8Pav/zSXuC4KbphkAIjys+r6+JhIxDUuciAjL/j8+9CFD0TO9kj6Sw9lvtMv4WqmvRzul7S0CGMkilJKLgg+P6bzmi8H9a5B5AXazHACC0Xv4tYdGG10XP5H1//ZWBq4PO/taeZ0PAjPdNmIt22d2pgwcpkoz7+4KTmw7+phJbDmhTQXzMSCzh7Mv9zvigWVOygt+lRUo2G8jJ4QLfZ6mtJ/YU0w4O2bLMTTe1UzQXVY8lEOFTErCmBv8sKtOMRVik+tR34QRODDHQY8L4Q0ibayDbwx+NtzAu9Dk58I7GCEA8KyEKcuz3+qQIDAQABo0IwQDAPBgNVHRMBAf8EBTADAQH/MB0GA1UdDgQWBBTz21GplsBklDK1rC6kZh78kSEExDAOBgNVHQ8BAf8EBAMCAoQwDQYJKoZIhvcNAQELBQADggEBAA9S1kdMaFNHod2MTk+5Ed4n4sch5Rv0NuJdalWwQ1guyTJ85MNuu256/onmrLTjfPkZUOVo+hdletIpKL5LMXt0Ae7fR6PVFVpnzCsFylnFnZOXeptflzdcVk52WfjF6dGeCxfb4p04jKVny1z3j+qVD3s/He2TWAnUrfVbmKwsl07e8lzzgaur57dflrhCGcKl+myI246lwVxDqtqejJBRCvWyY2k8eReEcFLz7QMD9YQWZWdJJr00JXXoQY2Rk3JBKkpIcMtj2QN3XlArQG0cCogWJ1XknXE033L0XcmpF5/yZudyUaWdqaPScwUK2OWs5xfJGdXnM9OqM7ddG4I=\"\n"
				+ "      ]\n" + "    }\n" + "  ]\n" + "}");
		
		PublicKey key = jwks.keys.get(0).asJwk().getPublicKey();
		
		assertEquals(1327188, key.hashCode());
		

	}

}

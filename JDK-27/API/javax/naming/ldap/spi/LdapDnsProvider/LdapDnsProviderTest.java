/*
 * @test
 * @summary javax.naming.ldap.spi.LdapDnsProvider conformance: white-box structure + black-box behavior.
 * @run main LdapDnsProviderTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.ldap.spi.*;
import java.util.HashMap;
import java.util.List;

public class LdapDnsProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.spi.LdapDnsProvider");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(hasMethod(c, "lookupEndpoints"), "lookupEndpoints");
    }

    static void testBlackBox() throws Exception {
        final LdapDnsProviderResult result = new LdapDnsProviderResult("example.com", List.of("ldap://a:389"));
        LdapDnsProvider provider = new LdapDnsProvider() {
            public Optional<LdapDnsProviderResult> lookupEndpoints(String url, Map<?, ?> env) {
                return Optional.of(result);
            }
        };
        Optional<LdapDnsProviderResult> out = provider.lookupEndpoints("ldap://example.com", new HashMap<String, Object>());
        check(out.isPresent(), "lookupEndpoints present");
        check(out.get() == result, "lookupEndpoints identity");
        check("example.com".equals(out.get().getDomainName()), "lookupEndpoints domain");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

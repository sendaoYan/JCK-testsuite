/*
 * @test
 * @summary javax.naming.ldap.spi.LdapDnsProviderResult conformance: white-box structure + black-box behavior.
 * @run main LdapDnsProviderResultTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.ldap.spi.*;

public class LdapDnsProviderResultTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.spi.LdapDnsProviderResult");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 2 && x.getParameterTypes()[0] == String.class && x.getParameterTypes()[1] == List.class), "ctor(String,List)");
        for (String m : new String[]{"getDomainName", "getEndpoints"}) {
            boolean found = false;
            for (Method mm : c.getMethods()) if (mm.getName().equals(m)) found = true;
            check(found, m + " present");
        }
    }

    static void testBlackBox() {
        LdapDnsProviderResult r = new LdapDnsProviderResult("example.com", List.of("ldap://a:389", "ldap://b:389"));
        check("example.com".equals(r.getDomainName()), "domainName");
        check(r.getEndpoints().equals(List.of("ldap://a:389", "ldap://b:389")), "endpoints");

        // null domainName normalizes to empty string
        LdapDnsProviderResult rn = new LdapDnsProviderResult(null, List.of("ldap://x"));
        check("".equals(rn.getDomainName()), "null domain -> empty");

        // endpoints list is defensively copied and unmodifiable
        List<String> mutable = new ArrayList<>(List.of("ldap://a", "ldap://b"));
        LdapDnsProviderResult rc = new LdapDnsProviderResult("d", mutable);
        mutable.clear();
        check(rc.getEndpoints().size() == 2, "copied list unaffected by source mutation");
        try {
            rc.getEndpoints().add("ldap://c");
            check(false, "endpoints should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            check(true, "endpoints unmodifiable");
        }

        // empty endpoints allowed
        LdapDnsProviderResult re = new LdapDnsProviderResult("d", List.of());
        check(re.getEndpoints().isEmpty(), "empty endpoints");

        // null endpoint element rejected
        try {
            new LdapDnsProviderResult("d", Arrays.asList("ldap://a", null));
            check(false, "null endpoint should throw");
        } catch (NullPointerException expected) {
            check(true, "null endpoint throws NullPointerException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

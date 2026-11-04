/*
 * @test
 * @summary java.security.Security conformance: white-box structure + black-box behavior.
 * @run main SecurityTest
 */

import java.lang.reflect.Modifier;
import java.security.Provider;
import java.security.Security;
import java.util.Set;

public class SecurityTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.Security");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isFinal(c.getModifiers()), "final class");
        checkMethods(c, new String[]{"getAlgorithmProperty", "getProviders", "getProvider",
            "getProperty", "getAlgorithms"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Provider discovery (read-only; no fixed provider count asserted)
        Provider[] providers = Security.getProviders();
        check(providers != null && providers.length >= 1, "getProviders non-empty");

        check(Security.getProvider("SUN") != null, "getProvider(SUN) non-null");
        check(Security.getProvider("SunRsaSign") != null, "getProvider(SunRsaSign) non-null");
        check(Security.getProvider("SunJCE") != null, "getProvider(SunJCE) non-null");

        // Core property read (do not write)
        String prop1 = Security.getProperty("security.provider.1");
        check(prop1 != null && prop1.length() > 0, "security.provider.1 non-empty");

        // getAlgorithms for a service type includes the expected algorithm
        Set<String> digests = Security.getAlgorithms("MessageDigest");
        check(digests != null && digests.contains("SHA-256"), "getAlgorithms(MessageDigest) has SHA-256");

        // getProviders(String filter) returns matching providers
        Provider[] mdProviders = Security.getProviders("MessageDigest.SHA-256");
        check(mdProviders != null && mdProviders.length >= 1, "getProviders(MessageDigest.SHA-256) non-empty");
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

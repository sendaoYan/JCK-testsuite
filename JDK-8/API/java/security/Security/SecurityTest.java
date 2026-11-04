/*
 * @test
 * @summary java.security.Security conformance: full contract coverage.
 *          getProviders()/getProvider(name) (incl. missing -> null), getAlgorithms
 *          for MessageDigest/Signature/Cipher, unknown service and null service ->
 *          empty set, getProviders(String filter) and getProviders(Map filter),
 *          getProperty/setProperty round-trip, and a deterministic (unique-key)
 *          property mutation that is restored afterwards.
 * @run main SecurityTest
 */

import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SecurityTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    public static void main(String[] args) throws Exception {
        // ---- getProviders ----
        check(Security.getProviders().length > 0, "getProviders non-empty");
        check(Security.getProviders("MessageDigest.SHA-256").length >= 1,
                "getProviders(String filter) non-empty");

        // ---- getProvider(name) ----
        check(Security.getProvider("SUN") != null, "getProvider(SUN) non-null");
        check(Security.getProvider("NoSuchProvider_XYZ") == null, "getProvider(missing) null");

        // ---- getAlgorithms ----
        Set<String> md = Security.getAlgorithms("MessageDigest");
        check(md != null && !md.isEmpty(), "getAlgorithms MessageDigest non-empty");
        check(md.contains("MD5"), "getAlgorithms MessageDigest contains MD5");
        check(md.contains("SHA-256"), "getAlgorithms MessageDigest contains SHA-256");

        Set<String> sig = Security.getAlgorithms("Signature");
        check(sig != null && !sig.isEmpty(), "getAlgorithms Signature non-empty");
        check(sig.contains("SHA1WITHRSA"), "getAlgorithms Signature contains SHA1WITHRSA");
        check(sig.contains("SHA256WITHRSA"), "getAlgorithms Signature contains SHA256WITHRSA");

        Set<String> cipher = Security.getAlgorithms("Cipher");
        check(cipher != null && cipher.contains("AES"), "getAlgorithms Cipher contains AES");

        check(Security.getAlgorithms("NoSuchService_XYZ").isEmpty(), "getAlgorithms unknown empty");
        check(Security.getAlgorithms(null).isEmpty(), "getAlgorithms(null) empty");

        // ---- getProviders(Map filter) ----
        Map<String, String> mdFilter = new HashMap<>();
        mdFilter.put("MessageDigest.SHA-256", "");
        check(Security.getProviders(mdFilter).length >= 1, "getProviders(Map MD filter) non-empty");

        Map<String, String> cipherFilter = new HashMap<>();
        cipherFilter.put("Cipher.AES", "");
        check(Security.getProviders(cipherFilter).length >= 1, "getProviders(Map Cipher filter) non-empty");

        // ---- getProperty/setProperty round-trip (unique key, restored) ----
        check(Security.getProperty("security.provider.1") != null, "getProperty provider.1 non-null");
        final String key = "jck.test.security.property";
        String original = Security.getProperty(key);
        try {
            Security.setProperty(key, "jck-value-123");
            check("jck-value-123".equals(Security.getProperty(key)), "setProperty/getProperty round-trip");
        } finally {
            if (original == null) {
                Security.setProperty(key, "");
            } else {
                Security.setProperty(key, original);
            }
        }

        // ==== contract deepening: add/remove provider slots, filter semantics, master flags ====

        // ---- addProvider slot semantics + state restore ----
        java.security.Provider testProv = new java.security.Provider(
                "JckTestProvider_XYZ", 1.0, "jck test provider") { };
        try {
            int slot = Security.addProvider(testProv);
            check(slot > 0, "addProvider returns positive slot");
            check(Security.getProvider("JckTestProvider_XYZ") == testProv, "added provider visible");
            check(java.util.Arrays.asList(Security.getProviders()).contains(testProv),
                    "listed in getProviders()");
            check(Security.addProvider(testProv) == -1, "re-adding same instance returns -1");
            // a distinct provider with the same name is also rejected
            java.security.Provider dupName = new java.security.Provider(
                    "JckTestProvider_XYZ", 2.0, "dup") { };
            check(Security.addProvider(dupName) == -1, "duplicate name rejected -1");
            // insertProviderAt within range
            java.security.Provider atTwo = new java.security.Provider("JckAt2_XYZ", 1.0, "at2") { };
            int pos = Security.insertProviderAt(atTwo, 2);
            try {
                check(pos == 2, "insertProviderAt returns position");
                check(Security.getProvider("JckAt2_XYZ") == atTwo, "inserted provider visible");
                check(Security.getProviders()[1].getName().equals("JckAt2_XYZ"),
                        "inserted provider at index 1");
            } finally {
                Security.removeProvider("JckAt2_XYZ");
            }
            Security.removeProvider("JckTestProvider_XYZ");
            check(Security.getProvider("JckTestProvider_XYZ") == null, "removed provider invisible");
            // removeProvider of an absent name is a silent no-op
            Security.removeProvider("JckTestProvider_XYZ");
            check(true, "removeProvider absent is no-op");
        } finally {
            Security.removeProvider("JckTestProvider_XYZ");
            Security.removeProvider("JckAt2_XYZ");
        }

        // ---- getProvider null/name-case semantics ----
        check(Security.getProvider(null) == null, "getProvider(null) returns null (recorded)");
        check(Security.getProvider("sun") == null, "getProvider is case-sensitive (recorded)");
        check(Security.getProvider("SUN").getName().equals("SUN"), "getProvider canonical name");

        // ---- getProviders(filter) edge cases ----
        check(Security.getProviders("NoSuchService_XYZ.Bogus") == null,
                "getProviders unknown filter returns null (recorded)");
        // filter with attribute value
        check(Security.getProviders("KeyFactory.RSA").length >= 1, "getProviders KeyFactory.RSA");
        Map<String, String> attrFilter = new HashMap<>();
        attrFilter.put("Signature.SHA256withDSA", "");
        attrFilter.put("MessageDigest.SHA-256", "");
        check(Security.getProviders(attrFilter).length >= 1, "getProviders multi-filter");
        Map<String, String> impossible = new HashMap<>();
        impossible.put("NoSuchService_XYZ.A", "");
        check(Security.getProviders(impossible) == null || Security.getProviders(impossible).length == 0,
                "getProviders impossible filter");
        // null filter map -> NPE
        checkThrows(NullPointerException.class,
                () -> Security.getProviders((java.util.Map<String, String>) null),
                "getProviders(null Map)");

        // ---- getAlgorithms casing: engine types are case-insensitive; algorithms uppercase-normalized ----
        check(Security.getAlgorithms("messagedigest").contains("MD5"), "getAlgorithms type case-insensitive");
        check(Security.getAlgorithms("SecureRandom").stream()
                .anyMatch(s -> s.equalsIgnoreCase("SHA1PRNG")), "SecureRandom SHA1PRNG present");
        check(Security.getAlgorithms("KeyStore").contains("PKCS12")
                && Security.getAlgorithms("KeyStore").contains("JKS"), "KeyStore algorithms present");
        check(Security.getAlgorithms("").isEmpty(), "getAlgorithms empty type empty");

        // ---- security master properties ----
        check(Security.getProperty("keystore.type") != null, "keystore.type defined");
        check(Security.getProperty("keystore.type").equals(java.security.KeyStore.getDefaultType()),
                "keystore.type equals getDefaultType()");
        check(Security.getProperty("no.such.property.XYZ") == null, "getProperty missing null");
        Security.setProperty("no.such.property.XYZ", "v");
        check("v".equals(Security.getProperty("no.such.property.XYZ")), "setProperty creates");
        Security.setProperty("no.such.property.XYZ", "");
        check("".equals(Security.getProperty("no.such.property.XYZ")), "setProperty overwrite empty");
        checkThrows(NullPointerException.class, () -> Security.getProperty(null), "getProperty(null)");
        checkThrows(NullPointerException.class, () -> Security.setProperty(null, "v"), "setProperty(null key)");
        checkThrows(NullPointerException.class, () -> Security.setProperty("k", null), "setProperty(null value)");
        checkThrows(NullPointerException.class, () -> Security.getProviders((String) null),
                "getProviders(null String filter)");
        checkThrows(NullPointerException.class, () -> Security.insertProviderAt(null, 1),
                "insertProviderAt(null provider)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

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

        System.out.println("PASS (" + passed + " assertions)");
    }
}

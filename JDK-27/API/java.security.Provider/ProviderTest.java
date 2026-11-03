/*
 * @test
 * @summary java.security.Provider conformance: white-box structure + black-box behavior.
 * @run main ProviderTest
 */

import java.lang.reflect.Modifier;
import java.security.Provider;
import java.security.Security;
import java.util.Set;

public class ProviderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.Provider");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.util.Properties"), "extends Properties");
        checkMethods(c, new String[]{"getName", "getVersion", "getVersionStr", "getInfo",
            "getService", "getServices", "getProperty", "toString"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Provider sun = Security.getProvider("SUN");
        check(sun != null, "getProvider(SUN) non-null");
        check("SUN".equals(sun.getName()), "getName SUN");
        check(sun.getVersion() > 0.0, "getVersion positive");
        check(sun.getVersionStr() != null, "getVersionStr non-null");
        check(sun.getInfo() != null && sun.getInfo().length() > 0, "getInfo non-empty");
        check(sun.toString() != null, "toString non-null");

        // getService for a well-known algorithm
        Provider.Service sha = sun.getService("MessageDigest", "SHA-256");
        check(sha != null, "getService(MessageDigest,SHA-256) non-null");
        check("SHA-256".equals(sha.getAlgorithm()), "service algorithm");
        check("MessageDigest".equals(sha.getType()), "service type");
        check(sun == sha.getProvider(), "service provider == SUN");
        check(sha.getClassName() != null, "service className non-null");
        check(sha.toString() != null, "service toString non-null");

        // getServices enumerates many services
        Set<Provider.Service> services = sun.getServices();
        check(services != null && !services.isEmpty(), "getServices non-empty");

        // property read is a core crypto mapping (read-only, no mutation)
        String prop = sun.getProperty("MessageDigest.SHA-256");
        check(prop != null && prop.length() > 0, "getProperty(MessageDigest.SHA-256) non-empty");

        // another provider
        Provider sunRsaSign = Security.getProvider("SunRsaSign");
        check(sunRsaSign != null, "getProvider(SunRsaSign) non-null");
        check(sunRsaSign.getService("Signature", "SHA256withRSA") != null,
            "SunRsaSign has SHA256withRSA");
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

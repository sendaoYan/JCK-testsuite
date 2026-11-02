/*
 * @test
 * @summary java.security.Provider.Service conformance: getType/getAlgorithm/getClassName/getProvider via SUN SHA-256.
 * @run main ServiceTest
 */

import java.security.Provider;
import java.security.Security;

public class ServiceTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Provider sun = Security.getProvider("SUN");
        check(sun != null, "SUN provider present");
        Provider.Service s = sun.getService("MessageDigest", "SHA-256");
        check(s != null, "MessageDigest SHA-256 service");
        check("MessageDigest".equals(s.getType()), "getType");
        check("SHA-256".equals(s.getAlgorithm()), "getAlgorithm");
        check(s.getClassName() != null, "getClassName");
        check(sun.equals(s.getProvider()), "getProvider");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

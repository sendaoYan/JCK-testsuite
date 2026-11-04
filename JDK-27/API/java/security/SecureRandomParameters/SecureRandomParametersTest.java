/*
 * @test
 * @summary java.security.SecureRandomParameters conformance: white-box structure + black-box behavior.
 * @run main SecureRandomParametersTest
 */

import java.lang.reflect.Modifier;
import java.security.DrbgParameters;
import java.security.SecureRandomParameters;

public class SecureRandomParametersTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.SecureRandomParameters");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
    }

    static void testBlackBox() throws Exception {
        check(DrbgParameters.instantiation(256, DrbgParameters.Capability.PR_AND_RESEED, null) instanceof SecureRandomParameters, "Instantiation implements SecureRandomParameters");
        check(DrbgParameters.nextBytes(128, false, null) instanceof SecureRandomParameters, "NextBytes implements SecureRandomParameters");
        check(DrbgParameters.reseed(false, null) instanceof SecureRandomParameters, "Reseed implements SecureRandomParameters");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

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
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: pure marker surface, custom implementations, engine wiring ----
    static void testContractDeep() throws Exception {
        Class<?> p = java.security.SecureRandomParameters.class;
        check(p.getDeclaredMethods().length == 0, "pure marker: zero declared methods");
        check(p.getInterfaces().length == 0, "extends no other interface");
        check(!java.io.Serializable.class.isAssignableFrom(p), "not Serializable");
        check(!java.lang.annotation.Annotation.class.isAssignableFrom(p), "not an annotation");
        check(p.getPackageName().equals("java.security"), "package java.security");
        // nothing else in the public API implements it besides the DrbgParameters family
        check(SecureRandomParameters.class.isAssignableFrom(DrbgParameters.Instantiation.class),
                "Instantiation assignable");
        check(SecureRandomParameters.class.isAssignableFrom(DrbgParameters.NextBytes.class),
                "NextBytes assignable");
        check(SecureRandomParameters.class.isAssignableFrom(DrbgParameters.Reseed.class),
                "Reseed assignable");
        check(!SecureRandomParameters.class.isAssignableFrom(java.security.SecureRandom.class),
                "SecureRandom is not a parameters type");

        // a hand-rolled implementation is a legal type but rejected by the DRBG engine
        SecureRandomParameters mine = new SecureRandomParameters() {};
        check(mine.getClass().getName().contains("SecureRandomParametersTest"), "anonymous impl type");
        check(mine.equals(mine), "identity equals reflexive");
        check(!mine.equals(new SecureRandomParameters() {}), "distinct impls not equal");
        check(mine.toString().contains("@"), "default Object toString form");
        try {
            java.security.SecureRandom.getInstance("DRBG", mine);
            check(false, "engine must reject unknown params");
        } catch (java.security.NoSuchAlgorithmException nsae) {
            check(nsae.getMessage() != null && nsae.getMessage().contains("Error constructing"),
                    "custom params -> NSAE 'Error constructing implementation'");
        }
        // SHA1PRNG accepts no params at all
        try {
            java.security.SecureRandom.getInstance("SHA1PRNG",
                    DrbgParameters.instantiation(128, DrbgParameters.Capability.NONE, null));
            check(false, "SHA1PRNG must reject params");
        } catch (java.security.NoSuchAlgorithmException nsae) {
            check(true, "SHA1PRNG + Instantiation -> NSAE");
        }
        // engine return wiring: DRBG hands back a SecureRandomParameters, SHA1PRNG hands back null
        java.security.SecureRandom drbg = java.security.SecureRandom.getInstance("DRBG");
        check(drbg.getParameters() instanceof SecureRandomParameters, "DRBG getParameters non-null");
        check(java.security.SecureRandom.getInstance("SHA1PRNG").getParameters() == null,
                "SHA1PRNG getParameters null");
    }
}

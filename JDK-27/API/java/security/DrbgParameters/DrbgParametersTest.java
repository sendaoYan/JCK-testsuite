/*
 * @test
 * @summary java.security.DrbgParameters conformance: white-box structure + black-box behavior.
 * @run main DrbgParametersTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.DrbgParameters;
import java.security.SecureRandomParameters;
import java.util.Arrays;

public class DrbgParametersTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.DrbgParameters");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface() && !c.isEnum(), "class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getConstructors().length == 0, "no public constructor");
        checkMethods(c, new String[]{"instantiation", "nextBytes", "reseed"});
    }

    static void testBlackBox() throws Exception {
        DrbgParameters.Instantiation inst = DrbgParameters.instantiation(256, DrbgParameters.Capability.PR_AND_RESEED, new byte[]{1, 2, 3});
        check(inst.getStrength() == 256, "instantiation strength");
        check(inst.getCapability() == DrbgParameters.Capability.PR_AND_RESEED, "instantiation capability");
        check(Arrays.equals(inst.getPersonalizationString(), new byte[]{1, 2, 3}), "instantiation personalization");
        check(inst instanceof SecureRandomParameters, "instantiation implements SecureRandomParameters");

        DrbgParameters.NextBytes nb = DrbgParameters.nextBytes(128, true, new byte[]{4});
        check(nb.getStrength() == 128, "nextBytes strength");
        check(nb.getPredictionResistance(), "nextBytes prediction resistance");
        check(nb instanceof SecureRandomParameters, "nextBytes implements SecureRandomParameters");

        DrbgParameters.Reseed rs = DrbgParameters.reseed(true, new byte[]{5});
        check(rs.getPredictionResistance(), "reseed prediction resistance");
        check(rs instanceof SecureRandomParameters, "reseed implements SecureRandomParameters");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: toString forms, constructor-side defensive copies,
    //      empty-vs-null inputs, strength bounds, engine wiring ----
    static void testContractDeep() throws Exception {
        // Instantiation.toString: "strength,capability,Arrays.toString(personalization)"
        byte[] ps = {1, 2, 3};
        DrbgParameters.Instantiation i = DrbgParameters.instantiation(256, DrbgParameters.Capability.RESEED_ONLY, ps);
        check("256,reseed_only,[1, 2, 3]".equals(i.toString()), "Instantiation toString exact");
        check("128,none,null".equals(
                DrbgParameters.instantiation(128, DrbgParameters.Capability.NONE, null).toString()),
                "Instantiation toString null personalization");
        check("128,none,[]".equals(
                DrbgParameters.instantiation(128, DrbgParameters.Capability.NONE, new byte[0]).toString()),
                "Instantiation toString empty personalization (distinct from null)");
        // constructor-side defensive copy: mutating the source after creation is invisible
        ps[0] = 9;
        check(Arrays.equals(i.getPersonalizationString(), new byte[]{1, 2, 3}),
                "Instantiation copies personalization at construction");
        // getters hand out a fresh copy every call
        check(i.getPersonalizationString() != i.getPersonalizationString(),
                "getPersonalizationString fresh array per call");

        // NextBytes: defensive copies, null vs empty, Object-form toString
        byte[] ai = {7};
        DrbgParameters.NextBytes nb = DrbgParameters.nextBytes(192, true, ai);
        ai[0] = 0;
        check(Arrays.equals(nb.getAdditionalInput(), new byte[]{7}), "NextBytes copies input at construction");
        check(nb.getAdditionalInput() != nb.getAdditionalInput(), "getAdditionalInput fresh array per call");
        check(DrbgParameters.nextBytes(128, false, null).getAdditionalInput() == null,
                "NextBytes null additional input stays null");
        check(DrbgParameters.nextBytes(128, false, new byte[0]).getAdditionalInput().length == 0,
                "NextBytes empty additional input preserved (distinct from null)");
        check(DrbgParameters.nextBytes(0, false, null).getStrength() == 0, "NextBytes strength 0 legal");
        check(nb.toString().startsWith("java.security.DrbgParameters$NextBytes@"),
                "NextBytes toString is Object-form");

        // Reseed: defensive copies, null vs empty, Object-form toString
        byte[] ent = {9, 9};
        DrbgParameters.Reseed rs = DrbgParameters.reseed(true, ent);
        ent[0] = 1;
        check(Arrays.equals(rs.getAdditionalInput(), new byte[]{9, 9}), "Reseed copies input at construction");
        check(DrbgParameters.reseed(false, null).getAdditionalInput() == null,
                "Reseed null additional input stays null");
        check(DrbgParameters.reseed(false, new byte[0]).getAdditionalInput().length == 0,
                "Reseed empty additional input preserved");
        check(rs.toString().startsWith("java.security.DrbgParameters$Reseed@"),
                "Reseed toString is Object-form");

        // strength validation on all factories: -1 is the wildcard, below -1 is rejected
        check(DrbgParameters.instantiation(-1, DrbgParameters.Capability.NONE, null).getStrength() == -1,
                "Instantiation strength -1 wildcard legal");
        try {
            DrbgParameters.instantiation(-2, DrbgParameters.Capability.NONE, null);
            check(false, "Instantiation -2 must fail");
        } catch (IllegalArgumentException iae) { check(true, "Instantiation -2 -> IAE"); }
        try {
            DrbgParameters.nextBytes(-2, false, null);
            check(false, "NextBytes -2 must fail");
        } catch (IllegalArgumentException iae) { check(true, "NextBytes -2 -> IAE"); }

        // engine wiring: instantiation params drive getInstance, nextBytes params drive output
        java.security.SecureRandom drbg = java.security.SecureRandom.getInstance("DRBG",
                DrbgParameters.instantiation(128, DrbgParameters.Capability.PR_AND_RESEED, null));
        drbg.nextBytes(new byte[8], DrbgParameters.nextBytes(128, true, null));
        check(true, "nextBytes(params) works on PR_AND_RESEED DRBG");
        drbg.reseed(DrbgParameters.reseed(false, new byte[4]));
        check(true, "reseed(params) works on PR_AND_RESEED DRBG");
        // prediction resistance is rejected when the capability does not allow it
        java.security.SecureRandom ro = java.security.SecureRandom.getInstance("DRBG",
                DrbgParameters.instantiation(128, DrbgParameters.Capability.RESEED_ONLY, null));
        try {
            ro.nextBytes(new byte[8], DrbgParameters.nextBytes(128, true, null));
            check(false, "pr must be rejected on RESEED_ONLY");
        } catch (IllegalArgumentException iae) {
            check("pr not available".equals(iae.getMessage()), "pr-on-RESEED_ONLY IAE message 'pr not available'");
        }
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static void checkEnumConstants(Class<?> c, String[] names) { Object[] cs = c.getEnumConstants(); check(cs != null, "enum constants"); check(cs.length == names.length, "enum constant count"); for (String n : names) check(hasConst(cs, n), "constant " + n); }
    static boolean hasConst(Object[] cs, String name) { for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true; return false; }
}

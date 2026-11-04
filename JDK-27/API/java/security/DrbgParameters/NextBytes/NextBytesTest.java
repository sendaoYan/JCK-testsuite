/*
 * @test
 * @summary java.security.DrbgParameters.NextBytes conformance: white-box structure + black-box behavior.
 * @run main NextBytesTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.DrbgParameters;
import java.security.DrbgParameters.NextBytes;
import java.security.SecureRandomParameters;
import java.util.Arrays;

public class NextBytesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.DrbgParameters$NextBytes");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface() && !c.isEnum(), "class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        checkInterfaces(c, new String[]{"java.security.SecureRandomParameters"});
        checkMethods(c, new String[]{"getStrength", "getPredictionResistance", "getAdditionalInput"});
    }

    static void testBlackBox() throws Exception {
        byte[] ai = new byte[]{9, 8};
        NextBytes nb = DrbgParameters.nextBytes(128, true, ai);
        check(nb.getStrength() == 128, "getStrength");
        check(nb.getPredictionResistance(), "getPredictionResistance");
        check(Arrays.equals(ai, nb.getAdditionalInput()), "getAdditionalInput content");
        byte[] first = nb.getAdditionalInput();
        first[0] = 99;
        check(Arrays.equals(nb.getAdditionalInput(), new byte[]{9, 8}), "getAdditionalInput defensive copy");
        NextBytes def = DrbgParameters.nextBytes(-1, false, null);
        check(def.getStrength() == -1, "getStrength -1");
        check(!def.getPredictionResistance(), "getPredictionResistance false");
        check(def.getAdditionalInput() == null, "null additional input");
        checkThrows(IllegalArgumentException.class, () -> DrbgParameters.nextBytes(-2, false, null));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: defensive copies, null-vs-empty, engine behavior matrix ----
    static void testContractDeep() throws Exception {
        // constructor-side defensive copy
        byte[] src = {3, 1};
        NextBytes d = DrbgParameters.nextBytes(192, false, src);
        src[0] = 55;
        check(Arrays.equals(d.getAdditionalInput(), new byte[]{3, 1}), "additional input copied at construction");
        check(d.getAdditionalInput() != d.getAdditionalInput(), "getter returns fresh copy each call");

        // null vs empty additional input are preserved distinctly
        check(DrbgParameters.nextBytes(128, false, null).getAdditionalInput() == null, "null stays null");
        check(DrbgParameters.nextBytes(128, false, new byte[0]).getAdditionalInput().length == 0,
                "empty stays empty");

        // strength and pr flag round-trips
        check(DrbgParameters.nextBytes(0, true, null).getStrength() == 0, "strength 0 preserved");
        check(DrbgParameters.nextBytes(192, true, null).getPredictionResistance(), "pr true preserved");
        check(DrbgParameters.nextBytes(192, false, null).getPredictionResistance() == false, "pr false preserved");

        // toString is Object-form (not overridden)
        check(d.toString().startsWith("java.security.DrbgParameters$NextBytes@"), "toString Object-form");

        // identity equality
        NextBytes e1 = DrbgParameters.nextBytes(128, false, null);
        check(e1.equals(e1), "identity equals reflexive");
        check(!e1.equals(DrbgParameters.nextBytes(128, false, null)), "same value not equal");
        check(e1.hashCode() == e1.hashCode(), "hashCode self-consistent");

        // engine: additional input is accepted and output delivered on a PR-capable DRBG
        java.security.SecureRandom pr = java.security.SecureRandom.getInstance("DRBG",
                DrbgParameters.instantiation(128, DrbgParameters.Capability.PR_AND_RESEED, null));
        byte[] out = new byte[16];
        pr.nextBytes(out, DrbgParameters.nextBytes(128, true, new byte[]{1, 2}));
        boolean nonZero = false;
        for (byte b : out) if (b != 0) nonZero = true;
        check(nonZero, "parameterized nextBytes delivers output");
        // prediction resistance demands PR capability
        java.security.SecureRandom ro = java.security.SecureRandom.getInstance("DRBG",
                DrbgParameters.instantiation(128, DrbgParameters.Capability.RESEED_ONLY, null));
        try {
            ro.nextBytes(new byte[8], DrbgParameters.nextBytes(128, true, null));
            check(false, "pr must be unavailable on RESEED_ONLY");
        } catch (IllegalArgumentException iae) {
            check("pr not available".equals(iae.getMessage()), "IAE message 'pr not available'");
        }
        // without pr the same DRBG accepts the call
        ro.nextBytes(new byte[8], DrbgParameters.nextBytes(128, false, null));
        check(true, "non-pr nextBytes accepted on RESEED_ONLY");
    }

    static void checkThrows(Class<? extends Throwable> type, Runnable r) {
        try { r.run(); } catch (Throwable t) { check(type.isInstance(t), "throws " + type.getSimpleName()); return; }
        throw new AssertionError("expected " + type.getSimpleName());
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static void checkEnumConstants(Class<?> c, String[] names) { Object[] cs = c.getEnumConstants(); check(cs != null, "enum constants"); check(cs.length == names.length, "enum constant count"); for (String n : names) check(hasConst(cs, n), "constant " + n); }
    static boolean hasConst(Object[] cs, String name) { for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true; return false; }
}

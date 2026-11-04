/*
 * @test
 * @summary java.security.DrbgParameters.Reseed conformance: white-box structure + black-box behavior.
 * @run main ReseedTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.DrbgParameters;
import java.security.DrbgParameters.Reseed;
import java.security.SecureRandomParameters;
import java.util.Arrays;

public class ReseedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.DrbgParameters$Reseed");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface() && !c.isEnum(), "class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        checkInterfaces(c, new String[]{"java.security.SecureRandomParameters"});
        checkMethods(c, new String[]{"getPredictionResistance", "getAdditionalInput"});
    }

    static void testBlackBox() throws Exception {
        Reseed noAi = DrbgParameters.reseed(false, null);
        check(!noAi.getPredictionResistance(), "getPredictionResistance false");
        check(noAi.getAdditionalInput() == null, "null additional input");
        byte[] ai = new byte[]{7};
        Reseed rs = DrbgParameters.reseed(true, ai);
        check(rs.getPredictionResistance(), "getPredictionResistance true");
        check(Arrays.equals(ai, rs.getAdditionalInput()), "getAdditionalInput content");
        byte[] first = rs.getAdditionalInput();
        first[0] = 99;
        check(Arrays.equals(rs.getAdditionalInput(), new byte[]{7}), "getAdditionalInput defensive copy");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: defensive copies, null-vs-empty, engine reseeding matrix ----
    static void testContractDeep() throws Exception {
        // constructor-side defensive copy
        byte[] src = {6, 7};
        Reseed d = DrbgParameters.reseed(true, src);
        src[0] = 88;
        check(Arrays.equals(d.getAdditionalInput(), new byte[]{6, 7}), "additional input copied at construction");
        check(d.getAdditionalInput() != d.getAdditionalInput(), "getter returns fresh copy each call");

        // null vs empty preserved distinctly; pr flag both ways
        check(DrbgParameters.reseed(false, null).getAdditionalInput() == null, "null stays null");
        check(DrbgParameters.reseed(false, new byte[0]).getAdditionalInput().length == 0, "empty stays empty");
        check(DrbgParameters.reseed(true, null).getPredictionResistance(), "pr true preserved");
        check(DrbgParameters.reseed(false, new byte[1]).getPredictionResistance() == false, "pr false preserved");

        // toString Object-form, identity equality
        check(d.toString().startsWith("java.security.DrbgParameters$Reseed@"), "toString Object-form");
        check(d.equals(d), "identity equals reflexive");
        check(!d.equals(DrbgParameters.reseed(true, new byte[]{6, 7})), "same value not equal");
        check(d.hashCode() == d.hashCode(), "hashCode self-consistent");

        // engine: a PR-capable DRBG accepts parameterized reseeds (both pr settings)
        java.security.SecureRandom pr = java.security.SecureRandom.getInstance("DRBG",
                DrbgParameters.instantiation(128, DrbgParameters.Capability.PR_AND_RESEED, null));
        pr.reseed(DrbgParameters.reseed(true, new byte[]{1}));
        check(true, "reseed(pr=true) works on PR_AND_RESEED");
        pr.reseed(DrbgParameters.reseed(false, null));
        check(true, "reseed(pr=false) works on PR_AND_RESEED");
        pr.nextBytes(new byte[8]);
        check(true, "DRBG still produces output after parameterized reseed");
        // RESEED_ONLY DRBG also reseeds (it just lacks prediction resistance)
        java.security.SecureRandom ro = java.security.SecureRandom.getInstance("DRBG",
                DrbgParameters.instantiation(128, DrbgParameters.Capability.RESEED_ONLY, null));
        ro.reseed(DrbgParameters.reseed(false, null));
        check(true, "RESEED_ONLY accepts reseed without pr");
        try {
            ro.reseed(DrbgParameters.reseed(true, null));
            check(false, "pr reseed must be unavailable on RESEED_ONLY");
        } catch (IllegalArgumentException iae) {
            check(true, "reseed(pr=true) rejected on RESEED_ONLY");
        }
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static void checkEnumConstants(Class<?> c, String[] names) { Object[] cs = c.getEnumConstants(); check(cs != null, "enum constants"); check(cs.length == names.length, "enum constant count"); for (String n : names) check(hasConst(cs, n), "constant " + n); }
    static boolean hasConst(Object[] cs, String name) { for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true; return false; }
}

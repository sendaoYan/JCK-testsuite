/*
 * @test
 * @summary java.security.DrbgParameters.Capability conformance: white-box structure + black-box behavior.
 * @run main CapabilityTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.DrbgParameters;
import java.security.DrbgParameters.Capability;

public class CapabilityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.DrbgParameters$Capability");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == Enum.class, "extends Enum");
        checkEnumConstants(c, new String[]{"PR_AND_RESEED", "RESEED_ONLY", "NONE"});
        checkMethods(c, new String[]{"valueOf", "values", "supportsReseeding", "supportsPredictionResistance", "toString"});
    }

    static void testBlackBox() throws Exception {
        check(Capability.values().length == 3, "three constants");
        check(Capability.valueOf("NONE") == Capability.NONE, "valueOf NONE");
        check(Capability.PR_AND_RESEED.supportsReseeding(), "PR_AND_RESEED supportsReseeding");
        check(Capability.PR_AND_RESEED.supportsPredictionResistance(), "PR_AND_RESEED supportsPredictionResistance");
        check(Capability.RESEED_ONLY.supportsReseeding(), "RESEED_ONLY supportsReseeding");
        check(!Capability.RESEED_ONLY.supportsPredictionResistance(), "RESEED_ONLY no prediction resistance");
        check(!Capability.NONE.supportsReseeding(), "NONE no reseeding");
        check(!Capability.NONE.supportsPredictionResistance(), "NONE no prediction resistance");
        for (Capability v : Capability.values()) {
            check(Capability.valueOf(v.name()) == v, "valueOf round-trip " + v.name());
        }
        check("pr_and_reseed".equals(Capability.PR_AND_RESEED.toString()), "PR_AND_RESEED toString");
        check("reseed_only".equals(Capability.RESEED_ONLY.toString()), "RESEED_ONLY toString");
        check("none".equals(Capability.NONE.toString()), "NONE toString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: order, enum machinery, name/toString case, wiring ----
    static void testContractDeep() throws Exception {
        // declaration order drives ordinal and compareTo
        check(Capability.PR_AND_RESEED.ordinal() == 0, "PR_AND_RESEED ordinal 0");
        check(Capability.RESEED_ONLY.ordinal() == 1, "RESEED_ONLY ordinal 1");
        check(Capability.NONE.ordinal() == 2, "NONE ordinal 2");
        check(Capability.values()[0] == Capability.PR_AND_RESEED, "values() order: first PR_AND_RESEED");
        check(Capability.values()[2] == Capability.NONE, "values() order: last NONE");
        check(Capability.PR_AND_RESEED.compareTo(Capability.RESEED_ONLY) < 0, "natural order 0 < 1");
        check(Capability.NONE.compareTo(Capability.PR_AND_RESEED) > 0, "natural order 2 > 0");

        // standard enum machinery
        check(Capability.NONE.getDeclaringClass() == Capability.class, "getDeclaringClass");
        check(java.io.Serializable.class.isAssignableFrom(Capability.class), "enum is Serializable");
        check(Comparable.class.isAssignableFrom(Capability.class), "enum is Comparable");
        try {
            Capability.valueOf("BOGUS");
            check(false, "valueOf(BOGUS) must fail");
        } catch (IllegalArgumentException iae) {
            check(true, "valueOf(BOGUS) -> IAE");
        }
        try {
            Capability.valueOf(null);
            check(false, "valueOf(null) must fail");
        } catch (NullPointerException npe) {
            check(true, "valueOf(null) -> NPE");
        }

        // name() is SCREAMING_SNAKE, toString() is lowercase: they differ on purpose
        for (Capability v : Capability.values()) {
            check(!v.name().equals(v.toString()), "name and toString differ: " + v.name());
            check(v.name().toLowerCase().equals(v.toString()), "toString is lowercased name: " + v.name());
        }

        // capability flags fully determine the feature matrix
        boolean[][] m = {
                {true,  true },  // PR_AND_RESEED
                {true,  false},  // RESEED_ONLY
                {false, false},  // NONE
        };
        int idx = 0;
        for (Capability v : Capability.values()) {
            check(v.supportsReseeding() == m[idx][0], "supportsReseeding " + v);
            check(v.supportsPredictionResistance() == m[idx][1], "supportsPredictionResistance " + v);
            idx++;
        }

        // wiring: capability rides through Instantiation into the engine
        java.security.SecureRandom drbg = java.security.SecureRandom.getInstance("DRBG",
                DrbgParameters.instantiation(128, Capability.PR_AND_RESEED, null));
        DrbgParameters.Instantiation back = (DrbgParameters.Instantiation) drbg.getParameters();
        check(back.getCapability() == Capability.PR_AND_RESEED, "capability survives engine round-trip");
        check(back.getCapability().supportsPredictionResistance(), "round-tripped capability still reports pr");
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static void checkEnumConstants(Class<?> c, String[] names) { Object[] cs = c.getEnumConstants(); check(cs != null, "enum constants"); check(cs.length == names.length, "enum constant count"); for (String n : names) check(hasConst(cs, n), "constant " + n); }
    static boolean hasConst(Object[] cs, String name) { for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true; return false; }
}

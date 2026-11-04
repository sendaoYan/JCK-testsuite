/*
 * @test
 * @summary java.security.DrbgParameters.Capability conformance: white-box structure + black-box behavior.
 * @run main CapabilityTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
        System.out.println("PASS (" + passed + " assertions)");
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static void checkEnumConstants(Class<?> c, String[] names) { Object[] cs = c.getEnumConstants(); check(cs != null, "enum constants"); check(cs.length == names.length, "enum constant count"); for (String n : names) check(hasConst(cs, n), "constant " + n); }
    static boolean hasConst(Object[] cs, String name) { for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true; return false; }
}

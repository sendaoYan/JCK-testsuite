/*
 * @test
 * @summary java.security.DrbgParameters.Instantiation conformance: white-box structure + black-box behavior.
 * @run main InstantiationTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.DrbgParameters;
import java.security.DrbgParameters.Instantiation;
import java.security.SecureRandomParameters;
import java.util.Arrays;

public class InstantiationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.DrbgParameters$Instantiation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface() && !c.isEnum(), "class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        checkInterfaces(c, new String[]{"java.security.SecureRandomParameters"});
        checkMethods(c, new String[]{"getStrength", "getCapability", "getPersonalizationString", "toString"});
    }

    static void testBlackBox() throws Exception {
        byte[] ps = new byte[]{1, 2, 3};
        Instantiation inst = DrbgParameters.instantiation(192, DrbgParameters.Capability.RESEED_ONLY, ps);
        check(inst.getStrength() == 192, "getStrength");
        check(inst.getCapability() == DrbgParameters.Capability.RESEED_ONLY, "getCapability");
        check(Arrays.equals(ps, inst.getPersonalizationString()), "getPersonalizationString content");
        byte[] first = inst.getPersonalizationString();
        first[0] = 99;
        check(Arrays.equals(inst.getPersonalizationString(), new byte[]{1, 2, 3}), "getPersonalizationString defensive copy");
        Instantiation none = DrbgParameters.instantiation(-1, DrbgParameters.Capability.NONE, null);
        check(none.getStrength() == -1, "getStrength -1");
        check(none.getPersonalizationString() == null, "null personalization");
        checkThrows(IllegalArgumentException.class, () -> DrbgParameters.instantiation(-2, DrbgParameters.Capability.NONE, null));
        checkThrows(NullPointerException.class, () -> DrbgParameters.instantiation(-1, null, null));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
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

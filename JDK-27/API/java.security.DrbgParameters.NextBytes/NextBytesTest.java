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

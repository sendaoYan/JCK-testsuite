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
        System.out.println("PASS (" + passed + " assertions)");
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static void checkEnumConstants(Class<?> c, String[] names) { Object[] cs = c.getEnumConstants(); check(cs != null, "enum constants"); check(cs.length == names.length, "enum constant count"); for (String n : names) check(hasConst(cs, n), "constant " + n); }
    static boolean hasConst(Object[] cs, String name) { for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true; return false; }
}

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
        System.out.println("PASS (" + passed + " assertions)");
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static void checkEnumConstants(Class<?> c, String[] names) { Object[] cs = c.getEnumConstants(); check(cs != null, "enum constants"); check(cs.length == names.length, "enum constant count"); for (String n : names) check(hasConst(cs, n), "constant " + n); }
    static boolean hasConst(Object[] cs, String name) { for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true; return false; }
}

/*
 * @test
 * @summary java.security.spec.ECField conformance: white-box structure + black-box behavior.
 * @run main ECFieldTest
 */

import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.spec.ECField;
import java.security.spec.ECFieldFp;

public class ECFieldTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = ECField.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkMethods(c, new String[]{"getFieldSize"});
    }

    static void testBlackBox() {
        check(new ECFieldFp(new BigInteger("23")) instanceof ECField, "ECFieldFp implements ECField");
        check(new ECFieldFp(new BigInteger("23")).getFieldSize() == 5, "getFieldSize via impl");
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
}

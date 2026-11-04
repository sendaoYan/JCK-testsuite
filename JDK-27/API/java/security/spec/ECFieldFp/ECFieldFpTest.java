/*
 * @test
 * @summary java.security.spec.ECFieldFp conformance: white-box structure + black-box behavior.
 * @run main ECFieldFpTest
 */

import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.spec.ECField;
import java.security.spec.ECFieldFp;

public class ECFieldFpTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = ECFieldFp.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.ECField"});
        checkMethods(c, new String[]{"getFieldSize", "getP", "equals", "hashCode"});
    }

    static void testBlackBox() {
        BigInteger p = new BigInteger("23");
        ECFieldFp f = new ECFieldFp(p);
        check(p.equals(f.getP()), "getP");
        check(f.getFieldSize() == 5, "getFieldSize 5 (23 < 32)");
        ECFieldFp f2 = new ECFieldFp(new BigInteger("23"));
        check(f.equals(f2), "equals");
        check(f.hashCode() == f2.hashCode(), "hashCode");
        check(!f.equals(new ECFieldFp(new BigInteger("29"))), "unequal p");
        check(!f.equals("23"), "unequal to non-ECFieldFp");
        check(f instanceof ECField, "instanceof ECField");
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

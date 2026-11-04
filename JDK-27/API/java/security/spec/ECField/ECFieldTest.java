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

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }


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

    static void testContracts() {
        Class<?> c = ECField.class;
        // interface shape: exactly one declared method, standalone
        check(c.getDeclaredMethods().length == 1, "exactly one declared method");
        check(c.getInterfaces().length == 0, "no super-interfaces");
        try {
            java.lang.reflect.Method fs = c.getDeclaredMethod("getFieldSize");
            check(fs.getReturnType() == int.class, "getFieldSize returns int");
            check(java.lang.reflect.Modifier.isAbstract(fs.getModifiers()), "getFieldSize abstract");
            check(fs.getParameterTypes().length == 0, "getFieldSize no params");
        } catch (Exception e) { throw new AssertionError("reflection failed", e); }
        // implementor matrix
        check(ECField.class.isAssignableFrom(java.security.spec.ECFieldF2m.class), "ECFieldF2m implements");
        check(!ECField.class.isAssignableFrom(java.security.spec.EllipticCurve.class), "EllipticCurve not ECField");
        check(!ECField.class.isAssignableFrom(java.security.spec.ECParameterSpec.class), "ECParameterSpec not ECField");
        // polymorphic dispatch
        ECField[] fields = { new java.security.spec.ECFieldFp(new BigInteger("23")),
                             new java.security.spec.ECFieldF2m(113),
                             new java.security.spec.ECFieldF2m(163, new int[]{7, 2, 1}) };
        int sum = 0;
        for (ECField f : fields) sum += f.getFieldSize();
        check(sum == 5 + 113 + 163, "polymorphic sum");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
}

/*
 * @test
 * @summary java.security.spec.EdECPublicKeySpec conformance: white-box structure + black-box behavior.
 * @run main EdECPublicKeySpecTest
 */

import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.spec.EdECPoint;
import java.security.spec.EdECPublicKeySpec;
import java.security.spec.KeySpec;
import java.security.spec.NamedParameterSpec;

public class EdECPublicKeySpecTest {
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
        Class<?> c = EdECPublicKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.KeySpec"});
        checkMethods(c, new String[]{"getParams", "getPoint"});
    }

    static void testBlackBox() {
        EdECPoint point = new EdECPoint(false, BigInteger.TEN);
        EdECPublicKeySpec spec = new EdECPublicKeySpec(NamedParameterSpec.ED25519, point);
        check(NamedParameterSpec.ED25519.equals(spec.getParams()), "getParams");
        check(point.equals(spec.getPoint()), "getPoint");
        check(spec instanceof KeySpec, "instanceof KeySpec");
    }

    static void testContracts() {
        // constructor null matrix (probed: 'params must not be null' / 'point must not be null')
        checkThrows(NullPointerException.class,
                () -> new EdECPublicKeySpec(null, new EdECPoint(false, BigInteger.ONE)),
                "null params -> NPE");
        checkThrows(NullPointerException.class,
                () -> new EdECPublicKeySpec(NamedParameterSpec.ED25519, null), "null point -> NPE");
        // getters round-trip (identity of params instance)
        EdECPoint pt = new EdECPoint(true, BigInteger.valueOf(7));
        EdECPublicKeySpec s = new EdECPublicKeySpec(NamedParameterSpec.ED25519, pt);
        check(s.getParams() == NamedParameterSpec.ED25519, "getParams identity round-trip");
        check(s.getPoint() == pt, "getPoint identity round-trip");
        check(s.getPoint().getY().intValue() == 7 && s.getPoint().isXOdd(), "point fields preserved");
        // no equals override (probed)
        check(!s.equals(new EdECPublicKeySpec(NamedParameterSpec.ED25519, pt)),
                "equal-content not equal (probed)");
        check(s.equals(s), "reflexive");
        check(s.toString().startsWith("java.security.spec.EdECPublicKeySpec@"), "toString default form");
        check(!java.io.Serializable.class.isAssignableFrom(EdECPublicKeySpec.class), "not Serializable");
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

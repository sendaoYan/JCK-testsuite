/*
 * @test
 * @summary java.security.spec.XECPrivateKeySpec conformance: white-box structure + black-box behavior.
 * @run main XECPrivateKeySpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.spec.XECPrivateKeySpec;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;

public class XECPrivateKeySpecTest {
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


    static boolean hasInterface(Class<?> c, String name){for(Class<?> i:c.getInterfaces())if(i.getName().equals(name))return true;return false;}
    static void checkInterfaces(Class<?> c, String[] names){for(String n:names)check(hasInterface(c,n),"implements "+n);}
    static void checkMethods(Class<?> c, String[] names){for(String n:names)check(hasMethod(c,n),"method "+n);}
    static boolean hasMethod(Class<?> c, String name){for(Method m:c.getDeclaredMethods())if(m.getName().equals(name))return true;return false;}

    static void testWhiteBox() {
        Class<?> c = XECPrivateKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        checkInterfaces(c, new String[]{"java.security.spec.KeySpec"});
        checkMethods(c, new String[]{"getParams","getScalar"});
    }

    static void testBlackBox() {
        byte[] scalar = new byte[]{1,2,3,4,5,6,7,8};
        XECPrivateKeySpec spec = new XECPrivateKeySpec(NamedParameterSpec.X25519, scalar);
        check(NamedParameterSpec.X25519.equals(spec.getParams()), "getParams");
        byte[] got = spec.getScalar();
        check(got != scalar, "getScalar defensive copy");
        check(Arrays.equals(scalar, got), "getScalar content");
        got[0] = 99;
        check(spec.getScalar()[0] == 1, "getScalar fresh copy each call");
        scalar[0] = 88;
        check(spec.getScalar()[0] == 1, "constructor defensive copy of input");

        check(spec instanceof java.security.spec.KeySpec, "instanceof KeySpec");
    }

    static void testContracts() {
        // constructor null matrix (probed: 'params must not be null' / 'scalar must not be null')
        checkThrows(NullPointerException.class,
                () -> new XECPrivateKeySpec(null, new byte[1]), "null params -> NPE");
        checkThrows(NullPointerException.class,
                () -> new XECPrivateKeySpec(NamedParameterSpec.X25519, null), "null scalar -> NPE");
        // params is AlgorithmParameterSpec-typed: any spec accepted
        XECPrivateKeySpec s = new XECPrivateKeySpec(new java.security.spec.ECGenParameterSpec("X25519"),
                new byte[]{1});
        check(s.getParams() instanceof java.security.spec.ECGenParameterSpec,
                "generic AlgorithmParameterSpec accepted");
        // empty scalar legal; content preserved
        check(new XECPrivateKeySpec(NamedParameterSpec.X448, new byte[0]).getScalar().length == 0,
                "empty scalar legal");
        // no equals override (probed)
        XECPrivateKeySpec a = new XECPrivateKeySpec(NamedParameterSpec.X25519, new byte[]{1, 2});
        check(!a.equals(new XECPrivateKeySpec(NamedParameterSpec.X25519, new byte[]{1, 2})),
                "equal-content not equal (probed)");
        check(a.equals(a), "reflexive");
        check(a.toString().startsWith("java.security.spec.XECPrivateKeySpec@"), "toString default form");
        check(!java.io.Serializable.class.isAssignableFrom(XECPrivateKeySpec.class), "not Serializable");
    }
    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

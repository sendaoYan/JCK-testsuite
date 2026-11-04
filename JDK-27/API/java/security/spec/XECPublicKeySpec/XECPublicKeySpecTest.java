/*
 * @test
 * @summary java.security.spec.XECPublicKeySpec conformance: white-box structure + black-box behavior.
 * @run main XECPublicKeySpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.spec.XECPublicKeySpec;
import java.security.spec.NamedParameterSpec;

public class XECPublicKeySpecTest {
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
        Class<?> c = XECPublicKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        checkInterfaces(c, new String[]{"java.security.spec.KeySpec"});
        checkMethods(c, new String[]{"getParams","getU"});
    }

    static void testBlackBox() {
        XECPublicKeySpec spec = new XECPublicKeySpec(NamedParameterSpec.X25519, BigInteger.TEN);
        check(NamedParameterSpec.X25519.equals(spec.getParams()), "getParams");
        check(BigInteger.TEN.equals(spec.getU()), "getU");
        check(spec instanceof java.security.spec.KeySpec, "instanceof KeySpec");
    }

    static void testContracts() {
        // constructor null matrix (probed: 'params must not be null' / 'u must not be null')
        checkThrows(NullPointerException.class,
                () -> new XECPublicKeySpec(null, BigInteger.ONE), "null params -> NPE");
        checkThrows(NullPointerException.class,
                () -> new XECPublicKeySpec(NamedParameterSpec.X25519, null), "null u -> NPE");
        // getters round-trip; u value semantics
        XECPublicKeySpec s = new XECPublicKeySpec(NamedParameterSpec.X448, BigInteger.ZERO);
        check(s.getU().equals(BigInteger.ZERO), "u == 0 legal");
        check(new XECPublicKeySpec(NamedParameterSpec.X448, BigInteger.ONE.negate()).getU()
                .intValue() == -1, "negative u accepted (no range validation)");
        check(s.getParams() == NamedParameterSpec.X448, "getParams identity round-trip");
        // no equals override (probed)
        XECPublicKeySpec a = new XECPublicKeySpec(NamedParameterSpec.X25519, BigInteger.TEN);
        check(!a.equals(new XECPublicKeySpec(NamedParameterSpec.X25519, BigInteger.TEN)),
                "equal-content not equal (probed)");
        check(a.equals(a), "reflexive");
        check(a.toString().startsWith("java.security.spec.XECPublicKeySpec@"), "toString default form");
        check(!java.io.Serializable.class.isAssignableFrom(XECPublicKeySpec.class), "not Serializable");
    }
    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

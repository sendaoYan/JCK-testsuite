/*
 * @test
 * @summary java.security.spec.EdECPrivateKeySpec conformance: white-box structure + black-box behavior.
 * @run main EdECPrivateKeySpecTest
 */

import java.lang.reflect.*;
import java.security.spec.EdECPrivateKeySpec;
import java.security.spec.KeySpec;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;

public class EdECPrivateKeySpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = EdECPrivateKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.KeySpec"});
        checkMethods(c, new String[]{"getParams", "getBytes"});
    }

    static void testBlackBox() {
        byte[] bytes = new byte[32];
        for (int i = 0; i < bytes.length; i++) bytes[i] = (byte) i;
        EdECPrivateKeySpec spec = new EdECPrivateKeySpec(NamedParameterSpec.ED25519, bytes);
        check(NamedParameterSpec.ED25519.equals(spec.getParams()), "getParams");
        check(Arrays.equals(bytes, spec.getBytes()), "getBytes");

        // defensive copy: mutate input after construction
        bytes[0] = 99;
        check(spec.getBytes()[0] == 0, "stored copy unaffected by input mutation");

        // defensive copy: mutate returned array
        byte[] got = spec.getBytes();
        got[0] = 42;
        check(spec.getBytes()[0] == 0, "getBytes returns fresh copy");

        check(spec instanceof KeySpec, "instanceof KeySpec");
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

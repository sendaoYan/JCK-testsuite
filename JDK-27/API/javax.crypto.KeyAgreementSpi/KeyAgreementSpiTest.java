/*
 * @test
 * @summary javax.crypto.KeyAgreementSpi conformance: key agreement SPI.
 * @run main KeyAgreementSpiTest
 */

import java.lang.reflect.*;

public class KeyAgreementSpiTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] ns) { for (String n : ns) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] ns) { for (String n : ns) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasPublicCtor(Class<?> c) { for (Constructor<?> k : c.getConstructors()) return true; return false; }
    static void checkField(Class<?> c, String n) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) { check(true, "field " + n); return; } check(false, "field " + n); }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.crypto.KeyAgreementSpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(hasPublicCtor(c), "public constructor");
        checkMethods(c, new String[]{"engineInit","engineDoPhase","engineGenerateSecret"});
        System.out.println("PASS (" + passed + " assertions)");
    }
}

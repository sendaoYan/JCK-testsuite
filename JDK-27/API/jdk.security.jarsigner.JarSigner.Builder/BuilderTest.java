/*
 * @test
 * @summary jdk.security.jarsigner.JarSigner.Builder conformance: jarsigner builder methods and defaults.
 * @run main BuilderTest
 */

import jdk.security.jarsigner.*;
import java.security.*;
import java.lang.reflect.*;

public class BuilderTest {
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
        Class<?> c = Class.forName("jdk.security.jarsigner.JarSigner$Builder");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public class");
        checkMethods(c, new String[]{"digestAlgorithm", "signatureAlgorithm", "tsa", "signerName", "eventHandler", "setProperty", "build", "getDefaultDigestAlgorithm", "getDefaultSignatureAlgorithm"});
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        KeyPair kp = kpg.generateKeyPair();
        check(JarSigner.Builder.getDefaultDigestAlgorithm() != null, "default digest algorithm non-null");
        check(JarSigner.Builder.getDefaultSignatureAlgorithm(kp.getPrivate()) != null, "default signature algorithm non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

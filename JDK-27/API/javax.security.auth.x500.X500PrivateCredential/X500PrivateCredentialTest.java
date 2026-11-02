/*
 * @test
 * @summary javax.security.auth.x500.X500PrivateCredential conformance: null certificate rejected and class shape.
 * @run main X500PrivateCredentialTest
 */

import javax.security.auth.x500.*;
import java.security.*;
import java.lang.reflect.*;

public class X500PrivateCredentialTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        KeyPair kp = kpg.generateKeyPair();
        boolean iae = false;
        try { new X500PrivateCredential(null, kp.getPrivate()); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "null certificate throws IllegalArgumentException");
        Class<?> c = Class.forName("javax.security.auth.x500.X500PrivateCredential");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()) && !c.isInterface(), "public final class");
        check(hasInterface(c, "javax.security.auth.Destroyable"), "implements Destroyable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

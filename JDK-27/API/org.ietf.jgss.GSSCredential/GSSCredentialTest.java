/*
 * @test
 * @summary org.ietf.jgss.GSSCredential conformance: white-box structure + black-box behavior.
 * @run main GSSCredentialTest
 */

import org.ietf.jgss.*;
import java.lang.reflect.*;

public class GSSCredentialTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static Field field(Class<?> c, String n) { try { return c.getDeclaredField(n); } catch (NoSuchFieldException e) { return null; } }
    static void checkConstInt(Class<?> c, String n) {
        Field f = field(c, n);
        check(f != null && Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())
                && Modifier.isFinal(f.getModifiers()) && f.getType() == int.class,
                "public static final int " + n);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("org.ietf.jgss.GSSCredential");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "java.lang.Cloneable"), "extends Cloneable");
        checkConstInt(c, "INITIATE_AND_ACCEPT");
        checkConstInt(c, "INITIATE_ONLY");
        checkConstInt(c, "ACCEPT_ONLY");
        checkConstInt(c, "DEFAULT_LIFETIME");
        checkConstInt(c, "INDEFINITE_LIFETIME");
        String[] names = { "dispose", "getName", "getRemainingLifetime", "getRemainingInitLifetime",
                "getRemainingAcceptLifetime", "getUsage", "getMechs", "add", "equals", "hashCode" };
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(GSSCredential.INITIATE_AND_ACCEPT == 0, "INITIATE_AND_ACCEPT == 0");
        check(GSSCredential.INITIATE_ONLY == 1, "INITIATE_ONLY == 1");
        check(GSSCredential.ACCEPT_ONLY == 2, "ACCEPT_ONLY == 2");
        check(GSSCredential.DEFAULT_LIFETIME == 0, "DEFAULT_LIFETIME == 0");
        check(GSSCredential.INDEFINITE_LIFETIME == Integer.MAX_VALUE, "INDEFINITE_LIFETIME == Integer.MAX_VALUE");
        check(GSSCredential.INITIATE_ONLY != GSSCredential.ACCEPT_ONLY
                && GSSCredential.ACCEPT_ONLY != GSSCredential.INITIATE_AND_ACCEPT
                && GSSCredential.INITIATE_ONLY != GSSCredential.INITIATE_AND_ACCEPT,
                "usage constants distinct");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

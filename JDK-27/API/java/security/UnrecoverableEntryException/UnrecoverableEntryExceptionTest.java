/*
 * @test
 * @summary java.security.UnrecoverableEntryException conformance: white-box structure + black-box behavior.
 * @run main UnrecoverableEntryExceptionTest
 */

import java.lang.reflect.*;
import java.security.*;

public class UnrecoverableEntryExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.UnrecoverableEntryException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.security.GeneralSecurityException"), "superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        UnrecoverableEntryException e0 = new UnrecoverableEntryException();
        check(e0.getMessage() == null, "null message");
        UnrecoverableEntryException e1 = new UnrecoverableEntryException("jck entry failure");
        check("jck entry failure".equals(e1.getMessage()), "message");
        check(e1 instanceof GeneralSecurityException, "hierarchy");
        // subclasses
        check(new UnrecoverableKeyException() instanceof UnrecoverableEntryException, "key subtype");
        // no cause constructor in the API: cause stays null
        check(e1.getCause() == null, "null cause");
        // thrown indirectly by KeyStore.getEntry with a wrong password
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        javax.crypto.SecretKey sk = javax.crypto.KeyGenerator.getInstance("AES").generateKey();
        ks.setEntry("s", new KeyStore.SecretKeyEntry(sk), new KeyStore.PasswordProtection("right".toCharArray()));
        try {
            ks.getEntry("s", new KeyStore.PasswordProtection("wrong".toCharArray()));
            check(false, "wrong entry password");
        } catch (UnrecoverableEntryException uee) {
            check(true, "getEntry wrong password");
        }
        check(ks.getEntry("s", new KeyStore.PasswordProtection("right".toCharArray())) != null, "right password");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static void checkInterfaces(Class<?> c, String[] names) {
        for (String n : names) check(hasInterface(c, n), "implements " + n);
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkEnumConstants(Class<?> c, String[] names) {
        Object[] cs = c.getEnumConstants();
        check(cs != null, "enum constants");
        check(cs.length == names.length, "enum constant count");
        for (String n : names) check(hasConst(cs, n), "constant " + n);
    }
    static boolean hasConst(Object[] cs, String name) {
        for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true;
        return false;
    }
}

/*
 * @test
 * @summary java.security.KeyStoreException conformance: white-box structure + black-box behavior.
 * @run main KeyStoreExceptionTest
 */

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

public class KeyStoreExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.KeyStoreException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.security.GeneralSecurityException"), "superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        KeyStoreException e0 = new KeyStoreException();
        check(e0.getMessage() == null, "null message");
        KeyStoreException e1 = new KeyStoreException("jck keystore failure");
        check("jck keystore failure".equals(e1.getMessage()), "message");
        Throwable t = new java.io.IOException("io root");
        KeyStoreException e2 = new KeyStoreException("wrapped", t);
        check(e2.getCause() == t, "cause");
        check(e2 instanceof GeneralSecurityException, "hierarchy");
        // real API: operations on an uninitialized KeyStore throw KeyStoreException
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try {
            ks.size();
            check(false, "size before load");
        } catch (KeyStoreException kse) {
            check(kse.getMessage() != null, "uninitialized size throws");
        }
        try {
            ks.aliases();
            check(false, "aliases before load");
        } catch (KeyStoreException kse) {
            check(true, "uninitialized aliases throws");
        }
        try {
            ks.containsAlias("x");
            check(false, "containsAlias before load");
        } catch (KeyStoreException kse) {
            check(true, "uninitialized containsAlias throws");
        }
        // unknown store type
        try {
            KeyStore.getInstance("JCK-NOT-A-TYPE");
            check(false, "unknown type");
        } catch (KeyStoreException kse) {
            check(true, "unknown type KeyStoreException");
        }
        // deleteEntry of a missing alias after init is a no-op success
        KeyStore init = KeyStore.getInstance("PKCS12");
        init.load(null, null);
        init.deleteEntry("no-such-alias");
        check(init.size() == 0, "delete no-op");

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

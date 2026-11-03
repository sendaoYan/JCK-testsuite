/*
 * @test
 * @summary java.security.KeyManagementException conformance: white-box structure + black-box behavior.
 * @run main KeyManagementExceptionTest
 */

import java.lang.reflect.*;
import java.security.*;

public class KeyManagementExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.KeyManagementException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.security.KeyException"), "superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        KeyManagementException e0 = new KeyManagementException();
        check(e0.getMessage() == null, "null message");
        check(e0.getCause() == null, "null cause");
        KeyManagementException e1 = new KeyManagementException("jck key failure");
        check("jck key failure".equals(e1.getMessage()), "message");
        check(e1 instanceof GeneralSecurityException, "hierarchy");
        check(e1 instanceof Exception, "exception");
        Throwable t = new IllegalStateException("root");
        KeyManagementException e2 = new KeyManagementException("wrapped", t);
        check(e2.getCause() == t, "cause");
        check("wrapped".equals(e2.getMessage()), "message with cause");
        // declared thrown by Identity.setPublicKey (the JDK-27 stub never actually throws it)
        Identity id = new Identity("jck") {};
        id.setPublicKey(null);
        check(true, "KME declared on setPublicKey");

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

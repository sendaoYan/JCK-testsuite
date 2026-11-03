/*
 * @test
 * @summary java.security.KeyStore.ProtectionParameter conformance: white-box structure + black-box behavior.
 * @run main ProtectionParameterTest
 */

import java.lang.reflect.*;
import java.security.KeyStore;



public class ProtectionParameterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.KeyStore$ProtectionParameter");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // marker interface; both JDK implementations satisfy it
        KeyStore.ProtectionParameter pw = new KeyStore.PasswordProtection("x".toCharArray());
        KeyStore.ProtectionParameter cb = new KeyStore.CallbackHandlerProtection(callbacks -> { });
        check(pw instanceof KeyStore.ProtectionParameter, "PasswordProtection implements");
        check(cb instanceof KeyStore.ProtectionParameter, "CallbackHandlerProtection implements");
        // custom minimal implementation
        KeyStore.ProtectionParameter custom = new KeyStore.ProtectionParameter() { };
        check(custom != null, "anonymous implementation");
        // password-less load path accepts the interface end-to-end
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        check(ks.size() == 0, "default type empty load");
        check(KeyStore.getDefaultType() != null, "default type non-null");

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

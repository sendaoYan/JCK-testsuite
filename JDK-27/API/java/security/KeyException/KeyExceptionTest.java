/*
 * @test
 * @summary java.security.KeyException conformance: white-box structure + black-box behavior.
 * @run main KeyExceptionTest
 */

import java.lang.reflect.*;
import java.security.KeyException;
import java.security.GeneralSecurityException;

public class KeyExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.KeyException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.security.GeneralSecurityException"), "superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(c.getDeclaredConstructors().length == 4, "constructor count");
        check(c.getDeclaredMethods().length == 0, "no declared methods");
    }
    static void testBlackBox() {
        KeyException e = new KeyException();
        check(e != null, "no-arg non-null");
        check(e.getMessage() == null, "no-arg getMessage null");
        check(e.getCause() == null, "no-arg getCause null");

        KeyException m = new KeyException("boom");
        check("boom".equals(m.getMessage()), "String ctor message");
        check(m.getCause() == null, "String ctor getCause null");

        Throwable t = new RuntimeException("cause");
        KeyException st = new KeyException("msg", t);
        check("msg".equals(st.getMessage()), "String+Throwable ctor message");
        check(st.getCause() == t, "String+Throwable ctor cause");

        KeyException tt = new KeyException(t);
        check(tt.getCause() == t, "Throwable ctor cause");
        check(tt.getMessage() != null, "Throwable ctor message non-null");

        check(e instanceof GeneralSecurityException, "instanceof GeneralSecurityException");
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

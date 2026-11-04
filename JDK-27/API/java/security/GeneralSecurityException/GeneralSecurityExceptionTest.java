/*
 * @test
 * @summary java.security.GeneralSecurityException conformance: white-box structure + black-box behavior.
 * @run main GeneralSecurityExceptionTest
 */

import java.lang.reflect.*;
import java.security.GeneralSecurityException;

public class GeneralSecurityExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.GeneralSecurityException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(c.getDeclaredConstructors().length == 4, "constructor count");
        check(c.getDeclaredMethods().length == 0, "no declared methods");
    }
    static void testBlackBox() {
        GeneralSecurityException e = new GeneralSecurityException();
        check(e != null, "no-arg non-null");
        check(e.getMessage() == null, "no-arg getMessage null");
        check(e.getCause() == null, "no-arg getCause null");

        GeneralSecurityException m = new GeneralSecurityException("boom");
        check("boom".equals(m.getMessage()), "String ctor message");
        check(m.getCause() == null, "String ctor getCause null");

        Throwable t = new RuntimeException("cause");
        GeneralSecurityException st = new GeneralSecurityException("msg", t);
        check("msg".equals(st.getMessage()), "String+Throwable ctor message");
        check(st.getCause() == t, "String+Throwable ctor cause");

        GeneralSecurityException tt = new GeneralSecurityException(t);
        check(tt.getCause() == t, "Throwable ctor cause");
        check(tt.getMessage() != null, "Throwable ctor message non-null");

        check(e instanceof Exception, "instanceof Exception");
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

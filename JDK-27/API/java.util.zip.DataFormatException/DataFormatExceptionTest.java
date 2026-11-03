/*
 * @test
 * @summary java.util.zip.DataFormatException conformance: white-box structure + black-box constructors/inheritance.
 * @run main DataFormatExceptionTest
 */

import java.lang.reflect.Modifier;
import java.util.zip.DataFormatException;

public class DataFormatExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> clazz(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = clazz("java.util.zip.DataFormatException");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(c.getSuperclass() == Exception.class, "extends Exception");
        c.getConstructor();
        c.getConstructor(String.class);
        check(true, "no-arg and String constructors present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DataFormatException e = new DataFormatException();
        check(e.getMessage() == null, "no-arg message null");
        check(e.getCause() == null, "no-arg cause null");
        check(e instanceof Exception, "is an Exception");
        check(e instanceof Throwable, "is a Throwable");

        DataFormatException e2 = new DataFormatException("bad format");
        check("bad format".equals(e2.getMessage()), "message ctor");
        check(e2 instanceof Exception, "message ctor is an Exception");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

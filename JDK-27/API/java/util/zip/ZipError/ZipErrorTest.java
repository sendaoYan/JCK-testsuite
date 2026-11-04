/*
 * @test
 * @summary java.util.zip.ZipError conformance: white-box structure + black-box constructors/inheritance.
 * @run main ZipErrorTest
 */

import java.lang.reflect.Modifier;
import java.util.zip.ZipError;

public class ZipErrorTest {
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
        Class<?> c = clazz("java.util.zip.ZipError");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(c.getSuperclass() == InternalError.class, "extends InternalError");
        c.getConstructor(String.class);
        check(true, "String constructor present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ZipError e = new ZipError("boom");
        check("boom".equals(e.getMessage()), "message ctor");
        check(e.getCause() == null, "cause null");
        check(e instanceof Error, "is an Error");
        check(e instanceof InternalError, "is an InternalError");
        check(e instanceof VirtualMachineError, "is a VirtualMachineError");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

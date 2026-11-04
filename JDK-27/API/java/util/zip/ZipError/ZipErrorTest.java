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


    // ===================== 契约深化：层级/cause/toString/suppressed =====================
    static void testContracts() {
        ZipError e = new ZipError("boom");
        check(e instanceof InternalError, "extends InternalError");
        check(e instanceof Error, "is an Error");
        check("java.util.zip.ZipError: boom".equals(e.toString()), "toString format");
        RuntimeException c = new RuntimeException("r");
        check(e.initCause(c).getCause() == c, "initCause returns this and stores");
        check(new ZipError(null).getMessage() == null, "null message accepted");
        // suppressed
        ZipError sup = new ZipError("s");
        RuntimeException r1 = new RuntimeException("1"), r2 = new RuntimeException("2");
        try {
            sup.addSuppressed(r1);
            sup.addSuppressed(r2);
        } finally { }
        Throwable[] sups = sup.getSuppressed();
        check(sups.length == 2 && sups[0] == r1 && sups[1] == r2, "suppressed preserved in order");
        // 无公开无参构造（API 面锁定）
        try {
            ZipError.class.getConstructor();
            check(false, "no public no-arg ctor expected");
        } catch (NoSuchMethodException ok) { check(true, "only (String) public ctor"); }
        check(e.getStackTrace() != null, "stack trace available");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

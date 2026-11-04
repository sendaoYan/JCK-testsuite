/*
 * @test
 * @summary java.util.zip.ZipError conformance: constructors/inheritance.
 * @run main ZipErrorTest
 */

import java.util.zip.ZipError;

public class ZipErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testZipError() {
        ZipError e = new ZipError("boom");
        check("boom".equals(e.getMessage()), "message ctor");
        check(e instanceof Error, "is an Error");
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

    public static void main(String[] args) {
        testZipError();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

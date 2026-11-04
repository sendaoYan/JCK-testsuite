/*
 * @test
 * @summary java.util.logging.ErrorManager conformance: white-box structure + black-box behavior.
 * @run main ErrorManagerTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.util.logging.ErrorManager;

public class ErrorManagerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.logging.ErrorManager");
        check(c != null, "ErrorManager loadable");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass() == Object.class, "direct superclass Object");

        int[] codes = {ErrorManager.GENERIC_FAILURE, ErrorManager.WRITE_FAILURE,
                ErrorManager.FLUSH_FAILURE, ErrorManager.CLOSE_FAILURE,
                ErrorManager.OPEN_FAILURE, ErrorManager.FORMAT_FAILURE};
        for (int code : codes) {
            check(Modifier.isPublic(ErrorManager.class.getField(
                    fieldNameFor(code)).getModifiers()), "constant " + code + " public");
            check(Modifier.isStatic(ErrorManager.class.getField(
                    fieldNameFor(code)).getModifiers()), "constant " + code + " static");
        }

        check(hasMethod(c, "error", String.class, Exception.class, int.class), "error(String,Exception,int)");
        check(c.getConstructor() != null, "no-arg constructor");
    }

    static String fieldNameFor(int code) {
        switch (code) {
            case 0: return "GENERIC_FAILURE";
            case 1: return "WRITE_FAILURE";
            case 2: return "FLUSH_FAILURE";
            case 3: return "CLOSE_FAILURE";
            case 4: return "OPEN_FAILURE";
            default: return "FORMAT_FAILURE";
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // failure-code constants are distinct and ordered 0..5
        check(ErrorManager.GENERIC_FAILURE == 0, "GENERIC_FAILURE == 0");
        check(ErrorManager.WRITE_FAILURE == 1, "WRITE_FAILURE == 1");
        check(ErrorManager.FLUSH_FAILURE == 2, "FLUSH_FAILURE == 2");
        check(ErrorManager.CLOSE_FAILURE == 3, "CLOSE_FAILURE == 3");
        check(ErrorManager.OPEN_FAILURE == 4, "OPEN_FAILURE == 4");
        check(ErrorManager.FORMAT_FAILURE == 5, "FORMAT_FAILURE == 5");

        ErrorManager em = new ErrorManager();
        check(em != null, "no-arg constructor works");

        // error() reports only the first error to System.err; capture and restore.
        PrintStream orig = System.err;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try {
            System.setErr(new PrintStream(buf, true));
            em.error("boom", null, ErrorManager.WRITE_FAILURE);
            String first = buf.toString();
            check(first.contains("ErrorManager"), "error writes diagnostic to stderr");
            check(first.contains("1"), "error includes failure code");

            em.error("second", null, ErrorManager.FORMAT_FAILURE);
            check(first.equals(buf.toString()), "subsequent error suppressed (reported-once)");
        } finally {
            System.setErr(orig);
        }

        // a fresh manager reports again
        ErrorManager em2 = new ErrorManager();
        PrintStream orig2 = System.err;
        ByteArrayOutputStream buf2 = new ByteArrayOutputStream();
        try {
            System.setErr(new PrintStream(buf2, true));
            em2.error(null, null, ErrorManager.GENERIC_FAILURE);
            check(buf2.toString().contains("0"), "fresh manager reports with its code");
        } finally {
            System.setErr(orig2);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

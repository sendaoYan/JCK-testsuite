/*
 * @test
 * @summary java.text.ParseException conformance: white-box hierarchy + black-box message/offset/cause chain.
 * @run main ParseExceptionTest
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ParseExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.ParseException");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "extends Exception");
        check(c.getConstructor(String.class, int.class) != null, "ctor(String,int)");
        check(c.getMethod("getErrorOffset") != null, "getErrorOffset");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ParseException e = new ParseException("bad", 5);
        check("bad".equals(e.getMessage()), "message");
        check(e.getErrorOffset() == 5, "error offset");
        check(e instanceof Exception, "is checked Exception");
        check(!(((Object) e) instanceof RuntimeException), "not unchecked");
        check(e.getStackTrace() != null, "getStackTrace non-null");

        // cause chain via Throwable.initCause
        Exception cause = new IllegalStateException("root");
        ParseException e2 = new ParseException("wrapped", 3);
        e2.initCause(cause);
        check(e2.getCause() == cause, "initCause/getCause");
        check(e2.getErrorOffset() == 3, "error offset preserved with cause");

        boolean thrown = false;
        try {
            new SimpleDateFormat("yyyy").parse("not-a-year");
        } catch (ParseException ex) {
            thrown = true;
            check(ex.getErrorOffset() >= 0, "thrown error offset non-negative");
        }
        check(thrown, "SimpleDateFormat.parse throws ParseException");
    }


    // ---- deepening helper (contract-level additions) ----
    interface TestBlock { void go() throws Exception; }

    static void expectX(Class<? extends Throwable> expected, TestBlock r, String m) {
        try {
            r.go();
        } catch (Throwable x) {
            if (expected.isInstance(x)) { passed++; return; }
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + x.getClass().getName(), x);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }


    // ================= contract deepening =================

    static void testCtorAndAccessorsContract() {
        ParseException pe = new ParseException("bad input", 42);
        check("bad input".equals(pe.getMessage()), "message preserved");
        check(pe.getErrorOffset() == 42, "error offset preserved");
        check(pe instanceof Exception, "ParseException is an Exception");
        check(new java.text.ParseException(null, 0).getMessage() == null, "null message allowed");
        ParseException neg = new ParseException("neg", -5);
        check(neg.getErrorOffset() == -5, "negative offset kept (no validation)");
        check(new ParseException("m", Integer.MAX_VALUE).getErrorOffset() == Integer.MAX_VALUE,
              "MAX_VALUE offset");
    }

    static void testSerializationContract() throws java.io.IOException, ClassNotFoundException {
        check(java.io.Serializable.class.isAssignableFrom(ParseException.class),
              "ParseException is Serializable");
        long uid = java.io.ObjectStreamClass.lookup(ParseException.class).getSerialVersionUID();
        check(2703218443322787634L == uid, "serialVersionUID is 2703218443322787634");
        ParseException pe = new ParseException("round", 7);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(pe); oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        ParseException back = (ParseException) ois.readObject();
        check("round".equals(back.getMessage()), "round-trip message");
        check(back.getErrorOffset() == 7, "round-trip error offset");
    }

    static void testChainedUsageContract() throws java.text.ParseException {
        // SimpleDateFormat failure reports offset of first offending field
        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH);
        f.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        f.setLenient(false);
        try {
            f.parse("2024-02-30");
            throw new AssertionError("expected PE");
        } catch (ParseException pe) {
            check(pe.getErrorOffset() == 10, "calendar field error offset points at day field");
        }
        try {
            f.parse("zzz");
            throw new AssertionError("expected PE");
        } catch (ParseException pe) {
            check(pe.getErrorOffset() == 0, "text mismatch offset 0");
        }
    }
    public static void main(String[] args) throws Exception {
        testCtorAndAccessorsContract();
        testSerializationContract();
        testChainedUsageContract();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

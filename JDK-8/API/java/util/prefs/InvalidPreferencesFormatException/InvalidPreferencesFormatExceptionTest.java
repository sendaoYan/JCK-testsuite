/*
 * @test
 * @summary java.util.prefs.InvalidPreferencesFormatException conformance: white-box + black-box.
 * @run main InvalidPreferencesFormatExceptionTest
 */

import java.io.Externalizable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.prefs.InvalidPreferencesFormatException;

public class InvalidPreferencesFormatExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.prefs.InvalidPreferencesFormatException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "class not interface");
        check(c.getSuperclass() == Exception.class, "extends Exception");
        check(!Externalizable.class.isAssignableFrom(c), "not Externalizable");

        Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 3, "three public constructors");
        c.getConstructor(Throwable.class);
        c.getConstructor(String.class);
        c.getConstructor(String.class, Throwable.class);
        check(true, "(Throwable), (String), (String, Throwable) ctors present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // (String)
        InvalidPreferencesFormatException e1 = new InvalidPreferencesFormatException("bad xml");
        check(e1 instanceof Exception, "is Exception");
        check("bad xml".equals(e1.getMessage()), "getMessage (String)");
        check(e1.getCause() == null, "no cause initially");

        // (Throwable): message mirrors cause.toString()
        IllegalStateException cause = new IllegalStateException("parse failed");
        InvalidPreferencesFormatException e2 = new InvalidPreferencesFormatException(cause);
        check(e2.getCause() == cause, "cause wired (Throwable)");
        check("java.lang.IllegalStateException: parse failed".equals(e2.getMessage()),
                "message == cause toString");

        // (String, Throwable): both message and cause set
        InvalidPreferencesFormatException e3 =
                new InvalidPreferencesFormatException("explicit", cause);
        check("explicit".equals(e3.getMessage()), "getMessage (String,Throwable)");
        check(e3.getCause() == cause, "cause wired (String,Throwable)");

        // inherited initCause works when no cause set yet
        InvalidPreferencesFormatException e4 = new InvalidPreferencesFormatException("later");
        check(e4.initCause(cause) == e4, "initCause returns this");
        check(e4.getCause() == cause, "getCause after initCause");

        // throw / catch round-trip
        try {
            throw new InvalidPreferencesFormatException(new IllegalArgumentException("bad"));
        } catch (InvalidPreferencesFormatException ex) {
            check(ex.getCause() instanceof IllegalArgumentException, "caught cause type");
        }
    }


    // ===================== 契约深化：坏 XML 导入抛 InvalidPreferencesFormatException =====================
    static void testContracts() throws Exception {
        // 畸形 XML
        byte[] bad = "<not-prefs></not-prefs>".getBytes("UTF-8");
        try {
            java.util.prefs.Preferences.importPreferences(new java.io.ByteArrayInputStream(bad));
            check(false, "malformed import throws");
        } catch (java.util.prefs.InvalidPreferencesFormatException e) {
            check(true, "malformed XML -> InvalidPreferencesFormatException");
        }
        try {
            java.util.prefs.Preferences.importPreferences(new java.io.ByteArrayInputStream("garbage".getBytes("UTF-8")));
            check(false, "non-XML import throws");
        } catch (java.util.prefs.InvalidPreferencesFormatException e) {
            check(true, "non-XML -> InvalidPreferencesFormatException");
        }
        // 构造器链
        java.util.prefs.InvalidPreferencesFormatException e1 =
                new java.util.prefs.InvalidPreferencesFormatException("m");
        check("m".equals(e1.getMessage()) && e1.getCause() == null, "message ctor");
        java.io.IOException c = new java.io.IOException("io");
        java.util.prefs.InvalidPreferencesFormatException e2 =
                new java.util.prefs.InvalidPreferencesFormatException("m2", c);
        check("m2".equals(e2.getMessage()) && e2.getCause() == c, "message+cause ctor");
    }

    public static void main(String[] args) throws Exception {
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

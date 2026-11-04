/*
 * @test
 * @summary java.util.prefs.BackingStoreException conformance: white-box structure + black-box behavior.
 * @run main BackingStoreExceptionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.prefs.BackingStoreException;

public class BackingStoreExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.prefs.BackingStoreException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "class not interface");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass() == Exception.class, "extends Exception");

        Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 2, "two public constructors");
        c.getConstructor(String.class);
        c.getConstructor(Throwable.class);
        check(true, "(String) and (Throwable) ctors present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // (String) constructor
        BackingStoreException e1 = new BackingStoreException("boom");
        check(e1 instanceof Exception, "is Exception");
        check("boom".equals(e1.getMessage()), "getMessage");
        check(e1.getCause() == null, "no cause initially");

        // (Throwable) constructor: message mirrors cause.toString()
        RuntimeException cause = new RuntimeException("root");
        BackingStoreException e2 = new BackingStoreException(cause);
        check(e2.getCause() == cause, "cause wired");
        check("java.lang.RuntimeException: root".equals(e2.getMessage()), "message == cause toString");

        // throw / catch round-trip
        try {
            throw new BackingStoreException("thrown");
        } catch (BackingStoreException ex) {
            check("thrown".equals(ex.getMessage()), "caught message");
        }

        // a caught cause can be unwrapped through the exception
        try {
            throw new BackingStoreException(new IllegalStateException("inner"));
        } catch (BackingStoreException ex) {
            check(ex.getCause() instanceof IllegalStateException, "unwrapped cause type");
        }
    }


    // ===================== 契约深化：受检异常链（cause 往返） =====================
    static void testContracts() {
        java.io.IOException cause = new java.io.IOException("disk gone");
        java.util.prefs.BackingStoreException e = new java.util.prefs.BackingStoreException(cause);
        check(e.getCause() == cause, "cause identity from throwable ctor");
        check("java.io.IOException: disk gone".equals(e.getMessage()), "message derived from cause");
        java.util.prefs.BackingStoreException m = new java.util.prefs.BackingStoreException("msg");
        check("msg".equals(m.getMessage()) && m.getCause() == null, "message ctor");
        // 受检：必须声明或捕获（编译器锁定——此处捕获即证明）
        try {
            throw new java.util.prefs.BackingStoreException("x");
        } catch (java.util.prefs.BackingStoreException caught) {
            check(true, "checked exception catchable");
        }
    }

    public static void main(String[] args) throws Exception {
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

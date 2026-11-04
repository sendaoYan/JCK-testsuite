/*
 * @test
 * @summary java.util.NoSuchElementException conformance: white-box structure + black-box behavior.
 * @run main NoSuchElementExceptionTest
 */

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class NoSuchElementExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.NoSuchElementException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        c.getDeclaredConstructor();
        c.getDeclaredConstructor(String.class);
        c.getDeclaredConstructor(Throwable.class);
        c.getDeclaredConstructor(String.class, Throwable.class);
        check(true, "four constructors present");
    }

    static void testBlackBox() {
        check(new NoSuchElementException().getMessage() == null, "no-arg message null");
        check("gone".equals(new NoSuchElementException("gone").getMessage()), "String ctor message");
        Throwable cause = new IllegalStateException("c");
        check(cause == new NoSuchElementException(cause).getCause(), "Throwable ctor cause");
        check(cause == new NoSuchElementException("m", cause).getCause(), "String+Throwable cause");

        Iterator<String> it = List.<String>of().iterator();
        expectThrows(it::next, NoSuchElementException.class, "empty iterator next");
    }


    // ===================== 契约深化：触发矩阵/(Throwable) 构造(9+)/空迭代器触发 =====================
    static void testNseeContracts() {
        check(NoSuchElementException.class.getSuperclass() == RuntimeException.class,
              "extends RuntimeException");
        check(new NoSuchElementException().getMessage() == null, "no-arg null message");
        java.util.Iterator<Object> empty = java.util.Collections.<Object>emptyList().iterator();
        try { empty.next(); check(false, "empty iterator next NSEE"); }
        catch (NoSuchElementException e) { check(true, "empty iterator next NSEE"); }
        java.util.Enumeration<Object> en = java.util.Collections.emptyEnumeration();
        try { en.nextElement(); check(false, "empty enumeration NSEE"); }
        catch (NoSuchElementException e) { check(true, "empty enumeration NSEE"); }
        try {
            java.lang.reflect.Constructor<NoSuchElementException> ct =
                  NoSuchElementException.class.getConstructor(Throwable.class);
            Throwable cause = new IllegalStateException("c");
            NoSuchElementException withCause = ct.newInstance(cause);
            check(withCause.getCause() == cause, "(Throwable) ctor (Java 9+)");
        } catch (NoSuchMethodException nsme) {
            check(false, "(Throwable) ctor expected present");
        } catch (ReflectiveOperationException re) {
            check(false, "(Throwable) ctor invocation failed");
        }
        try {
            java.lang.reflect.Constructor<NoSuchElementException> cst =
                  NoSuchElementException.class.getConstructor(String.class, Throwable.class);
            NoSuchElementException both = cst.newInstance("m", new IllegalStateException("c"));
            check("m".equals(both.getMessage()) && both.getCause() != null,
                  "(String,Throwable) ctor (Java 9+)");
        } catch (NoSuchMethodException nsme) {
            check(false, "(String,Throwable) ctor expected present");
        } catch (ReflectiveOperationException re) {
            check(false, "(String,Throwable) ctor invocation failed");
        }
    }

    // ===================== 契约深化：序列化 round-trip/直接构造 (Throwable)/(String,Throwable)/Vector 首末触发 =====================

    static void testSerializationRoundTrip() throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(new NoSuchElementException("keep-me"));
        }
        Object back = new java.io.ObjectInputStream(
              new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof NoSuchElementException, "round-trip type preserved");
        check("keep-me".equals(((NoSuchElementException) back).getMessage()),
              "round-trip message preserved");
        check(back instanceof java.io.Serializable, "NSEE is Serializable");
    }

    static void testDirectCauseConstructors() {
        // JDK-9+ 直接可用（编译期绑定）：(Throwable) 与 (String, Throwable)
        Throwable cause = new IllegalStateException("c");
        NoSuchElementException a = new NoSuchElementException(cause);
        check(a.getCause() == cause, "(Throwable) ctor cause identity");
        check(cause.toString().equals(a.getMessage()),
              "(Throwable) ctor message defaults to cause.toString()");
        NoSuchElementException b = new NoSuchElementException("m", cause);
        check(b.getCause() == cause && "m".equals(b.getMessage()),
              "(String,Throwable) ctor message and cause");
        try {
            b.initCause(new RuntimeException("other"));
            check(false, "initCause once only");
        } catch (IllegalStateException expected) {
            check(true, "initCause once only (ISE)");
        }
    }

    static void testVectorTriggers() {
        expectThrows(() -> new java.util.Vector<Object>().firstElement(),
              NoSuchElementException.class, "empty Vector firstElement NSEE");
        expectThrows(() -> new java.util.Vector<Object>().lastElement(),
              NoSuchElementException.class, "empty Vector lastElement NSEE");
        java.util.Vector<String> v = new java.util.Vector<>(List.of("a", "b"));
        check("a".equals(v.firstElement()) && "b".equals(v.lastElement()),
              "non-empty first/last element succeed");
        expectThrows(() -> java.util.Collections.emptyEnumeration().nextElement(),
              NoSuchElementException.class, "empty enumeration trigger");
        expectThrows(() -> java.util.Collections.<Object>emptyList().iterator().next(),
              NoSuchElementException.class, "empty iterator trigger");
    }

    static void testMessageExactness() {
        check("exact".equals(new NoSuchElementException("exact").getMessage()),
              "message preserved verbatim");
        check(new NoSuchElementException().getMessage() == null, "no-arg message null");
        check(new NoSuchElementException("t").toString().contains("t")
              && new NoSuchElementException("t").toString().contains("NoSuchElementException"),
              "toString includes class name and message");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNseeContracts();
        testSerializationRoundTrip();
        testDirectCauseConstructors();
        testVectorTriggers();
        testMessageExactness();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

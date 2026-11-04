/*
 * @test
 * @summary java.util.NoSuchElementException API conformance: hierarchy and the (), (String),
 *          and (String, Throwable) constructors.
 * @run main NoSuchElementExceptionTest
 */

import java.util.NoSuchElementException;

public class NoSuchElementExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testConstructors() {
        NoSuchElementException e = new NoSuchElementException();
        check(e instanceof RuntimeException, "is a RuntimeException");
        NoSuchElementException e2 = new NoSuchElementException("msg");
        check("msg".equals(e2.getMessage()), "(String) constructor");
        try {
            java.lang.reflect.Constructor<NoSuchElementException> c =
                NoSuchElementException.class.getConstructor(String.class, Throwable.class);
            NoSuchElementException e3 = c.newInstance("msg", new Exception("cause"));
            check("msg".equals(e3.getMessage()) && e3.getCause() != null,
                  "(String, Throwable) constructor [Java 9+]");
        } catch (NoSuchMethodException ex) {
            // JDK 8: the (String, Throwable) constructor does not exist.
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
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
            check(true, "(Throwable) ctor absent on JDK 8");
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
            check(true, "(String,Throwable) ctor absent on JDK 8");
        } catch (ReflectiveOperationException re) {
            check(false, "(String,Throwable) ctor invocation failed");
        }
    }

    // ===================== 契约深化：序列化 round-trip/Vector 首末元素触发/空枚举迭代器双触发 =====================

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

    static void testVectorTriggers() {
        try {
            new java.util.Vector<Object>().firstElement();
            check(false, "empty Vector firstElement NSEE");
        } catch (NoSuchElementException e) {
            check(true, "empty Vector firstElement NSEE");
        }
        try {
            new java.util.Vector<Object>().lastElement();
            check(false, "empty Vector lastElement NSEE");
        } catch (NoSuchElementException e) {
            check(true, "empty Vector lastElement NSEE");
        }
        // 非空时不抛
        java.util.Vector<String> v = new java.util.Vector<>(java.util.Arrays.asList("a", "b"));
        check("a".equals(v.firstElement()) && "b".equals(v.lastElement()),
              "non-empty first/last element succeed");
        // 空枚举与空迭代器双触发（同一异常类型）
        try {
            java.util.Collections.emptyEnumeration().nextElement();
            check(false, "empty enumeration trigger");
        } catch (NoSuchElementException e) {
            check(true, "empty enumeration trigger");
        }
        try {
            java.util.Collections.<Object>emptyList().iterator().next();
            check(false, "empty iterator trigger");
        } catch (NoSuchElementException e) {
            check(true, "empty iterator trigger");
        }
    }

    static void testMessageExactness() {
        check("exact".equals(new NoSuchElementException("exact").getMessage()),
              "message preserved verbatim");
        check(new NoSuchElementException().getMessage() == null, "no-arg message null");
        // toString 含消息
        check(new NoSuchElementException("t").toString().contains("t")
              && new NoSuchElementException("t").toString().contains("NoSuchElementException"),
              "toString includes class name and message");
    }

    public static void main(String[] args) throws Exception {
        testConstructors();
        testNseeContracts();
        testSerializationRoundTrip();
        testVectorTriggers();
        testMessageExactness();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

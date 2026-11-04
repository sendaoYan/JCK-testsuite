/*
 * @test
 * @summary java.util.ConcurrentModificationException API conformance: hierarchy and the (),
 *          (String), and (String, Throwable) constructors.
 * @run main ConcurrentModificationExceptionTest
 */

import java.util.ConcurrentModificationException;

public class ConcurrentModificationExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testConstructors() {
        ConcurrentModificationException e = new ConcurrentModificationException();
        check(e instanceof RuntimeException, "is a RuntimeException");
        ConcurrentModificationException e2 = new ConcurrentModificationException("msg");
        check("msg".equals(e2.getMessage()), "(String) constructor");
        ConcurrentModificationException e3 =
            new ConcurrentModificationException("msg", new Exception("cause"));
        check("msg".equals(e3.getMessage()) && e3.getCause() != null,
              "(String, Throwable) constructor");
    }


    // ===================== 契约深化：构造矩阵/cause 保持/触发（fail-fast 迭代） =====================
    static void testCmeContracts() {
        check(ConcurrentModificationException.class.getSuperclass() == RuntimeException.class,
              "extends RuntimeException");
        check(new ConcurrentModificationException().getMessage() == null, "no-arg null message");
        check("msg".equals(new ConcurrentModificationException("msg").getMessage()),
              "(String) ctor");
        Throwable cause = new IllegalStateException("root");
        check(new ConcurrentModificationException(cause).getCause() == cause,
              "(Throwable) ctor (Java 8+)");
        check(new ConcurrentModificationException("m", cause).getCause() == cause
              && "m".equals(new ConcurrentModificationException("m", cause).getMessage()),
              "(String,Throwable) ctor (Java 8+)");
        java.util.List<String> list = new java.util.ArrayList<String>(java.util.Arrays.asList("a", "b"));
        try {
            for (String s : list) {
                if ("b".equals(s)) list.add("c");
            }
            check(false, "structural modification during iteration CME");
        } catch (ConcurrentModificationException e) {
            check(true, "structural modification during iteration CME");
        }
    }

    // ===================== 契约深化：序列化 round-trip/initCause 一次性/suppressed/多载体触发 =====================

    static void testSerializationRoundTrip() throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        ConcurrentModificationException src = new ConcurrentModificationException("ser-msg");
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(src);
        }
        Object back = new java.io.ObjectInputStream(
              new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof ConcurrentModificationException, "round-trip type preserved");
        check("ser-msg".equals(((ConcurrentModificationException) back).getMessage()),
              "round-trip message preserved");
        check(back instanceof java.io.Serializable, "CME is Serializable");
    }

    static void testCauseLifecycle() {
        Throwable first = new IllegalStateException("first");
        ConcurrentModificationException e = new ConcurrentModificationException(first);
        check(e.getCause() == first, "cause is the given instance");
        check(first.toString().equals(e.getMessage()),
              "(Throwable) ctor message defaults to cause.toString()");
        try {
            e.initCause(new IllegalStateException("second"));
            check(false, "initCause after ctor-supplied cause throws ISE");
        } catch (IllegalStateException expected) {
            check(true, "initCause allowed only once (ISE)");
        }
        // 无 cause 构造时 initCause 可用一次
        ConcurrentModificationException fresh = new ConcurrentModificationException();
        Throwable supplied = new RuntimeException("r");
        check(fresh.initCause(supplied) == fresh, "initCause returns this");
        check(fresh.getCause() == supplied, "initCause installs the cause");
        // suppressed 异常
        ConcurrentModificationException sup = new ConcurrentModificationException("sup");
        RuntimeException hidden = new RuntimeException("hidden");
        sup.addSuppressed(hidden);
        check(sup.getSuppressed().length == 1 && sup.getSuppressed()[0] == hidden,
              "suppressed exception retained");
    }

    static void testMoreTriggers() {
        // HashMap 迭代中结构性修改 → CME
        java.util.HashMap<String, Integer> m = new java.util.HashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        java.util.Iterator<String> it = m.keySet().iterator();
        it.next();
        m.put("c", 3);
        try {
            it.next();
            check(false, "HashMap iterator CME on structural mod");
        } catch (ConcurrentModificationException e) {
            check(true, "HashMap iterator CME on structural mod");
        }
        // iterator.remove() 是唯一合法修改：不触发 CME
        java.util.HashMap<String, Integer> m2 = new java.util.HashMap<>();
        m2.put("k", 1);
        java.util.Iterator<String> it2 = m2.keySet().iterator();
        it2.next();
        it2.remove();
        check(m2.isEmpty(), "iterator.remove is the legal modification");
        // Vector 迭代器 fail-fast
        java.util.Vector<String> v = new java.util.Vector<>(java.util.Arrays.asList("x", "y"));
        java.util.Iterator<String> vit = v.iterator();
        vit.next();
        v.add("z");
        try {
            vit.next();
            check(false, "Vector iterator CME on structural mod");
        } catch (ConcurrentModificationException e) {
            check(true, "Vector iterator CME on structural mod");
        }
    }

    public static void main(String[] args) throws Exception {
        testConstructors();
        testCmeContracts();
        testSerializationRoundTrip();
        testCauseLifecycle();
        testMoreTriggers();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

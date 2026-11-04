/*
 * @test
 * @summary java.util.ConcurrentModificationException conformance: white-box structure + black-box behavior.
 * @run main ConcurrentModificationExceptionTest
 */

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class ConcurrentModificationExceptionTest {
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
        Class<?> c = Class.forName("java.util.ConcurrentModificationException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        c.getDeclaredConstructor();
        c.getDeclaredConstructor(String.class);
        c.getDeclaredConstructor(Throwable.class);
        c.getDeclaredConstructor(String.class, Throwable.class);
        check(true, "four constructors present");
    }

    static void testBlackBox() {
        check(new ConcurrentModificationException().getMessage() == null, "no-arg message null");
        check("boom".equals(new ConcurrentModificationException("boom").getMessage()),
                "String ctor message");
        Throwable cause = new IllegalStateException("c");
        check(cause == new ConcurrentModificationException(cause).getCause(), "Throwable ctor cause");
        check(cause == new ConcurrentModificationException("m", cause).getCause(),
                "String+Throwable ctor cause");

        List<String> l = new ArrayList<>();
        l.add("a");
        l.add("b");
        final Iterator<String> it = l.iterator();
        it.next();
        l.add("c");
        expectThrows(it::next, ConcurrentModificationException.class, "fail-fast iteration");
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
        ConcurrentModificationException fresh = new ConcurrentModificationException();
        Throwable supplied = new RuntimeException("r");
        check(fresh.initCause(supplied) == fresh, "initCause returns this");
        check(fresh.getCause() == supplied, "initCause installs the cause");
        ConcurrentModificationException sup = new ConcurrentModificationException("sup");
        RuntimeException hidden = new RuntimeException("hidden");
        sup.addSuppressed(hidden);
        check(sup.getSuppressed().length == 1 && sup.getSuppressed()[0] == hidden,
              "suppressed exception retained");
    }

    static void testMoreTriggers() {
        HashMap<String, Integer> m = new HashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        Iterator<String> it = m.keySet().iterator();
        it.next();
        m.put("c", 3);
        expectThrows(it::next, ConcurrentModificationException.class,
              "HashMap iterator CME on structural mod");
        HashMap<String, Integer> m2 = new HashMap<>();
        m2.put("k", 1);
        Iterator<String> it2 = m2.keySet().iterator();
        it2.next();
        it2.remove();
        check(m2.isEmpty(), "iterator.remove is the legal modification");
        Vector<String> v = new Vector<>(List.of("x", "y"));
        Iterator<String> vit = v.iterator();
        vit.next();
        v.add("z");
        expectThrows(vit::next, ConcurrentModificationException.class,
              "Vector iterator CME on structural mod");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCmeContracts();
        testSerializationRoundTrip();
        testCauseLifecycle();
        testMoreTriggers();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

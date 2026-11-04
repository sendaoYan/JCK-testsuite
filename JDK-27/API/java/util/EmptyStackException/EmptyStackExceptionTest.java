/*
 * @test
 * @summary java.util.EmptyStackException conformance: white-box structure + black-box behavior.
 * @run main EmptyStackExceptionTest
 */

import java.util.EmptyStackException;
import java.util.Stack;

public class EmptyStackExceptionTest {
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
        Class<?> c = Class.forName("java.util.EmptyStackException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        c.getDeclaredConstructor();
        check(c.getDeclaredConstructors().length == 1, "single constructor");
    }

    static void testBlackBox() {
        check(new EmptyStackException().getMessage() == null, "no-arg message null");
        final Stack<String> s = new Stack<>();
        expectThrows(s::pop, EmptyStackException.class, "empty pop");
        s.push("x");
        check("x".equals(s.pop()), "non-empty pop works");
    }


    // ===================== 契约深化：null 消息/Stack 触发矩阵/层次 =====================
    static void testEmptyStackContracts() {
        check(EmptyStackException.class.getSuperclass() == RuntimeException.class,
              "extends RuntimeException");
        check(new EmptyStackException().getMessage() == null, "message is null");
        java.util.Stack<Object> st = new java.util.Stack<Object>();
        check(st.empty(), "new stack is empty");
        try { st.pop(); check(false, "pop on empty EmptyStackException"); }
        catch (EmptyStackException e) { check(true, "pop on empty EmptyStackException"); }
        try { st.peek(); check(false, "peek on empty EmptyStackException"); }
        catch (EmptyStackException e) { check(true, "peek on empty EmptyStackException"); }
        st.push("v");
        check(!st.empty() && "v".equals(st.peek()) && "v".equals(st.pop()),
              "push/peek/pop after empty");
        try { st.pop(); check(false, "pop after drain EmptyStackException"); }
        catch (EmptyStackException e) { check(true, "pop after drain EmptyStackException"); }
    }

    // ===================== 契约深化：唯一构造器参数/序列化 round-trip/空栈 search 不抛/栈轨迹可设 =====================

    static void testSingleConstructorShape() {
        // EmptyStackException 只有无参构造器（无消息/cause 重载）
        check(EmptyStackException.class.getDeclaredConstructors()[0].getParameterCount() == 0,
              "the single constructor takes no arguments");
    }

    static void testSerializationRoundTrip() throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(new EmptyStackException());
        }
        Object back = new java.io.ObjectInputStream(
              new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof EmptyStackException, "round-trip type preserved");
        check(((EmptyStackException) back).getMessage() == null,
              "round-trip message stays null (no message by design)");
    }

    static void testSearchOnEmpty() {
        // 空栈上 search 返回 -1 而不抛异常（与 pop/peek 的 EmptyStackException 相对）
        Stack<Object> s = new Stack<>();
        check(s.search("x") == -1, "search on empty returns -1 (no exception)");
        s.push("x");
        check(s.search("x") == 1, "search is 1-based from the top");
        s.push("y");
        check(s.search("x") == 2 && s.search("y") == 1, "search counts from top");
        check(s.search("missing") == -1, "search of absent element -1");
    }

    static void testStackTraceContract() {
        EmptyStackException e = new EmptyStackException();
        check(e.getStackTrace() != null, "getStackTrace non-null");
        StackTraceElement[] frames = {new StackTraceElement("A", "m", "A.java", 1)};
        e.setStackTrace(frames);
        check(e.getStackTrace().length == 1 && e.getStackTrace()[0].getClassName().equals("A"),
              "setStackTrace installs provided frames");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmptyStackContracts();
        testSingleConstructorShape();
        testSerializationRoundTrip();
        testSearchOnEmpty();
        testStackTraceContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

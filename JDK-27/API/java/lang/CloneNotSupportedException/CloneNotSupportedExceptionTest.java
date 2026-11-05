/*
 * @test
 * @summary java.lang.CloneNotSupportedException conformance: white-box structure + black-box behavior.
 * @run main CloneNotSupportedExceptionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class CloneNotSupportedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expect(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": got " + t.getClass().getName());
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.CloneNotSupportedException");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == Exception.class, "superclass is Exception");
        check(c.getDeclaredConstructors().length == 2, "2 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.CloneNotSupportedException");

        CloneNotSupportedException e0 = new CloneNotSupportedException();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { CloneNotSupportedException e = new CloneNotSupportedException("boom"); check("boom".equals(e.getMessage()), "(String) ctor message"); }

        // common Throwable contract
        CloneNotSupportedException t = new CloneNotSupportedException();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.CloneNotSupportedException"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

    }

    // ==================== 契约深化（2026-11）====================

    /** 未实现 Cloneable 的样本：真触发 Object.clone 的 CNSE。 */
    static class NotCloneable {
        NotCloneable() { }
        NotCloneable cloneIt() throws CloneNotSupportedException {
            return (NotCloneable) this.clone();   // 类体内直调继承的 protected clone()
        }
    }

    /** 白盒补充：恰 2 个构造器（无 (String,Throwable) 形态）、无类型参数、具体类。 */
    static void testWhiteBoxShape() throws Exception {
        Class<?> c = Class.forName("java.lang.CloneNotSupportedException");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable (via Throwable)");
        boolean hasNoArg = false, hasString = false, hasCauseCtor = false;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (ct.getParameterCount() == 0) hasNoArg = true;
            if (ct.getParameterCount() == 1 && ct.getParameterTypes()[0] == String.class) hasString = true;
            if (ct.getParameterCount() == 2) hasCauseCtor = true;
        }
        check(hasNoArg, "no-arg constructor exists");
        check(hasString, "(String) constructor exists");
        check(!hasCauseCtor, "NO (String,Throwable) constructor (use initCause)");
        check(c.getTypeParameters().length == 0, "not generic");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete class");
    }

    /** 真触发：未实现 Cloneable 的 clone 调用 → 捕获同一异常类型 + 精确消息。 */
    static void testRealTriggerFromObjectClone() throws Exception {
        NotCloneable n = new NotCloneable();
        CloneNotSupportedException caught = null;
        try {
            n.cloneIt();
            throw new AssertionError("expected CloneNotSupportedException");
        } catch (CloneNotSupportedException e) {
            caught = e;
        }
        check(caught != null, "CNSE actually thrown");
        check(caught.getClass() == CloneNotSupportedException.class,
              "exact class java.lang.CloneNotSupportedException");
        check(caught.getMessage().equals(NotCloneable.class.getName()),
              "message is exactly the binary name of the clone target: " + caught.getMessage());
        try {
            n.cloneIt();
            throw new AssertionError("unreachable");
        } catch (Exception e) {
            check(e instanceof CloneNotSupportedException, "catchable as Exception (checked)");
        }
        try {
            new NotCloneable().cloneIt();
        } catch (CloneNotSupportedException e2) {
            check(e2.getMessage().equals(caught.getMessage()), "message stable across triggers");
        }
        // 精确 toString 形（无消息无冒号 / 有消息带冒号）
        check(caught.toString().startsWith("java.lang.CloneNotSupportedException"),
              "toString prefix");
        check(new CloneNotSupportedException().toString()
                  .equals("java.lang.CloneNotSupportedException"),
              "toString has no colon when message is null");
        check(caught.toString()
                  .equals("java.lang.CloneNotSupportedException: " + NotCloneable.class.getName()),
              "toString == ClassName: message");
    }

    /** 序列化：round-trip 保类与消息；SUID 锁定（与 JDK-8 一致）。 */
    static void testSerializationAndSvuid() throws Exception {
        long svuid = java.io.ObjectStreamClass.lookup(CloneNotSupportedException.class)
                         .getSerialVersionUID();
        check(svuid == 5195511250079656443L, "serialVersionUID == 5195511250079656443, got " + svuid);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        CloneNotSupportedException orig = new CloneNotSupportedException("ser-msg");
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
            new java.io.ByteArrayInputStream(bos.toByteArray()));
        Object back = ois.readObject();
        ois.close();
        check(back instanceof CloneNotSupportedException, "round-trip preserves the class");
        check("ser-msg".equals(((CloneNotSupportedException) back).getMessage()),
              "round-trip preserves the message");
        check(back.getClass() == CloneNotSupportedException.class, "exact class after round-trip");
    }

    /** 栈轨迹细节：setStackTrace 回读、防御拷贝、top 帧为本测试类。 */
    static void testStackTraceDetails() {
        CloneNotSupportedException e = new CloneNotSupportedException("detail");
        check(e.getStackTrace()[0].getClassName().endsWith("CloneNotSupportedExceptionTest"),
              "top frame is this test class");
        StackTraceElement[] st = {
            new StackTraceElement("Foo", "bar", "Foo.java", 42),
        };
        e.setStackTrace(st);
        check(e.getStackTrace().length == 1, "setStackTrace replaces the array");
        check(e.getStackTrace()[0].getMethodName().equals("bar"), "element round-trip");
        check(e.getStackTrace() != st, "getStackTrace returns a defensive copy");
        check(e.getMessage().equals("detail"), "message untouched by stack ops");
    }

    /** cause 深细节：自引用禁止（精确消息）、多 suppressed 保序、fillInStackTrace。 */
    static void testCauseEdgeCases() {
        CloneNotSupportedException e = new CloneNotSupportedException("edge");
        try {
            e.initCause(e);
            throw new AssertionError("initCause(this): expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            check("Self-causation not permitted".equals(ex.getMessage()),
                  "initCause(this) exact message");
        }
        check(e.getCause() == null, "self-cause attempt left cause null");
        RuntimeException r1 = new RuntimeException("s1"), r2 = new RuntimeException("s2");
        e.addSuppressed(r1);
        e.addSuppressed(r2);
        check(e.getSuppressed().length == 2, "two suppressed kept");
        check(e.getSuppressed()[0] == r1 && e.getSuppressed()[1] == r2,
              "suppressed preserved in insertion order");
        CloneNotSupportedException f = new CloneNotSupportedException("fill");
        int before = f.getStackTrace().length;
        f.fillInStackTrace();
        check(f.getStackTrace().length >= before, "fillInStackTrace refreshes the trace");
        check(!RuntimeException.class.isAssignableFrom(CloneNotSupportedException.class),
              "CNSE is NOT a RuntimeException (checked)");
        check(f instanceof CloneNotSupportedException, "instanceof itself");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testWhiteBoxShape();
        testBlackBox();
        testRealTriggerFromObjectClone();
        testSerializationAndSvuid();
        testStackTraceDetails();
        testCauseEdgeCases();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

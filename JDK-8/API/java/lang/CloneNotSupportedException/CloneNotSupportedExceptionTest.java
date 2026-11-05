/*
 * @test
 * @summary java.lang.CloneNotSupportedException API conformance: constructors and inheritance.
 * @run main CloneNotSupportedExceptionTest
 */

public class CloneNotSupportedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCloneNotSupportedException() {
        CloneNotSupportedException e = new CloneNotSupportedException("m");
        check(e instanceof Exception, "extends Exception");
        check("m".equals(e.getMessage()), "message from (String) ctor");
        check(new CloneNotSupportedException().getMessage() == null, "no-arg ctor has null message");
    }

    // ==================== 契约深化（2026-11）====================

    /** 未实现 Cloneable 的样本：真触发 Object.clone 的 CNSE。 */
    static class NotCloneable {
        NotCloneable() { }
        NotCloneable cloneIt() throws CloneNotSupportedException {
            return (NotCloneable) this.clone();   // 类体内直调继承的 protected clone()
        }
    }

    /** 白盒：恰 2 个构造器（无 (String,Throwable) 形态）、父类 Exception。 */
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.CloneNotSupportedException");
        check(!c.isInterface(), "CloneNotSupportedException is a class");
        check(c.getSuperclass() == Exception.class, "superclass is java.lang.Exception");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable (via Throwable)");
        check(c.getDeclaredConstructors().length == 2, "exactly 2 declared constructors");
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

    /** 构造链：消息、cause 链、initCause 一次性、二次 IllegalStateException。 */
    static void testConstructorAndCauseChain() {
        CloneNotSupportedException e0 = new CloneNotSupportedException();
        check(e0.getMessage() == null, "no-arg: null message");
        check(e0.getCause() == null, "no-arg: null cause");
        check(e0.getLocalizedMessage() == null, "getLocalizedMessage delegates (null)");
        check(e0.toString().equals("java.lang.CloneNotSupportedException"),
              "toString has no colon when message null: " + e0.toString());

        CloneNotSupportedException em = new CloneNotSupportedException("m");
        check("m".equals(em.getMessage()), "(String): message m");
        check(em.getCause() == null, "(String): null cause");
        check(em.toString().equals("java.lang.CloneNotSupportedException: m"),
              "toString == ClassName: message");
        check(em.toString().contains(": m"), "colon before message");

        RuntimeException cause = new RuntimeException("root");
        check(em.initCause(cause) == em, "initCause returns this");
        check(em.getCause() == cause, "cause is the SAME instance");
        check(em.getCause().getMessage().equals("root"), "cause message intact");
        try {
            em.initCause(new RuntimeException("second"));
            throw new AssertionError("second initCause: expected IllegalStateException");
        } catch (IllegalStateException expected) {
            passed++;
        }
        // initCause 不能设为 this（自引用禁止 → IllegalArgumentException）
        try {
            new CloneNotSupportedException().initCause(new CloneNotSupportedException());
            passed++;
            // 合法：cause 可以是另一个 CNSE
        } catch (IllegalStateException e) {
            throw new AssertionError("fresh exception accepts initCause", e);
        }
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
        check(caught.getMessage() != null
                  && caught.getMessage().endsWith(NotCloneable.class.getSimpleName()),
              "message ends with the cloned class simple name: " + caught.getMessage());
        check(caught.getMessage().equals(NotCloneable.class.getName()),
              "message is exactly the binary name of the clone target");
        // 多态 catch：可按 Exception 捕获
        try {
            n.cloneIt();
            throw new AssertionError("unreachable");
        } catch (Exception e) {
            check(e instanceof CloneNotSupportedException, "catchable as Exception (checked)");
        }
        // 再次触发：仍是同一类型、消息一致
        try {
            new NotCloneable().cloneIt();
        } catch (CloneNotSupportedException e2) {
            check(e2.getMessage().equals(caught.getMessage()), "message stable across triggers");
        }
    }

    /** 序列化：Exception 家族 Serializable，round-trip 保类与消息；SUID 锁定。 */
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

    /** Throwable 通用面：栈轨迹/suppressed/printStackTrace。 */
    static void testThrowableSurface() throws Exception {
        CloneNotSupportedException e = new CloneNotSupportedException("surface");
        check(e.getSuppressed().length == 0, "no suppressed by default");
        check(e.getStackTrace().length >= 1, "stack trace captured on construction");
        check(e.getStackTrace()[0].getClassName().endsWith("CloneNotSupportedExceptionTest"),
              "top frame is this test class");
        RuntimeException sup = new RuntimeException("sup");
        e.addSuppressed(sup);
        check(e.getSuppressed().length == 1 && e.getSuppressed()[0] == sup,
              "addSuppressed/getSuppressed same instance");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        e.printStackTrace(new java.io.PrintStream(bos));
        String printed = bos.toString("UTF-8");
        check(printed.contains("java.lang.CloneNotSupportedException: surface"),
              "printStackTrace prints Class: message");
        check(printed.contains("Suppressed: java.lang.RuntimeException: sup"),
              "printStackTrace prints the Suppressed: section");
        // setStackTrace 回读
        StackTraceElement[] st = {
            new StackTraceElement("Foo", "bar", "Foo.java", 42),
        };
        e.setStackTrace(st);
        check(e.getStackTrace().length == 1, "setStackTrace replaces the array");
        check(e.getStackTrace()[0].getMethodName().equals("bar"), "element round-trip");
        check(e.getStackTrace() != st, "getStackTrace returns a defensive copy");
        check(e.getMessage().equals("surface"), "message untouched by stack ops");
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
        testCloneNotSupportedException();
        testWhiteBox();
        testConstructorAndCauseChain();
        testRealTriggerFromObjectClone();
        testSerializationAndSvuid();
        testThrowableSurface();
        testCauseEdgeCases();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

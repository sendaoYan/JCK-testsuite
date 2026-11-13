/*
 * @test
 * @summary java.lang.StackOverflowError conformance: white-box structure + black-box behavior.
 * @run main StackOverflowErrorTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Modifier;

public class StackOverflowErrorTest {
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
        Class<?> c = Class.forName("java.lang.StackOverflowError");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == VirtualMachineError.class, "superclass is VirtualMachineError");
        check(c.getDeclaredConstructors().length == 2, "2 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.StackOverflowError");

        StackOverflowError e0 = new StackOverflowError();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { StackOverflowError e = new StackOverflowError("boom"); check("boom".equals(e.getMessage()), "(String) ctor message"); }

        // common Throwable contract
        StackOverflowError t = new StackOverflowError();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.StackOverflowError"), "toString prefix");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

    }

    static void expectMsg(Class<? extends Throwable> type, String exactMsg, Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (type.isInstance(t) && exactMsg.equals(t.getMessage())) { passed++; return; }
            throw new AssertionError(msg + ": got " + t.getClass().getName() + " [" + t.getMessage() + "]");
        }
    }
    // ---- 契约深化：白盒构造器面 + serialVersionUID 锁定 ----
    static void testContractStructure() throws Exception {
        Class<?> c = StackOverflowError.class;
        check(c == Class.forName("java.lang.StackOverflowError"), "class lookup identity");
        check(!c.isInterface(), "not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class (not abstract)");
        check(c.getSuperclass() == VirtualMachineError.class, "direct superclass is VirtualMachineError");
        check(Error.class.isAssignableFrom(c), "assignable to Error");
        check(Throwable.class.isAssignableFrom(c), "assignable to Throwable");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable (via Error)");
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 2, "exactly 2 declared constructors (javap face)");
        boolean sawNoArg = false, sawString = false, sawCauseForm = false, allPublic = true;
        for (java.lang.reflect.Constructor<?> k : ctors) {
            if (!Modifier.isPublic(k.getModifiers())) allPublic = false;
            Class<?>[] ps = k.getParameterTypes();
            if (ps.length == 0) sawNoArg = true;
            else if (ps.length == 1 && ps[0] == String.class) sawString = true;
            else sawCauseForm = true;
        }
        check(allPublic, "all constructors public");
        check(sawNoArg, "() constructor present");
        check(sawString, "(String) constructor present");
        check(!sawCauseForm, "no cause-taking constructor (cause only via initCause)");
        ObjectStreamClass osc = ObjectStreamClass.lookup(c);
        check(osc != null, "ObjectStreamClass.lookup non-null");
        check(osc.getSerialVersionUID() == 8609175038441759607L, "serialVersionUID locked to 8609175038441759607");
    }
    // ---- 契约深化：构造链值语义 ----
    static void testConstructorChain() {
        StackOverflowError e0 = new StackOverflowError();
        check(e0.getMessage() == null, "no-arg: null message");
        check(e0.getCause() == null, "no-arg: null cause");
        check("java.lang.StackOverflowError".equals(e0.toString()), "no-arg toString exact (no trailing colon)");
        StackOverflowError e1 = new StackOverflowError("m");
        check("m".equals(e1.getMessage()), "(String): message passthrough");
        check("java.lang.StackOverflowError: m".equals(e1.toString()), "(String): toString exact");
        check(e1.getCause() == null, "(String): cause null");
        check("java.lang.StackOverflowError: ".equals(new StackOverflowError("").toString()), "empty message keeps ': ' separator");
        check(new StackOverflowError((String) null).getMessage() == null, "(String)null: null message");
        StackOverflowError a = new StackOverflowError("m");
        check(a != e1 && !a.equals(e1), "identity semantics: equal messages stay distinct instances");
        VirtualMachineError poly = e1;
        check("m".equals(poly.getMessage()), "polymorphic VirtualMachineError ref: dynamic getMessage dispatch");
    }

    // ---- 契约深化：家族归属 + 多态捕获 ----
    static void testFamilyPolymorphism() {
        StackOverflowError e = new StackOverflowError("m");
        check(e instanceof VirtualMachineError, "instanceof VirtualMachineError");
        check(e instanceof Error, "instanceof Error");
        check(e instanceof Throwable, "instanceof Throwable");
        check(e instanceof java.io.Serializable, "instanceof Serializable");
        VirtualMachineError caught = null;
        try { throw e; } catch (VirtualMachineError v) { caught = v; }
        check(caught == e, "catch (VirtualMachineError) receives the same object");
        Error caughtErr = null;
        try { throw e; } catch (Error v) { caughtErr = v; }
        check(caughtErr == e, "catch (Error) receives the same object");
        VirtualMachineError[] family = { new InternalError(), new OutOfMemoryError(), new StackOverflowError(), new UnknownError(), e };
        check(family.length == 5 && family[4] == e, "storable in VirtualMachineError[] alongside all siblings");
    }

    // ---- 契约深化：cause 一次写 + 自反拒绝 ----
    static void testCauseContract() {
        StackOverflowError fresh = new StackOverflowError("m");
        check(fresh.getCause() == null, "fresh instance: cause null");
        RuntimeException c = new RuntimeException("c2");
        check(fresh.initCause(c) == fresh, "initCause returns this");
        check(fresh.getCause() == c, "initCause records the same cause instance");
        expect(IllegalStateException.class, () -> fresh.initCause(new RuntimeException("x")), "second initCause rejected");
        StackOverflowError self = new StackOverflowError();
        expectMsg(IllegalArgumentException.class, "Self-causation not permitted", () -> self.initCause(self), "self-causation rejected");
        check(self.getCause() == null, "failed self-causation leaves cause null");
        StackOverflowError nulled = new StackOverflowError("m");
        check(nulled.initCause(null) == nulled, "initCause(null) permitted (explicit no-cause)");
        expect(IllegalStateException.class, () -> nulled.initCause(new RuntimeException("x")), "initCause(null) also consumes the one-shot");
    }

    // ---- 契约深化：suppressed 抑制异常 ----
    static void testSuppressedContract() {
        StackOverflowError t = new StackOverflowError("m");
        check(t.getSuppressed().length == 0, "fresh instance: no suppressed");
        Exception s1 = new Exception("s1");
        Exception s2 = new Exception("s2");
        t.addSuppressed(s1);
        t.addSuppressed(s2);
        Throwable[] sup = t.getSuppressed();
        check(sup.length == 2, "both suppressed kept");
        check(sup[0] == s1 && sup[1] == s2, "suppressed order preserved");
        sup[0] = null;
        check(t.getSuppressed()[0] == s1, "getSuppressed returns a defensive copy");
    }

    // ---- 契约深化：栈迹写读 + 防御拷贝 ----
    static void testStackTraceContract() {
        StackOverflowError t = new StackOverflowError("m"); // created directly here: top frame is this method
        StackTraceElement[] st = t.getStackTrace();
        check(st.length >= 1, "getStackTrace non-empty");
        check("StackOverflowErrorTest".equals(st[0].getClassName()), "top frame class is the test class");
        check("testStackTraceContract".equals(st[0].getMethodName()), "top frame method is this method");
        check(t.fillInStackTrace() == t, "fillInStackTrace returns this");
        t.setStackTrace(new StackTraceElement[] { new StackTraceElement("Foo", "bar", "Foo.java", 42) });
        StackTraceElement[] back = t.getStackTrace();
        check(back.length == 1 && "Foo.bar(Foo.java:42)".equals(back[0].toString()), "setStackTrace/getStackTrace round-trip");
        check(back[0].equals(new StackTraceElement("Foo", "bar", "Foo.java", 42)), "StackTraceElement value equality");
        back[0] = null;
        check(t.getStackTrace()[0] != null, "getStackTrace returns a defensive copy");
        t.setStackTrace(new StackTraceElement[0]);
        check(t.getStackTrace().length == 0, "setStackTrace accepts empty array");
        t.fillInStackTrace();
        check(t.getStackTrace().length >= 1, "fillInStackTrace refreshes trace");
    }

    // ---- 契约深化：printStackTrace 精确格式 ----
    static void testPrintStackTraceContract() throws Exception {
        StackOverflowError t = new StackOverflowError("m");
        t.initCause(new RuntimeException("c"));
        t.addSuppressed(new Exception("s"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos, true, "UTF-8"));
        String txt = new String(bos.toByteArray(), "UTF-8");
        // printStackTrace terminates each line with the platform line
        // separator, so a Windows trace ends lines with \r\n.
        String[] lines = txt.split("\\r?\\n");
        check("java.lang.StackOverflowError: m".equals(lines[0]), "printStackTrace first line exact");
        check(txt.contains("\tat "), "printStackTrace contains tab-prefixed frames");
        check(txt.contains("Caused by: java.lang.RuntimeException: c"), "Caused by: line exact");
        check(txt.contains("\tSuppressed: java.lang.Exception: s"), "Suppressed: line exact (tab-prefixed)");
        PrintStream kept = System.err;
        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        try {
            System.setErr(new PrintStream(b2, true, "UTF-8"));
            t.printStackTrace(); // no-arg form
            check(new String(b2.toByteArray(), "UTF-8").contains("java.lang.StackOverflowError: m"), "no-arg printStackTrace writes to System.err");
        } finally {
            System.setErr(kept);
        }
    }

    // ---- 契约深化：序列化往返（message/cause/suppressed/栈迹保持）----
    static void testSerializationRoundTrip() throws Exception {
        StackOverflowError t = new StackOverflowError("m");
        t.initCause(new RuntimeException("c"));
        t.addSuppressed(new Exception("s"));
        t.setStackTrace(new StackTraceElement[] { new StackTraceElement("Foo", "bar", "Foo.java", 42) });
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(t);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Object backObj = ois.readObject();
        ois.close();
        check(backObj instanceof StackOverflowError, "round-trip: still a StackOverflowError");
        StackOverflowError back = (StackOverflowError) backObj;
        check(back.getClass() == StackOverflowError.class, "round-trip: exact class preserved");
        check("m".equals(back.getMessage()), "round-trip: message preserved");
        check(back.getCause() instanceof RuntimeException && "c".equals(back.getCause().getMessage()),
                "round-trip: cause preserved by class+message (graph is copied)");
        check(back.getSuppressed().length == 1 && "s".equals(back.getSuppressed()[0].getMessage()),
                "round-trip: suppressed preserved");
        check(back.getStackTrace().length == 1
                && back.getStackTrace()[0].equals(new StackTraceElement("Foo", "bar", "Foo.java", 42)),
                "round-trip: stack trace preserved");
        check(back.toString().equals(t.toString()), "round-trip: toString identical");
    }

    // ---- 契约深化：真实栈溢出触发闭环 ----
    static int depth;          // 已进入的递归层数（下限断言用，不锁具体值）
    static long sink;          // 跨调用累加器，防止递归被消除
    static StackOverflowError caughtSoe;

    static long deep(int n) {
        // 每层 8 个跨调用存活的局部变量，保证栈帧不可被消除
        long l0 = n, l1 = n + 1, l2 = n + 2, l3 = n + 3;
        long l4 = n + 4, l5 = n + 5, l6 = n + 6, l7 = n + 7;
        depth = n;
        long r = deep(n + 1);
        return r + l0 + l1 + l2 + l3 + l4 + l5 + l6 + l7;
    }

    static int triggerDeepRecursion() {
        depth = 0;
        caughtSoe = null;
        try {
            sink += deep(0);
        } catch (StackOverflowError e) {
            caughtSoe = e;
            return depth;
        }
        return -1;
    }

    static String recoveredCall() { return "ok"; }

    static void testRealStackOverflow() {
        int d1 = triggerDeepRecursion();
        check(caughtSoe != null, "first trigger produced a StackOverflowError");
        check(d1 > 1000, "first trigger: recursion depth exceeds 1000 lower bound (got " + d1 + ")");
        check(caughtSoe instanceof StackOverflowError, "caught error instanceof StackOverflowError");
        check(caughtSoe.getClass() == StackOverflowError.class, "JVM throws the exact class");
        check(caughtSoe instanceof VirtualMachineError, "real SOE instanceof VirtualMachineError");
        check(caughtSoe instanceof Error, "real SOE instanceof Error");
        check(caughtSoe.toString().startsWith("java.lang.StackOverflowError"), "toString starts with class name (message unspecified)");
        check(caughtSoe.getStackTrace().length >= 1, "real SOE carries a stack trace");
        byte[] small = new byte[100];
        check(small.length == 100, "small heap allocation succeeds while handling SOE (heap not exhausted: OOM boundary)");
        // 栈可复用：第二次触发同样被捕获
        int d2 = triggerDeepRecursion();
        check(caughtSoe != null, "second trigger also caught (stack reusable after SOE)");
        check(d2 > 1000, "second trigger: depth exceeds 1000 lower bound (got " + d2 + ")");
        check("ok".equals(recoveredCall()), "normal calls work again after SOE");
        // 真实 SOE 按家族句柄重抛，按 VirtualMachineError 捕获同一对象
        boolean viaFamily = false;
        try {
            throw caughtSoe;
        } catch (VirtualMachineError v) {
            viaFamily = (caughtSoe == v);
        }
        check(viaFamily, "real SOE rethrown and caught as VirtualMachineError (same object)");
    }

    // ---- 契约深化：相互（间接）递归同样触发 ----
    static int altDepth;
    static void ping(int n) { long a = n * 3L; altDepth = n; sink += a; pong(n + 1); }
    static void pong(int n) { long b = n * 5L; altDepth = n; sink += b; ping(n + 1); }

    static void testMutualRecursionTrigger() {
        altDepth = 0;
        boolean caught = false;
        try {
            ping(0);
        } catch (StackOverflowError e) {
            caught = true;
        }
        check(caught, "mutual (indirect) recursion also raises StackOverflowError");
        check(altDepth > 1000, "mutual recursion depth exceeds 1000 lower bound (got " + altDepth + ")");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractStructure();
        testConstructorChain();
        testFamilyPolymorphism();
        testCauseContract();
        testSuppressedContract();
        testStackTraceContract();
        testPrintStackTraceContract();
        testSerializationRoundTrip();
        testRealStackOverflow();
        testMutualRecursionTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

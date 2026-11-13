/*
 * @test
 * @summary java.lang.UnknownError conformance: white-box structure + black-box behavior.
 * @run main UnknownErrorTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Modifier;

public class UnknownErrorTest {
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
        Class<?> c = Class.forName("java.lang.UnknownError");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == VirtualMachineError.class, "superclass is VirtualMachineError");
        check(c.getDeclaredConstructors().length == 2, "2 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.UnknownError");

        UnknownError e0 = new UnknownError();
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { UnknownError e = new UnknownError("boom"); check("boom".equals(e.getMessage()), "(String) ctor message"); }

        // common Throwable contract
        UnknownError t = new UnknownError();
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith("java.lang.UnknownError"), "toString prefix");
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
        Class<?> c = UnknownError.class;
        check(c == Class.forName("java.lang.UnknownError"), "class lookup identity");
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
        check(osc.getSerialVersionUID() == 2524784860676771849L, "serialVersionUID locked to 2524784860676771849");
    }
    // ---- 契约深化：构造链值语义 ----
    static void testConstructorChain() {
        UnknownError e0 = new UnknownError();
        check(e0.getMessage() == null, "no-arg: null message");
        check(e0.getCause() == null, "no-arg: null cause");
        check("java.lang.UnknownError".equals(e0.toString()), "no-arg toString exact (no trailing colon)");
        UnknownError e1 = new UnknownError("m");
        check("m".equals(e1.getMessage()), "(String): message passthrough");
        check("java.lang.UnknownError: m".equals(e1.toString()), "(String): toString exact");
        check(e1.getCause() == null, "(String): cause null");
        check("java.lang.UnknownError: ".equals(new UnknownError("").toString()), "empty message keeps ': ' separator");
        check(new UnknownError((String) null).getMessage() == null, "(String)null: null message");
        UnknownError a = new UnknownError("m");
        check(a != e1 && !a.equals(e1), "identity semantics: equal messages stay distinct instances");
        VirtualMachineError poly = e1;
        check("m".equals(poly.getMessage()), "polymorphic VirtualMachineError ref: dynamic getMessage dispatch");
    }

    // ---- 契约深化：家族归属 + 多态捕获 ----
    static void testFamilyPolymorphism() {
        UnknownError e = new UnknownError("m");
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
        UnknownError fresh = new UnknownError("m");
        check(fresh.getCause() == null, "fresh instance: cause null");
        RuntimeException c = new RuntimeException("c2");
        check(fresh.initCause(c) == fresh, "initCause returns this");
        check(fresh.getCause() == c, "initCause records the same cause instance");
        expect(IllegalStateException.class, () -> fresh.initCause(new RuntimeException("x")), "second initCause rejected");
        UnknownError self = new UnknownError();
        expectMsg(IllegalArgumentException.class, "Self-causation not permitted", () -> self.initCause(self), "self-causation rejected");
        check(self.getCause() == null, "failed self-causation leaves cause null");
        UnknownError nulled = new UnknownError("m");
        check(nulled.initCause(null) == nulled, "initCause(null) permitted (explicit no-cause)");
        expect(IllegalStateException.class, () -> nulled.initCause(new RuntimeException("x")), "initCause(null) also consumes the one-shot");
    }

    // ---- 契约深化：suppressed 抑制异常 ----
    static void testSuppressedContract() {
        UnknownError t = new UnknownError("m");
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
        UnknownError t = new UnknownError("m"); // created directly here: top frame is this method
        StackTraceElement[] st = t.getStackTrace();
        check(st.length >= 1, "getStackTrace non-empty");
        check("UnknownErrorTest".equals(st[0].getClassName()), "top frame class is the test class");
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
        UnknownError t = new UnknownError("m");
        t.initCause(new RuntimeException("c"));
        t.addSuppressed(new Exception("s"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos, true, "UTF-8"));
        String txt = new String(bos.toByteArray(), "UTF-8");
        // printStackTrace terminates each line with the platform line
        // separator, so a Windows trace ends lines with \r\n.
        String[] lines = txt.split("\\r?\\n");
        check("java.lang.UnknownError: m".equals(lines[0]), "printStackTrace first line exact");
        check(txt.contains("\tat "), "printStackTrace contains tab-prefixed frames");
        check(txt.contains("Caused by: java.lang.RuntimeException: c"), "Caused by: line exact");
        check(txt.contains("\tSuppressed: java.lang.Exception: s"), "Suppressed: line exact (tab-prefixed)");
        PrintStream kept = System.err;
        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        try {
            System.setErr(new PrintStream(b2, true, "UTF-8"));
            t.printStackTrace(); // no-arg form
            check(new String(b2.toByteArray(), "UTF-8").contains("java.lang.UnknownError: m"), "no-arg printStackTrace writes to System.err");
        } finally {
            System.setErr(kept);
        }
    }

    // ---- 契约深化：序列化往返（message/cause/suppressed/栈迹保持）----
    static void testSerializationRoundTrip() throws Exception {
        UnknownError t = new UnknownError("m");
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
        check(backObj instanceof UnknownError, "round-trip: still a UnknownError");
        UnknownError back = (UnknownError) backObj;
        check(back.getClass() == UnknownError.class, "round-trip: exact class preserved");
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

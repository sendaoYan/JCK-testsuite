/*
 * @test
 * @summary java.lang.VirtualMachineError conformance: white-box structure + black-box behavior.
 * @run main VirtualMachineErrorTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Modifier;

public class VirtualMachineErrorTest {
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
        Class<?> c = Class.forName("java.lang.VirtualMachineError");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == Error.class, "superclass is Error");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getDeclaredConstructors().length == 4, "4 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.VirtualMachineError");

        VirtualMachineError e0 = new VirtualMachineError() {};
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { VirtualMachineError e = new VirtualMachineError("boom") {}; check("boom".equals(e.getMessage()), "(String) ctor message"); }
        { Exception cause = new Exception("cause"); VirtualMachineError e = new VirtualMachineError("boom", cause) {}; check("boom".equals(e.getMessage()) && e.getCause() == cause, "(String,Throwable) ctor"); }
        { Exception cause = new Exception("cause"); VirtualMachineError e = new VirtualMachineError(cause) {}; check(e.getCause() == cause, "(Throwable) ctor cause"); }

        // common Throwable contract
        VirtualMachineError t = new VirtualMachineError() {};
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith(t.getClass().getName()), "toString starts with runtime class name");
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
    // ---- 契约深化：abstract 白盒面 ----
    static void testContractStructure() throws Exception {
        Class<?> c = VirtualMachineError.class;
        check(c == Class.forName("java.lang.VirtualMachineError"), "class lookup identity");
        check(Modifier.isAbstract(c.getModifiers()), "Modifier.isAbstract: never instantiated directly");
        check(!c.isInterface(), "not an interface");
        check(c.getSuperclass() == Error.class, "direct superclass is Error");
        check(Error.class.isAssignableFrom(c), "assignable to Error");
        check(Throwable.class.isAssignableFrom(c), "assignable to Throwable");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable (via Error)");
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 4, "4 declared constructors despite abstractness");
        boolean sawNoArg = false, sawString = false, sawStringCause = false, sawCause = false, allPublic = true;
        for (java.lang.reflect.Constructor<?> k : ctors) {
            if (!Modifier.isPublic(k.getModifiers())) allPublic = false;
            Class<?>[] ps = k.getParameterTypes();
            if (ps.length == 0) sawNoArg = true;
            if (ps.length == 1 && ps[0] == String.class) sawString = true;
            if (ps.length == 2 && ps[0] == String.class && ps[1] == Throwable.class) sawStringCause = true;
            if (ps.length == 1 && ps[0] == Throwable.class) sawCause = true;
        }
        check(allPublic, "all 4 constructors public");
        check(sawNoArg, "() constructor present");
        check(sawString, "(String) constructor present");
        check(sawStringCause, "(String,Throwable) constructor present");
        check(sawCause, "(Throwable) constructor present");
        ObjectStreamClass osc = ObjectStreamClass.lookup(c);
        check(osc != null, "ObjectStreamClass.lookup non-null");
        check(osc.getSerialVersionUID() == 4161983926571568670L, "serialVersionUID locked to 4161983926571568670");
    }

    // ---- 契约深化：abstract 不可实例化（反射负例；编译期由本文件可编译证明）----
    static void testCannotInstantiate() throws Exception {
        boolean ok = false;
        try {
            VirtualMachineError.class.getDeclaredConstructor().newInstance();
        } catch (InstantiationException ie) {
            ok = true;
        }
        check(ok, "no-arg constructor newInstance on abstract class throws InstantiationException");
        int caught = 0;
        for (java.lang.reflect.Constructor<?> k : VirtualMachineError.class.getDeclaredConstructors()) {
            Class<?>[] ps = k.getParameterTypes();
            Object[] as = new Object[ps.length]; // 全 null 实参匹配形态
            try {
                k.newInstance(as);
            } catch (InstantiationException ie) {
                caught++;
            }
        }
        check(caught == 4, "every constructor's newInstance throws InstantiationException (4/4)");
        // 编译期验证：本源文件不含无体的 new VirtualMachineError(...) 调用；文件通过编译即为通过该项
    }

    // ---- 契约深化：具名/匿名子类走全部 4 个构造器 ----
    static void testConstructorSurfaceViaSubclass() {
        VirtualMachineError a = new VirtualMachineError() {};
        check(a.getMessage() == null, "anonymous no-arg: null message");
        check(a.getClass().getSuperclass() == VirtualMachineError.class, "anonymous subclass's superclass is VirtualMachineError");
        check(a instanceof VirtualMachineError, "anonymous subclass instanceof VirtualMachineError");
        VirtualMachineError b = new VirtualMachineError("m") {};
        check("m".equals(b.getMessage()), "anonymous (String): message passthrough");
        RuntimeException cse = new RuntimeException("c");
        VirtualMachineError c = new VirtualMachineError("m", cse) {};
        check("m".equals(c.getMessage()) && c.getCause() == cse, "anonymous (String,Throwable): message + same cause");
        VirtualMachineError d = new VirtualMachineError(cse) {};
        check(d.getCause() == cse && cse.toString().equals(d.getMessage()), "anonymous (Throwable): same cause + message = cause.toString()");
        check(new VirtualMachineError((Throwable) null) {}.getMessage() == null, "anonymous (Throwable)null: null message");
        SubVme s0 = new SubVme();
        check(s0.getMessage() == null, "named subclass no-arg delegation");
        SubVme s1 = new SubVme("m");
        check("m".equals(s1.getMessage()), "named subclass (String) delegation");
        SubVme s2 = new SubVme("m", cse);
        check("m".equals(s2.getMessage()) && s2.getCause() == cse, "named subclass (String,Throwable) delegation");
        SubVme s3 = new SubVme(cse);
        check(s3.getCause() == cse, "named subclass (Throwable) delegation");
    }

    // ---- 契约深化：4 个具体子类的家族归属 ----
    static void testConcreteFamily() {
        InternalError ie = new InternalError("i");
        OutOfMemoryError oome = new OutOfMemoryError("o");
        StackOverflowError soe = new StackOverflowError("s");
        UnknownError ue = new UnknownError("u");
        check(ie instanceof VirtualMachineError, "InternalError belongs to the family");
        check(oome instanceof VirtualMachineError, "OutOfMemoryError belongs to the family");
        check(soe instanceof VirtualMachineError, "StackOverflowError belongs to the family");
        check(ue instanceof VirtualMachineError, "UnknownError belongs to the family");
        VirtualMachineError[] family = { ie, oome, soe, ue };
        check(family.length == 4, "all four concrete siblings storable in VirtualMachineError[]");
        check("i".equals(family[0].getMessage()) && "u".equals(family[3].getMessage()),
                "polymorphic getMessage dispatch on family array");
        for (VirtualMachineError v : family) {
            try {
                throw v;
            } catch (VirtualMachineError caught) {
                check(caught == v, "caught as VirtualMachineError: " + v.getClass().getSimpleName());
            }
        }
        check(InternalError.class.getSuperclass() == VirtualMachineError.class, "InternalError is a direct subclass");
        check(OutOfMemoryError.class.getSuperclass() == VirtualMachineError.class, "OutOfMemoryError is a direct subclass");
        check(StackOverflowError.class.getSuperclass() == VirtualMachineError.class, "StackOverflowError is a direct subclass");
        check(UnknownError.class.getSuperclass() == VirtualMachineError.class, "UnknownError is a direct subclass");
    }

    // ---- 契约深化：子类承载 Throwable 通用契约 + 抽象父序列化 ----
    static void testThrowableContractOnSubclass() throws Exception {
        SubVme t = new SubVme("m");
        check(t.getCause() == null, "fresh cause null");
        RuntimeException c = new RuntimeException("c");
        check(t.initCause(c) == t && t.getCause() == c, "initCause returns this and keeps instance");
        expect(IllegalStateException.class, () -> t.initCause(new RuntimeException("x")), "second initCause rejected");
        SubVme self = new SubVme();
        expectMsg(IllegalArgumentException.class, "Self-causation not permitted", () -> self.initCause(self), "self-causation rejected");
        check(t.getSuppressed().length == 0, "no suppressed by default");
        t.addSuppressed(new Exception("s"));
        check(t.getSuppressed().length == 1, "suppressed kept");
        check(t.getStackTrace().length >= 1, "stack trace non-empty");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos, true, "UTF-8"));
        String txt = new String(bos.toByteArray(), "UTF-8");
        check(txt.startsWith("VirtualMachineErrorTest$SubVme: m"), "printStackTrace first line carries concrete subclass name");
        check(txt.contains("Caused by: java.lang.RuntimeException: c"), "Caused by printed");
        ByteArrayOutputStream ob = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(ob);
        oos.writeObject(t);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(ob.toByteArray()));
        SubVme back = (SubVme) ois.readObject();
        ois.close();
        check("m".equals(back.getMessage()), "round-trip message via abstract-parent serialization");
        check(back.getCause() instanceof RuntimeException && "c".equals(back.getCause().getMessage()), "round-trip cause");
        check(back.getSuppressed().length == 1, "round-trip suppressed");
    }

    static class SubVme extends VirtualMachineError {
        SubVme() { super(); }
        SubVme(String m) { super(m); }
        SubVme(String m, Throwable c) { super(m, c); }
        SubVme(Throwable c) { super(c); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractStructure();
        testCannotInstantiate();
        testConstructorSurfaceViaSubclass();
        testConcreteFamily();
        testThrowableContractOnSubclass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

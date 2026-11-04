/*
 * @test
 * @summary java.lang.invoke.MethodHandleProxies conformance: white-box + black-box behavior.
 *          Deepened: side-effect counting through Runnable, boxing flow
 *          (Integer args over an int handle, null box -> NPE with helpful
 *          message), identity-based equals/hashCode across two same-handle
 *          proxies, JDK-27 wrappers are hidden classes rather than
 *          java.lang.reflect.Proxy, default methods keep their own body,
 *          and the negative matrix with exact messages.
 * @run main MethodHandleProxiesTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MethodHandleProxiesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public interface StringIntFunction {
        int apply(String s);
    }

    public interface AddI {
        int add(Integer a, Integer b);
    }

    public interface ObjFn {
        String apply(Object o);
    }

    public interface Def {
        default String d() { return "DEFAULT"; }
        String apply(String s);
    }

    public interface TwoAbs {
        int a();
        int b();
    }

    static volatile boolean ran;
    static int sideEffects;

    static void mark2() { sideEffects++; }
    static String greet(String s) { return "hi " + s; }
    static int addInts(int a, int b) { return a + b; }

    static String consume(ObjFn f, Object v) { return f.apply(v); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.MethodHandleProxies");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 0, "no public constructor");
        String[] names = {"asInterfaceInstance", "isWrapperInstance", "wrapperInstanceTarget",
                "wrapperInstanceType"};
        for (String n : names) {
            boolean found = false;
            for (Method mm : c.getDeclaredMethods()) if (mm.getName().equals(n)) found = true;
            check(found, "declares " + n);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // Runnable over a void no-arg handle
        ran = false;
        MethodHandle setFlag = lookup.findStatic(MethodHandleProxiesTest.class, "mark",
                MethodType.methodType(void.class));
        Runnable r = MethodHandleProxies.asInterfaceInstance(Runnable.class, setFlag);
        check(r instanceof Runnable, "proxy implements the interface");
        r.run();
        check(ran, "run() dispatched to the handle");

        // function with argument and return value
        MethodHandle len = lookup.findVirtual(String.class, "length",
                MethodType.methodType(int.class));
        StringIntFunction f = MethodHandleProxies.asInterfaceInstance(
                StringIntFunction.class, len);
        check(f.apply("method") == 6, "argument and return flow through");

        // wrapper instance introspection
        check(MethodHandleProxies.isWrapperInstance(r), "isWrapperInstance true");
        check(!MethodHandleProxies.isWrapperInstance("plain"), "isWrapperInstance false");
        check(MethodHandleProxies.wrapperInstanceTarget(r).type()
                      .toMethodDescriptorString().equals("()V"),
              "wrapperInstanceTarget type");
        check(MethodHandleProxies.wrapperInstanceType(r) == Runnable.class,
              "wrapperInstanceType is the interface");
        check((int) MethodHandleProxies.wrapperInstanceTarget(f)
                      .invokeExact((String) "xyz") == 3,
              "wrapperInstanceTarget still computes");

        // non-wrapper input to the introspectors throws
        try {
            MethodHandleProxies.wrapperInstanceTarget(new Object());
            check(false, "wrapperInstanceTarget(non-wrapper) must throw");
        } catch (IllegalArgumentException expected) {
            check(true, "wrapperInstanceTarget IAE");
        }
        try {
            MethodHandleProxies.wrapperInstanceType(new Object());
            check(false, "wrapperInstanceType(non-wrapper) must throw");
        } catch (IllegalArgumentException expected) {
            check(true, "wrapperInstanceType IAE");
        }

        // bad arguments: null interface, non-interface
        try {
            MethodHandleProxies.asInterfaceInstance(null, setFlag);
            check(false, "null interface rejected");
        } catch (NullPointerException expected) {
            check(true, "null interface NPE");
        }
        try {
            MethodHandleProxies.asInterfaceInstance(String.class, len);
            check(false, "non-interface rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "non-interface IAE");
        }
        // handle type not compatible with the single abstract method
        try {
            MethodHandleProxies.asInterfaceInstance(StringIntFunction.class, setFlag);
            check(false, "incompatible handle rejected");
        } catch (RuntimeException expected) {
            check(expected instanceof IllegalArgumentException
                  || expected instanceof WrongMethodTypeException,
                  "incompatible handle rejected with " + expected.getClass().getSimpleName());
        }
    }

    static void mark() { ran = true; }

    // ---- Part 3: deepened contract (2026-11-05) ----

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r,
            String frag, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                if (t.getMessage() != null && t.getMessage().contains(frag)) {
                    passed++; return;
                }
                throw new AssertionError(msg + ": expected message fragment \"" + frag
                        + "\" but got \"" + t.getMessage() + "\"");
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // Each run() reaches the handle exactly once (counted, not just flagged).
    static void testRunnableCounting() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        sideEffects = 0;
        MethodHandle marker = lookup.findStatic(MethodHandleProxiesTest.class, "mark2",
                MethodType.methodType(void.class));
        Runnable r = MethodHandleProxies.asInterfaceInstance(Runnable.class, marker);
        r.run();
        r.run();
        r.run();
        check(sideEffects == 3, "three runs -> three handle invocations");
    }

    // Boxing: Integer parameters unbox into an int handle; null box dies in
    // unboxing; Object parameters widen to the handle's String.
    static void testBoxingFlow() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle adder = lookup.findStatic(MethodHandleProxiesTest.class, "addInts",
                MethodType.methodType(int.class, int.class, int.class));
        AddI box = MethodHandleProxies.asInterfaceInstance(AddI.class, adder);
        check(box.add(2, 3) == 5, "boxed args unboxed");
        check(box.add(-1, 1) == 0, "second boxed call");
        checkThrowsMsg(NullPointerException.class, () -> box.add(null, 3),
                "because \"x\" is null", "null Integer arg NPE message");
        MethodHandle greeter = lookup.findStatic(MethodHandleProxiesTest.class, "greet",
                MethodType.methodType(String.class, String.class));
        ObjFn wide = MethodHandleProxies.asInterfaceInstance(ObjFn.class, greeter);
        check("hi w".equals(wide.apply("w")), "Object param widens");
        check("hi q".equals(consume(wide, "q")), "proxy usable as the interface type");
    }

    // equals/hashCode are identity-flavoured; on JDK-27 the wrapper is a
    // hidden class (jdk.MHProxy*), not a java.lang.reflect.Proxy.
    static void testProxyObjectSemantics() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle greeter = lookup.findStatic(MethodHandleProxiesTest.class, "greet",
                MethodType.methodType(String.class, String.class));
        MethodHandle len = lookup.findVirtual(String.class, "length",
                MethodType.methodType(int.class));
        StringIntFunction p1 = MethodHandleProxies.asInterfaceInstance(
                StringIntFunction.class, len);
        ObjFn p2 = MethodHandleProxies.asInterfaceInstance(ObjFn.class, greeter);
        check(p2.equals(p2), "proxy equals itself");
        check(!p2.equals(MethodHandleProxies.asInterfaceInstance(ObjFn.class, greeter)),
              "same-handle proxies are distinct");
        check(p2.hashCode() == p2.hashCode(), "hashCode stable");
        check(p2.toString() != null && p2.toString().length() > 0, "toString non-empty");
        check(!java.lang.reflect.Proxy.isProxyClass(p2.getClass()),
              "JDK-27 wrapper is not a Proxy (hidden class)");
        boolean implementsSam = false;
        for (Class<?> ifc : p2.getClass().getInterfaces()) {
            if (ifc == ObjFn.class) implementsSam = true;
        }
        check(implementsSam, "wrapper class lists the SAM interface");
        check(p1.apply("method") == 6, "asType-adapted handle still reachable");
    }

    // Default methods run their own body; only the SAM routes to the handle.
    static void testDefaultMethods() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle greeter = lookup.findStatic(MethodHandleProxiesTest.class, "greet",
                MethodType.methodType(String.class, String.class));
        Def d = MethodHandleProxies.asInterfaceInstance(Def.class, greeter);
        check("hi d".equals(d.apply("d")), "SAM routes to the handle");
        check("DEFAULT".equals(d.d()), "default method keeps its own body");
    }

    // Negative matrix with exact messages (JDK-27 wording).
    static void testNegativesMessages() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle greeter = lookup.findStatic(MethodHandleProxiesTest.class, "greet",
                MethodType.methodType(String.class, String.class));
        MethodHandle one = lookup.findStatic(MethodHandleProxiesTest.class, "oneH",
                MethodType.methodType(int.class));
        checkThrowsMsg(IllegalArgumentException.class,
                () -> MethodHandleProxies.asInterfaceInstance(String.class, greeter),
                "not a public interface", "non-interface IAE message");
        checkThrowsMsg(NullPointerException.class,
                () -> MethodHandleProxies.asInterfaceInstance(null, greeter),
                "\"intfc\" is null", "null interface NPE message");
        checkThrows(NullPointerException.class,
                () -> MethodHandleProxies.asInterfaceInstance(ObjFn.class, null),
                "null handle NPE");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> MethodHandleProxies.asInterfaceInstance(CharSequence.class, greeter),
                "not a single-method interface", "CharSequence multi-SAM IAE message");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> MethodHandleProxies.asInterfaceInstance(TwoAbs.class, one),
                "not a single-method interface", "two-abstract-methods IAE message");
        checkThrowsMsg(WrongMethodTypeException.class,
                () -> MethodHandleProxies.asInterfaceInstance(ObjFn.class, one),
                "cannot convert MethodHandle()int to (Object)String",
                "incompatible handle WMTE message");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> MethodHandleProxies.wrapperInstanceTarget(new Object()),
                "not a wrapper instance", "wrapperInstanceTarget(non-wrapper) IAE message");
    }

    static int oneH() { return 1; }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testRunnableCounting();
        testBoxingFlow();
        testProxyObjectSemantics();
        testDefaultMethods();
        testNegativesMessages();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

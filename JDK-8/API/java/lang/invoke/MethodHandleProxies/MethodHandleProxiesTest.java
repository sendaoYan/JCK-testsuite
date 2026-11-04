/*
 * @test
 * @summary java.lang.invoke.MethodHandleProxies conformance: interface wrapper.
 *          Deepened: side-effect dispatch through Runnable (exact counter),
 *          value-returning SAM proxies, boxing/unboxing parameter flow
 *          (Integer args over an int handle, null box -> NPE), wrapper
 *          introspection (wrapperInstanceTarget identity, two proxies of one
 *          handle), identity-based equals/hashCode/toString semantics, default
 *          methods run their own body while the SAM routes to the handle, and
 *          the negative matrix (non-interface/non-SAM IAE messages, null NPE,
 *          incompatible handle WMTE, non-wrapper introspection IAE).
 * @run main MethodHandleProxiesTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Proxy;

public class MethodHandleProxiesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

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

    public interface IntSupplier {
        int get();
    }

    public interface Sam {
        String apply(String s);
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

    static int sideEffects;

    static void mark() { sideEffects++; }
    static String greet(String s) { return "hi " + s; }
    static int add(int a, int b) { return a + b; }
    static int one() { return 1; }

    static String consume(Sam f, String v) { return f.apply(v); }

    static void testMethodHandleProxies() throws Throwable {
        MethodHandle target = MethodHandles.constant(int.class, 42);
        IntSupplier supplier = MethodHandleProxies.asInterfaceInstance(IntSupplier.class, target);
        check(supplier.get() == 42, "asInterfaceInstance invoke");
        check(MethodHandleProxies.isWrapperInstance(supplier), "isWrapperInstance");
        MethodHandle back = MethodHandleProxies.wrapperInstanceTarget(supplier);
        check((int) back.invoke() == 42, "wrapperInstanceTarget");
        check(MethodHandleProxies.wrapperInstanceType(supplier) == IntSupplier.class,
              "wrapperInstanceType");
    }

    // run() dispatches to the void handle; the side-effect counter proves
    // each call reached the handle exactly once.
    static void testRunnableSideEffect() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        sideEffects = 0;
        MethodHandle marker = lookup.findStatic(MethodHandleProxiesTest.class, "mark",
                MethodType.methodType(void.class));
        Runnable r = MethodHandleProxies.asInterfaceInstance(Runnable.class, marker);
        check(r instanceof Runnable, "proxy implements Runnable");
        r.run();
        r.run();
        check(sideEffects == 2, "two runs -> two handle invocations");
        check(MethodHandleProxies.wrapperInstanceType(r) == Runnable.class,
              "wrapper type is Runnable");
    }

    // A value-returning SAM: exact result, usable anywhere the interface fits.
    static void testValueReturningSam() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle greeter = lookup.findStatic(MethodHandleProxiesTest.class, "greet",
                MethodType.methodType(String.class, String.class));
        Sam f = MethodHandleProxies.asInterfaceInstance(Sam.class, greeter);
        check("hi x".equals(f.apply("x")), "SAM returns exact value");
        check("hi y".equals(f.apply("y")), "per-call argument respected");
        check("hi z".equals(consume(f, "z")), "proxy usable as the interface type");
    }

    // Boxing flow: Integer boxes reach an int-parameter handle; a null box
    // dies inside the unboxing with NPE; reference widening also works.
    static void testBoxingFlow() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle adder = lookup.findStatic(MethodHandleProxiesTest.class, "add",
                MethodType.methodType(int.class, int.class, int.class));
        AddI box = MethodHandleProxies.asInterfaceInstance(AddI.class, adder);
        check(box.add(2, 3) == 5, "boxed args unboxed to the handle");
        check(box.add(-1, 1) == 0, "second boxed call");
        checkThrows(NullPointerException.class, () -> box.add(null, 3),
              "null Integer arg NPE during unboxing");
        MethodHandle greeter = lookup.findStatic(MethodHandleProxiesTest.class, "greet",
                MethodType.methodType(String.class, String.class));
        ObjFn wide = MethodHandleProxies.asInterfaceInstance(ObjFn.class, greeter);
        check("hi w".equals(wide.apply("w")), "Object param widens to String");
    }

    // Introspection: the exact original handle is recoverable, and two proxies
    // over one handle expose the very same target object.
    static void testWrapperIntrospection() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle greeter = lookup.findStatic(MethodHandleProxiesTest.class, "greet",
                MethodType.methodType(String.class, String.class));
        Sam p1 = MethodHandleProxies.asInterfaceInstance(Sam.class, greeter);
        Sam p2 = MethodHandleProxies.asInterfaceInstance(Sam.class, greeter);
        check(MethodHandleProxies.isWrapperInstance(p1), "p1 is wrapper instance");
        check(!MethodHandleProxies.isWrapperInstance("plain"), "String is not a wrapper");
        check(MethodHandleProxies.wrapperInstanceTarget(p1) == greeter,
              "wrapperInstanceTarget is the exact original handle");
        check(MethodHandleProxies.wrapperInstanceTarget(p1)
                    == MethodHandleProxies.wrapperInstanceTarget(p2),
              "both proxies expose the same target");
        check(MethodHandleProxies.wrapperInstanceType(p1) == Sam.class,
              "wrapperInstanceType is the SAM");
        MethodHandle back = MethodHandleProxies.wrapperInstanceTarget(p1);
        check("hi q".equals((String) back.invokeExact("q")),
              "recovered target still computes");
        checkThrowsMsg(IllegalArgumentException.class,
              () -> MethodHandleProxies.wrapperInstanceTarget(new Object()),
              "not a wrapper instance", "wrapperInstanceTarget(non-wrapper) IAE");
        checkThrowsMsg(IllegalArgumentException.class,
              () -> MethodHandleProxies.wrapperInstanceType(new Object()),
              "not a wrapper instance", "wrapperInstanceType(non-wrapper) IAE");
    }

    // equals/hashCode/toString are identity-flavoured: two proxies over the
    // same handle are distinct objects; each is equal only to itself.
    static void testProxyObjectSemantics() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle greeter = lookup.findStatic(MethodHandleProxiesTest.class, "greet",
                MethodType.methodType(String.class, String.class));
        Sam p1 = MethodHandleProxies.asInterfaceInstance(Sam.class, greeter);
        Sam p2 = MethodHandleProxies.asInterfaceInstance(Sam.class, greeter);
        check(p1.equals(p1), "proxy equals itself");
        check(!p1.equals(p2), "same-handle proxies are not equal");
        check(!p2.equals(p1), "not symmetric-equal either");
        check(p1.hashCode() == p1.hashCode(), "hashCode stable across calls");
        check(p1.toString() != null && p1.toString().length() > 0, "toString non-empty");
        // On JDK-8 the wrapper is a java.lang.reflect.Proxy over the SAM.
        check(Proxy.isProxyClass(p1.getClass()), "wrapper class is a Proxy (JDK-8)");
        boolean implementsSam = false;
        for (Class<?> ifc : p1.getClass().getInterfaces()) {
            if (ifc == Sam.class) implementsSam = true;
        }
        check(implementsSam, "proxy class lists the SAM interface");
    }

    // Default methods keep their own body; only the SAM routes to the handle.
    static void testDefaultMethods() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle greeter = lookup.findStatic(MethodHandleProxiesTest.class, "greet",
                MethodType.methodType(String.class, String.class));
        Def d = MethodHandleProxies.asInterfaceInstance(Def.class, greeter);
        check("hi d".equals(d.apply("d")), "SAM routes to the handle");
        check("DEFAULT".equals(d.d()), "default method runs its own body");
    }

    // Negative matrix with exact exception types and message fragments.
    static void testNegatives() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle greeter = lookup.findStatic(MethodHandleProxiesTest.class, "greet",
                MethodType.methodType(String.class, String.class));
        MethodHandle one = lookup.findStatic(MethodHandleProxiesTest.class, "one",
                MethodType.methodType(int.class));
        checkThrowsMsg(IllegalArgumentException.class,
              () -> MethodHandleProxies.asInterfaceInstance(String.class, greeter),
              "not a public interface", "non-interface IAE message");
        checkThrows(NullPointerException.class,
              () -> MethodHandleProxies.asInterfaceInstance(null, greeter),
              "null interface NPE");
        checkThrows(NullPointerException.class,
              () -> MethodHandleProxies.asInterfaceInstance(Sam.class, null),
              "null handle NPE");
        checkThrowsMsg(IllegalArgumentException.class,
              () -> MethodHandleProxies.asInterfaceInstance(CharSequence.class, greeter),
              "not a single-method interface", "multi-abstract-method IAE message");
        checkThrowsMsg(IllegalArgumentException.class,
              () -> MethodHandleProxies.asInterfaceInstance(TwoAbs.class, one),
              "not a single-method interface", "self-declared two-abstract IAE message");
        checkThrowsMsg(WrongMethodTypeException.class,
              () -> MethodHandleProxies.asInterfaceInstance(Sam.class, one),
              "cannot convert MethodHandle()int to (String)String",
              "incompatible handle WMTE message");
    }

    public static void main(String[] args) throws Throwable {
        testMethodHandleProxies();
        testRunnableSideEffect();
        testValueReturningSam();
        testBoxingFlow();
        testWrapperIntrospection();
        testProxyObjectSemantics();
        testDefaultMethods();
        testNegatives();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

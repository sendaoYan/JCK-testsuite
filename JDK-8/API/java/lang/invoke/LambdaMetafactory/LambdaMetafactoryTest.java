/*
 * @test
 * @summary java.lang.invoke.LambdaMetafactory conformance: flags, exact signatures, metafactory/altMetafactory linkage round-trips, flags and precise LambdaConversionException negatives.
 * @run main LambdaMetafactoryTest
 */

import java.io.Serializable;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class LambdaMetafactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLambdaMetafactory() throws Exception {
        check(LambdaMetafactory.FLAG_SERIALIZABLE != 0, "FLAG_SERIALIZABLE");
        check(LambdaMetafactory.FLAG_MARKERS != 0, "FLAG_MARKERS");
        check(LambdaMetafactory.FLAG_BRIDGES != 0, "FLAG_BRIDGES");
        check(LambdaMetafactory.FLAG_SERIALIZABLE != LambdaMetafactory.FLAG_MARKERS,
              "distinct flags");
        boolean hasMetafactory = false;
        boolean hasAltMetafactory = false;
        for (Method m : LambdaMetafactory.class.getMethods()) {
            if (m.getName().equals("metafactory")) hasMetafactory = true;
            if (m.getName().equals("altMetafactory")) hasAltMetafactory = true;
        }
        check(hasMetafactory, "has metafactory");
        check(hasAltMetafactory, "has altMetafactory");
    }

    // ---- helpers and self-authored sample shapes ----

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

    public interface Fn { Object len(String s); }
    public interface IntFn { Integer len(String s); }
    public interface Fn2 { String apply(int n); }
    public interface Op extends Serializable { String apply(String s, int n); }
    public interface Marker {}
    public interface Sink { void put(String s); }
    public interface TwoAbstract { Object len(String s); Object other(String s); }

    static String decorate(String s, int n) { return s + "_" + n; }
    static String strBody(String s) { return s + "-ok"; }
    static Object lenOf(Object o) { return Integer.valueOf(((String) o).length()); }
    static String greet() { return "hi"; }
    static int sinkCount;
    static void sinkBody(String s) { sinkCount += s.length(); }

    // ---- structure: exact signature and flag values ----
    static void testStructureDeep() throws Exception {
        Class<?> c = LambdaMetafactory.class;
        // JDK-8: public but not final (JDK-9+ made it final)
        check(Modifier.isPublic(c.getModifiers()), "class is public");
        check(!Modifier.isFinal(c.getModifiers()), "class not final on JDK-8");
        int pubCtors = 0;
        Constructor<?> pubCtor = null;
        for (Constructor<?> ct : c.getConstructors()) { pubCtors++; pubCtor = ct; }
        check(pubCtors == 1 && pubCtor.getParameterCount() == 0,
                "single public no-arg constructor on JDK-8");
        check(LambdaMetafactory.FLAG_SERIALIZABLE == 1, "FLAG_SERIALIZABLE == 1");
        check(LambdaMetafactory.FLAG_MARKERS == 2, "FLAG_MARKERS == 2");
        check(LambdaMetafactory.FLAG_BRIDGES == 4, "FLAG_BRIDGES == 4");
        Method mf = c.getMethod("metafactory", MethodHandles.Lookup.class, String.class,
                MethodType.class, MethodType.class, MethodHandle.class, MethodType.class);
        check(mf.getReturnType() == CallSite.class, "metafactory returns CallSite");
        check(Modifier.isStatic(mf.getModifiers()) && Modifier.isPublic(mf.getModifiers()),
                "metafactory is public static");
        Class<?>[] exs = mf.getExceptionTypes();
        boolean declaresLce = false;
        for (Class<?> e : exs) if (e == LambdaConversionException.class) declaresLce = true;
        check(declaresLce, "metafactory declares LambdaConversionException");
        Method alt = c.getMethod("altMetafactory", MethodHandles.Lookup.class, String.class,
                MethodType.class, Object[].class);
        check(alt.getReturnType() == CallSite.class, "altMetafactory returns CallSite");
        check(alt.getParameterTypes().length == 4
                && alt.getParameterTypes()[3] == Object[].class, "altMetafactory varargs tail");
        boolean altLce = false;
        for (Class<?> e : alt.getExceptionTypes()) if (e == LambdaConversionException.class) altLce = true;
        check(altLce, "altMetafactory declares LambdaConversionException");
    }

    // ---- metafactory core linkage round-trip ----
    static void testMetafactoryRoundTrip() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle impl = lookup.findStatic(LambdaMetafactoryTest.class, "strBody",
                MethodType.methodType(String.class, String.class));
        CallSite cs = LambdaMetafactory.metafactory(lookup, "len",
                MethodType.methodType(Fn.class),
                MethodType.methodType(Object.class, String.class),
                impl, MethodType.methodType(String.class, String.class));
        check(cs != null, "metafactory returns a CallSite");
        check(cs.type().equals(MethodType.methodType(Fn.class)), "CallSite type is ()Fn");
        check(cs.getTarget().type().equals(cs.type()), "getTarget type equals CallSite type");
        check("MethodHandle()Fn".equals(cs.getTarget().toString()), "target toString");
        check("java.lang.invoke.ConstantCallSite".equals(cs.getClass().getName()),
                "metafactory yields a ConstantCallSite");
        Fn f = (Fn) cs.dynamicInvoker().invokeExact();
        check(f.len("x").equals("x-ok"), "lambda computes x-ok");
        check(f.len("").equals("-ok"), "lambda computes -ok on empty");
        Fn f2 = (Fn) cs.dynamicInvoker().invoke();
        check(f2.len("y").equals("y-ok"), "second invocation computes identically");
        CallSite cs2 = LambdaMetafactory.metafactory(lookup, "len",
                MethodType.methodType(Fn.class),
                MethodType.methodType(Object.class, String.class),
                impl, MethodType.methodType(String.class, String.class));
        check(cs2 != cs, "two metafactory calls give independent CallSites");
        check(cs2.type().equals(cs.type()), "independent CallSites share the type");
        check(((Fn) cs2.dynamicInvoker().invokeExact()).len("z").equals("z-ok"),
                "second CallSite computes identically");
    }

    // ---- linkage against the standard functional interfaces ----
    static void testStandardInterfaces() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle counter = lookup.findStatic(LambdaMetafactoryTest.class, "lenOf",
                MethodType.methodType(Object.class, Object.class));
        @SuppressWarnings("unchecked")
        java.util.function.Function<String, Integer> fn =
                (java.util.function.Function<String, Integer>) LambdaMetafactory
                        .metafactory(lookup, "apply",
                                MethodType.methodType(java.util.function.Function.class),
                                MethodType.methodType(Object.class, Object.class),
                                counter, MethodType.methodType(Object.class, Object.class))
                        .dynamicInvoker().invokeExact();
        check(fn.apply("abcd").equals(Integer.valueOf(4)), "Function lambda computes 4");
        check(fn.apply("").equals(Integer.valueOf(0)), "Function lambda computes 0");

        MethodHandle greeter = lookup.findStatic(LambdaMetafactoryTest.class, "greet",
                MethodType.methodType(String.class));
        @SuppressWarnings("unchecked")
        java.util.function.Supplier<String> sup =
                (java.util.function.Supplier<String>) LambdaMetafactory
                        .metafactory(lookup, "get",
                                MethodType.methodType(java.util.function.Supplier.class),
                                MethodType.methodType(Object.class),
                                greeter, MethodType.methodType(String.class))
                        .dynamicInvoker().invokeExact();
        check(sup.get().equals("hi"), "Supplier lambda computes hi");

        // void SAM with a side-effecting implementation
        MethodHandle sink = lookup.findStatic(LambdaMetafactoryTest.class, "sinkBody",
                MethodType.methodType(void.class, String.class));
        Sink sk = (Sink) LambdaMetafactory.metafactory(lookup, "put",
                MethodType.methodType(Sink.class),
                MethodType.methodType(void.class, String.class),
                sink, MethodType.methodType(void.class, String.class))
                .dynamicInvoker().invokeExact();
        sinkCount = 0;
        sk.put("abc");
        check(sinkCount == 3, "void lambda ran the side effect");
        sk.put("de");
        check(sinkCount == 5, "side effect accumulates");
    }

    // ---- invokedType capture prefix (bound state) ----
    static void testCapturedArgs() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle impl = lookup.findStatic(LambdaMetafactoryTest.class, "decorate",
                MethodType.methodType(String.class, String.class, int.class));
        CallSite cs = LambdaMetafactory.metafactory(lookup, "apply",
                MethodType.methodType(Fn2.class, String.class),
                MethodType.methodType(String.class, int.class),
                impl, MethodType.methodType(String.class, int.class));
        check(cs.type().equals(MethodType.methodType(Fn2.class, String.class)),
                "capture: CallSite type is (String)Fn2");
        Fn2 f = (Fn2) cs.dynamicInvoker().invoke("pre");
        check(f.apply(9).equals("pre_9"), "captured prefix flows into the body");
        Fn2 g = (Fn2) cs.dynamicInvoker().invoke("post");
        check(g.apply(2).equals("post_2"), "each invocation captures independently");
    }

    // ---- altMetafactory flag battery ----
    static void testAltMetafactoryFlags() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle impl = lookup.findStatic(LambdaMetafactoryTest.class, "strBody",
                MethodType.methodType(String.class, String.class));
        CallSite ser = LambdaMetafactory.altMetafactory(lookup, "len",
                MethodType.methodType(Fn.class),
                MethodType.methodType(Object.class, String.class),
                impl, MethodType.methodType(String.class, String.class),
                LambdaMetafactory.FLAG_SERIALIZABLE);
        Fn sf = (Fn) ser.dynamicInvoker().invokeExact();
        check(sf instanceof Serializable, "FLAG_SERIALIZABLE adds Serializable");
        check(sf.len("s").equals("s-ok"), "serializable lambda computes");
        check(!(new Object() instanceof Fn), "sanity: Object is not Fn");

        CallSite marked = LambdaMetafactory.altMetafactory(lookup, "len",
                MethodType.methodType(Fn.class),
                MethodType.methodType(Object.class, String.class),
                impl, MethodType.methodType(String.class, String.class),
                LambdaMetafactory.FLAG_MARKERS, 1, Marker.class);
        Fn mf = (Fn) marked.dynamicInvoker().invokeExact();
        check(mf instanceof Marker, "FLAG_MARKERS adds the marker interface");
        check(!(mf instanceof Serializable), "FLAG_MARKERS alone does not serialize");

        CallSite both = LambdaMetafactory.altMetafactory(lookup, "len",
                MethodType.methodType(Fn.class),
                MethodType.methodType(Object.class, String.class),
                impl, MethodType.methodType(String.class, String.class),
                LambdaMetafactory.FLAG_SERIALIZABLE | LambdaMetafactory.FLAG_MARKERS,
                1, Marker.class);
        Fn bf = (Fn) both.dynamicInvoker().invokeExact();
        check(bf instanceof Serializable && bf instanceof Marker,
                "combined SERIALIZABLE|MARKERS flags");

        // FLAG_BRIDGES: an erased view method is added to the lambda class
        CallSite bridged = LambdaMetafactory.altMetafactory(lookup, "len",
                MethodType.methodType(Fn.class),
                MethodType.methodType(Object.class, String.class),
                impl, MethodType.methodType(String.class, String.class),
                LambdaMetafactory.FLAG_BRIDGES, 1,
                MethodType.methodType(Object.class, Object.class));
        Fn brf = (Fn) bridged.dynamicInvoker().invokeExact();
        check(brf.len("q").equals("q-ok"), "bridged lambda computes directly");
        Method bridge = brf.getClass().getMethod("len", Object.class);
        bridge.setAccessible(true);
        check(bridge.invoke(brf, "br").equals("br-ok"),
                "bridge method (Object)Object delegates and computes");

        // marker argument must be an interface
        checkThrowsMsg(LambdaConversionException.class, new ThrowingRunnable() {
            public void run() throws Throwable {
                LambdaMetafactory.altMetafactory(lookup, "len",
                        MethodType.methodType(Fn.class),
                        MethodType.methodType(Object.class, String.class),
                        impl, MethodType.methodType(String.class, String.class),
                        LambdaMetafactory.FLAG_MARKERS, 1, String.class);
            }
        }, "is not an interface", "non-interface marker rejected");
    }

    // ---- precise LambdaConversionException negatives ----
    static void testNegativeConversions() throws Throwable {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final MethodHandle strH = lookup.findStatic(LambdaMetafactoryTest.class, "strBody",
                MethodType.methodType(String.class, String.class));
        // impl returns String but SAM returns Integer
        checkThrowsMsg(LambdaConversionException.class, new ThrowingRunnable() {
            public void run() throws Throwable {
                LambdaMetafactory.metafactory(lookup, "len",
                        MethodType.methodType(IntFn.class),
                        MethodType.methodType(Integer.class, String.class),
                        strH, MethodType.methodType(String.class, String.class));
            }
        }, "Type mismatch for lambda expected return", "return mismatch");
        // SAM arity 2 vs impl arity 1
        checkThrowsMsg(LambdaConversionException.class, new ThrowingRunnable() {
            public void run() throws Throwable {
                LambdaMetafactory.metafactory(lookup, "len",
                        MethodType.methodType(Fn.class),
                        MethodType.methodType(Object.class, String.class, String.class),
                        strH, MethodType.methodType(String.class, String.class, String.class));
            }
        }, "Incorrect number of parameters", "arity mismatch");
        // parameter type not convertible: SAM wants Integer, impl takes String
        checkThrowsMsg(LambdaConversionException.class, new ThrowingRunnable() {
            public void run() throws Throwable {
                LambdaMetafactory.metafactory(lookup, "len",
                        MethodType.methodType(Fn.class),
                        MethodType.methodType(Object.class, Integer.class),
                        strH, MethodType.methodType(String.class, Integer.class));
            }
        }, "Type mismatch for lambda argument 0", "argument type mismatch");
        // void impl for a non-void SAM
        final MethodHandle voidH = lookup.findStatic(LambdaMetafactoryTest.class, "sinkBody",
                MethodType.methodType(void.class, String.class));
        checkThrowsMsg(LambdaConversionException.class, new ThrowingRunnable() {
            public void run() throws Throwable {
                LambdaMetafactory.metafactory(lookup, "put",
                        MethodType.methodType(Fn.class),
                        MethodType.methodType(Object.class, String.class),
                        voidH, MethodType.methodType(void.class, String.class));
            }
        }, "Type mismatch for lambda expected return", "void impl vs value SAM");
        // publicLookup has no access to the linkage (private-module semantics)
        checkThrowsMsg(LambdaConversionException.class, new ThrowingRunnable() {
            public void run() throws Throwable {
                LambdaMetafactory.metafactory(MethodHandles.publicLookup(), "len",
                        MethodType.methodType(Fn.class),
                        MethodType.methodType(Object.class, String.class),
                        strH, MethodType.methodType(String.class, String.class));
            }
        }, "Invalid caller", "publicLookup cannot link");
        // factory type must be an interface
        checkThrowsMsg(LambdaConversionException.class, new ThrowingRunnable() {
            public void run() throws Throwable {
                LambdaMetafactory.metafactory(lookup, "len",
                        MethodType.methodType(String.class),
                        MethodType.methodType(Object.class, String.class),
                        strH, MethodType.methodType(String.class, String.class));
            }
        }, "is not an interface", "non-interface factory type");
        // adapted (non-direct) handles cannot be linked
        final MethodHandle adapted = MethodHandles.insertArguments(
                lookup.findStatic(LambdaMetafactoryTest.class, "decorate",
                        MethodType.methodType(String.class, String.class, int.class)), 0, "k");
        checkThrowsMsg(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() throws Throwable {
                LambdaMetafactory.metafactory(lookup, "apply",
                        MethodType.methodType(Fn2.class),
                        MethodType.methodType(String.class, int.class),
                        adapted, MethodType.methodType(String.class, int.class));
            }
        }, "not a direct method handle", "non-direct impl handle rejected (JDK-8 wording)");
        // observed behavior: linkage does not validate the SAM method name at link time
        CallSite byName = LambdaMetafactory.metafactory(lookup, "noSuchName",
                MethodType.methodType(Fn.class),
                MethodType.methodType(Object.class, String.class),
                strH, MethodType.methodType(String.class, String.class));
        check(byName != null, "unknown SAM name still links (observed)");
        // observed behavior: more than one abstract method also links on JDK-8
        CallSite twoAbs = LambdaMetafactory.metafactory(lookup, "len",
                MethodType.methodType(TwoAbstract.class),
                MethodType.methodType(Object.class, String.class),
                strH, MethodType.methodType(String.class, String.class));
        check(twoAbs != null, "two-abstract-method interface still links (observed)");
    }

    public static void main(String[] args) throws Throwable {
        testLambdaMetafactory();
        testStructureDeep();
        testMetafactoryRoundTrip();
        testStandardInterfaces();
        testCapturedArgs();
        testAltMetafactoryFlags();
        testNegativeConversions();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

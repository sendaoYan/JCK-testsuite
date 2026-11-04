/*
 * @test
 * @summary java.lang.invoke.LambdaMetafactory conformance: white-box + black-box behavior, metafactory/altMetafactory linkage round-trips, flag battery and precise LambdaConversionException negatives.
 * @run main LambdaMetafactoryTest
 */

import java.io.Serializable;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class LambdaMetafactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public interface F {
        int len(String s);
    }

    static int body(String s) { return s.length(); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.LambdaMetafactory");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public class");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 0, "no public constructor");
        check(c.getField("FLAG_SERIALIZABLE").getInt(null) == 1, "FLAG_SERIALIZABLE == 1");
        check(c.getField("FLAG_MARKERS").getInt(null) == 2, "FLAG_MARKERS == 2");
        check(c.getField("FLAG_BRIDGES").getInt(null) == 4, "FLAG_BRIDGES == 4");
        check(c.getDeclaredMethod("metafactory", MethodHandles.Lookup.class, String.class,
                MethodType.class, MethodType.class, MethodHandle.class,
                MethodType.class).getReturnType() == CallSite.class,
              "metafactory returns CallSite");
        check(c.getDeclaredMethod("altMetafactory", MethodHandles.Lookup.class, String.class,
                MethodType.class, Object[].class) != null, "altMetafactory declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle impl = lookup.findStatic(LambdaMetafactoryTest.class, "body",
                MethodType.methodType(int.class, String.class));

        // metafactory: standard non-serializable lambda
        CallSite cs = LambdaMetafactory.metafactory(lookup, "len",
                MethodType.methodType(F.class),                       // invoked type: ()F
                MethodType.methodType(int.class, String.class),        // SAM signature
                impl,                                                  // implementation
                MethodType.methodType(int.class, String.class));       // instantiated SAM
        F f = (F) cs.dynamicInvoker().invokeExact();
        check(f.len("method") == 6, "metafactory lambda computes");
        check(f.len("") == 0, "lambda on empty string");
        // each invocation of the dynamic invoker yields a fresh instance
        F f2 = (F) cs.dynamicInvoker().invokeExact();
        check(f2.len("method") == 6, "second instance computes identically");

        // altMetafactory with FLAG_SERIALIZABLE
        CallSite alt = LambdaMetafactory.altMetafactory(lookup, "len",
                MethodType.methodType(F.class),
                MethodType.methodType(int.class, String.class),
                impl, MethodType.methodType(int.class, String.class),
                LambdaMetafactory.FLAG_SERIALIZABLE);
        F sf = (F) alt.dynamicInvoker().invokeExact();
        check(sf.len("abc") == 3, "altMetafactory lambda computes");
        check(sf instanceof java.io.Serializable, "FLAG_SERIALIZABLE adds Serializable");

        // altMetafactory with FLAG_MARKERS adds extra marker interfaces
        CallSite marked = LambdaMetafactory.altMetafactory(lookup, "len",
                MethodType.methodType(F.class),
                MethodType.methodType(int.class, String.class),
                impl, MethodType.methodType(int.class, String.class),
                LambdaMetafactory.FLAG_MARKERS, 1, java.io.Serializable.class);
        F mf = (F) marked.dynamicInvoker().invokeExact();
        check(mf instanceof java.io.Serializable, "FLAG_MARKERS adds the marker");

        // conversion failures are LambdaConversionException
        try {
            LambdaMetafactory.metafactory(lookup, "len",
                    MethodType.methodType(String.class),
                    MethodType.methodType(int.class, String.class),
                    impl, MethodType.methodType(int.class, String.class));
            check(false, "factory type must return an interface");
        } catch (LambdaConversionException e) {
            check(true, "non-interface factory type -> LambdaConversionException");
        }
        try {
            LambdaMetafactory.metafactory(lookup, "len",
                    MethodType.methodType(F.class),
                    MethodType.methodType(int.class, String.class),
                    impl, MethodType.methodType(String.class, String.class));
            check(false, "incompatible instantiated type must fail");
        } catch (LambdaConversionException e) {
            check(true, "incompatible instantiated type -> LambdaConversionException");
        }
        // impl method that cannot be adapted to the SAM signature
        MethodHandle bad = lookup.findStatic(LambdaMetafactoryTest.class, "intBody",
                MethodType.methodType(int.class, int.class));
        try {
            LambdaMetafactory.metafactory(lookup, "len",
                    MethodType.methodType(F.class),
                    MethodType.methodType(int.class, String.class),
                    bad, MethodType.methodType(int.class, String.class));
            check(false, "mismatched impl signature must fail");
        } catch (LambdaConversionException e) {
            check(true, "mismatched impl -> LambdaConversionException");
        }
    }

    static int intBody(int x) { return x; }

    // ---- helpers and further self-authored shapes ----

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

    public interface RefFn { Object len(String s); }
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

    // ---- structure: exact signatures and declared failures ----
    static void testStructureDeep() throws Exception {
        Class<?> c = LambdaMetafactory.class;
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "class is final (JDK-9+)");
        java.lang.reflect.Method mf = c.getMethod("metafactory", MethodHandles.Lookup.class,
                String.class, MethodType.class, MethodType.class, MethodHandle.class,
                MethodType.class);
        check(mf.getReturnType() == CallSite.class, "metafactory returns CallSite");
        boolean lce = false;
        for (Class<?> e : mf.getExceptionTypes()) if (e == LambdaConversionException.class) lce = true;
        check(lce, "metafactory declares LambdaConversionException");
        java.lang.reflect.Method alt = c.getMethod("altMetafactory", MethodHandles.Lookup.class,
                String.class, MethodType.class, Object[].class);
        check(alt.getReturnType() == CallSite.class, "altMetafactory returns CallSite");
        check(alt.getParameterTypes().length == 4
                && alt.getParameterTypes()[3] == Object[].class, "altMetafactory varargs tail");
        boolean altLce = false;
        for (Class<?> e : alt.getExceptionTypes()) if (e == LambdaConversionException.class) altLce = true;
        check(altLce, "altMetafactory declares LambdaConversionException");
    }

    // ---- linkage round-trip with call-site identity and capture prefix ----
    static void testMetafactoryRoundTrip() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle impl = lookup.findStatic(LambdaMetafactoryTest.class, "strBody",
                MethodType.methodType(String.class, String.class));
        CallSite cs = LambdaMetafactory.metafactory(lookup, "len",
                MethodType.methodType(RefFn.class),
                MethodType.methodType(Object.class, String.class),
                impl, MethodType.methodType(String.class, String.class));
        check(cs.type().equals(MethodType.methodType(RefFn.class)), "CallSite type is ()RefFn");
        check(cs.getTarget().type().equals(cs.type()), "getTarget type equals CallSite type");
        check("MethodHandle()RefFn".equals(cs.getTarget().toString()), "target toString");
        check("java.lang.invoke.ConstantCallSite".equals(cs.getClass().getName()),
                "metafactory yields a ConstantCallSite");
        RefFn f = (RefFn) cs.dynamicInvoker().invokeExact();
        check(f.len("x").equals("x-ok"), "lambda computes x-ok");
        check(f.len("").equals("-ok"), "lambda computes -ok on empty");
        RefFn f2 = (RefFn) cs.dynamicInvoker().invoke();
        check(f2.len("y").equals("y-ok"), "second invocation computes identically");
        CallSite cs2 = LambdaMetafactory.metafactory(lookup, "len",
                MethodType.methodType(RefFn.class),
                MethodType.methodType(Object.class, String.class),
                impl, MethodType.methodType(String.class, String.class));
        check(cs2 != cs, "two metafactory calls give independent CallSites");
        check(cs2.type().equals(cs.type()), "independent CallSites share the type");
        check(((RefFn) cs2.dynamicInvoker().invokeExact()).len("z").equals("z-ok"),
                "second CallSite computes identically");

        // invokedType capture prefix
        MethodHandle dec = lookup.findStatic(LambdaMetafactoryTest.class, "decorate",
                MethodType.methodType(String.class, String.class, int.class));
        CallSite cap = LambdaMetafactory.metafactory(lookup, "apply",
                MethodType.methodType(Fn2.class, String.class),
                MethodType.methodType(String.class, int.class),
                dec, MethodType.methodType(String.class, int.class));
        check(cap.type().equals(MethodType.methodType(Fn2.class, String.class)),
                "capture: CallSite type is (String)Fn2");
        Fn2 c1 = (Fn2) cap.dynamicInvoker().invoke("pre");
        check(c1.apply(9).equals("pre_9"), "captured prefix flows into the body");
        Fn2 c2 = (Fn2) cap.dynamicInvoker().invoke("post");
        check(c2.apply(2).equals("post_2"), "each invocation captures independently");
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

    // ---- altMetafactory flag battery including bridges ----
    static void testAltMetafactoryFlags() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle impl = lookup.findStatic(LambdaMetafactoryTest.class, "strBody",
                MethodType.methodType(String.class, String.class));
        CallSite ser = LambdaMetafactory.altMetafactory(lookup, "len",
                MethodType.methodType(RefFn.class),
                MethodType.methodType(Object.class, String.class),
                impl, MethodType.methodType(String.class, String.class),
                LambdaMetafactory.FLAG_SERIALIZABLE);
        RefFn sf = (RefFn) ser.dynamicInvoker().invokeExact();
        check(sf instanceof Serializable, "FLAG_SERIALIZABLE adds Serializable");
        check(sf.len("s").equals("s-ok"), "serializable lambda computes");

        CallSite marked = LambdaMetafactory.altMetafactory(lookup, "len",
                MethodType.methodType(RefFn.class),
                MethodType.methodType(Object.class, String.class),
                impl, MethodType.methodType(String.class, String.class),
                LambdaMetafactory.FLAG_MARKERS, 1, Marker.class);
        RefFn mf = (RefFn) marked.dynamicInvoker().invokeExact();
        check(mf instanceof Marker, "FLAG_MARKERS adds the marker interface");
        check(!(mf instanceof Serializable), "FLAG_MARKERS alone does not serialize");

        CallSite both = LambdaMetafactory.altMetafactory(lookup, "len",
                MethodType.methodType(RefFn.class),
                MethodType.methodType(Object.class, String.class),
                impl, MethodType.methodType(String.class, String.class),
                LambdaMetafactory.FLAG_SERIALIZABLE | LambdaMetafactory.FLAG_MARKERS,
                1, Marker.class);
        RefFn bf = (RefFn) both.dynamicInvoker().invokeExact();
        check(bf instanceof Serializable && bf instanceof Marker,
                "combined SERIALIZABLE|MARKERS flags");

        CallSite bridged = LambdaMetafactory.altMetafactory(lookup, "len",
                MethodType.methodType(RefFn.class),
                MethodType.methodType(Object.class, String.class),
                impl, MethodType.methodType(String.class, String.class),
                LambdaMetafactory.FLAG_BRIDGES, 1,
                MethodType.methodType(Object.class, Object.class));
        RefFn brf = (RefFn) bridged.dynamicInvoker().invokeExact();
        check(brf.len("q").equals("q-ok"), "bridged lambda computes directly");
        java.lang.reflect.Method bridge = brf.getClass().getMethod("len", Object.class);
        bridge.setAccessible(true);
        check(bridge.invoke(brf, "br").equals("br-ok"),
                "bridge method (Object)Object delegates and computes");

        checkThrowsMsg(LambdaConversionException.class, () -> LambdaMetafactory.altMetafactory(
                lookup, "len",
                MethodType.methodType(RefFn.class),
                MethodType.methodType(Object.class, String.class),
                impl, MethodType.methodType(String.class, String.class),
                LambdaMetafactory.FLAG_MARKERS, 1, String.class),
                "is not an interface", "non-interface marker rejected");
    }

    // ---- precise LambdaConversionException negatives ----
    static void testNegativeConversions() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle strH = lookup.findStatic(LambdaMetafactoryTest.class, "strBody",
                MethodType.methodType(String.class, String.class));
        checkThrowsMsg(LambdaConversionException.class, () -> LambdaMetafactory.metafactory(
                lookup, "len",
                MethodType.methodType(IntFn.class),
                MethodType.methodType(Integer.class, String.class),
                strH, MethodType.methodType(String.class, String.class)),
                "Type mismatch for lambda expected return", "return mismatch");
        checkThrowsMsg(LambdaConversionException.class, () -> LambdaMetafactory.metafactory(
                lookup, "len",
                MethodType.methodType(RefFn.class),
                MethodType.methodType(Object.class, String.class, String.class),
                strH, MethodType.methodType(String.class, String.class, String.class)),
                "Incorrect number of parameters", "arity mismatch");
        checkThrowsMsg(LambdaConversionException.class, () -> LambdaMetafactory.metafactory(
                lookup, "len",
                MethodType.methodType(RefFn.class),
                MethodType.methodType(Object.class, Integer.class),
                strH, MethodType.methodType(String.class, Integer.class)),
                "Type mismatch for lambda argument 0", "argument type mismatch");
        MethodHandle voidH = lookup.findStatic(LambdaMetafactoryTest.class, "sinkBody",
                MethodType.methodType(void.class, String.class));
        checkThrowsMsg(LambdaConversionException.class, () -> LambdaMetafactory.metafactory(
                lookup, "put",
                MethodType.methodType(RefFn.class),
                MethodType.methodType(Object.class, String.class),
                voidH, MethodType.methodType(void.class, String.class)),
                "Type mismatch for lambda expected return", "void impl vs value SAM");
        checkThrowsMsg(LambdaConversionException.class, () -> LambdaMetafactory.metafactory(
                MethodHandles.publicLookup(), "len",
                MethodType.methodType(RefFn.class),
                MethodType.methodType(Object.class, String.class),
                strH, MethodType.methodType(String.class, String.class)),
                "Invalid caller", "publicLookup cannot link");
        checkThrowsMsg(LambdaConversionException.class, () -> LambdaMetafactory.metafactory(
                lookup, "len",
                MethodType.methodType(String.class),
                MethodType.methodType(Object.class, String.class),
                strH, MethodType.methodType(String.class, String.class)),
                "is not an interface", "non-interface factory type");
        // adapted (non-direct) handles cannot be linked; JDK-9+ wraps as LCE
        MethodHandle adapted = MethodHandles.insertArguments(
                lookup.findStatic(LambdaMetafactoryTest.class, "decorate",
                        MethodType.methodType(String.class, String.class, int.class)), 0, "k");
        checkThrowsMsg(LambdaConversionException.class, () -> LambdaMetafactory.metafactory(
                lookup, "apply",
                MethodType.methodType(Fn2.class),
                MethodType.methodType(String.class, int.class),
                adapted, MethodType.methodType(String.class, int.class)),
                "not direct", "non-direct impl handle rejected (JDK-9+ wording)");
        // observed behavior: linkage does not validate the SAM method name at link time
        check(LambdaMetafactory.metafactory(lookup, "noSuchName",
                MethodType.methodType(RefFn.class),
                MethodType.methodType(Object.class, String.class),
                strH, MethodType.methodType(String.class, String.class)) != null,
                "unknown SAM name still links (observed)");
        check(LambdaMetafactory.metafactory(lookup, "len",
                MethodType.methodType(TwoAbstract.class),
                MethodType.methodType(Object.class, String.class),
                strH, MethodType.methodType(String.class, String.class)) != null,
                "two-abstract-method interface still links (observed)");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testStructureDeep();
        testMetafactoryRoundTrip();
        testStandardInterfaces();
        testAltMetafactoryFlags();
        testNegativeConversions();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

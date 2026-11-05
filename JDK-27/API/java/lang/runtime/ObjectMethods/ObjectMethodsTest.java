/*
 * @test
 * @summary java.lang.runtime.ObjectMethods conformance: white-box structure + black-box behavior.
 * @run main ObjectMethodsTest
 */

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.lang.runtime.ObjectMethods;

public class ObjectMethodsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- fixture record (compiler generates equals/hashCode/toString via ObjectMethods) ----
    public record Point(int x, int y) {
    }

    public record Named(String name) {
    }

    // plain class mirroring a record shape, accessors resolved via findGetter
    static class Pair {
        final int a;
        final int b;

        Pair(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public int a() { return a; }

        public int b() { return b; }
    }

    static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    static final MethodType EQ_DESC = MethodType.methodType(boolean.class, Pair.class, Object.class);
    static final MethodType HC_DESC = MethodType.methodType(int.class, Pair.class);
    static final MethodType TS_DESC = MethodType.methodType(String.class, Pair.class);

    static MethodHandle[] pairAccessors() {
        try {
            return new MethodHandle[]{
                    LOOKUP.findGetter(Pair.class, "a", int.class),
                    LOOKUP.findGetter(Pair.class, "b", int.class)};
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.runtime.ObjectMethods");
        check(Modifier.isFinal(c.getModifiers()), "ObjectMethods is final");
        check(Modifier.isPublic(c.getDeclaredConstructor().getModifiers()) == false,
              "no public constructor");
        int pubStatic = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("bootstrap") && Modifier.isPublic(m.getModifiers())
                && Modifier.isStatic(m.getModifiers())) {
                pubStatic++;
            }
        }
        check(pubStatic == 1, "single public static bootstrap method");
        check(c.getDeclaredMethod("bootstrap", MethodHandles.Lookup.class, String.class,
                                  java.lang.invoke.TypeDescriptor.class, Class.class, String.class,
                                  MethodHandle[].class)
                  .getReturnType() == Object.class, "bootstrap returns Object");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        MethodHandle[] acc = pairAccessors();

        // bootstrap("equals", ...) returns a ConstantCallSite whose target is the equals body
        Object eqBoot = ObjectMethods.bootstrap(LOOKUP, "equals", EQ_DESC, Pair.class, "a;b", acc);
        check(eqBoot instanceof CallSite, "equals bootstrap returns CallSite");
        MethodHandle eqMh = ((CallSite) eqBoot).dynamicInvoker();
        Pair p = new Pair(5, 5);
        check((boolean) eqMh.invokeExact(p, (Object) p), "equals self");
        check((boolean) eqMh.invokeExact(p, (Object) new Pair(5, 5)), "equals same values");
        check(!(boolean) eqMh.invokeExact(p, (Object) new Pair(5, 4)), "equals differs b");
        check(!(boolean) eqMh.invokeExact(p, (Object) new Pair(4, 5)), "equals differs a");
        check(!(boolean) eqMh.invokeExact(p, (Object) null), "equals null false");
        check(!(boolean) eqMh.invokeExact(p, (Object) "str"), "equals other type false");

        // bootstrap("hashCode", ...) combines component hashes as x*31 + y per component
        Object hcBoot = ObjectMethods.bootstrap(LOOKUP, "hashCode", HC_DESC, Pair.class, "a;b", acc);
        MethodHandle hcMh = ((CallSite) hcBoot).dynamicInvoker();
        check((int) hcMh.invokeExact(new Pair(5, 5)) == 5 * 31 + 5,
              "hashCode matches x*31+y combiner");
        check((int) hcMh.invokeExact(new Pair(100, -1)) == 100 * 31 + (-1),
              "hashCode other values");
        check((int) hcMh.invokeExact(new Pair(0, 0)) == 0, "hashCode zeros");

        // bootstrap("toString", ...) produces "Name[a=x, b=y]"
        Object tsBoot = ObjectMethods.bootstrap(LOOKUP, "toString", TS_DESC, Pair.class, "a;b", acc);
        MethodHandle tsMh = ((CallSite) tsBoot).dynamicInvoker();
        String s = (String) tsMh.invokeExact(new Pair(1, 2));
        check(s.equals("Pair[a=1, b=2]"), "toString format: " + s);

        // the same machinery powers real records: assert record-generated behavior equals
        // the explicitly-bootstrapped behavior on identical shapes
        Point pt = new Point(5, 5);
        check(pt.equals(new Point(5, 5)), "record equals true");
        check(!pt.equals(new Point(5, 4)), "record equals false");
        check(pt.hashCode() == 5 * 31 + 5, "record hashCode uses same combiner");
        check(pt.toString().equals("Point[x=5, y=5]"), "record toString: " + pt);

        // passing MethodHandle.class as the type descriptor returns a raw MethodHandle
        Object rawHandle = ObjectMethods.bootstrap(LOOKUP, "hashCode", MethodHandle.class,
                                                   Pair.class, "a;b", acc);
        check(rawHandle instanceof MethodHandle, "MethodHandle descriptor yields raw handle");
        check((int) ((MethodHandle) rawHandle).invokeExact(new Pair(7, 8))
                  == 7 * 31 + 8, "raw handle invocable");

        // unknown methodName throws IllegalArgumentException
        try {
            ObjectMethods.bootstrap(LOOKUP, "nope", HC_DESC, Pair.class, "a;b", acc);
            check(false, "bad methodName should throw");
        } catch (IllegalArgumentException iae1) {
            check(true, "bad methodName IAE");
        }
        // mismatched methodType for equals throws IllegalArgumentException
        try {
            ObjectMethods.bootstrap(LOOKUP, "equals", HC_DESC, Pair.class, "a;b", acc);
            check(false, "bad equals type should throw");
        } catch (IllegalArgumentException iae2) {
            check(true, "bad equals type IAE");
        }
        // name list not matching accessor count for toString throws
        try {
            ObjectMethods.bootstrap(LOOKUP, "toString", TS_DESC, Pair.class, "a", acc);
            check(false, "mismatched names should throw");
        } catch (IllegalArgumentException iae3) {
            check(true, "mismatched names IAE");
        }
        // null arguments rejected
        try {
            ObjectMethods.bootstrap(null, "hashCode", HC_DESC, Pair.class, "a;b", acc);
            check(false, "null lookup should throw");
        } catch (NullPointerException npe1) {
            check(true, "null lookup NPE");
        }
        // single-component record round trip
        Named n = new Named("z");
        check(n.toString().equals("Named[name=z]"), "single-component record toString");
        check(n.equals(new Named("z")) && n.hashCode() == "z".hashCode(),
              "single-component equals/hashCode");
    }


    // record fixture with a nullable reference component (JDK-27 only file)
    public record Labeled(int id, String tag) {
    }

    public static class Bare {
    }

    static void testDeepNullComponentRecord() {
        Labeled a = new Labeled(1, null);
        Labeled b = new Labeled(1, null);
        Labeled c = new Labeled(1, "x");
        check(a.equals(b), "null component: equals holds for same values");
        check(!a.equals(c), "null vs non-null component differ");
        check(a.hashCode() == b.hashCode(), "null component: hashCode consistent");
        check(a.toString().equals("Labeled[id=1, tag=null]"),
              "null component renders as 'tag=null', got " + a);
        check(!a.equals(null), "record equals(null) false");
        check(a.equals(a), "record equals self");
    }

    static void testDeepZeroComponentBootstrap() throws Throwable {
        // a zero-component bootstrap degenerates to the Object defaults:
        // hashCode 0, toString "Bare[]", equals = instanceof check
        Object hcBoot = ObjectMethods.bootstrap(LOOKUP, "hashCode",
                MethodType.methodType(int.class, Bare.class), Bare.class, "", new MethodHandle[]{});
        MethodHandle hc = ((CallSite) hcBoot).dynamicInvoker();
        check((int) hc.invokeExact(new Bare()) == 0, "zero-component hashCode is 0");
        Object tsBoot = ObjectMethods.bootstrap(LOOKUP, "toString",
                MethodType.methodType(String.class, Bare.class), Bare.class, "", new MethodHandle[]{});
        MethodHandle ts = ((CallSite) tsBoot).dynamicInvoker();
        check("Bare[]".equals((String) ts.invokeExact(new Bare())),
              "zero-component toString is 'Bare[]'");
        Object eqBoot = ObjectMethods.bootstrap(LOOKUP, "equals",
                MethodType.methodType(boolean.class, Bare.class, Object.class),
                Bare.class, "", new MethodHandle[]{});
        MethodHandle eq = ((CallSite) eqBoot).dynamicInvoker();
        check((boolean) eq.invokeExact(new Bare(), (Object) new Bare()),
              "zero-component equals: same type true");
        check(!(boolean) eq.invokeExact(new Bare(), (Object) null),
              "zero-component equals: null false");
        check(!(boolean) eq.invokeExact(new Bare(), (Object) "s"),
              "zero-component equals: other type false");
    }

    static void testDeepNullReceiver() throws Throwable {
        MethodHandle[] acc = pairAccessors();
        MethodHandle eq = ((CallSite) ObjectMethods.bootstrap(LOOKUP, "equals", EQ_DESC,
                Pair.class, "a;b", acc)).dynamicInvoker();
        try {
            boolean r = (boolean) eq.invokeExact((Pair) null, (Object) new Pair(1, 1));
            check(false, "null receiver equals must throw NPE, got " + r);
        } catch (NullPointerException expected) {
            check(true, "null receiver equals throws NPE");
        }
        MethodHandle hc = ((CallSite) ObjectMethods.bootstrap(LOOKUP, "hashCode", HC_DESC,
                Pair.class, "a;b", acc)).dynamicInvoker();
        try {
            int h = (int) hc.invokeExact((Pair) null);
            check(false, "null receiver hashCode must throw NPE, got " + h);
        } catch (NullPointerException expected) {
            check(true, "null receiver hashCode throws NPE");
        }
        MethodHandle ts = ((CallSite) ObjectMethods.bootstrap(LOOKUP, "toString", TS_DESC,
                Pair.class, "a;b", acc)).dynamicInvoker();
        try {
            String str = (String) ts.invokeExact((Pair) null);
            check(false, "null receiver toString must throw NPE, got " + str);
        } catch (NullPointerException expected) {
            check(true, "null receiver toString throws NPE");
        }
        // each bootstrap call returns a fresh CallSite (no cross-invocation caching)
        Object c1 = ObjectMethods.bootstrap(LOOKUP, "hashCode", HC_DESC, Pair.class, "a;b", acc);
        Object c2 = ObjectMethods.bootstrap(LOOKUP, "hashCode", HC_DESC, Pair.class, "a;b", acc);
        check(c1 != c2, "repeated bootstrap returns a fresh CallSite");
        check((int) ((CallSite) c2).dynamicInvoker().invokeExact(new Pair(7, 8)) == 7 * 31 + 8,
              "bootstrapped handles from separate calls are equivalent");
        // mixed record component types combine int/String hashing correctly
        Labeled x = new Labeled(5, "s");
        check(x.hashCode() == 5 * 31 + "s".hashCode(), "mixed components hash combiner x*31+y");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testDeepNullComponentRecord();
        testDeepZeroComponentBootstrap();
        testDeepNullReceiver();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

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

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

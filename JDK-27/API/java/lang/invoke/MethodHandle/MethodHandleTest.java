/*
 * @test
 * @summary java.lang.invoke.MethodHandle conformance: white-box + black-box behavior.
 * @run main MethodHandleTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;

public class MethodHandleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static int sum(int a, int b) { return a + b; }
    static int varsum(int first, int... rest) {
        int t = first;
        for (int r : rest) t += r;
        return t;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.MethodHandle");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public class");
        check(Class.forName("java.lang.constant.Constable").isAssignableFrom(c), "Constable");
        String[] names = {"type", "invokeExact", "invoke", "invokeWithArguments", "asType",
                "asSpreader", "asCollector", "asVarargsCollector", "asFixedArity",
                "isVarargsCollector", "bindTo", "describeConstable", "withVarargs",
                "toString"};
        for (String n : names) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getDeclaredMethods())
                if (mm.getName().equals(n)) found = true;
            check(found, "declares " + n);
        }
        // signature-polymorphic invokers are native, final and varargs-shaped
        java.lang.reflect.Method ie = c.getDeclaredMethod("invokeExact", Object[].class);
        check(java.lang.reflect.Modifier.isNative(ie.getModifiers())
              && java.lang.reflect.Modifier.isFinal(ie.getModifiers())
              && ie.isVarArgs(), "invokeExact is final native varargs");
        check(ie.getExceptionTypes().length == 1
              && ie.getExceptionTypes()[0] == Throwable.class, "invokeExact throws Throwable");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle sum = lookup.findStatic(MethodHandleTest.class, "sum",
                MethodType.methodType(int.class, int.class, int.class));

        // type reflects the member signature
        check(sum.type().toMethodDescriptorString().equals("(II)I"), "type()");

        // invokeExact is statically typed: (int,int)int
        check((int) sum.invokeExact(20, 22) == 42, "invokeExact");

        // invoke performs asType conversions automatically
        check((Integer) sum.invoke(Integer.valueOf(20), Integer.valueOf(22)) == 42,
              "invoke adapts boxed arguments");

        // invokeWithArguments takes a flat list and boxes/unboxes
        check((Integer) sum.invokeWithArguments(20, 22) == 42, "invokeWithArguments varargs");
        check((Integer) sum.invokeWithArguments(java.util.List.of(20, 22)) == 42,
              "invokeWithArguments list");
        // arity mismatch is a precise failure
        try {
            sum.invokeWithArguments(1);
            check(false, "arity mismatch must throw");
        } catch (WrongMethodTypeException e) {
            check(true, "arity mismatch -> WrongMethodTypeException");
        }

        // asType widens int -> long
        MethodHandle widened = sum.asType(MethodType.methodType(long.class, int.class, int.class));
        check((long) widened.invokeExact(20, 22) == 42L, "asType widens return");

        // bindTo attaches a leading receiver
        MethodHandle len = lookup.findVirtual(String.class, "length",
                MethodType.methodType(int.class));
        MethodHandle bound = len.bindTo("hello");
        check(bound.type().parameterCount() == 0, "bindTo consumes the receiver");
        check((int) bound.invokeExact() == 5, "bindTo invocation");

        // asCollector gathers trailing arguments into an array
        MethodHandle varsum = lookup.findStatic(MethodHandleTest.class, "varsum",
                MethodType.methodType(int.class, int.class, int[].class));
        check(varsum.isVarargsCollector(), "varargs method reports varargs collector");
        check((int) varsum.invokeExact(1, new int[]{2, 3}) == 6, "fixed-arity varargs call");
        check((int) varsum.invoke(1, 2, 3) == 6, "variable-arity via invoke");
        MethodHandle collected = varsum.asCollector(1, int[].class, 2);
        check(collected.type().toMethodDescriptorString().equals("(III)I"),
              "asCollector type: " + collected.type());
        check((int) collected.invokeExact(1, 2, 3) == 6, "asCollector invocation");
        MethodHandle fixed = varsum.asFixedArity();
        check(!fixed.isVarargsCollector(), "asFixedArity clears varargs flag");
        check((int) fixed.invokeExact(1, new int[]{4, 4}) == 9, "asFixedArity call");

        // asSpreader spreads a leading array into separate arguments
        MethodHandle spread = sum.asSpreader(int[].class, 2);
        check((int) spread.invokeExact(new int[]{20, 22}) == 42, "asSpreader");
        check(spread.type().parameterCount() == 1, "asSpreader type");

        // withVarargs toggles the bit without changing behavior
        check(!varsum.withVarargs(false).isVarargsCollector(), "withVarargs(false)");
        check(varsum.withVarargs(true).isVarargsCollector(), "withVarargs(true)");

        // describeConstable yields a resolvable nominal description
        java.util.Optional<java.lang.constant.MethodHandleDesc> nd = sum.describeConstable();
        check(nd.isPresent(), "describeConstable present");
        check(nd.get().resolveConstantDesc(lookup).invokeWithArguments(40, 2).equals(42),
              "nominal form resolves and computes");

        // toString describes the handle's type
        check(sum.toString().equals("MethodHandle(int,int)int"), "toString: " + sum);

        // asType to an impossible conversion throws
        try {
            sum.asType(MethodType.methodType(int.class, String.class, int.class));
            check(false, "impossible asType rejected");
        } catch (WrongMethodTypeException e) {
            check(true, "impossible asType -> WrongMethodTypeException");
        }
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

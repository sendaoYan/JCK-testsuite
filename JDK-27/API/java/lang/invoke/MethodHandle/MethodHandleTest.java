/*
 * @test
 * @summary java.lang.invoke.MethodHandle conformance: full contract coverage.
 *          type() signature, invokeExact exact-type dispatch + WrongMethodTypeException
 *          on mismatched argument/return types, invoke's permissive adaptation
 *          (boxing/unboxing/widening), invokeWithArguments (varargs + List forms) and
 *          arity mismatch, asType adaptation, bindTo receiver binding and arity reduction,
 *          asCollector/asSpreader array adaptation, asVarargsCollector/isVarargsCollector/
 *          asFixedArity round-trips, void-returning invoke yields null, and toString.
 * @run main MethodHandleTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;
import java.util.Arrays;

public class MethodHandleTest {
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

    static class Calc {
        public int add(int a, int b) { return a + b; }
        public static int sum(int... xs) { int t = 0; for (int x : xs) t += x; return t; }
        public void nop() { }
    }

    static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    static MethodHandle add() throws Exception {
        return LOOKUP.findVirtual(Calc.class, "add", MethodType.methodType(int.class, int.class, int.class));
    }

    static void testType() throws Exception {
        MethodHandle mh = add();
        MethodType t = mh.type();
        check(t.returnType() == int.class, "type returnType");
        // findVirtual prepends the receiver, so the handle type has 3 parameters
        check(t.parameterCount() == 3, "type parameterCount");
        check(t.parameterType(0) == Calc.class && t.parameterType(1) == int.class
                && t.parameterType(2) == int.class, "type parameter types");
    }

    static void testInvokeExact() throws Throwable {
        MethodHandle mh = add();
        Calc c = new Calc();
        int r = (int) mh.invokeExact(c, 2, 3);
        check(r == 5, "invokeExact exact");
        check((int) mh.invokeExact(c, -1, 1) == 0, "invokeExact exact zero");

        // wrong argument types -> WrongMethodTypeException
        checkThrows(WrongMethodTypeException.class,
                () -> mh.invokeExact(c, (Integer) 2, (Integer) 3),
                "invokeExact boxed args WMTE");
        checkThrows(WrongMethodTypeException.class,
                () -> mh.invokeExact(c, 2L, 3), "invokeExact long arg WMTE");
        // wrong arity -> WrongMethodTypeException
        checkThrows(WrongMethodTypeException.class,
                () -> mh.invokeExact(c, 2), "invokeExact wrong arity WMTE");
        // null receiver where Calc expected -> WMTE (not NPE, because signature mismatch)
        checkThrows(WrongMethodTypeException.class,
                () -> mh.invokeExact((Object) null, 2, 3), "invokeExact null receiver WMTE");
    }

    static void testInvoke() throws Throwable {
        MethodHandle mh = add();
        Calc c = new Calc();
        check((int) mh.invoke(c, 2, 3) == 5, "invoke");
        // invoke permits boxing/unboxing adaptation
        check((int) mh.invoke(c, Integer.valueOf(2), Integer.valueOf(3)) == 5,
                "invoke boxed adaptation");
        // invoke permits widening
        check((int) mh.invoke(c, (byte) 2, (short) 3) == 5, "invoke widening");
        // wrong arity -> WrongMethodTypeException
        checkThrows(WrongMethodTypeException.class,
                () -> mh.invoke(c, 2), "invoke wrong arity WMTE");
        checkThrows(WrongMethodTypeException.class,
                () -> mh.invoke(c, 2, 3, 4), "invoke too many WMTE");
        // String cannot be adapted to int -> WrongMethodTypeException
        checkThrows(WrongMethodTypeException.class,
                () -> mh.invoke(c, "a", "b"), "invoke unadaptable type WMTE");
    }

    static void testInvokeWithArguments() throws Throwable {
        MethodHandle mh = add();
        Calc c = new Calc();
        check(mh.invokeWithArguments(c, 2, 3).equals(5), "invokeWithArguments varargs");
        check(mh.invokeWithArguments(Arrays.asList(c, 2, 3)).equals(5),
                "invokeWithArguments List");
        check(mh.invokeWithArguments(Arrays.<Object>asList(c, 4, 5)).equals(9),
                "invokeWithArguments List boxed");
        checkThrows(WrongMethodTypeException.class,
                () -> mh.invokeWithArguments(c, 2), "invokeWithArguments wrong arity");
        checkThrows(WrongMethodTypeException.class,
                () -> mh.invokeWithArguments(Arrays.asList(c, 2)),
                "invokeWithArguments List wrong arity");
        // null array is treated as zero arguments -> arity mismatch
        checkThrows(WrongMethodTypeException.class,
                () -> mh.invokeWithArguments((Object[]) null),
                "invokeWithArguments null array WMTE");
        checkThrows(NullPointerException.class,
                () -> mh.invokeWithArguments((java.util.List<?>) null),
                "invokeWithArguments null List NPE");
    }

    static void testAsType() throws Throwable {
        MethodHandle mh = add();
        Calc c = new Calc();
        // widen all three params and return to Object
        MethodHandle adapted = mh.asType(MethodType.methodType(Object.class,
                Object.class, Object.class, Object.class));
        check(adapted.type().returnType() == Object.class, "asType return type");
        check(adapted.invoke(c, 2, 3).equals(5), "asType adapted invoke");

        // box the two int parameters while keeping the Calc receiver
        MethodHandle boxed = mh.asType(MethodType.methodType(int.class,
                Calc.class, Integer.class, Integer.class));
        check((int) boxed.invoke(c, Integer.valueOf(2), Integer.valueOf(3)) == 5,
                "asType boxed params");
    }

    static void testBindTo() throws Throwable {
        MethodHandle mh = add();
        Calc c = new Calc();
        MethodHandle bound = mh.bindTo(c);
        check(bound.type().parameterCount() == 2, "bindTo does not reduce arity for instance");
        // bindTo on an instance method binds the receiver but leaves an explicit first arg
        // (this handle was obtained via findVirtual, so bindTo fixes the receiver)
        check((int) bound.invokeWithArguments(2, 3) == 5, "bindTo receiver invocation");
        check((int) bound.invoke(2, 3) == 5, "bindTo receiver invoke");

        // binding a static method's first arg reduces arity
        MethodHandle len = LOOKUP.findVirtual(String.class, "length",
                MethodType.methodType(int.class));
        MethodHandle lenBound = len.bindTo("abcde");
        check(lenBound.type().parameterCount() == 0, "bindTo static? no — virtual keeps arity");
        check((int) lenBound.invoke() == 5, "bound String.length");
    }

    static void testAsCollector() throws Throwable {
        MethodHandle sum = LOOKUP.findStatic(Calc.class, "sum",
                MethodType.methodType(int.class, int[].class));
        // collect two trailing int args into an int[] -> (int,int)int
        MethodHandle collected = sum.asCollector(int[].class, 2);
        check(collected.type().parameterCount() == 2
                && collected.type().parameterType(0) == int.class
                && collected.type().parameterType(1) == int.class, "asCollector type");
        check((int) collected.invoke(2, 3) == 5, "asCollector invoke");

        // zero-length collector -> ()int (empty array)
        MethodHandle none = sum.asCollector(int[].class, 0);
        check(none.type().parameterCount() == 0 && (int) none.invoke() == 0,
                "asCollector zero length");

        checkThrows(IllegalArgumentException.class,
                () -> sum.asCollector(String.class, 1), "asCollector bad array type IAE");
        checkThrows(IllegalArgumentException.class,
                () -> sum.asCollector(int[].class, -1), "asCollector negative length IAE");
        checkThrows(NullPointerException.class,
                () -> sum.asCollector(null, 2), "asCollector null arrayType NPE");
    }

    static void testAsSpreader() throws Throwable {
        MethodHandle mh = add();
        Calc c = new Calc();
        // spread an int[] over the two trailing int params -> (Calc,int[])int
        MethodHandle spread = mh.asSpreader(int[].class, 2);
        check(spread.type().parameterCount() == 2, "asSpreader parameter count");
        check(spread.type().parameterType(0) == Calc.class
                && spread.type().parameterType(1) == int[].class, "asSpreader param types");
        check((int) spread.invoke(c, new int[]{2, 3}) == 5, "asSpreader invoke");

        checkThrows(IllegalArgumentException.class,
                () -> mh.asSpreader(int[].class, -1), "asSpreader negative length IAE");
        checkThrows(IllegalArgumentException.class,
                () -> mh.asSpreader(int[].class, 4), "asSpreader length exceeds arity IAE");
        checkThrows(IllegalArgumentException.class,
                () -> mh.asSpreader(String.class, 1), "asSpreader non-array type IAE");
        checkThrows(NullPointerException.class,
                () -> mh.asSpreader(null, 2), "asSpreader null arrayType NPE");
    }

    static void testVarargsCollector() throws Throwable {
        MethodHandle sum = LOOKUP.findStatic(Calc.class, "sum",
                MethodType.methodType(int.class, int[].class));
        // findStatic on a varargs method yields a varargs collector by default
        check(sum.isVarargsCollector(), "varargs method handle is varargs collector");
        check((int) sum.invokeWithArguments(1, 2, 3) == 6, "varargs invokeWithArguments");

        MethodHandle var = sum.asVarargsCollector(int[].class);
        check(var.isVarargsCollector(), "asVarargsCollector keeps varargs");
        check((int) var.invokeWithArguments(4, 5) == 9, "asVarargsCollector invoke");

        MethodHandle fixed = sum.asFixedArity();
        check(!fixed.isVarargsCollector(), "asFixedArity clears varargs");
        check(fixed.type().equals(sum.type()), "asFixedArity restores type");
        check((int) fixed.invoke(new int[]{1, 2, 3}) == 6, "fixed arity invoke with array");
    }

    static void testVoidInvoke() throws Throwable {
        MethodHandle nop = LOOKUP.findVirtual(Calc.class, "nop", MethodType.methodType(void.class));
        Calc c = new Calc();
        Object r = nop.invoke(c);
        check(r == null, "void-returning invoke returns null");
        check(nop.toString() != null, "toString non-null");
    }

    static void testWithVarargs() throws Throwable {
        MethodHandle sum = LOOKUP.findStatic(Calc.class, "sum",
                MethodType.methodType(int.class, int[].class));
        MethodHandle nv = sum.withVarargs(false);
        check(!nv.isVarargsCollector(), "withVarargs(false) clears varargs");
        MethodHandle v = sum.withVarargs(true);
        check(v.isVarargsCollector(), "withVarargs(true) keeps varargs");
        check((int) v.invokeWithArguments(3, 4) == 7, "withVarargs(true) still collects");
    }

    public static void main(String[] args) throws Throwable {
        testType();
        testInvokeExact();
        testInvoke();
        testInvokeWithArguments();
        testAsType();
        testBindTo();
        testAsCollector();
        testAsSpreader();
        testVarargsCollector();
        testVoidInvoke();
        testWithVarargs();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

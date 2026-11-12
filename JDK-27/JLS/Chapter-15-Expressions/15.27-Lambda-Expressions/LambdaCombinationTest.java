/*
 * @test
 * @summary JLS 15.27 / 15.13 / 9.8 / 5.1.4: lambda expressions composed with
 *          the surrounding language rules the basic lambda tests leave to the
 *          official combination matrix — overload resolution (void- vs
 *          value-compatible bodies, arity), effective-final capture (local,
 *          this, per-iteration copies), nesting (outer parameter captured by
 *          an inner lambda), generic target types / type-variable inference,
 *          method & constructor & array references, and functional interfaces
 *          with default methods plus intersection-type cast targets.
 * @run main LambdaCombinationTest
 */

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class LambdaCombinationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- functional interfaces used by the combination matrix ----
    interface Box<T> { T make(); }
    interface MapFn<A, B> { B map(A a); }
    interface Greeter {
        String greet(String name);
        default String greetTwice(String name) { return greet(name) + " " + greet(name); }
    }
    interface HasR { int r(); }
    interface HasDefault { default int m() { return 42; } }

    // ---- overloads exercising lambda-body / arity disambiguation ----
    static String pick(Runnable r) { r.run(); return "runnable"; }
    static String pick(Supplier<String> s) { return "supplier:" + s.get(); }
    static String ar(IntUnaryOperator f) { return "unary:" + f.applyAsInt(1); }
    static String ar(IntBinaryOperator f) { return "binary:" + f.applyAsInt(1, 2); }
    static String callableOf(Callable<String> c) throws Exception { return "call:" + c.call(); }
    static <T> T through(Function<T, T> f, T x) { return f.apply(x); }

    // 'this' is only meaningful in a non-static context.
    private final int field = 9;
    Supplier<LambdaCombinationTest> selfCapture() { return () -> this; }
    Supplier<Integer> fieldCapture() { return () -> field; }

    public static void main(String[] args) throws Exception {
        LambdaCombinationTest inst = new LambdaCombinationTest();
        testOverloadResolution();
        testCapture(inst);
        testNesting();
        testTypeVariables();
        testMethodReferences();
        testDefaultAndIntersection();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testOverloadResolution() throws Exception {
        // A block body with no value is void-compatible only -> Runnable.
        check(pick(() -> {}).equals("runnable"),
                "void-compatible block lambda selects the Runnable overload");
        // An expression body that is not a statement expression is
        // value-compatible only -> Supplier<String>.
        check(pick(() -> "x").equals("supplier:x"),
                "value-compatible expression lambda selects the Supplier overload");

        // Arity alone disambiguates the two Int* functional interfaces.
        check(ar(x -> x + 1).equals("unary:2"),
                "one-parameter lambda selects IntUnaryOperator");
        check(ar((x, y) -> x + y).equals("binary:3"),
                "two-parameter lambda selects IntBinaryOperator");

        // A lambda compatible with a checked-exception functional interface.
        check(callableOf(() -> "z").equals("call:z"),
                "lambda instantiates a checked-exception (Callable) interface");
    }

    static void testCapture(LambdaCombinationTest inst) {
        int local = 5;
        Supplier<Integer> s = () -> local;
        check(s.get() == 5, "captures an effectively final local variable");

        check(inst.selfCapture().get() == inst, "lambda captures this");
        check(inst.fieldCapture().get() == 9, "lambda captures an instance field");

        // Each loop iteration gets its own final copy, all captured correctly.
        int sum = 0;
        for (int i = 0; i < 4; i++) {
            final int v = i;
            sum += ((IntSupplier) () -> v).getAsInt();
        }
        check(sum == 6, "per-iteration final copies are captured distinctly");
    }

    static void testNesting() {
        // Outer lambda returns an inner lambda that captures the outer parameter.
        Function<Integer, IntUnaryOperator> addN = x -> y -> x + y;
        check(addN.apply(2).applyAsInt(3) == 5,
                "nested lambda captures the outer lambda's parameter");

        // Outer lambda body invokes an inner lambda that captures a local.
        int base = 10;
        IntUnaryOperator plusBase = x -> x + ((IntSupplier) () -> base).getAsInt();
        check(plusBase.applyAsInt(5) == 15,
                "outer lambda composes an inner lambda capturing a local");
    }

    static void testTypeVariables() {
        // Generic functional interface target with inferred type arguments.
        MapFn<String, Integer> len = s -> s.length();
        check(len.map("four").intValue() == 4,
                "generic functional interface infers its type arguments");

        // Generic method: T is inferred jointly from the lambda and the argument.
        check(through(s -> s + "!", "hi").equals("hi!"),
                "generic method infers T from lambda and argument");

        // Explicitly-typed generic functional interface target.
        Box<Integer> bi = () -> 5;
        check(bi.make() == 5, "lambda targets a generic interface with explicit type args");
    }

    static void testMethodReferences() {
        Supplier<Object> ctor = Object::new;
        check(ctor.get() != null, "constructor reference instantiates a class");

        IntFunction<int[]> arr = int[]::new;
        check(arr.apply(3).length == 3, "array constructor reference allocates the length");

        String hello = "hello";
        Supplier<String> up = hello::toUpperCase;
        check(up.get().equals("HELLO"), "bound instance method reference on a receiver");

        UnaryOperator<String> u = String::trim;
        check(u.apply("  x  ").equals("x"), "unbound static method reference");
    }

    static void testDefaultAndIntersection() {
        // Functional interface that also declares a default method.
        Greeter g = n -> "Hi " + n;
        check(g.greet("Bob").equals("Hi Bob"),
                "lambda implements the single abstract method of a default-bearing interface");
        check(g.greetTwice("Bob").equals("Hi Bob Hi Bob"),
                "default method is callable on the lambda instance");

        // Intersection-type cast target: exactly one abstract method across the
        // two types (HasR.r; HasDefault.m is a default), so the pair is functional.
        HasR r = (HasR & HasDefault) () -> 7;
        check(r.r() == 7,
                "intersection cast lambda implements the one abstract method");
        check(((HasDefault) r).m() == 42,
                "default method from the intersection partner is reachable");
    }
}

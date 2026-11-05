/*
 * @test
 * @summary java.lang.FunctionalInterface API conformance: RUNTIME retention, TYPE target,
 *          usable as a lambda target, and reflected presence on an annotated interface.
 * @run main FunctionalInterfaceTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class FunctionalInterfaceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    @FunctionalInterface
    interface FI { int apply(int x); }

    static void testRetentionAndTarget() {
        check(FunctionalInterface.class.getAnnotation(Retention.class).value()
              == RetentionPolicy.RUNTIME, "RUNTIME retention");
        boolean hasType = false;
        for (ElementType e : FunctionalInterface.class.getAnnotation(Target.class).value()) {
            if (e == ElementType.TYPE) hasType = true;
        }
        check(hasType, "@Target includes TYPE");
    }

    static void testUsableAsLambda() {
        FI f = x -> x * 2;
        check(f.apply(3) == 6, "@FunctionalInterface type used as a lambda target");
    }

    static void testPresent() {
        check(FI.class.isAnnotationPresent(FunctionalInterface.class),
              "annotated interface carries @FunctionalInterface");
    }

    // ==== Contract deepening (lcN1, 2026-11) ====
    @FunctionalInterface
    interface WithDefault { int f(); default int g() { return 1; } }

    @FunctionalInterface
    interface WithEquals { int f(); boolean equals(Object o); }

    @FunctionalInterface
    interface StringFn extends java.util.function.Function<String, Integer> {}

    static int square(int x) { return x * x; }

    static String target() { return "mref"; }

    @FunctionalInterface
    interface Supplier2 { String get2(); }

    static void testTargetExactSingleValue() {
        ElementType[] ts = FunctionalInterface.class.getAnnotation(Target.class).value();
        check(ts.length == 1, "@Target has exactly one element");
        check(ts[0] == ElementType.TYPE, "@Target is exactly {TYPE}");
        check(!java.util.Arrays.asList(ts).contains(ElementType.METHOD),
              "METHOD not allowed (only interfaces can be marked)");
    }

    static void testDocumentedAndMeta() {
        check(FunctionalInterface.class.isAnnotationPresent(java.lang.annotation.Documented.class),
              "@Documented present");
        check(FunctionalInterface.class.getAnnotations().length == 3,
              "three runtime-visible meta-annotations (Documented, Retention, Target)");
        check(FunctionalInterface.class.getAnnotation(java.lang.annotation.Inherited.class) == null,
              "@Inherited absent");
    }

    static void testMemberlessShape() {
        check(FunctionalInterface.class.getDeclaredMethods().length == 0,
              "declares no members");
        check(FunctionalInterface.class.getDeclaredFields().length == 0, "no fields");
        check(FunctionalInterface.class.getDeclaredClasses().length == 0, "no nested classes");
        check("java.lang.FunctionalInterface".equals(FunctionalInterface.class.getName()),
              "binary name");
        check(FunctionalInterface.class.isAnnotation(), "is an annotation type");
        check(FunctionalInterface.class.isInterface(), "annotation types are interfaces");
        check(java.lang.annotation.Annotation.class.isAssignableFrom(FunctionalInterface.class),
              "assignable to Annotation");
        check(FunctionalInterface.class.getAnnotation(FunctionalInterface.class) == null,
              "not self-annotated");
    }

    static void testAnnotationInstanceContract() {
        FunctionalInterface fi = FI.class.getAnnotation(FunctionalInterface.class);
        check(fi != null, "live proxy instance available (RUNTIME retention)");
        check(fi.annotationType() == FunctionalInterface.class,
              "annotationType() == FunctionalInterface.class");
        check(fi.equals(fi), "equals self");
        check(fi.equals(FI.class.getAnnotation(FunctionalInterface.class)),
              "two lookups yield equal proxies");
        check(fi.hashCode() == 0, "memberless proxy hashCode == 0");
        check("@java.lang.FunctionalInterface()".equals(fi.toString()),
              "memberless toString exact: " + fi.toString());
        check(FI.class.getDeclaredAnnotations().length == 1, "single declared annotation");
    }

    static void testFunctionalShapesAllLegal() {
        // lambda
        FI f = x -> x * 2;
        check(f.apply(3) == 6, "lambda implementation");
        // method reference (static)
        java.util.function.IntUnaryOperator op = FunctionalInterfaceTest::square;
        check(op.applyAsInt(5) == 25, "static method reference target");
        // method reference to instance method
        Supplier2 s = FunctionalInterfaceTest::target;
        check("mref".equals(s.get2()), "bound method reference target");
        // anonymous class
        FI anon = new FI() { public int apply(int x) { return x + 1; } };
        check(anon.apply(1) == 2, "anonymous class implementation");
        // default-method-bearing functional interface
        WithDefault wd = () -> 10;
        check(wd.f() == 10 && wd.g() == 1, "default method does not spoil functionality");
        // Object-method signatures do not count toward the single abstract method
        WithEquals we = () -> 7;
        check(we.f() == 7, "equals(Object) declaration does not spoil SAM");
        check(!we.equals("no"), "Object.equals identity semantics retained");
        // generic functional interface by inheritance
        StringFn sf = String::length;
        check(sf.apply("abcd") == 4, "generic functional interface via inheritance");
        check(sf instanceof java.util.function.Function, "instanceof inherited interface");
    }

    static void testRealJdkInterfaces() {
        check(Runnable.class.isAnnotationPresent(FunctionalInterface.class),
              "Runnable is @FunctionalInterface");
        check(java.util.concurrent.Callable.class
                  .isAnnotationPresent(FunctionalInterface.class),
              "Callable is @FunctionalInterface");
        check(java.util.function.Function.class
                  .isAnnotationPresent(FunctionalInterface.class),
              "java.util.function.Function is @FunctionalInterface");
        check(java.util.function.Predicate.class
                  .isAnnotationPresent(FunctionalInterface.class),
              "Predicate is @FunctionalInterface");
        check(!Comparable.class.isAnnotationPresent(FunctionalInterface.class),
              "Comparable is NOT annotated (multi-type-purpose interface)");
        check(!java.io.Serializable.class.isAnnotationPresent(FunctionalInterface.class),
              "Serializable is NOT annotated (no abstract methods)");
        Runnable r = () -> { };
        check(r != null, "Runnable lambda still works");
        java.util.function.Supplier<String> sup = () -> "s";
        check("s".equals(sup.get()), "Supplier lambda works");
    }

    public static void main(String[] args) {
        testRetentionAndTarget();
        testUsableAsLambda();
        testPresent();
        testTargetExactSingleValue();
        testDocumentedAndMeta();
        testMemberlessShape();
        testAnnotationInstanceContract();
        testFunctionalShapesAllLegal();
        testRealJdkInterfaces();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

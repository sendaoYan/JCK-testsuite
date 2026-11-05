/*
 * @test
 * @summary java.lang.SafeVarargs API conformance: RUNTIME retention, CONSTRUCTOR/METHOD
 *          targets, and usable on a static method to suppress a varargs warning.
 * @run main SafeVarargsTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class SafeVarargsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    @SafeVarargs
    static final <T> java.util.List<T> asList(T... a) { return java.util.Arrays.asList(a); }

    static void testRetentionAndTarget() {
        check(SafeVarargs.class.getAnnotation(Retention.class).value() == RetentionPolicy.RUNTIME,
              "RUNTIME retention");
        boolean hasMethod = false, hasCtor = false;
        for (ElementType e : SafeVarargs.class.getAnnotation(Target.class).value()) {
            if (e == ElementType.METHOD) hasMethod = true;
            if (e == ElementType.CONSTRUCTOR) hasCtor = true;
        }
        check(hasMethod && hasCtor, "@Target includes METHOD and CONSTRUCTOR");
    }

    static void testUsable() {
        java.util.List<Integer> xs = asList(1, 2, 3);
        check(xs.size() == 3, "@SafeVarargs method works normally");
    }

    // ==== Contract deepening (lcN1, 2026-11) ====
    @SafeVarargs
    private final <T> String join(T... parts) {
        StringBuilder sb = new StringBuilder();
        for (T p : parts) sb.append(String.valueOf(p));
        return sb.toString();
    }

    @SafeVarargs
    SafeVarargsTest(java.util.List<String>... lists) { this.lists = lists.length; }

    int lists;

    static void testTargetExactValueSet() {
        ElementType[] ts = SafeVarargs.class.getAnnotation(Target.class).value();
        check(ts.length == 2, "@Target has exactly two elements");
        check(ts[0] == ElementType.CONSTRUCTOR && ts[1] == ElementType.METHOD,
              "@Target is exactly {CONSTRUCTOR, METHOD} in that order");
        check(!java.util.Arrays.asList(ts).contains(ElementType.TYPE), "TYPE not allowed");
    }

    static void testDocumentedAndMeta() {
        check(SafeVarargs.class.isAnnotationPresent(java.lang.annotation.Documented.class),
              "@Documented present");
        check(SafeVarargs.class.getAnnotations().length == 3,
              "three runtime-visible meta-annotations (Documented, Retention, Target)");
        check(SafeVarargs.class.getAnnotation(java.lang.annotation.Inherited.class) == null,
              "@Inherited absent");
    }

    static void testPlacementRoundTrip() throws Exception {
        java.lang.reflect.Method m =
            SafeVarargsTest.class.getDeclaredMethod("asList", Object[].class);
        check(m.getAnnotation(SafeVarargs.class) != null,
              "annotation on static final varargs generic method");
        java.lang.reflect.Method priv =
            SafeVarargsTest.class.getDeclaredMethod("join", Object[].class);
        check(priv.getAnnotation(SafeVarargs.class) != null,
              "annotation on private final varargs method");
        java.lang.reflect.Constructor<?> c =
            SafeVarargsTest.class.getDeclaredConstructor(java.util.List[].class);
        check(c.getAnnotation(SafeVarargs.class) != null,
              "annotation on varargs constructor");
        check(m.isAnnotationPresent(SafeVarargs.class), "isAnnotationPresent true");
        check(m.getDeclaredAnnotations().length == 1, "single declared annotation");
    }

    static void testAnnotationInstanceContract() throws Exception {
        SafeVarargs sv =
            SafeVarargsTest.class.getDeclaredMethod("asList", Object[].class)
                .getAnnotation(SafeVarargs.class);
        check(sv.annotationType() == SafeVarargs.class, "annotationType() == SafeVarargs.class");
        check(sv.equals(sv), "equals self");
        check(sv.equals(SafeVarargsTest.class.getDeclaredMethod("asList", Object[].class)
                            .getAnnotation(SafeVarargs.class)),
              "two lookups yield equal proxies");
        check(sv.hashCode() == 0, "memberless proxy hashCode == 0");
        check("@java.lang.SafeVarargs()".equals(sv.toString()),
              "memberless toString exact: " + sv.toString());
        check(SafeVarargs.class.getDeclaredMethods().length == 0, "declares no members");
        check(SafeVarargs.class.getDeclaredFields().length == 0, "no fields");
        check(SafeVarargs.class.getDeclaredClasses().length == 0, "no nested classes");
    }

    static void testStructuralExtras() {
        check("java.lang.SafeVarargs".equals(SafeVarargs.class.getName()), "binary name");
        check(SafeVarargs.class.isAnnotation(), "is an annotation type");
        check(SafeVarargs.class.isInterface(), "annotation types are interfaces");
        check(java.lang.annotation.Annotation.class.isAssignableFrom(SafeVarargs.class),
              "assignable to Annotation");
        check(SafeVarargs.class.getAnnotation(SafeVarargs.class) == null,
              "SafeVarargs is not itself @SafeVarargs");
    }

    static void testVarargsBehavior() throws Exception {
        java.util.List<Integer> xs = asList(1, 2, 3);
        check(xs.size() == 3, "varargs arity 3");
        check(xs.get(0) == 1 && xs.get(1) == 2 && xs.get(2) == 3, "varargs element order");
        check(asList().isEmpty(), "empty varargs call");
        check(asList((Integer) null).size() == 1, "null element permitted");
        SafeVarargsTest t = new SafeVarargsTest(new java.util.ArrayList<String>(),
                                                new java.util.ArrayList<String>());
        check(t.lists == 2, "varargs constructor arity recorded");
        java.lang.reflect.Method join =
            SafeVarargsTest.class.getDeclaredMethod("join", Object[].class);
        join.setAccessible(true);
        // NOTE: reflective invocation of a varargs method requires the argument
        // array to be passed as the single (already-packed) parameter.
        check("abc".equals(join.invoke(new SafeVarargsTest(),
                                       (Object) new Object[]{"a", "b", "c"})),
              "private varargs method joins varargs");
        check("".equals(join.invoke(new SafeVarargsTest(), (Object) new Object[]{})),
              "private varargs empty join");
        check("null".equals(join.invoke(new SafeVarargsTest(),
                                        (Object) new Object[]{null})),
              "null renders as literal null");
    }

    public static void main(String[] args) throws Exception {
        testRetentionAndTarget();
        testUsable();
        testTargetExactValueSet();
        testDocumentedAndMeta();
        testPlacementRoundTrip();
        testAnnotationInstanceContract();
        testStructuralExtras();
        testVarargsBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

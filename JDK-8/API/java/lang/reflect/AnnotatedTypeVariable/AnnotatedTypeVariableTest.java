/*
 * @test
 * @summary java.lang.reflect.AnnotatedTypeVariable conformance: annotated bounds on
 *          a type variable parameter, field/return/constructor use sites, use-site
 *          annotation on the variable itself, class and method type parameter
 *          declarations, multiple bounds with per-bound annotations, implicit
 *          Object bound for unbounded variables, and TypeVariable-level contract
 *          (name, generic declaration).
 * @run main AnnotatedTypeVariableTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;

public class AnnotatedTypeVariableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- fixtures ----

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface A {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
    @interface Bound {
    }

    static class Holder<T> {
        public T value;

        public Holder(T value) {
            this.value = value;
        }

        public T get() {
            return value;
        }
    }

    static class AnnotatedUse<T> {
        public @A
        T tagged;
    }

    static class Bounded<T extends @Bound Number & Comparable<T>> {
    }

    static class Free<T> {
    }

    public static <T extends Number> void typeVar(T t) {
    }

    public static <T extends @Bound Number> void annotatedBound(T t) {
    }

    static void testAnnotatedTypeVariable() throws Exception {
        Method m = AnnotatedTypeVariableTest.class.getMethod("typeVar", Number.class);
        AnnotatedType at = m.getParameters()[0].getAnnotatedType();
        check(at instanceof AnnotatedTypeVariable, "is AnnotatedTypeVariable");
        AnnotatedTypeVariable atv = (AnnotatedTypeVariable) at;
        check(atv.getAnnotatedBounds().length == 1, "one annotated bound");
        check(atv.getAnnotatedBounds()[0].getType() == Number.class, "bound is Number");
        check(atv.getType() instanceof java.lang.reflect.TypeVariable, "getType is TypeVariable");
    }

    // field of type T resolves to an annotated type variable
    static void testFieldUse() throws Exception {
        AnnotatedType at = Holder.class.getField("value").getAnnotatedType();
        check(at instanceof AnnotatedTypeVariable, "field T is AnnotatedTypeVariable");
        AnnotatedTypeVariable atv = (AnnotatedTypeVariable) at;
        check(atv.getType() instanceof TypeVariable, "underlying is TypeVariable");
        check("T".equals(atv.getType().getTypeName()), "underlying name T");
        check(atv.getAnnotations().length == 0, "unannotated use");
        check(atv.getAnnotation(A.class) == null, "no annotation when plain");
        check(!atv.isAnnotationPresent(A.class), "not present when plain");
        check(atv.getAnnotatedBounds().length == 1, "implicit single bound");
        check(atv.getAnnotatedBounds()[0].getType() == Object.class, "implicit bound Object");
        check(atv.getAnnotatedBounds()[0].getAnnotations().length == 0,
              "implicit bound unannotated");
    }

    // annotation directly on the type variable use site
    static void testAnnotatedUseSite() throws Exception {
        AnnotatedType at = AnnotatedUse.class.getField("tagged").getAnnotatedType();
        check(at instanceof AnnotatedTypeVariable, "annotated use still AnnotatedTypeVariable");
        check(at.getAnnotation(A.class) != null, "use-site annotation visible");
        check(at.isAnnotationPresent(A.class), "use-site present");
        check(at.getAnnotations().length == 1, "use-site one annotation");
        check(at.getAnnotationsByType(A.class).length == 1, "use-site byType one");
        AnnotatedTypeVariable atv = (AnnotatedTypeVariable) at;
        check(atv.getType() instanceof TypeVariable, "underlying still TypeVariable");
        check(atv.getAnnotatedBounds()[0].getType() == Object.class, "bounds unaffected");
    }

    // method generic return and constructor parameter paths
    static void testMethodAndCtorPaths() throws Exception {
        AnnotatedType ret = Holder.class.getMethod("get").getAnnotatedReturnType();
        check(ret instanceof AnnotatedTypeVariable, "return T is AnnotatedTypeVariable");
        check("T".equals(ret.getType().getTypeName()), "return underlying T");
        check(ret.getAnnotations().length == 0, "return unannotated");

        Constructor<?> ctor = Holder.class.getDeclaredConstructor(Object.class);
        AnnotatedTypeVariable p = (AnnotatedTypeVariable) ctor.getParameters()[0].getAnnotatedType();
        check(p instanceof AnnotatedTypeVariable, "ctor parameter T is AnnotatedTypeVariable");
        check("T".equals(p.getType().getTypeName()), "ctor parameter underlying T");
        check(p.getAnnotatedBounds()[0].getType() == Object.class, "ctor parameter implicit bound");
    }

    // class-level declaration: bounds carry their own annotations
    static void testClassDeclarationBounds() {
        TypeVariable<?> tv = Bounded.class.getTypeParameters()[0];
        check("T".equals(tv.getName()), "class type parameter name");
        check(tv.getGenericDeclaration() == Bounded.class, "generic declaration is the class");
        AnnotatedType[] bounds = tv.getAnnotatedBounds();
        check(bounds.length == 2, "two declared bounds");
        check(bounds[0].getType() == Number.class, "first bound Number");
        check(bounds[0].getAnnotation(Bound.class) != null, "first bound annotated");
        check(bounds[0].isAnnotationPresent(Bound.class), "first bound present");
        check(bounds[1].getType() instanceof java.lang.reflect.ParameterizedType,
              "second bound parameterized");
        check(((java.lang.reflect.ParameterizedType) bounds[1].getType()).getRawType()
                == Comparable.class, "second bound raw Comparable");
        check(bounds[1].getAnnotation(Bound.class) == null, "second bound unannotated");
    }

    // use-site bounds mirror the declaration
    static void testMultiBoundUseSite() throws Exception {
        Method m = AnnotatedTypeVariableTest.class.getMethod("annotatedBound", Number.class);
        AnnotatedTypeVariable atv = (AnnotatedTypeVariable) m.getParameters()[0].getAnnotatedType();
        check(atv.getAnnotatedBounds().length == 1, "single explicit bound");
        AnnotatedType b = atv.getAnnotatedBounds()[0];
        check(b.getType() == Number.class, "use-site bound Number");
        check(b.getAnnotation(Bound.class) != null, "use-site bound annotated");
        check(b.getAnnotations().length == 1, "use-site bound one annotation");

        // the method's own type parameter table reports the same bounds
        TypeVariable<?>[] tvs = m.getTypeParameters();
        check(tvs.length == 1, "one method type parameter");
        check("T".equals(tvs[0].getName()), "method type parameter name");
        check(m instanceof GenericDeclaration, "Method is a GenericDeclaration");
        check(tvs[0].getGenericDeclaration() == m, "method TV declared by the method");
        check(tvs[0].getAnnotatedBounds()[0].getAnnotation(Bound.class) != null,
              "method TV bound annotated");
    }

    // unbounded variable: exactly one implicit Object bound, no annotations anywhere
    static void testUnbounded() {
        TypeVariable<?> tv = Free.class.getTypeParameters()[0];
        check(tv.getAnnotatedBounds().length == 1, "unbounded single implicit bound");
        check(tv.getAnnotatedBounds()[0].getType() == Object.class, "implicit bound is Object");
        check(tv.getAnnotatedBounds()[0].getAnnotations().length == 0, "implicit bound clean");
        check(tv.getAnnotations().length == 0, "declaration TV unannotated");
    }

    // the underlying TypeVariable contract itself
    static void testTypeVariableContract() {
        TypeVariable<?> tv = Holder.class.getTypeParameters()[0];
        check("T".equals(tv.getName()), "name T");
        check(tv.getGenericDeclaration() == Holder.class, "declared by Holder");
        check(tv.getBounds()[0] == Object.class, "raw bounds Object");
        check(tv instanceof java.lang.reflect.Type, "TypeVariable is a Type");
    }

    public static void main(String[] args) throws Exception {
        testAnnotatedTypeVariable();
        testFieldUse();
        testAnnotatedUseSite();
        testMethodAndCtorPaths();
        testClassDeclarationBounds();
        testMultiBoundUseSite();
        testUnbounded();
        testTypeVariableContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

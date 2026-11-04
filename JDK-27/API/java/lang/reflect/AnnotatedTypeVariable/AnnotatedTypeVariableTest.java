/*
 * @test
 * @summary java.lang.reflect.AnnotatedTypeVariable conformance: white-box structure + black-box behavior.
 * @run main AnnotatedTypeVariableTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;

public class AnnotatedTypeVariableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Use {
    }

    @Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Bound {
    }

    static class Holder<T> {
        public T value;

        public T get() {
            return null;
        }
    }

    static class Bounded<T extends @Bound Number & Comparable<T>> {
    }

    public static <T extends Number> void typeVar(T t) {
    }

    public static <T extends @Bound Number> void annotatedBound(T t) {
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() throws Exception {
        Method m = AnnotatedTypeVariableTest.class.getMethod("typeVar", Number.class);
        AnnotatedType at = m.getParameters()[0].getAnnotatedType();
        check(at instanceof AnnotatedTypeVariable, "is AnnotatedTypeVariable");
        AnnotatedTypeVariable atv = (AnnotatedTypeVariable) at;
        check(atv.getAnnotatedBounds().length == 1, "one annotated bound");
        check(atv.getAnnotatedBounds()[0].getType() == Number.class, "bound is Number");
        check(atv.getType() instanceof java.lang.reflect.TypeVariable, "getType is TypeVariable");
        check(AnnotatedTypeVariable.class.isInterface(), "is an interface");
        check(AnnotatedType.class.isAssignableFrom(AnnotatedTypeVariable.class),
              "extends AnnotatedType");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. field of type T resolves to an annotated type variable
        AnnotatedType atv = Holder.class.getField("value").getAnnotatedType();
        check(atv instanceof AnnotatedTypeVariable, "field T is AnnotatedTypeVariable");
        check(atv.getType() instanceof TypeVariable, "underlying is TypeVariable");
        check("T".equals(atv.getType().getTypeName()), "underlying name T");
        check(atv.getAnnotations().length == 0, "unannotated use");
        check(atv.getAnnotatedOwnerType() == null, "type variable has no owner");

        // 2. method generic return type
        AnnotatedType ret = Holder.class.getMethod("get").getAnnotatedReturnType();
        check(ret instanceof AnnotatedTypeVariable, "return T is AnnotatedTypeVariable");
        check(((AnnotatedTypeVariable) ret).getType().getTypeName().equals("T"),
              "return underlying T");

        // 3. annotated bounds from the declaration site
        TypeVariable<?> tv = Bounded.class.getTypeParameters()[0];
        AnnotatedType[] abs = tv.getAnnotatedBounds();
        check(abs.length == 2, "two bounds");
        check(abs[0].getType() == Number.class, "first bound raw Number");
        check(abs[1].getType() instanceof java.lang.reflect.ParameterizedType
              && ((java.lang.reflect.ParameterizedType) abs[1].getType()).getRawType() == Comparable.class,
              "second bound is Comparable<T> PT");
        check(abs[0].getAnnotation(Bound.class) != null, "first bound carries @Bound");
        check(abs[1].getAnnotation(Bound.class) == null, "second bound unannotated");

        // 4. annotated bounds seen from the use site match declaration
        Method ab = AnnotatedTypeVariableTest.class.getMethod("annotatedBound", Number.class);
        AnnotatedTypeVariable param = (AnnotatedTypeVariable) ab.getParameters()[0].getAnnotatedType();
        AnnotatedType[] useBounds = param.getAnnotatedBounds();
        check(useBounds.length == 1, "single bound");
        check(useBounds[0].getType() == Number.class, "use-site bound Number");
        check(useBounds[0].getAnnotation(Bound.class) != null, "use-site bound annotated");

        // 5. unbounded variable: implicit Object bound
        TypeVariable<?> free = Holder.class.getTypeParameters()[0];
        check(free.getAnnotatedBounds().length == 1, "implicit single bound");
        check(free.getAnnotatedBounds()[0].getType() == Object.class, "implicit bound Object");
        check(free.getAnnotatedBounds()[0].getAnnotations().length == 0, "implicit unannotated");

        // 6. annotations directly on the type variable use (TYPE_USE target)
        // (Holder is declared plainly; verify query surface returns empty, then via method)
        AnnotatedType plainUse = Holder.class.getField("value").getAnnotatedType();
        check(plainUse.getAnnotation(Use.class) == null, "no use-annotation present");
        check(!plainUse.isAnnotationPresent(Use.class), "isAnnotationPresent false");
        check(plainUse.getDeclaredAnnotations().length == 0, "no declared annotations");

        // 7. type variable on the class declaration also has no annotations itself
        check(free.getAnnotations().length == 0, "declaration TV unannotated");
        check(free.getAnnotation(Bound.class) == null, "declaration TV bound-annotation null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

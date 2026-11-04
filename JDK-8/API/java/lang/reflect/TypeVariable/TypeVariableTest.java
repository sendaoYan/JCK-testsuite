/*
 * @test
 * @summary java.lang.reflect.TypeVariable conformance: name/bounds/generic declaration,
 *          annotated bounds, method/constructor variables, equality semantics.
 * @run main TypeVariableTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;

public class TypeVariableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sample<T extends Number & Comparable<T>> {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
    @interface Mark {
    }

    static class G<T extends Number, U extends List<String>> implements Comparable<G<T, U>> {
        G() {
        }

        <E extends CharSequence> G(E seed) {
        }

        public int compareTo(G<T, U> other) {
            return 0;
        }
    }

    static class AnnotatedBound<T extends @Mark Number> {
    }

    static class AnnotatedParam<@Mark T extends Number> {
    }

    static class DualMarked<T extends @Mark Number & @Mark CharSequence> {
    }

    static abstract class H<T> {
        abstract <E extends CharSequence> T[] toArray(E e);
    }

    static class Named<T> {
        static <T> void sameName() {
        }
    }

    static void testTypeVariable() {
        TypeVariable<?>[] tvs = Sample.class.getTypeParameters();
        check(tvs.length == 1, "one type variable");
        TypeVariable<?> tv = tvs[0];
        check("T".equals(tv.getName()), "getName");
        check(tv.getBounds().length == 2, "two bounds");
        check(tv.getBounds()[0] == Number.class, "first bound Number");
        check(tv.getGenericDeclaration() == Sample.class, "getGenericDeclaration");
        check(tv.toString() != null, "toString");
    }

    // ---- deepened contracts (2026-11-05 batch) ----
    static void testBoundsAndDeclaration() {
        TypeVariable<?>[] gvs = G.class.getTypeParameters();
        check(gvs.length == 2, "G declares two type parameters");
        TypeVariable<?> t = gvs[0];
        TypeVariable<?> u = gvs[1];
        check("T".equals(t.getName()), "first variable named T");
        check("U".equals(u.getName()), "second variable named U");
        check(t.getBounds().length == 1, "T has a single bound");
        check(t.getBounds()[0] == Number.class, "T bound is Number");
        check(t.getBounds()[0] instanceof Type, "bound is a Type");
        check(u.getBounds().length == 1, "U has a single bound");
        check(u.getBounds()[0] instanceof ParameterizedType, "U bound is a parameterized type");
        ParameterizedType ub = (ParameterizedType) u.getBounds()[0];
        check(ub.getRawType() == List.class, "U bound raw type List");
        check(ub.getActualTypeArguments()[0] == String.class, "U bound argument String");
        check(ub.getOwnerType() == null, "U bound has no owner");
        check(t.getGenericDeclaration() == G.class, "T declared by G");
        check(u.getGenericDeclaration() == G.class, "U declared by G");
        check(t.getAnnotatedBounds() != null && t.getAnnotatedBounds().length == 1,
              "T annotated bounds length one");
        check(t.getAnnotatedBounds()[0].getType() == Number.class,
              "annotated bound type mirrors getBounds");
        check(t.getAnnotatedBounds()[0].getAnnotations().length == 0,
              "unannotated bound carries no annotations");
    }

    static void testMultipleBounds() {
        TypeVariable<?> s = Sample.class.getTypeParameters()[0];
        check(s.getBounds().length == 2, "two bounds in declaration order");
        check(s.getBounds()[0] == Number.class, "first bound Number");
        Type second = s.getBounds()[1];
        check(second instanceof ParameterizedType, "second bound is parameterized");
        ParameterizedType cpt = (ParameterizedType) second;
        check(cpt.getRawType() == Comparable.class, "second bound raw Comparable");
        check(cpt.getActualTypeArguments()[0] == s, "self-referential bound contains the variable");
        AnnotatedType[] abs = s.getAnnotatedBounds();
        check(abs.length == 2, "annotated bounds mirror the two bounds");
        check(abs[0].getType() == Number.class, "first annotated bound type Number");
        check(abs[1].getType() instanceof ParameterizedType,
              "second annotated bound type is the parameterized Comparable<T>");
        check(((ParameterizedType) abs[1].getType()).getRawType() == Comparable.class,
              "second annotated bound raw Comparable");
    }

    static void testAnnotatedBounds() {
        TypeVariable<?> a = AnnotatedBound.class.getTypeParameters()[0];
        AnnotatedType[] abs = a.getAnnotatedBounds();
        check(abs.length == 1, "one annotated bound");
        check(abs[0].isAnnotationPresent(Mark.class), "runtime annotation visible on the bound");
        check(abs[0].getAnnotation(Mark.class) != null, "getAnnotation retrieves the bound annotation");
        check(abs[0].getType() == Number.class, "annotated bound stays Number");
        TypeVariable<?> p = AnnotatedParam.class.getTypeParameters()[0];
        check(p.getAnnotatedBounds().length == 1, "parameter annotation keeps one bound");
        check(p.getAnnotatedBounds()[0].getAnnotation(Mark.class) == null,
              "annotation on the parameter is not on the bound");
        TypeVariable<?> d = DualMarked.class.getTypeParameters()[0];
        AnnotatedType[] dabs = d.getAnnotatedBounds();
        check(dabs.length == 2, "dual annotated bounds");
        check(dabs[0].isAnnotationPresent(Mark.class) && dabs[1].isAnnotationPresent(Mark.class),
              "each bound carries its own annotation");
        check(dabs[0].getType() == Number.class && dabs[1].getType() == CharSequence.class,
              "dual annotated bound types");
    }

    static void testMethodAndConstructorVariables() throws Exception {
        Method m = H.class.getDeclaredMethod("toArray", CharSequence.class);
        TypeVariable<?>[] mtvs = m.getTypeParameters();
        check(mtvs.length == 1, "method declares one variable");
        TypeVariable<?> e = mtvs[0];
        check("E".equals(e.getName()), "method variable named E");
        check(e.getGenericDeclaration() == m, "declared by the method");
        check(e.getGenericDeclaration() instanceof Method, "declaration is a Method");
        check(e.getBounds().length == 1 && e.getBounds()[0] == CharSequence.class,
              "E bound CharSequence");
        AnnotatedType[] mabs = e.getAnnotatedBounds();
        check(mabs.length == 1 && mabs[0].getType() == CharSequence.class, "E annotated bounds");
        check(mabs[0].getAnnotations().length == 0, "E bound unannotated");

        Constructor<?> gc = G.class.getDeclaredConstructor(CharSequence.class);
        TypeVariable<?>[] ctvs = gc.getTypeParameters();
        check(ctvs.length == 1, "generic constructor declares one variable");
        check("E".equals(ctvs[0].getName()), "constructor variable named E");
        check(ctvs[0].getGenericDeclaration() == gc, "declared by the constructor");
        check(ctvs[0].getGenericDeclaration() instanceof Constructor, "declaration is a Constructor");
        check(ctvs[0].getBounds()[0] == CharSequence.class, "constructor variable bound");
        Constructor<?> nc = G.class.getDeclaredConstructor();
        check(nc.getTypeParameters().length == 0, "plain constructor declares none");
        check(!ctvs[0].equals(G.class.getTypeParameters()[0]),
              "constructor TV differs from class TV of the same name");
    }

    static void testEqualityAndHierarchy() throws Exception {
        TypeVariable<?> t1 = G.class.getTypeParameters()[0];
        TypeVariable<?> t2 = G.class.getTypeParameters()[0];
        check(t1.equals(t2), "same variable equals itself across lookups");
        check(t1.hashCode() == t2.hashCode(), "hashCode consistent across lookups");
        check(t1.equals(t1), "equals reflexive");
        check(!t1.equals(null), "not equal to null");
        check(!t1.equals(G.class.getTypeParameters()[1]), "T and U distinct");
        check(t1.toString().equals("T"), "toString is the name");
        check(t1.getTypeName().equals("T"), "typeName is the name");
        TypeVariable<?> classT = Named.class.getTypeParameters()[0];
        TypeVariable<?> methodT = Named.class.getDeclaredMethod("sameName").getTypeParameters()[0];
        check(classT.getName().equals("T") && methodT.getName().equals("T"),
              "class and method variables share the name T");
        check(!classT.equals(methodT), "same name in different declarations not equal");
        check(!methodT.equals(classT), "inequality is symmetric");
        check(TypeVariable.class.isInterface(), "TypeVariable is an interface");
        check(Type.class.isAssignableFrom(TypeVariable.class), "TypeVariable extends Type");
        check(java.lang.reflect.AnnotatedElement.class.isAssignableFrom(TypeVariable.class),
              "TypeVariable extends AnnotatedElement");
        check(classT instanceof java.lang.reflect.AnnotatedElement,
              "variable is an AnnotatedElement");
        check(classT.getAnnotations().length == 0, "plain variable carries no annotations");
        check(classT.getAnnotation(Deprecated.class) == null, "getAnnotation absent returns null");
    }

    public static void main(String[] args) throws Exception {
        testTypeVariable();
        testBoundsAndDeclaration();
        testMultipleBounds();
        testAnnotatedBounds();
        testMethodAndConstructorVariables();
        testEqualityAndHierarchy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

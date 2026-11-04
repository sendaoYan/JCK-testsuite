/*
 * @test
 * @summary java.lang.reflect.TypeVariable conformance: white-box structure + black-box behavior.
 * @run main TypeVariableTest
 */

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

public class TypeVariableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sample<T extends Number & Comparable<T>> {
    }

    static class Box<T, U extends Number & java.io.Serializable> {
        T first;

        Box(T first) {
            this.first = first;
        }

        public <V> V echo(V v) {
            return v;
        }
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() {
        TypeVariable<?>[] tvs = Sample.class.getTypeParameters();
        check(tvs.length == 1, "one type variable");
        TypeVariable<?> tv = tvs[0];
        check("T".equals(tv.getName()), "getName");
        check(tv.getBounds().length == 2, "two bounds");
        check(tv.getBounds()[0] == Number.class, "first bound Number");
        check(tv.getGenericDeclaration() == Sample.class, "getGenericDeclaration");
        check(tv.toString() != null, "toString");
        check(TypeVariable.class.isInterface(), "is an interface");
        check(Type.class.isAssignableFrom(TypeVariable.class), "extends Type");
        check(TypeVariableTest.countAbstract(TypeVariable.class) >= 3,
              "name/bounds/genericDeclaration abstract");
    }

    static int countAbstract(Class<?> c) {
        int n = 0;
        for (Method m : c.getDeclaredMethods())
            if (java.lang.reflect.Modifier.isAbstract(m.getModifiers())) n++;
        return n;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. multiple bounds resolved exactly, in declaration order
        TypeVariable<Class<Box>> t = Box.class.getTypeParameters()[0];
        TypeVariable<Class<Box>> u = Box.class.getTypeParameters()[1];
        check("T".equals(t.getName()) && "U".equals(u.getName()), "names in order");
        check(t.getBounds().length == 1 && t.getBounds()[0] == Object.class,
              "unbounded T has implicit Object bound");
        check(u.getBounds().length == 2, "U has two bounds");
        check(u.getBounds()[0] == Number.class, "U first bound Number");
        check(u.getBounds()[1] == java.io.Serializable.class, "U second bound Serializable");
        check(t.getGenericDeclaration() == Box.class && u.getGenericDeclaration() == Box.class,
              "class-level declaration");
        check(t.getTypeName().equals("T"), "typeName");
        check(t.toString().equals("T"), "toString");
        check(!t.equals(u), "distinct type variables not equal");
        check(t.equals(Box.class.getTypeParameters()[0]), "same variable equals itself");

        // 2. self-referential bound keeps the type variable inside
        TypeVariable<?> s = Sample.class.getTypeParameters()[0];
        Type second = s.getBounds()[1];
        check(second instanceof ParameterizedType, "Comparable<T> bound is PT");
        ParameterizedType cpt = (ParameterizedType) second;
        check(cpt.getRawType() == Comparable.class, "bound raw Comparable");
        check(cpt.getActualTypeArguments()[0] == s, "bound argument is the variable itself");
        check(cpt.getTypeName().equals("java.lang.Comparable<T>"), "bound typeName");

        // 3. method-level type variables
        Method echo = Box.class.getMethod("echo", Object.class);
        TypeVariable<?> v = echo.getTypeParameters()[0];
        check("V".equals(v.getName()), "method type variable name");
        check(echo.equals(v.getGenericDeclaration()), "declared by the method");
        check(v.getBounds().length == 1 && v.getBounds()[0] == Object.class,
              "method TV unbounded");
        check(echo.getGenericReturnType() == v, "generic return is the TV");
        check(echo.getGenericParameterTypes()[0] == v, "generic parameter is the TV");

        // 4. constructor declaration carries class-level variables
        Constructor<?> ctor = Box.class.getDeclaredConstructor(Object.class);
        check(ctor.getTypeParameters().length == 0, "ctor has no own type parameters");
        check(ctor.getGenericParameterTypes()[0] instanceof TypeVariable
              && ((TypeVariable<?>) ctor.getGenericParameterTypes()[0]).getName().equals("T"),
              "ctor param refers to class TV T");

        // 5. annotated bounds mirror the bound list
        AnnotatedType[] aubs = u.getAnnotatedBounds();
        check(aubs.length == 2, "annotated bounds length matches");
        check(aubs[0].getType() == Number.class && aubs[1].getType() == java.io.Serializable.class,
              "annotated bound types");
        check(u.getAnnotatedBounds()[0].getAnnotations().length == 0, "unannotated bound");
        check(TypeVariableTest.uses(s), "TV is AnnotatedElement");
        check(s.getAnnotations().length == 0, "no annotations on plain TV");
        check(s.getAnnotation(Deprecated.class) == null, "getAnnotation null");
    }

    static boolean uses(Type t) {
        // TypeVariable extends AnnotatedElement: verify via instanceof-free check
        return t instanceof java.lang.reflect.AnnotatedElement;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

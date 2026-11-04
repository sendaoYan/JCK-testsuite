/*
 * @test
 * @summary java.lang.reflect.GenericDeclaration conformance: white-box structure + black-box behavior.
 * @run main GenericDeclarationTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;

public class GenericDeclarationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sample<T extends Number> {
    }

    static class Pair<K, V> {
        Pair(K k, V v) {
        }

        public <R> R combine(java.util.function.Function<K, R> f) {
            return null;
        }
    }

    static class Plain {
    }

    public static <X> void m() {
    }

    public static <A extends Number, B> B cast(A a, B b) {
        return b;
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() throws Exception {
        GenericDeclaration c = Sample.class;
        check(c.getTypeParameters().length == 1, "class type parameter count");
        check("T".equals(c.getTypeParameters()[0].getName()), "type parameter name");

        GenericDeclaration m = GenericDeclarationTest.class.getMethod("m");
        check(m.getTypeParameters().length == 1, "method type parameter count");
        check("X".equals(m.getTypeParameters()[0].getName()), "method type parameter name");
        check(GenericDeclaration.class.isInterface(), "is an interface");
        check(GenericDeclarationTest.countAbstract(GenericDeclaration.class) == 1,
              "single abstract method getTypeParameters");
    }

    static int countAbstract(Class<?> c) {
        int n = 0;
        for (Method mm : c.getDeclaredMethods())
            if (java.lang.reflect.Modifier.isAbstract(mm.getModifiers())) n++;
        return n;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. class-level declarations keep order and bounds
        GenericDeclaration pair = Pair.class;
        TypeVariable<?>[] tvs = pair.getTypeParameters();
        check(tvs.length == 2, "two class type parameters");
        check("K".equals(tvs[0].getName()) && "V".equals(tvs[1].getName()), "names in order");
        check(tvs[0].getGenericDeclaration() == Pair.class
              && tvs[1].getGenericDeclaration() == Pair.class, "declaration is the class");
        check(tvs[0].getBounds().length == 1 && tvs[0].getBounds()[0] == Object.class,
              "unbounded K defaults to Object");

        // 2. non-generic declaration yields an empty (non-null) array
        GenericDeclaration plain = Plain.class;
        check(plain.getTypeParameters() != null && plain.getTypeParameters().length == 0,
              "plain class empty type parameters");
        GenericDeclaration plainCtor = Plain.class.getDeclaredConstructor();
        check(plainCtor.getTypeParameters().length == 0, "plain ctor empty type parameters");

        // 3. method-level declarations with bounds
        GenericDeclaration cast = GenericDeclarationTest.class.getMethod("cast", Number.class, Object.class);
        TypeVariable<?>[] mtvs = cast.getTypeParameters();
        check(mtvs.length == 2, "two method type parameters");
        check("A".equals(mtvs[0].getName()) && "B".equals(mtvs[1].getName()), "method names in order");
        check(mtvs[0].getBounds()[0] == Number.class, "A bound Number");
        check(mtvs[1].getBounds()[0] == Object.class, "B implicit bound Object");
        check(cast.equals(mtvs[0].getGenericDeclaration()), "declaration is the method");

        // 4. constructor of a generic class: no own parameters, params reference class TV
        GenericDeclaration ctor = Pair.class.getDeclaredConstructor(Object.class, Object.class);
        check(ctor.getTypeParameters().length == 0, "generic-class ctor no own type params");
        Constructor<?> pc = Pair.class.getDeclaredConstructor(Object.class, Object.class);
        check(pc.getGenericParameterTypes()[0] instanceof TypeVariable
              && "K".equals(pc.getGenericParameterTypes()[0].getTypeName()),
              "ctor params reference class TVs");

        // 5. generic method on a generic class keeps both scopes separate
        GenericDeclaration combine = Pair.class.getMethod("combine", java.util.function.Function.class);
        TypeVariable<?> r = combine.getTypeParameters()[0];
        check("R".equals(r.getName()), "method scope variable R");
        check(combine.equals(r.getGenericDeclaration()), "R declared by the method");
        check(!r.equals(pair.getTypeParameters()[0]), "method TV distinct from class TV");

        // 6. the big three implement the interface
        check(GenericDeclaration.class.isAssignableFrom(Class.class), "Class is a GenericDeclaration");
        check(GenericDeclaration.class.isAssignableFrom(Method.class), "Method is a GenericDeclaration");
        check(GenericDeclaration.class.isAssignableFrom(Constructor.class),
              "Constructor is a GenericDeclaration");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

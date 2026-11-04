/*
 * @test
 * @summary java.lang.reflect.GenericDeclaration conformance: getTypeParameters on class/method/
 *          constructor/interface/enum/array classes, bounds and declaration round-trips.
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
        Pair() {
        }

        <E extends CharSequence> Pair(E seed) {
        }
    }

    static class Plain {
    }

    interface Numbered<I extends Number> {
    }

    enum Color {
        RED
    }

    public static <X> void m() {
    }

    public static <A extends Number & CharSequence, B> B cast(A a, B b) {
        return b;
    }

    static void testGenericDeclaration() throws Exception {
        GenericDeclaration c = Sample.class;
        check(c.getTypeParameters().length == 1, "class type parameter count");
        check("T".equals(c.getTypeParameters()[0].getName()), "type parameter name");

        GenericDeclaration m = GenericDeclarationTest.class.getMethod("m");
        check(m.getTypeParameters().length == 1, "method type parameter count");
        check("X".equals(m.getTypeParameters()[0].getName()), "method type parameter name");
    }

    // ---- deepened contracts (2026-11-05 batch) ----
    static void testDeclarerKinds() {
        check(GenericDeclaration.class.isAssignableFrom(Class.class), "Class is a GenericDeclaration");
        check(GenericDeclaration.class.isAssignableFrom(Method.class), "Method is a GenericDeclaration");
        check(GenericDeclaration.class.isAssignableFrom(Constructor.class),
              "Constructor is a GenericDeclaration");
        check(GenericDeclarationTest.class instanceof GenericDeclaration,
              "every class instance is a declarer");
        check(String[].class instanceof GenericDeclaration, "array class is a declarer");
        check(int[].class instanceof GenericDeclaration, "primitive array class is a declarer");
        check(Color.class instanceof GenericDeclaration, "enum class is a declarer");
        check(String[].class.getTypeParameters() != null, "array type parameters non-null");
        check(String[].class.getTypeParameters().length == 0, "array type parameters empty");
        check(int[].class.getTypeParameters().length == 0, "primitive array type parameters empty");
        check(Color.class.getTypeParameters().length == 0, "enum type parameters empty");
        check(Plain.class.getTypeParameters().length == 0, "non-generic class empty");
        check(GenericDeclaration.class.getTypeParameters().length == 0,
              "the interface itself is not generic");
    }

    static void testClassLevelDeclarations() {
        TypeVariable<?>[] pairTvs = ((GenericDeclaration) Pair.class).getTypeParameters();
        check(pairTvs.length == 2, "Pair declares two variables");
        check("K".equals(pairTvs[0].getName()), "first variable K");
        check("V".equals(pairTvs[1].getName()), "second variable V");
        check(pairTvs[0].getGenericDeclaration() == Pair.class, "K declared by Pair");
        check(pairTvs[0].getBounds()[0] == Object.class, "unbounded K defaults to Object");
        GenericDeclaration iface = Numbered.class;
        check(iface.getTypeParameters().length == 1, "generic interface declares one variable");
        check("I".equals(iface.getTypeParameters()[0].getName()), "interface variable I");
        check(iface.getTypeParameters()[0].getBounds()[0] == Number.class, "I bound Number");
        check(iface.getTypeParameters()[0].getGenericDeclaration() == Numbered.class,
              "I declared by the interface");
        GenericDeclaration sample = Sample.class;
        check(sample.getTypeParameters()[0].getBounds()[0] == Number.class,
              "Sample bound preserved through the interface");
    }

    static void testMethodAndConstructorDeclarations() throws Exception {
        Method cast = GenericDeclarationTest.class.getMethod("cast", Number.class, Object.class);
        TypeVariable<?>[] mtvs = cast.getTypeParameters();
        check(mtvs.length == 2, "cast declares two variables");
        check("A".equals(mtvs[0].getName()) && "B".equals(mtvs[1].getName()),
              "method variable names in order");
        check(mtvs[0].getBounds().length == 2, "A has two bounds");
        check(mtvs[0].getBounds()[0] == Number.class, "A first bound Number");
        check(mtvs[0].getBounds()[1] == CharSequence.class, "A second bound CharSequence");
        check(cast.equals(mtvs[0].getGenericDeclaration()), "A declared by the method");
        check(mtvs[1].getBounds()[0] == Object.class, "B implicit bound Object");
        check(cast.getTypeParameters()[0].getGenericDeclaration() == cast,
              "method declaration round-trip");

        Constructor<?> plainCtor = Plain.class.getDeclaredConstructor();
        check(plainCtor.getTypeParameters().length == 0, "plain constructor empty");
        Constructor<?> genCtor = Pair.class.getDeclaredConstructor(CharSequence.class);
        TypeVariable<?>[] ctvs = genCtor.getTypeParameters();
        check(ctvs.length == 1, "generic constructor declares one variable");
        check("E".equals(ctvs[0].getName()), "constructor variable E");
        check(ctvs[0].getBounds()[0] == CharSequence.class, "E bound CharSequence");
        check(genCtor.equals(ctvs[0].getGenericDeclaration()), "E declared by the constructor");
        check(!ctvs[0].equals(Pair.class.getTypeParameters()[0]),
              "constructor TV distinct from class TV");
        check(Pair.class.getTypeParameters()[0].getGenericDeclaration() == Pair.class,
              "class declaration round-trip");
        Method m = GenericDeclarationTest.class.getMethod("m");
        check(m.getTypeParameters()[0].getGenericDeclaration().equals(m),
              "existing method m round-trip");
    }

    public static void main(String[] args) throws Exception {
        testGenericDeclaration();
        testDeclarerKinds();
        testClassLevelDeclarations();
        testMethodAndConstructorDeclarations();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

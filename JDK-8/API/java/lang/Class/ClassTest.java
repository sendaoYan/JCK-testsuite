/*
 * @test
 * @summary java.lang.Class API conformance: name/simple/canonical/type name,
 *          superclass/interfaces/modifiers, primitive/array/enum/annotation
 *          classification, component types, forName (init flags + ClassNotFoundException),
 *          class loaders (bootstrap null), isInstance/isAssignableFrom/cast/asSubclass,
 *          and declared-member introspection.
 * @run main ClassTest
 */

import java.lang.reflect.Modifier;

public class ClassTest {
    enum Color { RED, GREEN }

    @interface Marker {}

    static class Member {}
    static interface Iface extends Runnable {}

    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPrimitive() {
        check(int.class.isPrimitive(), "int.class.isPrimitive()");
        check(int.class.getName().equals("int"), "int.class.getName() == int");
        check(int.class.getSimpleName().equals("int"), "int.class.getSimpleName() == int");
        check(int.class.getCanonicalName().equals("int"), "int.class.getCanonicalName() == int");
        check(int.class.getTypeName().equals("int"), "int.class.getTypeName() == int");
        check(int.class.getSuperclass() == null, "int.class.getSuperclass() == null");
        check(!int.class.isArray(), "!int.class.isArray()");
        check(!int.class.isInterface(), "!int.class.isInterface()");
        check(!int.class.isEnum(), "!int.class.isEnum()");
        check(!int.class.isAnnotation(), "!int.class.isAnnotation()");
        check(void.class.isPrimitive() && void.class.getName().equals("void"), "void.class");
    }

    static void testNames() {
        check(String.class.getName().equals("java.lang.String"), "String getName");
        check(String.class.getSimpleName().equals("String"), "String getSimpleName");
        check(String.class.getCanonicalName().equals("java.lang.String"), "String getCanonicalName");
        check(String.class.getTypeName().equals("java.lang.String"), "String getTypeName");
        check(String[].class.getName().equals("[Ljava.lang.String;"), "String[] getName");
        check(int[].class.getName().equals("[I"), "int[] getName");
    }

    static void testHierarchy() {
        check(Object.class.getSuperclass() == null, "Object superclass is null");
        check(String.class.getSuperclass() == Object.class, "String extends Object");
        check(Integer.class.getSuperclass() == Number.class, "Integer extends Number");
        check(Number.class.getSuperclass() == Object.class, "Number extends Object");
        check(Member.class.getSuperclass() == Object.class, "Member extends Object");
        check(Marker.class.getSuperclass() == null, "annotation is an interface: superclass null");
    }

    static void testInterfaces() {
        check(Runnable.class.isInterface(), "Runnable.isInterface()");
        check(!Runnable.class.isEnum(), "!Runnable.isEnum()");
        check(Runnable.class.getSuperclass() == null, "Runnable superclass null");
        check(Runnable.class.getInterfaces().length == 0, "Runnable implements nothing");
        check(Iface.class.isInterface(), "Iface.isInterface()");
        check(Iface.class.getInterfaces().length == 1
              && Iface.class.getInterfaces()[0] == Runnable.class, "Iface extends Runnable");
    }

    static void testArrays() {
        check(int[].class.isArray(), "int[].isArray()");
        check(int[].class.getComponentType() == int.class, "int[] component == int");
        check(String[].class.getComponentType() == String.class, "String[] component == String");
        check(int[][].class.getComponentType() == int[].class, "int[][] component == int[]");
        check(int[].class.getSuperclass() == Object.class, "int[] extends Object");
        check(!int[].class.isPrimitive(), "!int[].isPrimitive()");
    }

    static void testEnumAnnotation() {
        check(Color.class.isEnum(), "Color.isEnum()");
        check(Color.class.getSuperclass() == Enum.class, "enum extends Enum");
        check(Marker.class.isAnnotation(), "Marker.isAnnotation()");
        check(!Marker.class.isEnum(), "!Marker.isEnum()");
        check(Override.class.isAnnotation(), "java.lang.Override.isAnnotation()");
        check(Deprecated.class.isAnnotation(), "java.lang.Deprecated.isAnnotation()");
    }

    static void testIsInstanceIsAssignableCast() {
        check(Number.class.isAssignableFrom(Integer.class), "Number assignable from Integer");
        check(!Integer.class.isAssignableFrom(Number.class), "Integer not assignable from Number");
        check(Object.class.isAssignableFrom(int[].class), "Object assignable from int[]");
        check(Number.class.isAssignableFrom(Number.class), "assignable reflexive");

        check(Number.class.isInstance(Integer.valueOf(1)), "Number.isInstance(Integer)");
        check(!Integer.class.isInstance(Long.valueOf(1)), "!Integer.isInstance(Long)");

        Object i = Integer.valueOf(5);
        check(Number.class.cast(i) == i, "Number.cast(Integer)");
        check(Integer.class.cast(i) == i, "Integer.cast(Integer)");
        try {
            Integer.class.cast(Long.valueOf(1));
            throw new AssertionError("cast(Long as Integer): expected ClassCastException");
        } catch (ClassCastException expected) {
            passed++;
        }
    }

    static void testAsSubclass() {
        check(String.class.asSubclass(CharSequence.class) == String.class,
              "asSubclass to implemented interface");
        try {
            String.class.asSubclass(Integer.class);
            throw new AssertionError("asSubclass(Integer): expected ClassCastException");
        } catch (ClassCastException expected) {
            passed++;
        }
    }

    static void testForName() throws Exception {
        check(Class.forName("java.lang.String") == String.class, "forName(java.lang.String)");
        check(Class.forName("java.lang.Integer", true, ClassLoader.getSystemClassLoader())
              == Integer.class, "forName with init + loader");
        check(Class.forName("[I") == int[].class, "forName([I) array");
        try {
            Class.forName("java.lang.DoesNotExist");
            throw new AssertionError("forName(missing): expected ClassNotFoundException");
        } catch (ClassNotFoundException expected) {
            passed++;
        }
    }

    static void testClassLoader() {
        check(String.class.getClassLoader() == null, "bootstrap loader -> null");
        check(int.class.getClassLoader() == null, "primitive class loader -> null");
        check(ClassTest.class.getClassLoader() != null, "user class loader != null");
    }

    static void testModifiers() {
        check(Modifier.isPublic(String.class.getModifiers()), "String is public");
        check(Modifier.isFinal(String.class.getModifiers()), "String is final");
        check(Modifier.isPublic(Runnable.class.getModifiers()), "Runnable is public");
        check(Modifier.isInterface(Runnable.class.getModifiers()), "Runnable modifiers interface");
        check(Modifier.isStatic(Member.class.getModifiers()), "Member nested class is static");
        check(Modifier.isInterface(Iface.class.getModifiers()), "Iface modifiers interface");
    }

    static void testDeclaredMembers() {
        check(String.class.getDeclaredMethods().length > 0, "String declared methods > 0");
        check(String.class.getDeclaredConstructors().length > 0, "String declared ctors > 0");
        check(Member.class.getDeclaredConstructors().length == 1, "Member default ctor count");
    }

    static void testClassKinds() {
        Object anon = new Object() {};
        check(anon.getClass().isAnonymousClass(), "anonymous class detected");
        check(!anon.getClass().isLocalClass(), "anonymous is not local");
        check(!anon.getClass().isMemberClass(), "anonymous is not member");

        class Local {}
        check(Local.class.isLocalClass(), "local class detected");
        check(!Local.class.isAnonymousClass(), "local is not anonymous");

        check(Member.class.isMemberClass(), "member class detected");
        check(!Member.class.isAnonymousClass(), "member is not anonymous");
        check(!Member.class.isSynthetic(), "member not synthetic");
    }

    public static void main(String[] args) throws Exception {
        testPrimitive();
        testNames();
        testHierarchy();
        testInterfaces();
        testArrays();
        testEnumAnnotation();
        testIsInstanceIsAssignableCast();
        testAsSubclass();
        testForName();
        testClassLoader();
        testModifiers();
        testDeclaredMembers();
        testClassKinds();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

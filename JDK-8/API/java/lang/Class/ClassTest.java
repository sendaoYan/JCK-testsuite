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

    // ===== 契约深化（2026-11）新增 fixtures =====
    interface IOne {}
    interface ITwo {}
    static class Base { public static final int PUB = 1; }
    static class Derived extends Base {}
    static class TwoIface implements IOne, ITwo {}
    public static class NewInst { public NewInst() {} }
    static class PrivCtor { private PrivCtor() {} }
    public static class CtorArgs {
        public final int sum;
        public CtorArgs(int a, int b) { sum = a + b; }
    }
    static class Overload {
        public void foo(int x) {}
        public void foo(Integer x) {}
    }
    static class InitCounter { static int count; }
    static class InitTarget { static { InitCounter.count++; } }


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

    // ===== 契约深化（2026-11）新增断言 =====

    static boolean hasMethodNamed(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    static boolean hasDeclaredMethodNamed(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    static void testPrimitiveNameMatrix() {
        Class<?>[] prims = { boolean.class, byte.class, char.class, short.class, int.class,
                             long.class, float.class, double.class, void.class };
        String[] names = { "boolean", "byte", "char", "short", "int", "long", "float", "double", "void" };
        Class<?>[] types = { Boolean.TYPE, Byte.TYPE, Character.TYPE, Short.TYPE, Integer.TYPE,
                             Long.TYPE, Float.TYPE, Double.TYPE, Void.TYPE };
        for (int i = 0; i < prims.length; i++) {
            check(prims[i].getName().equals(names[i]), names[i] + ".getName()");
            check(prims[i].getSimpleName().equals(names[i]), names[i] + ".getSimpleName()");
            check(prims[i].getCanonicalName().equals(names[i]), names[i] + ".getCanonicalName()");
            check(prims[i].getTypeName().equals(names[i]), names[i] + ".getTypeName()");
            check(prims[i].isPrimitive(), names[i] + ".isPrimitive()");
            check(prims[i].getSuperclass() == null, names[i] + ".getSuperclass() == null");
            check(prims[i].getClassLoader() == null, names[i] + ".getClassLoader() == null");
            check(!prims[i].isArray() && !prims[i].isInterface() && !prims[i].isEnum()
                  && !prims[i].isAnnotation() && !prims[i].isSynthetic(), names[i] + " kind flags");
            check(prims[i] == types[i], names[i] + " == wrapper TYPE");
            check(prims[i].toString().equals(names[i]), names[i] + ".toString()");
            check(prims[i].getFields().length == 0, names[i] + ".getFields() empty");
            check(prims[i].getMethods().length == 0, names[i] + ".getMethods() empty");
            check(prims[i].getDeclaredFields().length == 0, names[i] + ".getDeclaredFields() empty");
        }
    }

    static void testArrayNameMatrix() {
        check(new boolean[0].getClass().getName().equals("[Z"), "boolean[] name [Z");
        check(new byte[0].getClass().getName().equals("[B"), "byte[] name [B");
        check(new char[0].getClass().getName().equals("[C"), "char[] name [C");
        check(new short[0].getClass().getName().equals("[S"), "short[] name [S");
        check(new int[0].getClass().getName().equals("[I"), "int[] name [I");
        check(new long[0].getClass().getName().equals("[J"), "long[] name [J");
        check(new float[0].getClass().getName().equals("[F"), "float[] name [F");
        check(new double[0].getClass().getName().equals("[D"), "double[] name [D");
        check(new String[0].getClass().getName().equals("[Ljava.lang.String;"), "String[] name");
        check(String[][].class.getName().equals("[[Ljava.lang.String;"), "String[][] name");
        check(int[][].class.getName().equals("[[I"), "int[][] name");
        check(new int[0].getClass() == int[].class, "array class literal identity");
        check(int[].class.getSimpleName().equals("int[]"), "int[] simple name");
        check(String[].class.getSimpleName().equals("String[]"), "String[] simple name");
        check(int[][].class.getSimpleName().equals("int[][]"), "int[][] simple name");
        check(int[].class.getCanonicalName().equals("int[]"), "int[] canonical");
        check(String[].class.getCanonicalName().equals("java.lang.String[]"), "String[] canonical");
        check(int[][].class.getCanonicalName().equals("int[][]"), "int[][] canonical");
        check(int[].class.getTypeName().equals("int[]"), "int[] type name");
        check(String[].class.getTypeName().equals("java.lang.String[]"), "String[] type name");
        check(int[][].class.getTypeName().equals("int[][]"), "int[][] type name");
        check(int[].class.toString().equals("class [I"), "int[] toString");
        check(String[].class.toString().equals("class [Ljava.lang.String;"), "String[] toString");
        check(Color[].class.getName().equals("[LClassTest$Color;"), "nested enum array name");
        check(Color[].class.getSimpleName().equals("Color[]"), "nested enum array simple name");
        check(boolean[].class.getComponentType() == boolean.class, "boolean[] component");
        check(int[][].class.getComponentType() == int[].class, "int[][] component");
        check(String[][].class.getComponentType() == String[].class, "String[][] component");
        check(int[].class.getSuperclass() == Object.class, "array superclass Object");
        check(int[].class.getComponentType().isPrimitive(), "component is primitive");
        check(!int[].class.isPrimitive() && !int[].class.isEnum() && !int[].class.isInterface()
              && !int[].class.isAnnotation(), "array kind flags");
        check(Object.class.isAssignableFrom(int[].class), "Object assignable from array");
    }

    static void testNestedNamesDeep() {
        check(Member.class.getName().equals("ClassTest$Member"), "nested binary name Outer$Inner");
        check(Member.class.getSimpleName().equals("Member"), "nested simple name");
        check(Member.class.getCanonicalName().equals("ClassTest.Member"), "nested canonical name");
        check(Member.class.getDeclaringClass() == ClassTest.class, "nested declaring class");
        check(Member.class.isMemberClass(), "member class kind");
        check(Iface.class.getDeclaringClass() == ClassTest.class, "member interface declaring class");
        check(Color.class.getName().equals("ClassTest$Color"), "enum binary name");
        check(Color.class.getCanonicalName().equals("ClassTest.Color"), "enum canonical name");
        check(Color.class.getSimpleName().equals("Color"), "enum simple name");
        check(Marker.class.getName().equals("ClassTest$Marker"), "annotation binary name");
        check(Marker.class.toString().equals("interface ClassTest$Marker"), "annotation toString");
        Object anon = new Object() {};
        check(anon.getClass().getCanonicalName() == null, "anonymous canonical null");
        check(anon.getClass().getSimpleName().equals(""), "anonymous simple name empty");
        check(anon.getClass().getName().matches("ClassTest\\$\\d+"), "anonymous binary name numbered");
        check(anon.getClass().toString().startsWith("class ClassTest$"), "anonymous toString");
        check(anon.getClass().getDeclaringClass() == null, "anonymous declaring class null");
        check(anon.getClass().isAnonymousClass(), "anonymous kind");
        check(anon.getClass().getEnclosingMethod() != null, "anonymous in method has enclosing method");
        check("testNestedNamesDeep".equals(anon.getClass().getEnclosingMethod().getName()),
              "anonymous enclosing method name");
        check(anon.getClass().getEnclosingConstructor() == null, "anonymous enclosing ctor null");
        class DeepLocal {}
        check(DeepLocal.class.getCanonicalName() == null, "local canonical null");
        check(DeepLocal.class.getSimpleName().equals("DeepLocal"), "local simple name kept");
        check(DeepLocal.class.isLocalClass(), "local class kind");
        check(DeepLocal.class.getDeclaringClass() == null, "local declaring class null");
        check(DeepLocal.class.getEnclosingMethod() != null, "local enclosing method");
        check("testNestedNamesDeep".equals(DeepLocal.class.getEnclosingMethod().getName()),
              "local enclosing method name");
        check(DeepLocal.class.getEnclosingConstructor() == null, "local enclosing ctor null");
        check(Member.class.getEnclosingMethod() == null, "member has no enclosing method");
        check(Member.class.getEnclosingClass() == ClassTest.class, "member enclosing class");
        check(anon.getClass().getEnclosingClass() == ClassTest.class, "anonymous enclosing class");
        Runnable lambda = () -> {};
        check(lambda.getClass().getName().contains("Lambda"), "lambda class name contains Lambda");
        check(lambda.getClass().isSynthetic(), "lambda class is synthetic");
        check(!lambda.getClass().isLocalClass() && !lambda.getClass().isMemberClass(), "lambda kinds");
        check(!lambda.getClass().isAnonymousClass(), "lambda is not anonymous class");
    }

    static void testToStringFamily() {
        check(String.class.toString().equals("class java.lang.String"), "class toString");
        check(Runnable.class.toString().equals("interface java.lang.Runnable"), "interface toString");
        check(Marker.class.toString().equals("interface ClassTest$Marker"), "annotation toString");
        check(int[].class.toString().equals("class [I"), "array toString");
        check(Color.class.toString().equals("class ClassTest$Color"), "enum toString");
        check(Member.class.toString().equals("class ClassTest$Member"), "nested toString");
        check(String.class.toGenericString().equals("public final class java.lang.String"),
              "toGenericString String exact");
        check(int.class.toGenericString().equals("int"), "toGenericString primitive exact");
        // JDK-8 quirk: array toGenericString prints abstract final + descriptor form.
        check("public abstract final class [I".equals(int[].class.toGenericString()),
              "toGenericString array exact (JDK-8 form): " + int[].class.toGenericString());
        check("public abstract final class [Ljava.lang.String;".equals(String[].class.toGenericString()),
              "toGenericString reference array exact (JDK-8 form)");
        check(Member.class.toGenericString().equals("static class ClassTest$Member"),
              "toGenericString nested static exact");
        check(Base.class.toGenericString().equals("static class ClassTest$Base"),
              "toGenericString base exact");
        check(Derived.class.toGenericString().equals("static class ClassTest$Derived"),
              "toGenericString derived exact");
        String rg = Runnable.class.toGenericString();
        check(rg.contains("public") && rg.contains("abstract") && rg.contains("interface")
              && rg.endsWith("java.lang.Runnable"), "toGenericString interface modifiers");
        String mg = Marker.class.toGenericString();
        check(mg.contains("interface") && mg.endsWith("ClassTest$Marker"),
              "toGenericString annotation contains interface");
    }

    static void testCastContract() {
        Object five = Integer.valueOf(5);
        check(Integer.class.cast(five) == five, "cast returns the same object");
        check(Number.class.cast(five) == five, "cast to superclass identity");
        check(Object.class.cast(five) == five, "cast to Object identity");
        check(Integer.class.cast(null) == null, "cast(null) returns null");
        check(CharSequence.class.cast("abc") == "abc", "cast to implemented interface");
        try {
            Integer.class.cast(Long.valueOf(1));
            throw new AssertionError("cast(Long as Integer): expected CCE");
        } catch (ClassCastException e) {
            check("Cannot cast java.lang.Long to java.lang.Integer".equals(e.getMessage()),
                  "cast CCE message exact: " + e.getMessage());
        }
        try {
            int.class.cast(five);
            throw new AssertionError("primitive cast of non-null: expected CCE");
        } catch (ClassCastException e) {
            check("Cannot cast java.lang.Integer to int".equals(e.getMessage()),
                  "primitive cast CCE message exact");
        }
        check(int.class.cast(null) == null, "primitive cast(null) returns null");
        check(Integer.class.isInstance(five), "isInstance true");
        check(!Integer.class.isInstance("x"), "isInstance wrong type false");
        check(!Integer.class.isInstance(null), "isInstance(null) false");
        check(!Object.class.isInstance(null), "Object.isInstance(null) false");
        check(!int.class.isInstance(five), "primitive isInstance always false");
        check(Number.class.isInstance(Double.valueOf(1.0)), "Number.isInstance(Double)");
        check(int[].class.isInstance(new int[0]), "array isInstance");
        check(!int[].class.isInstance(new long[0]), "array isInstance type strict");
        check(!int[].class.isInstance(new Integer[0]), "primitive vs boxed array distinct");
    }

    static void testAsSubclassContract() {
        check(Integer.class.asSubclass(Number.class) == Integer.class, "asSubclass to superclass");
        check(Integer.class.asSubclass(Integer.class) == Integer.class, "asSubclass to itself");
        check(String.class.asSubclass(Object.class) == String.class, "asSubclass to Object");
        check(String.class.asSubclass(CharSequence.class) == String.class, "asSubclass to interface");
        check(Member.class.asSubclass(Object.class) == Member.class, "asSubclass nested class");
        try {
            String.class.asSubclass(Number.class);
            throw new AssertionError("asSubclass(Number): expected CCE");
        } catch (ClassCastException e) {
            check("class java.lang.String".equals(e.getMessage()),
                  "asSubclass CCE message exact: " + e.getMessage());
        }
        try {
            int.class.asSubclass(Object.class);
            throw new AssertionError("primitive asSubclass: expected CCE");
        } catch (ClassCastException expected) {
            passed++;
        }
    }

    @SuppressWarnings("deprecation")
    static void testNewInstanceContract() throws Exception {
        Object o = NewInst.class.newInstance();
        check(o instanceof NewInst, "newInstance returns instance");
        check(o.getClass() == NewInst.class, "newInstance exact class");
        check("".equals(String.class.newInstance().toString()), "newInstance String empty content");
        try {
            System.class.newInstance();
            throw new AssertionError("private ctor: expected IllegalAccessException");
        } catch (IllegalAccessException e) {
            check(e.getMessage() != null && e.getMessage().contains("java.lang.System"),
                  "IAE message mentions System");
        }
        try {
            Number.class.newInstance();
            throw new AssertionError("abstract class: expected InstantiationException");
        } catch (InstantiationException expected) {
            passed++;
        }
        try {
            Runnable.class.newInstance();
            throw new AssertionError("interface: expected InstantiationException");
        } catch (InstantiationException expected) {
            passed++;
        }
        try {
            CtorArgs.class.newInstance();
            throw new AssertionError("no nullary ctor: expected InstantiationException");
        } catch (InstantiationException expected) {
            passed++;
        }
        // JDK-8: compiled class files carry no nest attributes, so the enclosing
        // class cannot reflectively reach a nested private constructor.
        try {
            PrivCtor.class.newInstance();
            throw new AssertionError("nested private ctor: expected IllegalAccessException (JDK-8)");
        } catch (IllegalAccessException e) {
            check(e.getMessage() != null && e.getMessage().contains("PrivCtor"),
                  "IAE message mentions PrivCtor");
        }
        Object rt = CtorArgs.class.getConstructor(int.class, int.class).newInstance(2, 3);
        check(rt instanceof CtorArgs && ((CtorArgs) rt).sum == 5,
              "getConstructor().newInstance() round-trip with args");
        Object rt2 = CtorArgs.class.getConstructor(Integer.TYPE, Integer.TYPE).newInstance(4, 6);
        check(((CtorArgs) rt2).sum == 10, "Integer.TYPE resolves to int ctor");
        try {
            CtorArgs.class.getConstructor(Integer.class, Integer.class);
            throw new AssertionError("boxed params: expected NoSuchMethodException (exact match)");
        } catch (NoSuchMethodException e) {
            check("ClassTest$CtorArgs.<init>(java.lang.Integer, java.lang.Integer)".equals(e.getMessage()),
                  "constructor NSME message exact: " + e.getMessage());
        }
        try {
            CtorArgs.class.getConstructor();
            throw new AssertionError("missing nullary ctor: expected NoSuchMethodException");
        } catch (NoSuchMethodException expected) {
            passed++;
        }
    }

    static void testFieldLookup() throws Exception {
        check(Integer.class.getField("MAX_VALUE") != null, "getField MAX_VALUE");
        check(Integer.class.getField("MIN_VALUE") != null, "getField MIN_VALUE");
        check(Integer.class.getField("TYPE") != null, "getField TYPE");
        check(String.class.getField("CASE_INSENSITIVE_ORDER") != null, "getField CASE_INSENSITIVE_ORDER");
        check(Base.class.getField("PUB") != null, "getField declared public field");
        check(Derived.class.getField("PUB") != null, "getField finds inherited public field");
        try {
            Integer.class.getField("NOPE");
            throw new AssertionError("missing field: expected NoSuchFieldException");
        } catch (NoSuchFieldException e) {
            check("NOPE".equals(e.getMessage()), "NSFE message is the bare field name");
        }
        try {
            Member.class.getField("nope");
            throw new AssertionError("nested missing field: expected NoSuchFieldException");
        } catch (NoSuchFieldException expected) {
            passed++;
        }
        try {
            Integer.class.getField(null);
            throw new AssertionError("getField(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
        check(int.class.getFields().length == 0, "primitive getFields empty");
        check(int.class.getDeclaredFields().length == 0, "primitive getDeclaredFields empty");
        check(Overload.class.getFields().length == 0, "class without public fields");
        check(Member.class.getDeclaredFields().length == 0, "class without declared fields");
        check(Member.class.getDeclaredConstructors().length == 1, "implicit ctor still declared");
        check(String.class.getFields().length > 0, "String has public fields");
    }

    static void testMethodLookup() throws Exception {
        java.lang.reflect.Method mInt = Overload.class.getDeclaredMethod("foo", int.class);
        java.lang.reflect.Method mBox = Overload.class.getDeclaredMethod("foo", Integer.class);
        check(mInt != null && mBox != null, "both overloads resolvable");
        check(mInt != mBox, "exact parameter types distinguish overloads");
        check(mInt.getName().equals("foo") && mBox.getName().equals("foo"), "method names");
        try {
            Overload.class.getDeclaredMethod("foo", Long.class);
            throw new AssertionError("unrelated param box: expected NoSuchMethodException");
        } catch (NoSuchMethodException e) {
            check("ClassTest$Overload.foo(java.lang.Long)".equals(e.getMessage()),
                  "NSME message exact: " + e.getMessage());
        }
        try {
            Overload.class.getDeclaredMethod("nope");
            throw new AssertionError("missing method: expected NoSuchMethodException");
        } catch (NoSuchMethodException e) {
            check("ClassTest$Overload.nope()".equals(e.getMessage()), "no-arg NSME message exact");
        }
        check(hasMethodNamed(String.class, "toString"), "String getMethods toString");
        check(hasMethodNamed(String.class, "equals"), "String getMethods equals");
        check(hasMethodNamed(String.class, "hashCode"), "String getMethods hashCode");
        check(hasMethodNamed(String.class, "length"), "String getMethods length");
        check(hasMethodNamed(String.class, "charAt"), "String getMethods charAt");
        check(hasMethodNamed(String.class, "isEmpty"), "String getMethods isEmpty");
        check(hasMethodNamed(Runnable.class, "run"), "Runnable getMethods run");
        check(!hasMethodNamed(Runnable.class, "toString"), "interface getMethods excludes Object.toString");
        check(!hasMethodNamed(Runnable.class, "equals"), "interface getMethods excludes Object.equals");
        check(hasMethodNamed(Member.class, "toString"), "class getMethods includes Object methods");
        check(hasDeclaredMethodNamed(Overload.class, "foo"), "getDeclaredMethods sees foo");
        int fooCount = 0;
        for (java.lang.reflect.Method m : Overload.class.getDeclaredMethods())
            if (m.getName().equals("foo")) fooCount++;
        check(fooCount == 2, "declared methods count both overloads");
        check(Overload.class.getMethod("foo", int.class) != null, "getMethod public overload");
        check(Member.class.getMethod("toString") != null, "getMethod finds inherited toString");
        check(int.class.getMethods().length == 0, "primitive getMethods empty");
        check(int.class.getDeclaredMethods().length == 0, "primitive getDeclaredMethods empty");
        try {
            Integer.class.getMethod("nope");
            throw new AssertionError("missing public method: expected NoSuchMethodException");
        } catch (NoSuchMethodException e) {
            check("java.lang.Integer.nope()".equals(e.getMessage()), "public NSME message exact");
        }
        try {
            Integer.class.getMethod(null);
            throw new AssertionError("getMethod(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    @SuppressWarnings("deprecation")
    static void testInterfacesContract() throws Exception {
        Class<?>[] ifc = TwoIface.class.getInterfaces();
        check(ifc.length == 2, "two directly declared interfaces");
        check(ifc[0] == IOne.class && ifc[1] == ITwo.class, "declaration order preserved");
        check(Derived.class.getInterfaces().length == 0, "plain class has no interfaces");
        check(Base.class.getInterfaces().length == 0, "base has no interfaces");
        check(IOne.class.getInterfaces().length == 0, "interface without superinterfaces");
        check(Marker.class.getInterfaces().length == 1
              && Marker.class.getInterfaces()[0] == java.lang.annotation.Annotation.class,
              "annotation directly extends Annotation");
        check(IOne.class.isAssignableFrom(TwoIface.class), "assignable through interface");
        check(!IOne.class.isAssignableFrom(Derived.class), "unrelated not assignable");
        check(!TwoIface.class.isAssignableFrom(IOne.class), "reverse not assignable");
        Class<?>[] enumIfc = Enum.class.getInterfaces();
        check(enumIfc.length == 2, "Enum implements 2 interfaces on JDK-8");
        boolean hasCmp = false, hasSer = false;
        for (Class<?> c : enumIfc) {
            if (c == Comparable.class) hasCmp = true;
            if (c == java.io.Serializable.class) hasSer = true;
        }
        check(hasCmp && hasSer, "Enum implements Comparable and Serializable");
        check(Color.class.getInterfaces().length == 0, "enum without constant bodies: no direct ifaces");
        check(Color.class.isAssignableFrom(Color.RED.getClass()), "constant assignable to enum");
        check(!TwoIface.class.isInterface(), "class not interface");
        check(TwoIface.class.newInstance() instanceof IOne, "instance of first interface");
    }

    static void testEnumConstantsContract() {
        Color[] arr = Color.class.getEnumConstants();
        check(arr != null && arr.length == 2, "constants array size");
        check(arr[0] == Color.RED && arr[1] == Color.GREEN, "constants identity and order");
        arr[0] = Color.GREEN; // mutate the returned array
        Color[] fresh = Color.class.getEnumConstants();
        check(fresh[0] == Color.RED, "getEnumConstants returns a defensive copy");
        check(fresh != arr, "fresh array on each call");
        check(Color.class.getEnumConstants() != Color.class.getEnumConstants(),
              "successive calls do not share arrays");
        check(String.class.getEnumConstants() == null, "non-enum class -> null");
        check(int.class.getEnumConstants() == null, "primitive -> null");
        check(int[].class.getEnumConstants() == null, "array -> null");
        check(Marker.class.getEnumConstants() == null, "annotation -> null");
        check(Member.class.getEnumConstants() == null, "nested plain class -> null");
        check(Color.class.isEnum(), "enum kind");
        check(!String.class.isEnum() && !int.class.isEnum() && !Marker.class.isEnum(),
              "non-enum kinds");
        check(Color.RED.getDeclaringClass() == Color.class, "constant declaring class");
        check(Color.RED.name().equals("RED") && Color.RED.ordinal() == 0, "constant name and ordinal");
        check(Color.class.getSuperclass() == Enum.class, "enum superclass is Enum");
        check(Color.GREEN.ordinal() == 1, "second constant ordinal");
    }

    static void testForNameInitialization() throws Exception {
        ClassLoader cl = ClassTest.class.getClassLoader();
        String target = "ClassTest$InitTarget";
        Class<?> uninit = Class.forName(target, false, cl);
        check(InitCounter.count == 0, "forName(initialize=false) skips the static initializer");
        check(uninit == InitTarget.class, "forName(false) still loads the same Class object");
        Class.forName(target);
        check(InitCounter.count == 1, "first initializing forName runs the static block once");
        Class.forName(target, true, cl);
        check(InitCounter.count == 1, "second initializing forName does not re-run");
        Class.forName(target);
        check(InitCounter.count == 1, "repeat default forName does not re-run");
        Class.forName(target, false, cl);
        check(InitCounter.count == 1, "forName(false) after init does not re-run");
        check(Class.forName("ClassTest$Member") == Member.class, "forName nested member");
        check(Class.forName("ClassTest$Color") == Color.class, "forName nested enum");
        check(Class.forName("ClassTest$Marker") == Marker.class, "forName nested annotation");
        check(Class.forName("[I") == int[].class, "forName [I");
        check(Class.forName("[[I") == int[][].class, "forName [[I");
        check(Class.forName("[Ljava.lang.String;") == String[].class, "forName [Ljava.lang.String;");
        check(Class.forName("[LClassTest$Color;") == Color[].class, "forName nested array type");
        check(Class.forName("java.lang.Runnable", false, cl) == Runnable.class,
              "forName interface without initialization");
        try {
            Class.forName("java.lang.String[]");
            throw new AssertionError("source-form array name: expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            check("java/lang/String[]".equals(e.getMessage()),
                  "source-form array CNFE message exact: " + e.getMessage());
        }
        try {
            Class.forName("int");
            throw new AssertionError("primitive name: expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            check("int".equals(e.getMessage()), "primitive name CNFE message exact");
        }
        try {
            Class.forName("java.lang.DoesNotExist");
            throw new AssertionError("missing class: expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            check("java.lang.DoesNotExist".equals(e.getMessage()), "missing class CNFE message exact");
        }
        try {
            Class.forName(null);
            throw new AssertionError("forName(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void testResourcePackageMisc() throws Exception {
        check(String.class.getResourceAsStream("/java/lang/String.class") != null,
              "absolute resource lookup");
        check(String.class.getResourceAsStream("String.class") != null, "relative resource lookup");
        check(String.class.getResource("String.class") != null, "getResource relative form");
        check(String.class.getResource("/no/such/thing.bin") == null, "missing resource -> null");
        check(String.class.getResourceAsStream("Nope.class") == null, "missing stream -> null");
        check(int.class.getResourceAsStream("x") == null, "primitive has no resources");
        java.io.InputStream in = String.class.getResourceAsStream("/java/lang/String.class");
        byte[] magic = new byte[4];
        int off = 0;
        while (off < 4) {
            int r = in.read(magic, off, 4 - off);
            if (r < 0) break;
            off += r;
        }
        in.close();
        check(off == 4 && (magic[0] & 0xFF) == 0xCA && (magic[1] & 0xFF) == 0xFE
              && (magic[2] & 0xFF) == 0xBA && (magic[3] & 0xFF) == 0xBE,
              "stream starts with the CAFEBABE class-file magic");
        Package p = String.class.getPackage();
        check(p != null && "java.lang".equals(p.getName()), "String package is java.lang");
        check(p == Integer.class.getPackage(), "same Package instance within a package");
        check(p == Object.class.getPackage(), "Object shares the java.lang Package");
        check(ClassTest.class.getPackage() == null, "default package yields null on JDK-8");
        check(String.class.getPackage() != Color.class.getPackage(),
              "default-package class has a different (null) Package");
        check(!String.class.desiredAssertionStatus(), "desiredAssertionStatus false");
        check(!int.class.desiredAssertionStatus(), "primitive assertion status false");
        check(!ClassTest.class.desiredAssertionStatus(), "user class assertion status false");
        check(String.class.getProtectionDomain() != null, "bootstrap ProtectionDomain non-null");
        check(ClassTest.class.getProtectionDomain() != null, "user ProtectionDomain non-null");
        check(String.class.getSigners() == null, "bootstrap class has no signers");
    }

    static void testSuperclassGeneric() {
        check(Object.class.getSuperclass() == null, "Object superclass null");
        check(Runnable.class.getSuperclass() == null, "interface superclass null");
        check(Marker.class.getSuperclass() == null, "annotation superclass null");
        check(int.class.getSuperclass() == null, "primitive superclass null");
        check(void.class.getSuperclass() == null, "void superclass null");
        check(Color.class.getSuperclass() == Enum.class, "enum superclass Enum");
        check(int[].class.getSuperclass() == Object.class, "primitive array superclass Object");
        check(String[].class.getSuperclass() == Object.class, "reference array superclass Object");
        check(Derived.class.getSuperclass() == Base.class, "nested chain superclass");
        check(Base.class.getSuperclass() == Object.class, "nested base superclass Object");
        check(String.class.getGenericSuperclass() == Object.class, "raw generic superclass");
        check(Derived.class.getGenericSuperclass() == Base.class, "nested generic superclass");
        check(Object.class.getGenericSuperclass() == null, "Object generic superclass null");
        check(int.class.getGenericSuperclass() == null, "primitive generic superclass null");
        check(Runnable.class.getGenericSuperclass() == null, "interface generic superclass null");
        check(int[].class.getGenericSuperclass() == Object.class, "array generic superclass Object");
        check(Derived.class.getGenericInterfaces().length == 0, "no generic interfaces");
        check(TwoIface.class.getGenericInterfaces().length == 2, "two generic interfaces");
        check(Integer.class.getGenericInterfaces().length == 1,
              "Integer implements only Comparable on JDK-8");
        check(Object.class.getGenericInterfaces().length == 0, "Object has no interfaces");
        check(String.class.getGenericInterfaces().length == 3,
              "String implements CharSequence, Comparable, Serializable");
    }

    static void testSyntheticAnnotationFlags() {
        check(Override.class.isAnnotation(), "Override is an annotation");
        check(Deprecated.class.isAnnotation(), "Deprecated is an annotation");
        check(SuppressWarnings.class.isAnnotation(), "SuppressWarnings is an annotation");
        check(FunctionalInterface.class.isAnnotation(), "FunctionalInterface is an annotation");
        check(SafeVarargs.class.isAnnotation(), "SafeVarargs is an annotation");
        check(Marker.class.isAnnotation(), "user annotation is an annotation");
        check(!Member.class.isAnnotation(), "class not annotation");
        check(!int.class.isAnnotation(), "primitive not annotation");
        check(!Color.class.isAnnotation(), "enum not annotation");
        check(!String[].class.isAnnotation(), "array not annotation");
        check(!String.class.isSynthetic(), "String not synthetic");
        check(!Member.class.isSynthetic(), "member not synthetic");
        check(!int.class.isSynthetic(), "primitive not synthetic");
        check(!Color.class.isSynthetic(), "enum not synthetic");
        check(!Marker.class.isSynthetic(), "annotation not synthetic");
        Runnable r = () -> {};
        check(r.getClass().isSynthetic(), "lambda class is synthetic");
        check(Member.class.isMemberClass(), "member kind positive");
        check(!int.class.isMemberClass(), "primitive not a member class");
        check(!Color.class.isLocalClass(), "enum not a local class");
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
        testPrimitiveNameMatrix();
        testArrayNameMatrix();
        testNestedNamesDeep();
        testToStringFamily();
        testCastContract();
        testAsSubclassContract();
        testNewInstanceContract();
        testFieldLookup();
        testMethodLookup();
        testInterfacesContract();
        testEnumConstantsContract();
        testForNameInitialization();
        testResourcePackageMisc();
        testSuperclassGeneric();
        testSyntheticAnnotationFlags();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

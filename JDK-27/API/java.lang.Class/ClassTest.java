/*
 * @test
 * @summary java.lang.Class conformance: white-box structure + black-box behavior.
 * @run main ClassTest
 */

import java.lang.constant.ConstantDesc;
import java.lang.invoke.TypeDescriptor;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Modifier;

public class ClassTest {
    enum Color { RED, GREEN }

    @interface Marker {}

    static class Member {}
    public static class PublicMember {}
    static interface Iface extends Runnable {}

    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Class");
        check(Modifier.isFinal(c.getModifiers()), "Class is final");
        check(!c.isInterface(), "Class is a class");
        check(c.getSuperclass() == Object.class, "Class extends Object");
        // Class has no public constructor (instances are created by the JVM).
        check(c.getConstructors().length == 0, "Class has no public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
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
        check(Member.class.getCanonicalName().equals("ClassTest.Member"), "nested class canonical name");
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
        check(Iface.class.isAssignableFrom(Iface.class), "interface assignable from itself");
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

    // ---- Part 2b: black-box for previously uncovered methods ----
    static void testBlackBox() throws Exception {
        // accessFlags(): Set<AccessFlag>
        check(String.class.accessFlags().contains(AccessFlag.PUBLIC), "String accessFlags PUBLIC");
        check(String.class.accessFlags().contains(AccessFlag.FINAL), "String accessFlags FINAL");
        check(int.class.accessFlags().contains(AccessFlag.PUBLIC), "int accessFlags PUBLIC");
        check(Runnable.class.accessFlags().contains(AccessFlag.INTERFACE), "Runnable accessFlags INTERFACE");

        // arrayType() / componentType() (Class view + TypeDescriptor.OfField view)
        check(int.class.arrayType() == int[].class, "int.arrayType() == int[]");
        check(String.class.arrayType() == String[].class, "String.arrayType() == String[]");
        check(int[].class.arrayType() == int[][].class, "int[].arrayType() == int[][]");
        check(int[].class.componentType() == int.class, "int[].componentType() == int");
        check(int.class.componentType() == null, "primitive componentType() == null");
        try {
            void.class.arrayType();
            throw new AssertionError("void.arrayType(): expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            passed++;
        }
        TypeDescriptor.OfField<?> tf = String[].class;
        check(tf.componentType() == String.class, "OfField.componentType()");
        check(tf.arrayType() == String[][].class, "OfField.arrayType()");

        // desiredAssertionStatus(): deterministic; primitives never enable assertions
        check(!int.class.desiredAssertionStatus(), "int desiredAssertionStatus false");
        check(String.class.desiredAssertionStatus() == String.class.desiredAssertionStatus(),
              "desiredAssertionStatus stable");

        // forPrimitiveName()
        check(Class.forPrimitiveName("int") == int.class, "forPrimitiveName(int)");
        check(Class.forPrimitiveName("void") == void.class, "forPrimitiveName(void)");
        check(Class.forPrimitiveName("boolean") == boolean.class, "forPrimitiveName(boolean)");
        check(Class.forPrimitiveName("double") == double.class, "forPrimitiveName(double)");
        check(Class.forPrimitiveName("java.lang.String") == null, "forPrimitiveName(non-primitive) == null");
        try {
            Class.forPrimitiveName(null);
            throw new AssertionError("forPrimitiveName(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }

        // getAnnotatedSuperclass / getAnnotatedInterfaces
        check(Integer.class.getAnnotatedSuperclass().getType() == Number.class,
              "Integer annotated superclass is Number");
        check(Object.class.getAnnotatedSuperclass() == null, "Object annotated superclass null");
        check(int.class.getAnnotatedSuperclass() == null, "primitive annotated superclass null");
        check(Runnable.class.getAnnotatedInterfaces().length == 0,
              "Runnable has no annotated interfaces");
        check(Iface.class.getAnnotatedInterfaces().length == 1
              && Iface.class.getAnnotatedInterfaces()[0].getType() == Runnable.class,
              "Iface annotated interface is Runnable");

        // getClasses(): public member classes (including inherited)
        boolean foundPublicMember = false;
        for (Class<?> mc : ClassTest.class.getClasses()) if (mc == PublicMember.class) foundPublicMember = true;
        check(foundPublicMember, "getClasses includes PublicMember");

        // getDeclaredAnnotationsByType(): repeatable-annotation-aware lookup
        check(Override.class.getDeclaredAnnotationsByType(java.lang.annotation.Target.class).length == 1,
              "Override has one @Target");
        check(String.class.getDeclaredAnnotationsByType(Deprecated.class).length == 0,
              "String has no @Deprecated");

        // getEnclosingMethod / getEnclosingConstructor
        class LocalInBlackBox {}
        check(LocalInBlackBox.class.getEnclosingMethod() != null, "local class has enclosing method");
        check("testBlackBox".equals(LocalInBlackBox.class.getEnclosingMethod().getName()),
              "enclosing method name is testBlackBox");
        check(LocalInBlackBox.class.getEnclosingConstructor() == null, "local class has no enclosing ctor");
        check(Member.class.getEnclosingMethod() == null, "member class has no enclosing method");
        check(Member.class.getEnclosingConstructor() == null, "member class has no enclosing ctor");

        // nest: getNestHost / getNestMembers / isNestmateOf
        check(String.class.getNestHost() == String.class, "String nest host is itself");
        check(Member.class.getNestHost() == ClassTest.class, "Member nest host is ClassTest");
        check(int.class.getNestHost() == int.class, "primitive nest host is itself");
        check(Member.class.isNestmateOf(ClassTest.class), "Member is nestmate of ClassTest");
        check(ClassTest.class.isNestmateOf(Member.class), "ClassTest is nestmate of Member");
        check(!String.class.isNestmateOf(Integer.class), "String not nestmate of Integer");
        check(int.class.isNestmateOf(int.class), "primitive nestmate of itself");
        check(!int.class.isNestmateOf(long.class), "different primitives not nestmates");
        check(int.class.getNestMembers().length == 1 && int.class.getNestMembers()[0] == int.class,
              "primitive nest members == {this}");
        boolean foundMemberNest = false;
        for (Class<?> c : ClassTest.class.getNestMembers()) if (c == Member.class) foundMemberNest = true;
        check(foundMemberNest, "ClassTest.getNestMembers includes Member");

        // getPermittedSubclasses / isSealed (using the sealed java.lang.constant.ConstantDesc)
        check(ConstantDesc.class.isSealed(), "ConstantDesc is sealed");
        check(ConstantDesc.class.getPermittedSubclasses() != null
              && ConstantDesc.class.getPermittedSubclasses().length > 0,
              "ConstantDesc has permitted subclasses");
        check(!String.class.isSealed(), "String is not sealed");
        check(String.class.getPermittedSubclasses() == null, "String permitted subclasses null");
        check(!int.class.isSealed(), "primitive not sealed");
        check(int.class.getPermittedSubclasses() == null, "primitive permitted subclasses null");

        // getProtectionDomain / getResource / getSigners
        check(String.class.getProtectionDomain() != null, "bootstrap class has ProtectionDomain");
        check(String.class.getResource("Object.class") != null, "getResource resolves java/lang/Object.class");
        check(String.class.getResource("jck/No/Such.class") == null, "getResource missing -> null");
        check(String.class.getSigners() == null, "bootstrap class signers null");

        // newInstance() (deprecated but still functional for public nullary ctor)
        check(StringBuilder.class.newInstance() instanceof StringBuilder, "newInstance public nullary ctor");
        try {
            Runnable.class.newInstance();
            throw new AssertionError("newInstance(interface): expected InstantiationException");
        } catch (InstantiationException expected) {
            passed++;
        }

        // toGenericString / toString
        check(String.class.toGenericString().equals("public final class java.lang.String"),
              "toGenericString for String");
        check(int.class.toGenericString().equals("int"), "toGenericString for int");
        check(int[].class.toGenericString().equals("int[]"), "toGenericString for int[]");
        check(Runnable.class.toGenericString().contains("java.lang.Runnable")
              && Runnable.class.toGenericString().contains("interface"),
              "toGenericString for interface");
        check(String.class.toString().equals("class java.lang.String"), "toString class");
        check(int.class.toString().equals("int"), "toString primitive");
        check(Runnable.class.toString().equals("interface java.lang.Runnable"), "toString interface");
        check(int[].class.toString().equals("class [I"), "toString array");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
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
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

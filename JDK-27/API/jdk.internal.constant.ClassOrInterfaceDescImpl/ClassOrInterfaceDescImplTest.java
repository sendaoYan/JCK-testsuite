/*
 * @test
 * @summary jdk.internal.constant.ClassOrInterfaceDescImpl conformance: internal package reflection.
 * @modules java.base/jdk.internal.constant:+open
 * @run main ClassOrInterfaceDescImplTest
 */
public class ClassOrInterfaceDescImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.constant.ClassOrInterfaceDescImpl", false, ClassOrInterfaceDescImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.constant.ClassDesc"), "interface java.lang.constant.ClassDesc");
        check(hasMethod(c, "arrayType"), "method arrayType");
        check(hasMethod(c, "descriptorString"), "method descriptorString");
        check(hasMethod(c, "displayName"), "method displayName");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "internalName"), "method internalName");
        check(hasMethod(c, "isClassOrInterface"), "method isClassOrInterface");
        check(hasMethod(c, "nested"), "method nested");
        check(hasMethod(c, "ofValidated"), "method ofValidated");
        check(hasMethod(c, "packageName"), "method packageName");
        check(hasMethod(c, "resolveConstantDesc"), "method resolveConstantDesc");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "descriptor"), "field descriptor");
        check(hasField(c, "internalName"), "field internalName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

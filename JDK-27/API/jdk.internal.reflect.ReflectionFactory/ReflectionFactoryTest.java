/*
 * @test
 * @summary jdk.internal.reflect.ReflectionFactory conformance: internal package reflection.
 * @modules java.base/jdk.internal.reflect:+open
 * @run main ReflectionFactoryTest
 */
public class ReflectionFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.reflect.ReflectionFactory", false, ReflectionFactoryTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "copyConstructor"), "method copyConstructor");
        check(hasMethod(c, "copyField"), "method copyField");
        check(hasMethod(c, "copyMethod"), "method copyMethod");
        check(hasMethod(c, "defaultReadObjectForSerialization"), "method defaultReadObjectForSerialization");
        check(hasMethod(c, "defaultWriteObjectForSerialization"), "method defaultWriteObjectForSerialization");
        check(hasMethod(c, "getExecutableSharedParameterTypes"), "method getExecutableSharedParameterTypes");
        check(hasMethod(c, "getExecutableTypeAnnotationBytes"), "method getExecutableTypeAnnotationBytes");
        check(hasMethod(c, "getReflectionFactory"), "method getReflectionFactory");
        check(hasMethod(c, "hasStaticInitializerForSerialization"), "method hasStaticInitializerForSerialization");
        check(hasMethod(c, "leafCopyMethod"), "method leafCopyMethod");
        check(hasMethod(c, "newConstructorAccessor"), "method newConstructorAccessor");
        check(hasMethod(c, "newConstructorForExternalization"), "method newConstructorForExternalization");
        check(hasMethod(c, "newConstructorForSerialization"), "method newConstructorForSerialization");
        check(hasMethod(c, "newFieldAccessor"), "method newFieldAccessor");
        check(hasMethod(c, "newInstance"), "method newInstance");
        check(hasMethod(c, "newMethodAccessor"), "method newMethodAccessor");
        check(hasMethod(c, "newOptionalDataExceptionForSerialization"), "method newOptionalDataExceptionForSerialization");
        check(hasMethod(c, "parseAccessFlags"), "method parseAccessFlags");
        check(hasMethod(c, "readObjectForSerialization"), "method readObjectForSerialization");
        check(hasMethod(c, "readObjectNoDataForSerialization"), "method readObjectNoDataForSerialization");
        check(hasMethod(c, "readResolveForSerialization"), "method readResolveForSerialization");
        check(hasMethod(c, "serialPersistentFields"), "method serialPersistentFields");
        check(hasMethod(c, "writeObjectForSerialization"), "method writeObjectForSerialization");
        check(hasMethod(c, "writeReplaceForSerialization"), "method writeReplaceForSerialization");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "DEFAULT_CONFIG"), "field DEFAULT_CONFIG");
        check(hasField(c, "config"), "field config");
        check(hasField(c, "hasStaticInitializerMethod"), "field hasStaticInitializerMethod");
        check(hasField(c, "langReflectAccess"), "field langReflectAccess");
        check(hasField(c, "nonSerializableLeafClasses"), "field nonSerializableLeafClasses");
        check(hasField(c, "soleInstance"), "field soleInstance");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

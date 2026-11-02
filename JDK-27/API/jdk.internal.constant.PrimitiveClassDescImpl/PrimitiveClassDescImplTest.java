/*
 * @test
 * @summary jdk.internal.constant.PrimitiveClassDescImpl conformance: internal package reflection.
 * @modules java.base/jdk.internal.constant:+open
 * @run main PrimitiveClassDescImplTest
 */
public class PrimitiveClassDescImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.constant.PrimitiveClassDescImpl", false, PrimitiveClassDescImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.constant.DynamicConstantDesc"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.constant.ClassDesc"), "interface java.lang.constant.ClassDesc");
        check(hasMethod(c, "arrayType"), "method arrayType");
        check(hasMethod(c, "descriptorString"), "method descriptorString");
        check(hasMethod(c, "displayName"), "method displayName");
        check(hasMethod(c, "isPrimitive"), "method isPrimitive");
        check(hasMethod(c, "resolveConstantDesc"), "method resolveConstantDesc");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "wrapper"), "method wrapper");
        check(hasField(c, "CD_boolean"), "field CD_boolean");
        check(hasField(c, "CD_byte"), "field CD_byte");
        check(hasField(c, "CD_char"), "field CD_char");
        check(hasField(c, "CD_double"), "field CD_double");
        check(hasField(c, "CD_float"), "field CD_float");
        check(hasField(c, "CD_int"), "field CD_int");
        check(hasField(c, "CD_long"), "field CD_long");
        check(hasField(c, "CD_short"), "field CD_short");
        check(hasField(c, "CD_void"), "field CD_void");
        check(hasField(c, "descriptor"), "field descriptor");
        check(hasField(c, "lazyWrapper"), "field lazyWrapper");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

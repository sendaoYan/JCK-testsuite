/*
 * @test
 * @summary jdk.internal.constant.DirectMethodHandleDescImpl conformance: internal package reflection.
 * @modules java.base/jdk.internal.constant:+open
 * @run main DirectMethodHandleDescImplTest
 */
public class DirectMethodHandleDescImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.constant.DirectMethodHandleDescImpl", false, DirectMethodHandleDescImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.constant.DirectMethodHandleDesc"), "interface java.lang.constant.DirectMethodHandleDesc");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "invocationType"), "method invocationType");
        check(hasMethod(c, "isOwnerInterface"), "method isOwnerInterface");
        check(hasMethod(c, "kind"), "method kind");
        check(hasMethod(c, "lookupDescriptor"), "method lookupDescriptor");
        check(hasMethod(c, "methodName"), "method methodName");
        check(hasMethod(c, "owner"), "method owner");
        check(hasMethod(c, "refKind"), "method refKind");
        check(hasMethod(c, "resolveConstantDesc"), "method resolveConstantDesc");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "invocationType"), "field invocationType");
        check(hasField(c, "kind"), "field kind");
        check(hasField(c, "name"), "field name");
        check(hasField(c, "owner"), "field owner");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

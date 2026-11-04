/*
 * @test
 * @summary sun.reflect.annotation.TypeAnnotation.TypeAnnotationTargetInfo conformance: internal package reflection.
 * @modules java.base/sun.reflect.annotation:+open
 * @run main TypeAnnotationTargetInfoTest
 */
public class TypeAnnotationTargetInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.annotation.TypeAnnotation$TypeAnnotationTargetInfo", false, TypeAnnotationTargetInfoTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getCount"), "method getCount");
        check(hasMethod(c, "getSecondaryIndex"), "method getSecondaryIndex");
        check(hasMethod(c, "getTarget"), "method getTarget");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "UNUSED_INDEX"), "field UNUSED_INDEX");
        check(hasField(c, "count"), "field count");
        check(hasField(c, "secondaryIndex"), "field secondaryIndex");
        check(hasField(c, "target"), "field target");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

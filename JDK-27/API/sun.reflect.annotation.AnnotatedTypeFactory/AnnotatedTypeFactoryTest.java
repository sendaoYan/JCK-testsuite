/*
 * @test
 * @summary sun.reflect.annotation.AnnotatedTypeFactory conformance: internal package reflection.
 * @modules java.base/sun.reflect.annotation:+open
 * @run main AnnotatedTypeFactoryTest
 */
public class AnnotatedTypeFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.annotation.AnnotatedTypeFactory", false, AnnotatedTypeFactoryTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "buildAnnotatedType"), "method buildAnnotatedType");
        check(hasMethod(c, "nestingForType"), "method nestingForType");
        check(hasField(c, "EMPTY_ANNOTATED_TYPE"), "field EMPTY_ANNOTATED_TYPE");
        check(hasField(c, "EMPTY_ANNOTATED_TYPE_ARRAY"), "field EMPTY_ANNOTATED_TYPE_ARRAY");
        check(hasField(c, "EMPTY_TYPE_ANNOTATION_ARRAY"), "field EMPTY_TYPE_ANNOTATION_ARRAY");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

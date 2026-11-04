/*
 * @test
 * @summary sun.reflect.annotation.TypeAnnotation conformance: internal package reflection.
 * @modules java.base/sun.reflect.annotation:+open
 * @run main TypeAnnotationTest
 */
public class TypeAnnotationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.annotation.TypeAnnotation", false, TypeAnnotationTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "filter"), "method filter");
        check(hasMethod(c, "getAnnotation"), "method getAnnotation");
        check(hasMethod(c, "getBaseDeclaration"), "method getBaseDeclaration");
        check(hasMethod(c, "getLocationInfo"), "method getLocationInfo");
        check(hasMethod(c, "getTargetInfo"), "method getTargetInfo");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "annotation"), "field annotation");
        check(hasField(c, "baseDeclaration"), "field baseDeclaration");
        check(hasField(c, "loc"), "field loc");
        check(hasField(c, "targetInfo"), "field targetInfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

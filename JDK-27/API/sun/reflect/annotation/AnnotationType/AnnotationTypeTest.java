/*
 * @test
 * @summary sun.reflect.annotation.AnnotationType conformance: internal package reflection.
 * @modules java.base/sun.reflect.annotation:+open
 * @run main AnnotationTypeTest
 */
public class AnnotationTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.annotation.AnnotationType", false, AnnotationTypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getInstance"), "method getInstance");
        check(hasMethod(c, "invocationHandlerReturnType"), "method invocationHandlerReturnType");
        check(hasMethod(c, "isInherited"), "method isInherited");
        check(hasMethod(c, "memberDefaults"), "method memberDefaults");
        check(hasMethod(c, "memberTypes"), "method memberTypes");
        check(hasMethod(c, "members"), "method members");
        check(hasMethod(c, "retention"), "method retention");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "inherited"), "field inherited");
        check(hasField(c, "memberDefaults"), "field memberDefaults");
        check(hasField(c, "memberTypes"), "field memberTypes");
        check(hasField(c, "members"), "field members");
        check(hasField(c, "retention"), "field retention");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

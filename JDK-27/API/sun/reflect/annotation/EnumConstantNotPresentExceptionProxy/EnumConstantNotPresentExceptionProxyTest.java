/*
 * @test
 * @summary sun.reflect.annotation.EnumConstantNotPresentExceptionProxy conformance: internal package reflection.
 * @modules java.base/sun.reflect.annotation:+open
 * @run main EnumConstantNotPresentExceptionProxyTest
 */
public class EnumConstantNotPresentExceptionProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.annotation.EnumConstantNotPresentExceptionProxy", false, EnumConstantNotPresentExceptionProxyTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.reflect.annotation.ExceptionProxy"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "generateException"), "method generateException");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "constName"), "field constName");
        check(hasField(c, "enumType"), "field enumType");
        check(hasField(c, "serialVersionUID"), "field serialVersionUID");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

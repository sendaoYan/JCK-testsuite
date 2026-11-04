/*
 * @test
 * @summary sun.reflect.annotation.TypeNotPresentExceptionProxy conformance: internal package reflection.
 * @modules java.base/sun.reflect.annotation:+open
 * @run main TypeNotPresentExceptionProxyTest
 */
public class TypeNotPresentExceptionProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.annotation.TypeNotPresentExceptionProxy", false, TypeNotPresentExceptionProxyTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.reflect.annotation.ExceptionProxy"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "generateException"), "method generateException");
        check(hasMethod(c, "getCause"), "method getCause");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "typeName"), "method typeName");
        check(hasField(c, "cause"), "field cause");
        check(hasField(c, "serialVersionUID"), "field serialVersionUID");
        check(hasField(c, "typeName"), "field typeName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

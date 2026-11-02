/*
 * @test
 * @summary java.beans.beancontext.BeanContextServiceRevokedEvent conformance: concrete class reflection.
 * @run main BeanContextServiceRevokedEventTest
 */

public class BeanContextServiceRevokedEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServiceRevokedEvent");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.beans.beancontext.BeanContextEvent".equals(c.getSuperclass().getName()), "superclass java.beans.beancontext.BeanContextEvent");
        check(hasMethod(c, "getServiceClass"), "declares getServiceClass");
        check(hasMethod(c, "getSourceAsBeanContextServices"), "declares getSourceAsBeanContextServices");
        check(hasMethod(c, "isCurrentServiceInvalidNow"), "declares isCurrentServiceInvalidNow");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

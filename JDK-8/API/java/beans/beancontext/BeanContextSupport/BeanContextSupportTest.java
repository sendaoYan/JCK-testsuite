/*
 * @test
 * @summary java.beans.beancontext.BeanContextSupport conformance: concrete class reflection.
 * @run main BeanContextSupportTest
 */

public class BeanContextSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextSupport");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.beans.beancontext.BeanContextChildSupport".equals(c.getSuperclass().getName()), "superclass java.beans.beancontext.BeanContextChildSupport");
        check(Class.forName("java.beans.beancontext.BeanContext").isAssignableFrom(c), "implements/extends java.beans.beancontext.BeanContext");
        check(Class.forName("java.io.Serializable").isAssignableFrom(c), "implements/extends java.io.Serializable");
        check(Class.forName("java.beans.PropertyChangeListener").isAssignableFrom(c), "implements/extends java.beans.PropertyChangeListener");
        check(Class.forName("java.beans.VetoableChangeListener").isAssignableFrom(c), "implements/extends java.beans.VetoableChangeListener");
        check(hasMethod(c, "add"), "declares add");
        check(hasMethod(c, "addAll"), "declares addAll");
        check(hasMethod(c, "addBeanContextMembershipListener"), "declares addBeanContextMembershipListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

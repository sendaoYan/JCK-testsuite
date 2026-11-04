/*
 * @test
 * @summary java.beans.beancontext.BeanContextChildSupport conformance: concrete class reflection.
 * @run main BeanContextChildSupportTest
 */

public class BeanContextChildSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextChildSupport");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(Class.forName("java.beans.beancontext.BeanContextChild").isAssignableFrom(c), "implements/extends java.beans.beancontext.BeanContextChild");
        check(Class.forName("java.beans.beancontext.BeanContextServicesListener").isAssignableFrom(c), "implements/extends java.beans.beancontext.BeanContextServicesListener");
        check(Class.forName("java.io.Serializable").isAssignableFrom(c), "implements/extends java.io.Serializable");
        check(hasMethod(c, "addPropertyChangeListener"), "declares addPropertyChangeListener");
        check(hasMethod(c, "addVetoableChangeListener"), "declares addVetoableChangeListener");
        check(hasMethod(c, "firePropertyChange"), "declares firePropertyChange");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

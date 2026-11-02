/*
 * @test
 * @summary java.beans.beancontext.BeanContextServicesSupport.BCSSChild conformance: concrete class reflection.
 * @run main BCSSChildTest
 */

public class BCSSChildTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServicesSupport$BCSSChild");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.beans.beancontext.BeanContextSupport$BCSChild".equals(c.getSuperclass().getName()), "superclass java.beans.beancontext.BeanContextSupport$BCSChild");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

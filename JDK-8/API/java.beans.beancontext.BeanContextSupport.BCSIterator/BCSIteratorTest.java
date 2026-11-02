/*
 * @test
 * @summary java.beans.beancontext.BeanContextSupport.BCSIterator conformance: concrete class reflection.
 * @run main BCSIteratorTest
 */

public class BCSIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextSupport$BCSIterator");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(Class.forName("java.util.Iterator").isAssignableFrom(c), "implements/extends java.util.Iterator");
        check(hasMethod(c, "hasNext"), "declares hasNext");
        check(hasMethod(c, "next"), "declares next");
        check(hasMethod(c, "remove"), "declares remove");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

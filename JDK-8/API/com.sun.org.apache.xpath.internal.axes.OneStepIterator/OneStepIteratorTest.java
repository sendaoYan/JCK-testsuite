/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.axes.OneStepIterator conformance: reflection.
 * @run main OneStepIteratorTest
 */

public class OneStepIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.axes.OneStepIterator", false, OneStepIteratorTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("OneStepIterator"), "simple name OneStepIterator");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.axes.ChildTestIterator"), "superclass com.sun.org.apache.xpath.internal.axes.ChildTestIterator");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "setRoot"), "method setRoot");
        check(has(c, "detach"), "method detach");
        check(has(c, "clone"), "method clone");
        check(has(c, "cloneWithReset"), "method cloneWithReset");
        check(has(c, "isReverseAxes"), "method isReverseAxes");
        check(has(c, "getLength"), "method getLength");
        check(has(c, "reset"), "method reset");
        check(has(c, "getAxis"), "method getAxis");
        check(has(c, "deepEquals"), "method deepEquals");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}

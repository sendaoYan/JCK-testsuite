/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.axes.ReverseAxesWalker conformance: reflection.
 * @run main ReverseAxesWalkerTest
 */

public class ReverseAxesWalkerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.axes.ReverseAxesWalker", false, ReverseAxesWalkerTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ReverseAxesWalker"), "simple name ReverseAxesWalker");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.axes.AxesWalker"), "superclass com.sun.org.apache.xpath.internal.axes.AxesWalker");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "setRoot"), "method setRoot");
        check(has(c, "detach"), "method detach");
        check(has(c, "isReverseAxes"), "method isReverseAxes");
        check(has(c, "getLastPos"), "method getLastPos");
        check(has(c, "isDocOrdered"), "method isDocOrdered");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}

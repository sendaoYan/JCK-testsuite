/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.axes.AttributeIterator conformance: reflection.
 * @run main AttributeIteratorTest
 */

public class AttributeIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.axes.AttributeIterator", false, AttributeIteratorTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("AttributeIterator"), "simple name AttributeIterator");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.axes.ChildTestIterator"), "superclass com.sun.org.apache.xpath.internal.axes.ChildTestIterator");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getAxis"), "method getAxis");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}

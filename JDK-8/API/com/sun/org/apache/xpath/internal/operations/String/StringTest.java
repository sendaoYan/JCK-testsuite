/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.operations.String conformance: reflection.
 * @run main StringTest
 */

public class StringTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.operations.String", false, StringTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("String"), "simple name String");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.operations.UnaryOperation"), "superclass com.sun.org.apache.xpath.internal.operations.UnaryOperation");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "operate"), "method operate");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}

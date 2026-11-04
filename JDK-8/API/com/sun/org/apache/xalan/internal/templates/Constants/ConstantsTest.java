/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.templates.Constants conformance: reflection.
 * @run main ConstantsTest
 */

public class ConstantsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.templates.Constants", false, ConstantsTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Constants"), "simple name Constants");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.utils.Constants"), "superclass com.sun.org.apache.xml.internal.utils.Constants");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}

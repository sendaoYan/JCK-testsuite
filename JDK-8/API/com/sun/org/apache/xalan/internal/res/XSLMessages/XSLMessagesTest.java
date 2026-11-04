/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.res.XSLMessages conformance: reflection.
 * @run main XSLMessagesTest
 */

public class XSLMessagesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.res.XSLMessages", false, XSLMessagesTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("XSLMessages"), "simple name XSLMessages");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.res.XPATHMessages"), "superclass com.sun.org.apache.xpath.internal.res.XPATHMessages");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "createMessage"), "method createMessage");
        check(has(c, "createWarning"), "method createWarning");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}

/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.res.XPATHMessages conformance: reflection.
 * @run main XPATHMessagesTest
 */

public class XPATHMessagesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.res.XPATHMessages", false, XPATHMessagesTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("XPATHMessages"), "simple name XPATHMessages");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.res.XMLMessages"), "superclass com.sun.org.apache.xml.internal.res.XMLMessages");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "createXPATHMessage"), "method createXPATHMessage");
        check(has(c, "createXPATHWarning"), "method createXPATHWarning");
        check(has(c, "createXPATHMsg"), "method createXPATHMsg");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}

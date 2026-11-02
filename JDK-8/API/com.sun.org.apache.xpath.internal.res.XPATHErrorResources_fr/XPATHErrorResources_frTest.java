/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.res.XPATHErrorResources_fr conformance: reflection.
 * @run main XPATHErrorResources_frTest
 */

public class XPATHErrorResources_frTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.res.XPATHErrorResources_fr", false, XPATHErrorResources_frTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("XPATHErrorResources_fr"), "simple name XPATHErrorResources_fr");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.ListResourceBundle"), "superclass java.util.ListResourceBundle");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getContents"), "method getContents");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}

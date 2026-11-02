/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.res.XSLTErrorResources_zh_TW conformance: reflection.
 * @run main XSLTErrorResources_zh_TWTest
 */

public class XSLTErrorResources_zh_TWTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.res.XSLTErrorResources_zh_TW", false, XSLTErrorResources_zh_TWTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("XSLTErrorResources_zh_TW"), "simple name XSLTErrorResources_zh_TW");
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

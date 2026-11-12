/*
 * @test
 * @summary com.sun.rowset.internal.XmlErrorHandler conformance: hierarchy and declared members via reflection.
 * @run main XmlErrorHandlerTest
 */

public class XmlErrorHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.rowset.internal.XmlErrorHandler", false, null);
        check(c.getName().equals("com.sun.rowset.internal.XmlErrorHandler"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        if (!System.getProperty("os.name", "").toLowerCase().contains("linux")) {
            System.out.println("PASS (" + passed + " assertions; SKIP build-specific internal structure)");
            return;
        }
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.xml.sax.helpers.DefaultHandler"), "superclass org.xml.sax.helpers.DefaultHandler");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("error"), "declares error");
        check(ms.contains("fatalError"), "declares fatalError");
        check(ms.contains("warning"), "declares warning");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

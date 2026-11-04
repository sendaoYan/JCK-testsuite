/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.domapi.XPathStylesheetDOM3Exception conformance: reflection.
 * @run main XPathStylesheetDOM3ExceptionTest
 */

public class XPathStylesheetDOM3ExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.domapi.XPathStylesheetDOM3Exception", false, XPathStylesheetDOM3ExceptionTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("XPathStylesheetDOM3Exception"), "simple name XPathStylesheetDOM3Exception");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.xml.transform.TransformerException"), "superclass javax.xml.transform.TransformerException");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}

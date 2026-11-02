/*
 * @test
 * @summary jdk.internal.org.xml.sax.SAXParseException conformance: exception/value behavior.
 * @run main SAXParseExceptionTest
 */

import java.lang.reflect.Method;

public class SAXParseExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.xml.sax.SAXParseException", false, SAXParseExceptionTest.class.getClassLoader());
        Object e = c.getConstructor(String.class, String.class, String.class, int.class, int.class).newInstance("msg", "pub", "sys", 10, 20);
        check("msg".equals(c.getMethod("getMessage").invoke(e)), "getMessage");
        check("pub".equals(c.getMethod("getPublicId").invoke(e)), "getPublicId");
        check("sys".equals(c.getMethod("getSystemId").invoke(e)), "getSystemId");
        check(((Integer) c.getMethod("getLineNumber").invoke(e)).intValue() == 10, "getLineNumber");
        check(((Integer) c.getMethod("getColumnNumber").invoke(e)).intValue() == 20, "getColumnNumber");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

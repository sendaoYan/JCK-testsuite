/*
 * @test
 * @summary jdk.internal.org.xml.sax.SAXNotSupportedException conformance: exception/value behavior.
 * @run main SAXNotSupportedExceptionTest
 */

import java.lang.reflect.Method;

public class SAXNotSupportedExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.xml.sax.SAXNotSupportedException", false, SAXNotSupportedExceptionTest.class.getClassLoader());
        check("jdk.internal.org.xml.sax.SAXException".equals(c.getSuperclass().getName()), "extends SAXException");
        Object e = c.getConstructor(String.class).newInstance("feature");
        check("feature".equals(c.getMethod("getMessage").invoke(e)), "getMessage");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

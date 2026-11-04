/*
 * @test
 * @summary jdk.internal.org.xml.sax.SAXException conformance: exception/value behavior.
 * @run main SAXExceptionTest
 */

import java.lang.reflect.Method;

public class SAXExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.xml.sax.SAXException", false, SAXExceptionTest.class.getClassLoader());
        check("java.lang.Exception".equals(c.getSuperclass().getName()), "extends Exception");
        Object e = c.getConstructor(String.class).newInstance("boom");
        check("boom".equals(c.getMethod("getMessage").invoke(e)), "getMessage");
        Object e2 = c.getConstructor(String.class, Exception.class).newInstance("outer", new Exception("inner"));
        check("outer".equals(c.getMethod("getMessage").invoke(e2)), "outer message");
        Object ex = c.getMethod("getException").invoke(e2);
        check(ex != null && "inner".equals(((Throwable) ex).getMessage()), "getException inner");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

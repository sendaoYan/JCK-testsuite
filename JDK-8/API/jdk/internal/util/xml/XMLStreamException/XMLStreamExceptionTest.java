/*
 * @test
 * @summary jdk.internal.util.xml.XMLStreamException conformance: exception/value behavior.
 * @run main XMLStreamExceptionTest
 */

import java.lang.reflect.Method;

public class XMLStreamExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.util.xml.XMLStreamException", false, XMLStreamExceptionTest.class.getClassLoader());
        check("java.lang.Exception".equals(c.getSuperclass().getName()), "extends Exception");
        Object e = c.getConstructor(String.class).newInstance("x");
        check("x".equals(c.getMethod("getMessage").invoke(e)), "getMessage");
        check(c.getMethod("getNestedException").invoke(e) == null, "no nested");
        Object e2 = c.getConstructor(String.class, Throwable.class).newInstance("x", new Exception("nested"));
        Object n = c.getMethod("getNestedException").invoke(e2);
        check(n != null && "nested".equals(((Throwable) n).getMessage()), "nested cause");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary com.oracle.webservices.internal.api.message.ReadOnlyPropertyException conformance: exception/value behavior.
 * @run main ReadOnlyPropertyExceptionTest
 */

import java.lang.reflect.Method;

public class ReadOnlyPropertyExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.oracle.webservices.internal.api.message.ReadOnlyPropertyException", false, ReadOnlyPropertyExceptionTest.class.getClassLoader());
        check("java.lang.IllegalArgumentException".equals(c.getSuperclass().getName()), "extends IllegalArgumentException");
        Object e = c.getConstructor(String.class).newInstance("prop1");
        check("prop1".equals(c.getMethod("getPropertyName").invoke(e)), "getPropertyName");
        String m = (String) c.getMethod("getMessage").invoke(e);
        check(m != null && m.contains("prop1"), "getMessage mentions property");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

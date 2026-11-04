/*
 * @test
 * @summary com.sun.org.apache.xml.internal.utils.WrongParserException conformance: exception hierarchy and construction.
 * @run main WrongParserExceptionTest
 */
public class WrongParserExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        java.lang.Throwable x = new com.sun.org.apache.xml.internal.utils.WrongParserException("msg");
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.utils.WrongParserException", false, WrongParserExceptionTest.class.getClassLoader());
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("java.lang.RuntimeException", false, null).isAssignableFrom(c), "extends java.lang.RuntimeException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

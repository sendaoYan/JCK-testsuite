/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.ClassFormatException conformance: exception construction.
 * @run main ClassFormatExceptionTest
 */

public class ClassFormatExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.apache.bcel.internal.classfile.ClassFormatException x = new com.sun.org.apache.bcel.internal.classfile.ClassFormatException("msg");
        check(java.lang.Throwable.class.isAssignableFrom(x.getClass()), "is Throwable");
        check(java.lang.RuntimeException.class.isAssignableFrom(x.getClass()), "extends java.lang.RuntimeException");
        check("msg".equals(x.getMessage()), "message equals msg");
        check(x.getCause() == null, "cause is null");
        check(x.toString() != null, "toString non-null");
        x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

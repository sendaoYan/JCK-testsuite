/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.runtime.InternalRuntimeError conformance: exception construction.
 * @run main InternalRuntimeErrorTest
 */

public class InternalRuntimeErrorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.apache.xalan.internal.xsltc.runtime.InternalRuntimeError x = new com.sun.org.apache.xalan.internal.xsltc.runtime.InternalRuntimeError("msg");
        check(java.lang.Throwable.class.isAssignableFrom(x.getClass()), "is Throwable");
        check(java.lang.Error.class.isAssignableFrom(x.getClass()), "extends java.lang.Error");
        check("msg".equals(x.getMessage()), "message equals msg");
        check(x.getCause() == null, "cause is null");
        check(x.toString() != null, "toString non-null");
        x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

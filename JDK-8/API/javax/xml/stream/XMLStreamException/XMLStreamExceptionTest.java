/*
 * @test
 * @summary javax.xml.stream.XMLStreamException conformance: exception hierarchy and constructors.
 * @run main XMLStreamExceptionTest
 */


public class XMLStreamExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        check(javax.xml.stream.XMLStreamException.class.getSuperclass().getName().equals("java.lang.Exception"), "extends java.lang.Exception");
        javax.xml.stream.XMLStreamException xe = new javax.xml.stream.XMLStreamException("msg");
        check(xe.getNestedException() == null, "nested exception null");
        check(xe.getLocation() == null, "location null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

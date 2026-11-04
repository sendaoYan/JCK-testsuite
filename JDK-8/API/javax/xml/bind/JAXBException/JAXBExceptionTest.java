/*
 * @test
 * @summary javax.xml.bind.JAXBException conformance: exception hierarchy and constructors.
 * @run main JAXBExceptionTest
 */


public class JAXBExceptionTest {
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
        check(javax.xml.bind.JAXBException.class.getSuperclass().getName().equals("java.lang.Exception"), "extends java.lang.Exception");
        javax.xml.bind.JAXBException je = new javax.xml.bind.JAXBException("msg", "code");
        check("code".equals(je.getErrorCode()), "error code");
        java.lang.Throwable le = new java.lang.Exception("linked");
        je.setLinkedException(le);
        check(je.getLinkedException() == le, "linked exception");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

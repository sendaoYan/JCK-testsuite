/*
 * @test
 * @summary javax.xml.ws.http.HTTPException conformance: exception hierarchy and constructors.
 * @run main HTTPExceptionTest
 */


public class HTTPExceptionTest {
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
        check(javax.xml.ws.http.HTTPException.class.getSuperclass().getName().equals("javax.xml.ws.ProtocolException"), "extends javax.xml.ws.ProtocolException");
        javax.xml.ws.http.HTTPException he = new javax.xml.ws.http.HTTPException(404);
        check(he.getStatusCode() == 404, "status code");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

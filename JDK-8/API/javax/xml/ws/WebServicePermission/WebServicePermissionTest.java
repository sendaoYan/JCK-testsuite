/*
 * @test
 * @summary javax.xml.ws.WebServicePermission conformance: web service permission name.
 * @run main WebServicePermissionTest
 */


public class WebServicePermissionTest {
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
        javax.xml.ws.WebServicePermission p = new javax.xml.ws.WebServicePermission("publishEndpoint");
        check("publishEndpoint".equals(p.getName()), "name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

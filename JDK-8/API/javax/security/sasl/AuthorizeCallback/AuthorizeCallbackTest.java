/*
 * @test
 * @summary javax.security.sasl.AuthorizeCallback conformance: constructor and getters.
 * @run main AuthorizeCallbackTest
 */

public class AuthorizeCallbackTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.sasl.AuthorizeCallback c = new javax.security.sasl.AuthorizeCallback("authn", "authz");
        check("authn".equals(c.getAuthenticationID()), "getAuthenticationID");
        check("authz".equals(c.getAuthorizationID()), "getAuthorizationID");
        check(!c.isAuthorized(), "isAuthorized false");
        c.setAuthorized(true);
        check(c.isAuthorized(), "setAuthorized");
        c.setAuthorizedID("id");
        check("id".equals(c.getAuthorizedID()), "set/getAuthorizedID");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

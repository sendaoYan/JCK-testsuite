/*
 * @test
 * @summary javax.security.sasl.AuthorizeCallback conformance: SASL authorize callback.
 * @run main AuthorizeCallbackTest
 */

import javax.security.sasl.*;

public class AuthorizeCallbackTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        AuthorizeCallback c = new AuthorizeCallback("authId", "authzId");
        check("authId".equals(c.getAuthenticationID()), "getAuthenticationID");
        check("authzId".equals(c.getAuthorizationID()), "getAuthorizationID");
        check(!c.isAuthorized(), "not authorized initially");
        c.setAuthorized(true);
        c.setAuthorizedID("newId");
        check(c.isAuthorized(), "authorized after set");
        check("newId".equals(c.getAuthorizedID()), "getAuthorizedID");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

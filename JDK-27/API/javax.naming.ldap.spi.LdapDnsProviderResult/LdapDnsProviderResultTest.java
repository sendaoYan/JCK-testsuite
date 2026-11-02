/*
 * @test
 * @summary javax.naming.ldap.spi.LdapDnsProviderResult conformance: API type verification.
 * @run main LdapDnsProviderResultTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class LdapDnsProviderResultTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.ldap.spi.LdapDnsProviderResult r = new javax.naming.ldap.spi.LdapDnsProviderResult("example.com", java.util.List.of("ldap://a:389"));
check(r.getDomainName().equals("example.com"), "domain");
check(r.getEndpoints().equals(java.util.List.of("ldap://a:389")), "endpoints");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

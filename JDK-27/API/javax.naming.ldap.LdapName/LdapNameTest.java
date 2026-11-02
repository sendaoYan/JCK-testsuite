/*
 * @test
 * @summary javax.naming.ldap.LdapName conformance: API type verification.
 * @run main LdapNameTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class LdapNameTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.ldap.LdapName ln = new javax.naming.ldap.LdapName("cn=John,dc=example,dc=com");
check(ln.size() == 3, "size");
check(ln.getRdns().size() == 3, "rdns");
check(ln.get(2).equals("cn=John"), "first component");
check(ln.toString().equals("cn=John,dc=example,dc=com"), "toString");
check(ln instanceof javax.naming.Name, "Name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

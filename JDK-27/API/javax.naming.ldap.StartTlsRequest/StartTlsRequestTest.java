/*
 * @test
 * @summary javax.naming.ldap.StartTlsRequest conformance: API type verification.
 * @run main StartTlsRequestTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class StartTlsRequestTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.ldap.StartTlsRequest r = new javax.naming.ldap.StartTlsRequest();
check(r.getID().equals(javax.naming.ldap.StartTlsRequest.OID), "oid");
check(r instanceof javax.naming.ldap.ExtendedRequest, "ExtendedRequest");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.net.ssl.SSLParameters conformance: API type verification.
 * @run main SSLParametersTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SSLParametersTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.net.ssl.SSLParameters p = new javax.net.ssl.SSLParameters();
p.setNeedClientAuth(true);
check(p.getNeedClientAuth(), "needClientAuth");
p.setWantClientAuth(false);
check(!p.getWantClientAuth(), "wantClientAuth");
p.setProtocols(new String[]{"TLSv1.3"});
check(p.getProtocols().length == 1 && p.getProtocols()[0].equals("TLSv1.3"), "protocols");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

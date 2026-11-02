/*
 * @test
 * @summary javax.net.ssl.SNIHostName conformance: API type verification.
 * @run main SNIHostNameTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SNIHostNameTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.net.ssl.SNIHostName s = new javax.net.ssl.SNIHostName("example.com");
check(s.getAsciiName().equals("example.com"), "ascii");
check(s.getType() == javax.net.ssl.StandardConstants.SNI_HOST_NAME, "type");
check(s instanceof javax.net.ssl.SNIServerName, "SNIServerName");
javax.net.ssl.SNIMatcher m = javax.net.ssl.SNIHostName.createSNIMatcher("example.com");
check(m != null, "matcher");
check(m.matches(s), "matches");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

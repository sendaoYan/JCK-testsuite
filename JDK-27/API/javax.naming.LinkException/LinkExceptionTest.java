/*
 * @test
 * @summary javax.naming.LinkException conformance: API type verification.
 * @run main LinkExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class LinkExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.LinkException e = new javax.naming.LinkException("msg");
check("msg".equals(e.getMessage()), "message");
e.setLinkExplanation("expl");
check("expl".equals(e.getLinkExplanation()), "link explanation");
check(e instanceof javax.naming.NamingException, "NamingException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

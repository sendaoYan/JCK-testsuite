/*
 * @test
 * @summary javax.naming.StringRefAddr conformance: API type verification.
 * @run main StringRefAddrTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class StringRefAddrTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.StringRefAddr ra = new javax.naming.StringRefAddr("type", "value");
check(ra.getType().equals("type"), "type");
check("value".equals(ra.getContent()), "content");
check(ra instanceof javax.naming.RefAddr, "RefAddr");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

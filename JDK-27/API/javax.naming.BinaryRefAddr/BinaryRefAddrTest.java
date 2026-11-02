/*
 * @test
 * @summary javax.naming.BinaryRefAddr conformance: API type verification.
 * @run main BinaryRefAddrTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class BinaryRefAddrTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        byte[] b = new byte[]{1,2,3};
javax.naming.BinaryRefAddr ra = new javax.naming.BinaryRefAddr("type", b);
check(ra.getType().equals("type"), "type");
check(java.util.Arrays.equals((byte[])ra.getContent(), b), "content");
check(ra instanceof javax.naming.RefAddr, "RefAddr");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

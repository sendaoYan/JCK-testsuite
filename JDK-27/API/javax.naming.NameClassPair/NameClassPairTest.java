/*
 * @test
 * @summary javax.naming.NameClassPair conformance: API type verification.
 * @run main NameClassPairTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class NameClassPairTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.NameClassPair ncp = new javax.naming.NameClassPair("name", "java.lang.String", false);
check(ncp.getName().equals("name"), "name");
check(ncp.getClassName().equals("java.lang.String"), "className");
check(!ncp.isRelative(), "absolute");
ncp.setRelative(true);
check(ncp.isRelative(), "relative");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

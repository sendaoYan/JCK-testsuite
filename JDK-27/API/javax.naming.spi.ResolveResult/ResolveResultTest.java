/*
 * @test
 * @summary javax.naming.spi.ResolveResult conformance: API type verification.
 * @run main ResolveResultTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ResolveResultTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.spi.ResolveResult r = new javax.naming.spi.ResolveResult("obj", "a/b");
check("obj".equals(r.getResolvedObj()), "object");
check(r.getRemainingName() != null, "remaining");
check(r.getRemainingName().toString().equals("a/b"), "remaining toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

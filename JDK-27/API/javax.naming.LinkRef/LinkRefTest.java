/*
 * @test
 * @summary javax.naming.LinkRef conformance: API type verification.
 * @run main LinkRefTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class LinkRefTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.LinkRef lr = new javax.naming.LinkRef("target");
check(lr.getLinkName().equals("target"), "link name");
check(lr instanceof javax.naming.Reference, "Reference");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

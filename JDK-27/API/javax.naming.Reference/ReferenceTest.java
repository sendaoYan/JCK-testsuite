/*
 * @test
 * @summary javax.naming.Reference conformance: API type verification.
 * @run main ReferenceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ReferenceTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.Reference r = new javax.naming.Reference("java.lang.String");
check(r.getClassName().equals("java.lang.String"), "className");
check(r.size() == 0, "empty");
r.add(new javax.naming.StringRefAddr("type", "value"));
check(r.size() == 1, "size 1");
check(r.get(0) instanceof javax.naming.RefAddr, "addr");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

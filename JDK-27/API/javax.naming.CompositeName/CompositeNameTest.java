/*
 * @test
 * @summary javax.naming.CompositeName conformance: API type verification.
 * @run main CompositeNameTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class CompositeNameTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.CompositeName cn = new javax.naming.CompositeName("a/b/c");
check(cn.size() == 3, "size");
check(cn.get(0).equals("a"), "get0");
check(cn.getPrefix(1).toString().equals("a"), "prefix");
check(cn.getSuffix(1).toString().equals("b/c"), "suffix");
check(!cn.isEmpty(), "not empty");
check(cn instanceof javax.naming.Name, "Name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

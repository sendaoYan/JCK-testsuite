/*
 * @test
 * @summary javax.naming.CompoundName conformance: API type verification.
 * @run main CompoundNameTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class CompoundNameTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.util.Properties props = new java.util.Properties();
props.setProperty("jndi.syntax.direction", "left_to_right");
props.setProperty("jndi.syntax.separator", ".");
javax.naming.CompoundName cn = new javax.naming.CompoundName("a.b.c", props);
check(cn.size() == 3, "size");
check(cn.get(0).equals("a"), "get0");
check(cn instanceof javax.naming.Name, "Name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

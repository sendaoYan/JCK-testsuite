/*
 * @test
 * @summary javax.naming.Binding conformance: API type verification.
 * @run main BindingTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class BindingTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.Binding b = new javax.naming.Binding("name", "object");
check(b.getName().equals("name"), "name");
check("object".equals(b.getObject()), "object");
check(b instanceof javax.naming.NameClassPair, "NameClassPair");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

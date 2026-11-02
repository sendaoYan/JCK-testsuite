/*
 * @test
 * @summary javax.naming.directory.BasicAttribute conformance: API type verification.
 * @run main BasicAttributeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class BasicAttributeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.directory.BasicAttribute a = new javax.naming.directory.BasicAttribute("cn", "value");
check(a.getID().equals("cn"), "id");
check(a.size() == 1, "size");
check("value".equals(a.get()), "get");
a.add("value2");
check(a.size() == 2, "size 2");
check(a.contains("value"), "contains");
check(a instanceof javax.naming.directory.Attribute, "Attribute");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

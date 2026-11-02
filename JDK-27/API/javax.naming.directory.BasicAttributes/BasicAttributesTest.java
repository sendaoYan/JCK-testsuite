/*
 * @test
 * @summary javax.naming.directory.BasicAttributes conformance: API type verification.
 * @run main BasicAttributesTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class BasicAttributesTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.directory.BasicAttributes as = new javax.naming.directory.BasicAttributes();
check(as.size() == 0, "empty");
as.put("cn", "value");
check(as.size() == 1, "size 1");
check(as.get("cn") != null, "get");
check("value".equals(as.get("cn").get()), "value");
check(as instanceof javax.naming.directory.Attributes, "Attributes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

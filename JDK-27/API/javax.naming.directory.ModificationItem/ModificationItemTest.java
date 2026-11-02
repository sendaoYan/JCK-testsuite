/*
 * @test
 * @summary javax.naming.directory.ModificationItem conformance: API type verification.
 * @run main ModificationItemTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ModificationItemTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.directory.BasicAttribute a = new javax.naming.directory.BasicAttribute("cn", "value");
javax.naming.directory.ModificationItem mi = new javax.naming.directory.ModificationItem(javax.naming.directory.DirContext.REPLACE_ATTRIBUTE, a);
check(mi.getModificationOp() == javax.naming.directory.DirContext.REPLACE_ATTRIBUTE, "op");
check(mi.getAttribute() == a, "attribute");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

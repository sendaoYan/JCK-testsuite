/*
 * @test
 * @summary javax.naming.ldap.BasicControl conformance: API type verification.
 * @run main BasicControlTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class BasicControlTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.ldap.BasicControl c = new javax.naming.ldap.BasicControl("1.2.3", true, new byte[]{1,2});
check(c.getID().equals("1.2.3"), "id");
check(c.isCritical(), "critical");
check(java.util.Arrays.equals(c.getEncodedValue(), new byte[]{1,2}), "encoded");
check(c instanceof javax.naming.ldap.Control, "Control");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

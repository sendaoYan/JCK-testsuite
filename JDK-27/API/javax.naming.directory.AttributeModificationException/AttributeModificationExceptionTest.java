/*
 * @test
 * @summary javax.naming.directory.AttributeModificationException conformance: API type verification.
 * @run main AttributeModificationExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class AttributeModificationExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.directory.AttributeModificationException e = new javax.naming.directory.AttributeModificationException("msg");
check("msg".equals(e.getMessage()), "message");
e.setUnexecutedModifications(new javax.naming.directory.ModificationItem[0]);
check(e.getUnexecutedModifications() != null, "unexecuted");
check(e instanceof javax.naming.NamingException, "NamingException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

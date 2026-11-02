/*
 * @test
 * @summary javax.naming.spi.DirectoryManager conformance: API type verification.
 * @run main DirectoryManagerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class DirectoryManagerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
static boolean hasMethod(Class<?> c, String n) {
    for (java.lang.reflect.Method m : c.getMethods()) {
        if (m.getName().equals(n)) return true;
    }
    return false;
}

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.spi.DirectoryManager");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(c.getSuperclass().getName().equals("javax.naming.spi.NamingManager"), "extends javax.naming.spi.NamingManager");
check(hasMethod(c, "getContinuationDirContext"), "getContinuationDirContext present");
check(hasMethod(c, "getObjectInstance"), "getObjectInstance present");
check(hasMethod(c, "getStateToBind"), "getStateToBind present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

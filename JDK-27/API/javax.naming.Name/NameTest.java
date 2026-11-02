/*
 * @test
 * @summary javax.naming.Name conformance: API type verification.
 * @run main NameTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class NameTest {
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
        Class<?> c = Class.forName("javax.naming.Name");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(c.isInterface(), "is interface");
check(c.getSuperclass() == null, "no superclass");
check(hasMethod(c, "clone"), "clone present");
check(hasMethod(c, "compareTo"), "compareTo present");
check(hasMethod(c, "size"), "size present");
check(hasMethod(c, "isEmpty"), "isEmpty present");
check(hasMethod(c, "getAll"), "getAll present");
check(hasMethod(c, "get"), "get present");
check(hasMethod(c, "getPrefix"), "getPrefix present");
check(hasMethod(c, "getSuffix"), "getSuffix present");
check(hasMethod(c, "startsWith"), "startsWith present");
check(hasMethod(c, "endsWith"), "endsWith present");
check(hasMethod(c, "addAll"), "addAll present");
check(hasMethod(c, "add"), "add present");
check(hasMethod(c, "remove"), "remove present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

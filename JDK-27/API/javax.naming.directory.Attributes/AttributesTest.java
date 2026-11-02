/*
 * @test
 * @summary javax.naming.directory.Attributes conformance: API type verification.
 * @run main AttributesTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class AttributesTest {
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
        Class<?> c = Class.forName("javax.naming.directory.Attributes");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(c.isInterface(), "is interface");
check(c.getSuperclass() == null, "no superclass");
check(hasMethod(c, "isCaseIgnored"), "isCaseIgnored present");
check(hasMethod(c, "size"), "size present");
check(hasMethod(c, "get"), "get present");
check(hasMethod(c, "getAll"), "getAll present");
check(hasMethod(c, "getIDs"), "getIDs present");
check(hasMethod(c, "put"), "put present");
check(hasMethod(c, "remove"), "remove present");
check(hasMethod(c, "clone"), "clone present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

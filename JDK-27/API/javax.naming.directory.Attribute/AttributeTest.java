/*
 * @test
 * @summary javax.naming.directory.Attribute conformance: API type verification.
 * @run main AttributeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class AttributeTest {
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
        Class<?> c = Class.forName("javax.naming.directory.Attribute");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(c.isInterface(), "is interface");
check(c.getSuperclass() == null, "no superclass");
check(hasMethod(c, "getAll"), "getAll present");
check(hasMethod(c, "get"), "get present");
check(hasMethod(c, "size"), "size present");
check(hasMethod(c, "getID"), "getID present");
check(hasMethod(c, "contains"), "contains present");
check(hasMethod(c, "add"), "add present");
check(hasMethod(c, "remove"), "remove present");
check(hasMethod(c, "clear"), "clear present");
check(hasMethod(c, "getAttributeSyntaxDefinition"), "getAttributeSyntaxDefinition present");
check(hasMethod(c, "getAttributeDefinition"), "getAttributeDefinition present");
check(hasMethod(c, "clone"), "clone present");
check(hasMethod(c, "isOrdered"), "isOrdered present");
check(hasMethod(c, "set"), "set present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

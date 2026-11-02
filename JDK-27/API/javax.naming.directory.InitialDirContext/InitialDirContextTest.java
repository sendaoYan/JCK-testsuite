/*
 * @test
 * @summary javax.naming.directory.InitialDirContext conformance: API type verification.
 * @run main InitialDirContextTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class InitialDirContextTest {
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
        Class<?> c = Class.forName("javax.naming.directory.InitialDirContext");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(c.getSuperclass().getName().equals("javax.naming.InitialContext"), "extends javax.naming.InitialContext");
check(hasMethod(c, "getAttributes"), "getAttributes present");
check(hasMethod(c, "modifyAttributes"), "modifyAttributes present");
check(hasMethod(c, "bind"), "bind present");
check(hasMethod(c, "rebind"), "rebind present");
check(hasMethod(c, "createSubcontext"), "createSubcontext present");
check(hasMethod(c, "getSchema"), "getSchema present");
check(hasMethod(c, "getSchemaClassDefinition"), "getSchemaClassDefinition present");
check(hasMethod(c, "search"), "search present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

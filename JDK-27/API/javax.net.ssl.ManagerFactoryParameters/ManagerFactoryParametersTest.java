/*
 * @test
 * @summary javax.net.ssl.ManagerFactoryParameters conformance: API type verification.
 * @run main ManagerFactoryParametersTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ManagerFactoryParametersTest {
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
        Class<?> c = Class.forName("javax.net.ssl.ManagerFactoryParameters");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(c.isInterface(), "is interface");
check(c.getSuperclass() == null, "no superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.naming.spi.ObjectFactoryBuilder conformance: API type verification.
 * @run main ObjectFactoryBuilderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ObjectFactoryBuilderTest {
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
        Class<?> c = Class.forName("javax.naming.spi.ObjectFactoryBuilder");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(c.isInterface(), "is interface");
check(c.getSuperclass() == null, "no superclass");
check(hasMethod(c, "createObjectFactory"), "createObjectFactory present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

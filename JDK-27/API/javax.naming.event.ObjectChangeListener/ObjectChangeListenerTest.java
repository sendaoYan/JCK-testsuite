/*
 * @test
 * @summary javax.naming.event.ObjectChangeListener conformance: API type verification.
 * @run main ObjectChangeListenerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ObjectChangeListenerTest {
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
        Class<?> c = Class.forName("javax.naming.event.ObjectChangeListener");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(c.isInterface(), "is interface");
check(c.getSuperclass() == null, "no superclass");
check(hasMethod(c, "objectChanged"), "objectChanged present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

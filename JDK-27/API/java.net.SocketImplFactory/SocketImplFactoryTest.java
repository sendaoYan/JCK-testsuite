/*
 * @test
 * @summary java.net.SocketImplFactory conformance: API type verification.
 * @run main SocketImplFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SocketImplFactoryTest {
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
        Class<?> c = Class.forName("java.net.SocketImplFactory");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(c.isInterface(), "is interface");
check(c.getSuperclass() == null, "no superclass");
check(hasMethod(c, "createSocketImpl"), "createSocketImpl present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

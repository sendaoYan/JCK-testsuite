/*
 * @test
 * @summary javax.naming.InitialContext conformance: API type verification.
 * @run main InitialContextTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class InitialContextTest {
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
        Class<?> c = Class.forName("javax.naming.InitialContext");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(hasMethod(c, "doLookup"), "doLookup present");
check(hasMethod(c, "lookup"), "lookup present");
check(hasMethod(c, "bind"), "bind present");
check(hasMethod(c, "rebind"), "rebind present");
check(hasMethod(c, "unbind"), "unbind present");
check(hasMethod(c, "rename"), "rename present");
check(hasMethod(c, "list"), "list present");
check(hasMethod(c, "listBindings"), "listBindings present");
check(hasMethod(c, "destroySubcontext"), "destroySubcontext present");
check(hasMethod(c, "createSubcontext"), "createSubcontext present");
check(hasMethod(c, "lookupLink"), "lookupLink present");
check(hasMethod(c, "getNameParser"), "getNameParser present");
check(hasMethod(c, "composeName"), "composeName present");
check(hasMethod(c, "addToEnvironment"), "addToEnvironment present");
check(hasMethod(c, "removeFromEnvironment"), "removeFromEnvironment present");
check(hasMethod(c, "getEnvironment"), "getEnvironment present");
check(hasMethod(c, "close"), "close present");
check(hasMethod(c, "getNameInNamespace"), "getNameInNamespace present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

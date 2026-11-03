/*
 * @test
 * @summary java.security.Policy.Parameters conformance: white-box structure + black-box behavior.
 * @run main ParametersTest
 */

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

public class ParametersTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.Policy$Parameters");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // Marker interface for policy configuration parameters; URIParameter implements it
        Class<?> c = Class.forName("java.security.Policy$Parameters");
        check(c.isInterface(), "interface");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        // URIParameter is the shipped implementation
        java.net.URI uri = java.net.URI.create("file:/tmp/jck-policy.conf");
        URIParameter p = new URIParameter(uri);
        check(p instanceof Policy.Parameters, "implements");
        check(p.getURI().equals(uri), "uri");
        // anonymous implementation is possible (marker interface)
        Policy.Parameters anon = new Policy.Parameters() {};
        check(anon != null, "anonymous impl");
        // usable where login Configuration.Parameters is expected too
        check(p instanceof javax.security.auth.login.Configuration.Parameters, "login Configuration.Parameters");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static void checkInterfaces(Class<?> c, String[] names) {
        for (String n : names) check(hasInterface(c, n), "implements " + n);
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkEnumConstants(Class<?> c, String[] names) {
        Object[] cs = c.getEnumConstants();
        check(cs != null, "enum constants");
        check(cs.length == names.length, "enum constant count");
        for (String n : names) check(hasConst(cs, n), "constant " + n);
    }
    static boolean hasConst(Object[] cs, String name) {
        for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true;
        return false;
    }
}

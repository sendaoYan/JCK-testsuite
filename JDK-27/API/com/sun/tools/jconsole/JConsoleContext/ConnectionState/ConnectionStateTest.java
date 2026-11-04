/*
 * @test
 * @summary com.sun.tools.jconsole.JConsoleContext.ConnectionState conformance: enum constants.
 * @run main ConnectionStateTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ConnectionStateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> fieldNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Field f : c.getDeclaredFields()) s.add(f.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    public static void main(String[] a) throws Exception {
        com.sun.tools.jconsole.JConsoleContext.ConnectionState[] v = com.sun.tools.jconsole.JConsoleContext.ConnectionState.values();
        check(v.length == 3, "3 constants");
        check(com.sun.tools.jconsole.JConsoleContext.ConnectionState.CONNECTED.name().equals("CONNECTED"), "CONNECTED");
        check(com.sun.tools.jconsole.JConsoleContext.ConnectionState.DISCONNECTED.name().equals("DISCONNECTED"), "DISCONNECTED");
        check(com.sun.tools.jconsole.JConsoleContext.ConnectionState.valueOf("CONNECTING") == com.sun.tools.jconsole.JConsoleContext.ConnectionState.CONNECTING, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

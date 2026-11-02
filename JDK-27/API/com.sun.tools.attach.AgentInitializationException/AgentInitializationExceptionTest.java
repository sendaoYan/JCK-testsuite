/*
 * @test
 * @summary com.sun.tools.attach.AgentInitializationException conformance: exception accessors.
 * @run main AgentInitializationExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;

public class AgentInitializationExceptionTest {
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
        com.sun.tools.attach.AgentInitializationException e = new com.sun.tools.attach.AgentInitializationException("msg", 5);
        check(e.getMessage().equals("msg"), "message");
        check(e.returnValue() == 5, "returnValue");
        check(e instanceof java.lang.Exception, "extends Exception");
        new com.sun.tools.attach.AgentInitializationException();
        new com.sun.tools.attach.AgentInitializationException("only msg");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

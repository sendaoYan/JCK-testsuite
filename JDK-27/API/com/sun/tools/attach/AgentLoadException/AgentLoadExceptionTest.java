/*
 * @test
 * @summary com.sun.tools.attach.AgentLoadException conformance: white-box structure + black-box behavior.
 * @run main AgentLoadExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;

public class AgentLoadExceptionTest {
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

    static void testWhiteBox() throws Exception {
        com.sun.tools.attach.AgentLoadException e = new com.sun.tools.attach.AgentLoadException("msg");
        check(e.getMessage().equals("msg"), "message");
        check(e instanceof java.lang.Exception, "extends Exception");
        new com.sun.tools.attach.AgentLoadException();
    }

    static void testBlackBox() throws Exception {
        com.sun.tools.attach.AgentLoadException e = new com.sun.tools.attach.AgentLoadException("load failed");
        check(e.getMessage().equals("load failed"), "message");
        check(e instanceof java.lang.Exception, "extends Exception");
        com.sun.tools.attach.AgentLoadException e0 = new com.sun.tools.attach.AgentLoadException();
        check(e0.getMessage() == null, "default message null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

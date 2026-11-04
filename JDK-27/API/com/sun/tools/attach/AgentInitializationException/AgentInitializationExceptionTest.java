/*
 * @test
 * @summary com.sun.tools.attach.AgentInitializationException conformance: white-box structure + black-box behavior.
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

    static void testWhiteBox() throws Exception {
        com.sun.tools.attach.AgentInitializationException e = new com.sun.tools.attach.AgentInitializationException("msg", 5);
        check(e.getMessage().equals("msg"), "message");
        check(e.returnValue() == 5, "returnValue");
        check(e instanceof java.lang.Exception, "extends Exception");
        new com.sun.tools.attach.AgentInitializationException();
        new com.sun.tools.attach.AgentInitializationException("only msg");
    }

    static void testBlackBox() throws Exception {
        com.sun.tools.attach.AgentInitializationException e = new com.sun.tools.attach.AgentInitializationException("msg", 5);
        check(e.getMessage().equals("msg"), "message");
        check(e.returnValue() == 5, "returnValue");
        com.sun.tools.attach.AgentInitializationException e0 = new com.sun.tools.attach.AgentInitializationException();
        check(e0.returnValue() == 0, "default returnValue 0");
        check(e0.getMessage() == null, "default message null");
        com.sun.tools.attach.AgentInitializationException e1 = new com.sun.tools.attach.AgentInitializationException("only msg");
        check(e1.returnValue() == 0, "returnValue 0");
        check(e1.getMessage().equals("only msg"), "message");
        check(e instanceof java.lang.Exception, "extends Exception");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

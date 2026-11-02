/*
 * @test
 * @summary com.sun.jdi.ClassType conformance: interface hierarchy and methods.
 * @run main ClassTypeTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ClassTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.ClassType");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("com.sun.jdi.ReferenceType"), "extends com.sun.jdi.ReferenceType");
        check(si.size() == 1, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("superclass"), "method superclass");
        check(dm.contains("interfaces"), "method interfaces");
        check(dm.contains("allInterfaces"), "method allInterfaces");
        check(dm.contains("subclasses"), "method subclasses");
        check(dm.contains("isEnum"), "method isEnum");
        check(dm.contains("setValue"), "method setValue");
        check(dm.contains("invokeMethod"), "method invokeMethod");
        check(dm.contains("newInstance"), "method newInstance");
        check(dm.contains("concreteMethodByName"), "method concreteMethodByName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

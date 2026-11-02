/*
 * @test
 * @summary com.sun.java.accessibility.util.EventID conformance: event-type constants.
 * @run main EventIDTest
 */

import java.lang.reflect.*;
import java.util.*;

public class EventIDTest {
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
        Class<?> c = load("com.sun.java.accessibility.util.EventID");
        check(!c.isInterface(), "not an interface");
        Set<String> f = fieldNames(c);
        String[] names = {"ACTION","ADJUSTMENT","COMPONENT","CONTAINER","FOCUS","ITEM","KEY","MOUSE","MOTION","TEXT","WINDOW","PROPERTYCHANGE"};
        for (String n : names) check(f.contains(n), "field " + n);
        check((Integer) c.getField("ACTION").get(null) >= 0, "ACTION value int");
        new com.sun.java.accessibility.util.EventID();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

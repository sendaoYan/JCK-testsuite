/*
 * @test
 * @summary com.sun.jdi.connect.Connector conformance: interface hierarchy and methods.
 * @run main ConnectorTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ConnectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.connect.Connector");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.size() == 0, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("name"), "method name");
        check(dm.contains("description"), "method description");
        check(dm.contains("transport"), "method transport");
        check(dm.contains("defaultArguments"), "method defaultArguments");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

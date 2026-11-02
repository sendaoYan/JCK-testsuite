/*
 * @test
 * @summary com.sun.jdi.connect.ListeningConnector conformance: interface hierarchy and methods.
 * @run main ListeningConnectorTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ListeningConnectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.connect.ListeningConnector");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("com.sun.jdi.connect.Connector"), "extends com.sun.jdi.connect.Connector");
        check(si.size() == 1, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("supportsMultipleConnections"), "method supportsMultipleConnections");
        check(dm.contains("startListening"), "method startListening");
        check(dm.contains("stopListening"), "method stopListening");
        check(dm.contains("accept"), "method accept");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

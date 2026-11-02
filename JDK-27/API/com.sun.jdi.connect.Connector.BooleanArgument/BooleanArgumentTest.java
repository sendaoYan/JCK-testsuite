/*
 * @test
 * @summary com.sun.jdi.connect.Connector.BooleanArgument conformance: interface hierarchy and methods.
 * @run main BooleanArgumentTest
 */

import java.lang.reflect.*;
import java.util.*;

public class BooleanArgumentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.connect.Connector$BooleanArgument");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("com.sun.jdi.connect.Connector$Argument"), "extends com.sun.jdi.connect.Connector$Argument");
        check(si.size() == 1, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("setValue"), "method setValue");
        check(dm.contains("isValid"), "method isValid");
        check(dm.contains("stringValueOf"), "method stringValueOf");
        check(dm.contains("booleanValue"), "method booleanValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

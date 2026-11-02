/*
 * @test
 * @summary com.sun.jdi.PrimitiveValue conformance: interface hierarchy and methods.
 * @run main PrimitiveValueTest
 */

import java.lang.reflect.*;
import java.util.*;

public class PrimitiveValueTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.PrimitiveValue");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("com.sun.jdi.Value"), "extends com.sun.jdi.Value");
        check(si.size() == 1, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("booleanValue"), "method booleanValue");
        check(dm.contains("byteValue"), "method byteValue");
        check(dm.contains("charValue"), "method charValue");
        check(dm.contains("shortValue"), "method shortValue");
        check(dm.contains("intValue"), "method intValue");
        check(dm.contains("longValue"), "method longValue");
        check(dm.contains("floatValue"), "method floatValue");
        check(dm.contains("doubleValue"), "method doubleValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

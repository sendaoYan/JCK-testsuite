/*
 * @test
 * @summary com.sun.jdi.StackFrame conformance: interface hierarchy and methods.
 * @run main StackFrameTest
 */

import java.lang.reflect.*;
import java.util.*;

public class StackFrameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.StackFrame");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("com.sun.jdi.Mirror"), "extends com.sun.jdi.Mirror");
        check(si.contains("com.sun.jdi.Locatable"), "extends com.sun.jdi.Locatable");
        check(si.size() == 2, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("location"), "method location");
        check(dm.contains("thread"), "method thread");
        check(dm.contains("thisObject"), "method thisObject");
        check(dm.contains("visibleVariables"), "method visibleVariables");
        check(dm.contains("visibleVariableByName"), "method visibleVariableByName");
        check(dm.contains("getValue"), "method getValue");
        check(dm.contains("getValues"), "method getValues");
        check(dm.contains("setValue"), "method setValue");
        check(dm.contains("getArgumentValues"), "method getArgumentValues");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

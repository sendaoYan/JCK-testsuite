/*
 * @test
 * @summary com.sun.jdi.Method conformance: interface hierarchy and methods.
 * @run main MethodTest
 */

import java.lang.reflect.*;
import java.util.*;

public class MethodTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.Method");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("com.sun.jdi.TypeComponent"), "extends com.sun.jdi.TypeComponent");
        check(si.contains("com.sun.jdi.Locatable"), "extends com.sun.jdi.Locatable");
        check(si.contains("java.lang.Comparable"), "extends java.lang.Comparable");
        check(si.size() == 3, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("returnTypeName"), "method returnTypeName");
        check(dm.contains("returnType"), "method returnType");
        check(dm.contains("argumentTypeNames"), "method argumentTypeNames");
        check(dm.contains("argumentTypes"), "method argumentTypes");
        check(dm.contains("isAbstract"), "method isAbstract");
        check(dm.contains("isDefault"), "method isDefault");
        check(dm.contains("isSynchronized"), "method isSynchronized");
        check(dm.contains("isNative"), "method isNative");
        check(dm.contains("isVarArgs"), "method isVarArgs");
        check(dm.contains("isBridge"), "method isBridge");
        check(dm.contains("isConstructor"), "method isConstructor");
        check(dm.contains("isStaticInitializer"), "method isStaticInitializer");
        check(dm.contains("isObsolete"), "method isObsolete");
        check(dm.contains("allLineLocations"), "method allLineLocations");
        check(dm.contains("locationsOfLine"), "method locationsOfLine");
        check(dm.contains("locationOfCodeIndex"), "method locationOfCodeIndex");
        check(dm.contains("variables"), "method variables");
        check(dm.contains("variablesByName"), "method variablesByName");
        check(dm.contains("arguments"), "method arguments");
        check(dm.contains("bytecodes"), "method bytecodes");
        check(dm.contains("location"), "method location");
        check(dm.contains("equals"), "method equals");
        check(dm.contains("hashCode"), "method hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

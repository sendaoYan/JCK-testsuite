/*
 * @test
 * @summary com.sun.jdi.ReferenceType conformance: interface hierarchy and methods.
 * @run main ReferenceTypeTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ReferenceTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.ReferenceType");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("com.sun.jdi.Type"), "extends com.sun.jdi.Type");
        check(si.contains("java.lang.Comparable"), "extends java.lang.Comparable");
        check(si.contains("com.sun.jdi.Accessible"), "extends com.sun.jdi.Accessible");
        check(si.size() == 3, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("name"), "method name");
        check(dm.contains("genericSignature"), "method genericSignature");
        check(dm.contains("classLoader"), "method classLoader");
        check(dm.contains("module"), "method module");
        check(dm.contains("sourceName"), "method sourceName");
        check(dm.contains("sourceNames"), "method sourceNames");
        check(dm.contains("sourcePaths"), "method sourcePaths");
        check(dm.contains("sourceDebugExtension"), "method sourceDebugExtension");
        check(dm.contains("isStatic"), "method isStatic");
        check(dm.contains("isAbstract"), "method isAbstract");
        check(dm.contains("isFinal"), "method isFinal");
        check(dm.contains("isPrepared"), "method isPrepared");
        check(dm.contains("isVerified"), "method isVerified");
        check(dm.contains("isInitialized"), "method isInitialized");
        check(dm.contains("failedToInitialize"), "method failedToInitialize");
        check(dm.contains("fields"), "method fields");
        check(dm.contains("visibleFields"), "method visibleFields");
        check(dm.contains("allFields"), "method allFields");
        check(dm.contains("fieldByName"), "method fieldByName");
        check(dm.contains("methods"), "method methods");
        check(dm.contains("visibleMethods"), "method visibleMethods");
        check(dm.contains("allMethods"), "method allMethods");
        check(dm.contains("methodsByName"), "method methodsByName");
        check(dm.contains("nestedTypes"), "method nestedTypes");
        check(dm.contains("getValue"), "method getValue");
        check(dm.contains("getValues"), "method getValues");
        check(dm.contains("classObject"), "method classObject");
        check(dm.contains("allLineLocations"), "method allLineLocations");
        check(dm.contains("locationsOfLine"), "method locationsOfLine");
        check(dm.contains("availableStrata"), "method availableStrata");
        check(dm.contains("defaultStratum"), "method defaultStratum");
        check(dm.contains("instances"), "method instances");
        check(dm.contains("equals"), "method equals");
        check(dm.contains("hashCode"), "method hashCode");
        check(dm.contains("majorVersion"), "method majorVersion");
        check(dm.contains("minorVersion"), "method minorVersion");
        check(dm.contains("constantPoolCount"), "method constantPoolCount");
        check(dm.contains("constantPool"), "method constantPool");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

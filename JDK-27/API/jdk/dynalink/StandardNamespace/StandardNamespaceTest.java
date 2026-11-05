/*
 * @test
 * @summary jdk.dynalink.StandardNamespace conformance: white-box structure + black-box behavior.
 * @run main StandardNamespaceTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class StandardNamespaceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.StandardNamespace");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isEnum(), "is enum");
        check(hasMethod(c, "values"), "values");
        check(hasMethod(c, "valueOf"), "valueOf");
        check(hasMethod(c, "findFirst"), "findFirst");

    }

    static void testBlackBox() throws Exception {
        check(StandardNamespace.METHOD != null, "METHOD");
        check(StandardNamespace.PROPERTY != null, "PROPERTY");
        check(StandardNamespace.ELEMENT != null, "ELEMENT");
        check(StandardNamespace.values().length == 3, "3 constants");
        NamespaceOperation op = new NamespaceOperation(StandardOperation.GET, StandardNamespace.PROPERTY);
        check(StandardNamespace.findFirst(op) == StandardNamespace.PROPERTY, "findFirst");
        check(StandardNamespace.valueOf("METHOD") == StandardNamespace.METHOD, "valueOf");

    }


    static void testDeepContract() {
        List<StandardNamespace> vals = List.of(StandardNamespace.values());
        check(vals.equals(List.of(StandardNamespace.PROPERTY, StandardNamespace.ELEMENT, StandardNamespace.METHOD)),
              "values in canonical order: " + vals);
        check(StandardNamespace.valueOf("PROPERTY") == StandardNamespace.PROPERTY, "valueOf");
        check(StandardNamespace.PROPERTY.toString().equals("PROPERTY"), "toString is the name");
        // findFirst walks the operation namespaces in order
        Operation multi = StandardOperation.GET.withNamespaces(StandardNamespace.ELEMENT, StandardNamespace.PROPERTY);
        check(StandardNamespace.findFirst(multi) == StandardNamespace.ELEMENT, "findFirst picks the first namespace");
        check(StandardNamespace.findFirst(StandardOperation.GET.withNamespace(StandardNamespace.METHOD)) == StandardNamespace.METHOD,
              "findFirst single");
        check(StandardNamespace.findFirst(StandardOperation.GET.withNamespace(StandardNamespace.METHOD).named("m")) == StandardNamespace.METHOD,
              "findFirst unwraps a named operation");
        check(StandardNamespace.findFirst(StandardOperation.GET) == null, "findFirst on a bare operation is null");
        check(StandardNamespace.findFirst(null) == null, "findFirst(null) is null");
        // namespaces compose into operation toString
        check(StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).toString().equals("GET:PROPERTY"), "GET:PROPERTY");
        check(StandardOperation.SET.withNamespace(StandardNamespace.ELEMENT).named("i").toString().equals("SET:ELEMENT:i"),
              "SET:ELEMENT:i");
        // each standard namespace is a Namespace
        for (StandardNamespace ns : StandardNamespace.values()) check(ns instanceof Namespace, ns + " is a Namespace");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

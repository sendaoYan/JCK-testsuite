/*
 * @test
 * @summary jdk.dynalink.NamespaceOperation conformance: white-box structure + black-box behavior.
 * @run main NamespaceOperationTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class NamespaceOperationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.NamespaceOperation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Operation.class.isAssignableFrom(c), "implements Operation");
        check(hasMethod(c, "getBaseOperation"), "getBaseOperation");
        check(hasMethod(c, "getNamespaces"), "getNamespaces");
        check(hasMethod(c, "contains"), "contains");
        check(hasMethod(c, "getNamespaceCount"), "getNamespaceCount");

    }

    static void testBlackBox() throws Exception {
        NamespaceOperation op = new NamespaceOperation(StandardOperation.GET, StandardNamespace.PROPERTY, StandardNamespace.ELEMENT);
        check(op.getBaseOperation() == StandardOperation.GET, "base op");
        check(op.getNamespaceCount() == 2, "count");
        check(op.getNamespaces().length == 2, "namespaces length");
        check(op.contains(StandardNamespace.PROPERTY), "contains PROPERTY");
        check(op.contains(StandardNamespace.ELEMENT), "contains ELEMENT");
        check(!op.contains(StandardNamespace.METHOD), "not contains METHOD");
        check(NamespaceOperation.getBaseOperation(op) == StandardOperation.GET, "static getBaseOperation");

    }


    static void testDeepContract() {
        NamespaceOperation op = StandardOperation.GET.withNamespaces(StandardNamespace.ELEMENT, StandardNamespace.PROPERTY);
        check(op.getBaseOperation() == StandardOperation.GET, "base kept by identity");
        check(op.getNamespaceCount() == 2, "namespace count");
        check(op.getNamespace(0) == StandardNamespace.ELEMENT && op.getNamespace(1) == StandardNamespace.PROPERTY,
              "namespaces in declaration order");
        check(op.contains(StandardNamespace.ELEMENT) && op.contains(StandardNamespace.PROPERTY), "contains");
        check(!op.contains(StandardNamespace.METHOD), "does not contain absent namespace");
        check(op.toString().equals("GET:ELEMENT|PROPERTY"), "multi toString: " + op);
        check(op.named("q").toString().equals("GET:ELEMENT|PROPERTY:q"), "named on namespace op: " + op.named("q"));
        // equality follows base + namespace set (order and content)
        NamespaceOperation same = StandardOperation.GET.withNamespaces(StandardNamespace.ELEMENT, StandardNamespace.PROPERTY);
        check(op.equals(same) && op.hashCode() == same.hashCode(), "equal operations");
        NamespaceOperation swapped = StandardOperation.GET.withNamespaces(StandardNamespace.PROPERTY, StandardNamespace.ELEMENT);
        check(!op.equals(swapped), "order matters for equality");
        NamespaceOperation fewer = StandardOperation.GET.withNamespace(StandardNamespace.ELEMENT);
        check(!op.equals(fewer), "different count not equal");
        // the namespaces array is defensively copied in both directions
        Namespace[] arr = { StandardNamespace.PROPERTY };
        NamespaceOperation one = new NamespaceOperation(StandardOperation.GET, arr);
        arr[0] = StandardNamespace.ELEMENT;
        check(one.getNamespaces()[0] == StandardNamespace.PROPERTY, "ctor copies the array");
        one.getNamespaces()[0] = StandardNamespace.METHOD;
        check(one.contains(StandardNamespace.PROPERTY), "getNamespaces returns a copy");
        // static helpers
        check(NamespaceOperation.getBaseOperation(StandardOperation.GET) == StandardOperation.GET, "static base on bare op");
        check(NamespaceOperation.getNamespaces(StandardOperation.GET).length == 0, "static namespaces on bare op");
        check(NamespaceOperation.getBaseOperation(op) == StandardOperation.GET, "static base unwraps");
        check(NamespaceOperation.contains(op, StandardOperation.GET, StandardNamespace.PROPERTY), "static contains true");
        check(!NamespaceOperation.contains(op, StandardOperation.GET, StandardNamespace.METHOD), "static contains false");
        // constructor validation table
        try { new NamespaceOperation(null, StandardNamespace.PROPERTY); check(false, "null base"); }
        catch (NullPointerException expected) { check(true, "null base NPE"); }
        try { new NamespaceOperation(StandardOperation.GET.named("x"), StandardNamespace.PROPERTY); check(false, "named base"); }
        catch (IllegalArgumentException expected) { check(true, "named base IAE"); }
        try { new NamespaceOperation(StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY), StandardNamespace.ELEMENT); check(false, "ns base"); }
        catch (IllegalArgumentException expected) { check(true, "ns base IAE"); }
        try { new NamespaceOperation(StandardOperation.GET); check(false, "no namespaces"); }
        catch (IllegalArgumentException expected) { check(true, "no namespaces IAE"); }
        try { new NamespaceOperation(StandardOperation.GET, new Namespace[]{ null }); check(false, "null element"); }
        catch (NullPointerException expected) { check(true, "null element NPE"); }
        try { new NamespaceOperation(StandardOperation.GET, (Namespace[]) null); check(false, "null array"); }
        catch (NullPointerException expected) { check(true, "null array NPE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

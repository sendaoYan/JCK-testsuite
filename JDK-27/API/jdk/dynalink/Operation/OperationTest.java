/*
 * @test
 * @summary jdk.dynalink.Operation conformance: white-box structure + black-box behavior.
 * @run main OperationTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class OperationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.Operation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "withNamespace"), "withNamespace");
        check(hasMethod(c, "withNamespaces"), "withNamespaces");
        check(hasMethod(c, "named"), "named");

    }

    static void testBlackBox() throws Exception {
        Operation op = StandardOperation.GET;
        NamespaceOperation ns = op.withNamespace(StandardNamespace.PROPERTY);
        check(ns.getBaseOperation() == StandardOperation.GET, "withNamespace");
        NamespaceOperation ns2 = op.withNamespaces(StandardNamespace.PROPERTY, StandardNamespace.ELEMENT);
        check(ns2.getNamespaceCount() == 2, "withNamespaces");
        NamedOperation named = op.named("foo");
        check(named.getName().equals("foo"), "named");

    }


    static void testDeepContract() {
        // the three default methods build the wrapper types
        check(StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY) instanceof NamespaceOperation, "withNamespace");
        NamespaceOperation multi = StandardOperation.GET.withNamespaces(StandardNamespace.ELEMENT, StandardNamespace.PROPERTY);
        check(multi.getNamespaceCount() == 2, "withNamespaces varargs");
        check(multi.toString().equals("GET:ELEMENT|PROPERTY"), "multi toString: " + multi);
        check(StandardOperation.GET.named("x") instanceof NamedOperation, "named");
        check(StandardOperation.GET.toString().equals("GET"), "standard toString is the name");
        // validation of the default methods
        try { StandardOperation.GET.withNamespace(null); check(false, "withNamespace(null)"); }
        catch (NullPointerException expected) { check(true, "withNamespace(null) NPE"); }
        try { StandardOperation.GET.withNamespaces(StandardNamespace.PROPERTY, null); check(false, "withNamespaces(.., null)"); }
        catch (NullPointerException expected) { check(true, "withNamespaces(.., null) NPE"); }
        try { StandardOperation.GET.named(null); check(false, "named(null)"); }
        catch (NullPointerException expected) { check(true, "named(null) NPE"); }
        try { StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).withNamespace(StandardNamespace.ELEMENT); check(false, "ns on ns"); }
        catch (IllegalArgumentException expected) { check(true, "ns on ns IAE"); }
        try { StandardOperation.GET.named("a").named("b"); check(false, "named on named"); }
        catch (IllegalArgumentException expected) { check(true, "named on named IAE"); }
        // Operation is trivially implementable: an enum, a lambda-free class, anything
        Operation custom = new Operation() { public String toString() { return "CUSTOM"; } };
        check(custom.withNamespace(StandardNamespace.PROPERTY).getBaseOperation() == custom, "custom base kept");
        check(custom.named("k").getName().equals("k"), "custom named works");
        check(custom.withNamespace(StandardNamespace.PROPERTY).toString().equals("CUSTOM:PROPERTY"), "custom toString");
        // the wrappers are Operations themselves
        check(multi instanceof Operation && StandardOperation.GET.named("x") instanceof Operation, "wrappers are Operations");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

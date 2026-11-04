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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

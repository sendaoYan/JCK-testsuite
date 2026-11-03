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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

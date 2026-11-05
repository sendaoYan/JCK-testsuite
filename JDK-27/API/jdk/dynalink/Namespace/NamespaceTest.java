/*
 * @test
 * @summary jdk.dynalink.Namespace conformance: interface reflection.
 * @run main NamespaceTest
 */

import jdk.dynalink.*;
import java.lang.reflect.*;

public class NamespaceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testDeepContract() {
        check(Namespace.class.getDeclaredMethods().length == 0, "Namespace is a pure marker interface");
        check(StandardNamespace.PROPERTY instanceof Namespace, "StandardNamespace implements Namespace");
        // a hand-written namespace works wherever a standard one does
        Namespace custom = new Namespace() { public String toString() { return "NSX"; } };
        NamespaceOperation op = StandardOperation.GET.withNamespaces(StandardNamespace.PROPERTY, custom);
        check(op.contains(custom), "custom namespace usable in an operation");
        check(op.getNamespace(1) == custom, "custom namespace retrieved by index");
        check(op.toString().startsWith("GET:PROPERTY|"), "multi-namespace toString keeps order: " + op);
        // custom namespaces compare by identity
        Namespace other = new Namespace() { public String toString() { return "NSX"; } };
        check(op.contains(other) == false, "namespace equality is identity-based");
        check(NamespaceOperation.contains(op, StandardOperation.GET, custom), "static contains with custom namespace");
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.Namespace");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

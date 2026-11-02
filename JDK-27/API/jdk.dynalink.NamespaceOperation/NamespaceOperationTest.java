/*
 * @test
 * @summary jdk.dynalink.NamespaceOperation conformance: namespace operation.
 * @run main NamespaceOperationTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.util.*;

public class NamespaceOperationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        NamespaceOperation op = new NamespaceOperation(StandardOperation.GET, StandardNamespace.PROPERTY, StandardNamespace.ELEMENT);
        check(op.getBaseOperation() == StandardOperation.GET, "base op");
        check(op.getNamespaceCount() == 2, "count");
        check(op.getNamespaces().length == 2, "namespaces length");
        check(op.contains(StandardNamespace.PROPERTY), "contains PROPERTY");
        check(NamespaceOperation.getBaseOperation(op) == StandardOperation.GET, "static getBaseOperation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary jdk.dynalink.StandardNamespace conformance: namespace enum constants.
 * @run main StandardNamespaceTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.util.*;

public class StandardNamespaceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check(StandardNamespace.METHOD != null, "METHOD");
        check(StandardNamespace.PROPERTY != null, "PROPERTY");
        check(StandardNamespace.ELEMENT != null, "ELEMENT");
        check(StandardNamespace.values().length == 3, "3 constants");
        NamespaceOperation op = new NamespaceOperation(StandardOperation.GET, StandardNamespace.PROPERTY);
        check(StandardNamespace.findFirst(op) == StandardNamespace.PROPERTY, "findFirst");
        check(StandardNamespace.valueOf("METHOD") == StandardNamespace.METHOD, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

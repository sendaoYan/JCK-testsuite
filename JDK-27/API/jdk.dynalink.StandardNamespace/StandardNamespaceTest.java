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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary jdk.dynalink.NamedOperation conformance: white-box structure + black-box behavior.
 * @run main NamedOperationTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class NamedOperationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.NamedOperation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Operation.class.isAssignableFrom(c), "implements Operation");
        check(hasMethod(c, "getBaseOperation"), "getBaseOperation");
        check(hasMethod(c, "getName"), "getName");
        check(hasMethod(c, "changeName"), "changeName");

    }

    static void testBlackBox() throws Exception {
        NamedOperation op = new NamedOperation(StandardOperation.GET, "name");
        check(op.getBaseOperation() == StandardOperation.GET, "base op");
        check(op.getName().equals("name"), "name");
        check(op.changeName("x").getName().equals("x"), "changeName");
        check(NamedOperation.getBaseOperation(op) == StandardOperation.GET, "static getBaseOperation");
        check(NamedOperation.getName(op).equals("name"), "static getName");

    }


    static void testDeepContract() {
        NamedOperation n = StandardOperation.GET.named("x");
        check(n.getBaseOperation() == StandardOperation.GET, "base kept");
        check(n.getName().equals("x"), "name kept");
        check(n.toString().equals("GET:x"), "toString: " + n);
        // the name is an arbitrary Object, not only a String
        NamedOperation ne = StandardOperation.GET.named(StandardNamespace.PROPERTY);
        check(ne.getName() == StandardNamespace.PROPERTY, "non-String name object kept by identity");
        NamedOperation ni = StandardOperation.GET.named(Integer.valueOf(42));
        check(ni.getName().equals(42), "Integer name kept");
        check(ni.toString().equals("GET:42"), "Integer name toString: " + ni);
        // equals / hashCode follow base + name
        check(StandardOperation.GET.named("x").equals(n), "equals on equal components");
        check(StandardOperation.GET.named("x").hashCode() == n.hashCode(), "hashCode follows equals");
        check(!StandardOperation.GET.named("y").equals(n), "different name not equal");
        check(!StandardOperation.SET.named("x").equals(n), "different base not equal");
        check(!n.equals(StandardOperation.GET), "never equals the bare operation");
        // changeName produces a new operation with the same base
        NamedOperation cn = n.changeName("y");
        check(cn.getBaseOperation() == StandardOperation.GET, "changeName keeps base");
        check(cn.getName().equals("y"), "changeName sets name");
        check(cn.toString().equals("GET:y"), "changeName toString");
        check(n.getName().equals("x"), "changeName immutable on original");
        // static unwrap helpers on a bare operation
        check(NamedOperation.getBaseOperation(StandardOperation.GET) == StandardOperation.GET, "static getBaseOperation on bare op");
        check(NamedOperation.getName(StandardOperation.GET) == null, "static getName on bare op is null");
        check(NamedOperation.getBaseOperation(n) == StandardOperation.GET, "static getBaseOperation unwraps");
        check(NamedOperation.getName(n).equals("x"), "static getName unwraps");
        // wrapping rules
        try { StandardOperation.GET.named(null); check(false, "null name must throw"); }
        catch (NullPointerException expected) { check(true, "null name NPE"); }
        try { new NamedOperation(StandardOperation.GET.named("a"), "b"); check(false, "named base must throw"); }
        catch (IllegalArgumentException expected) { check(true, "named base IAE"); }
        try { new NamedOperation(null, "n"); check(false, "null base must throw"); }
        catch (NullPointerException expected) { check(true, "null base NPE"); }
        // a NamedOperation is itself an Operation
        check(n instanceof Operation, "implements Operation");
        try { n.named("z"); check(false, "named on named must throw"); }
        catch (IllegalArgumentException expected) { check(true, "named on named IAE"); }
        check(StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("f").toString().equals("GET:PROPERTY:f"),
              "namespace-then-name toString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

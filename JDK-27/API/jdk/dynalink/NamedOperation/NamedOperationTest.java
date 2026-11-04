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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

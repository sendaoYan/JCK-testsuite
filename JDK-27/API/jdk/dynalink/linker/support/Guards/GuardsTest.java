/*
 * @test
 * @summary jdk.dynalink.linker.support.Guards conformance: white-box structure + black-box behavior.
 * @run main GuardsTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class GuardsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.support.Guards");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "isNull"), "isNull");
        check(hasMethod(c, "isNotNull"), "isNotNull");
        check(hasMethod(c, "getInstanceOfGuard"), "getInstanceOfGuard");
        check(hasMethod(c, "getIdentityGuard"), "getIdentityGuard");
        check(hasMethod(c, "isOfClass"), "isOfClass");
        check(hasMethod(c, "getClassGuard"), "getClassGuard");
        check(hasMethod(c, "isInstance"), "isInstance");

    }

    static void testBlackBox() throws Exception {
        check(Guards.isNull() != null, "isNull");
        check(Guards.isNotNull() != null, "isNotNull");
        check(Guards.getInstanceOfGuard(String.class) != null, "instanceof guard");
        check(Guards.getIdentityGuard("x") != null, "identity guard");
        check(Guards.isOfClass(String.class, MethodType.methodType(boolean.class, Object.class)) != null, "isOfClass");
        check(Guards.getClassGuard(String.class) != null, "class guard");
        check(Guards.isInstance(String.class, MethodType.methodType(boolean.class, Object.class)) != null, "isInstance");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

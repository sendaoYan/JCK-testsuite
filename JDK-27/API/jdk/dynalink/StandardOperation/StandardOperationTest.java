/*
 * @test
 * @summary jdk.dynalink.StandardOperation conformance: white-box structure + black-box behavior.
 * @run main StandardOperationTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class StandardOperationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.StandardOperation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isEnum(), "is enum");
        check(Operation.class.isAssignableFrom(c), "implements Operation");
        check(hasMethod(c, "values"), "values");
        check(hasMethod(c, "valueOf"), "valueOf");

    }

    static void testBlackBox() throws Exception {
        check(StandardOperation.GET != null, "GET");
        check(StandardOperation.SET != null, "SET");
        check(StandardOperation.REMOVE != null, "REMOVE");
        check(StandardOperation.CALL != null, "CALL");
        check(StandardOperation.NEW != null, "NEW");
        check(StandardOperation.values().length == 5, "5 constants");
        check(StandardOperation.valueOf("CALL") == StandardOperation.CALL, "valueOf");
        NamedOperation named = StandardOperation.GET.named("x");
        check(named.getName().equals("x"), "named via Operation");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

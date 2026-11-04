/*
 * @test
 * @summary jdk.dynalink.linker.support.TypeUtilities conformance: white-box structure + black-box behavior.
 * @run main TypeUtilitiesTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class TypeUtilitiesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.support.TypeUtilities");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "isSubtype"), "isSubtype");
        check(hasMethod(c, "getWrapperType"), "getWrapperType");
        check(hasMethod(c, "getPrimitiveTypeByName"), "getPrimitiveTypeByName");
        check(hasMethod(c, "isWrapperType"), "isWrapperType");
        check(hasMethod(c, "getPrimitiveType"), "getPrimitiveType");
        check(hasMethod(c, "isMethodInvocationConvertible"), "isMethodInvocationConvertible");

    }

    static void testBlackBox() throws Exception {
        check(TypeUtilities.isSubtype(String.class, Object.class), "isSubtype");
        check(TypeUtilities.getWrapperType(int.class) == Integer.class, "wrapper type");
        check(TypeUtilities.getPrimitiveTypeByName("int") == int.class, "primitive by name");
        check(TypeUtilities.isWrapperType(Integer.class), "isWrapperType");
        check(TypeUtilities.getPrimitiveType(Integer.class) == int.class, "primitive type");
        check(TypeUtilities.isMethodInvocationConvertible(int.class, Integer.class), "method invocation convertible");
        check(TypeUtilities.isConvertibleWithoutLoss(int.class, long.class), "convertible without loss");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

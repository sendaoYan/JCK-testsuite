/*
 * @test
 * @summary jdk.dynalink.beans.StaticClass conformance: white-box structure + black-box behavior.
 * @run main StaticClassTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class StaticClassTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.beans.StaticClass");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(hasMethod(c, "forClass"), "forClass");
        check(hasMethod(c, "getRepresentedClass"), "getRepresentedClass");

    }

    static void testBlackBox() throws Exception {
        StaticClass sc = StaticClass.forClass(String.class);
        check(sc.getRepresentedClass() == String.class, "represented class");
        check(sc.toString().contains("String"), "toString");
        check(sc.equals(StaticClass.forClass(String.class)), "equals");
        check(sc.hashCode() == StaticClass.forClass(String.class).hashCode(), "hashCode");
        check(StaticClass.forClass(Integer.class).getRepresentedClass() == Integer.class, "second class");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

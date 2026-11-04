/*
 * @test
 * @summary jdk.dynalink.beans.BeansLinker conformance: white-box structure + black-box behavior.
 * @run main BeansLinkerTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class BeansLinkerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.beans.BeansLinker");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(GuardingDynamicLinker.class.isAssignableFrom(c), "implements GuardingDynamicLinker");
        check(hasMethod(c, "getLinkerForClass"), "getLinkerForClass");
        check(hasMethod(c, "getGuardedInvocation"), "getGuardedInvocation");
        check(hasMethod(c, "getInstanceMethodNames"), "getInstanceMethodNames");
        check(hasMethod(c, "getStaticMethodNames"), "getStaticMethodNames");

    }

    static void testBlackBox() throws Exception {
        BeansLinker b = new BeansLinker();
        check(b != null, "constructed");
        check(b.getLinkerForClass(String.class) != null, "getLinkerForClass");
        check(BeansLinker.getInstanceMethodNames(String.class).contains("length"), "instance method names");
        check(BeansLinker.getStaticMethodNames(Math.class).contains("abs"), "static method names");
        check(!BeansLinker.isDynamicMethod(b), "isDynamicMethod false");
        check(!BeansLinker.isDynamicConstructor(b), "isDynamicConstructor false");
        check(BeansLinker.getReadableInstancePropertyNames(String.class) != null, "property names");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

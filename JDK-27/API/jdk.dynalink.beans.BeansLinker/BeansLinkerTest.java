/*
 * @test
 * @summary jdk.dynalink.beans.BeansLinker conformance: create and inspect bean linker methods.
 * @run main BeansLinkerTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.util.*;

public class BeansLinkerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        BeansLinker b = new BeansLinker();
        check(b != null, "constructed");
        check(b.getLinkerForClass(String.class) != null, "getLinkerForClass");
        check(BeansLinker.getInstanceMethodNames(String.class).contains("length"), "instance method names");
        check(BeansLinker.getStaticMethodNames(Math.class).contains("abs"), "static method names");
        check(!BeansLinker.isDynamicMethod(b), "isDynamicMethod false");
        check(!BeansLinker.isDynamicConstructor(b), "isDynamicConstructor false");
        check(BeansLinker.getReadableInstancePropertyNames(String.class) != null, "property names");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

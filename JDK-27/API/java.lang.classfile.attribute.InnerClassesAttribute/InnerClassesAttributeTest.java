/*
 * @test
 * @summary java.lang.classfile.attribute.InnerClassesAttribute conformance: behavioral.
 * @run main InnerClassesAttributeTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class InnerClassesAttributeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        InnerClassInfo i = InnerClassInfo.of(ClassDesc.of("pkg.C"), Optional.empty(), Optional.of("C"), AccessFlag.PUBLIC);
        InnerClassesAttribute a = InnerClassesAttribute.of(i);
        check(a.classes().size() == 1, "classes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

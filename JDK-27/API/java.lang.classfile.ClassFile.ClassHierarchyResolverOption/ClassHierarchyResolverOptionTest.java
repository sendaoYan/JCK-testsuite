/*
 * @test
 * @summary java.lang.classfile.ClassFile.ClassHierarchyResolverOption conformance: behavioral.
 * @run main ClassHierarchyResolverOptionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ClassHierarchyResolverOptionTest {
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
        ClassFile.ClassHierarchyResolverOption o = ClassFile.ClassHierarchyResolverOption.of(ClassHierarchyResolver.defaultResolver());
        check(o.classHierarchyResolver() != null, "classHierarchyResolver");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

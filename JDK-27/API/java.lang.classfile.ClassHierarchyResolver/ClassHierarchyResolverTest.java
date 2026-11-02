/*
 * @test
 * @summary java.lang.classfile.ClassHierarchyResolver conformance: behavioral.
 * @run main ClassHierarchyResolverTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ClassHierarchyResolverTest {
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
        ClassHierarchyResolver r = ClassHierarchyResolver.defaultResolver();
        check(r != null, "defaultResolver");
        ClassHierarchyResolver.ClassHierarchyInfo ci = r.getClassInfo(ClassDesc.of("java.lang.String"));
        check(ci != null, "getClassInfo String");
        ClassHierarchyResolver r2 = ClassHierarchyResolver.ofClassLoading(ClassLoader.getSystemClassLoader());
        check(r2 != null, "ofClassLoading");
        check(r.orElse(r2) != null, "orElse");
        check(r.cached() != null, "cached");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

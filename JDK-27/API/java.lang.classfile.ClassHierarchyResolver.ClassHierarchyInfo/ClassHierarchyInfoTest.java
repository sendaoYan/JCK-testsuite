/*
 * @test
 * @summary java.lang.classfile.ClassHierarchyResolver.ClassHierarchyInfo conformance: behavioral.
 * @run main ClassHierarchyInfoTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ClassHierarchyInfoTest {
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
        ClassHierarchyResolver.ClassHierarchyInfo ci = ClassHierarchyResolver.ClassHierarchyInfo.ofClass(ClassDesc.of("java.lang.String"));
        check(ci != null, "ofClass");
        ClassHierarchyResolver.ClassHierarchyInfo ii = ClassHierarchyResolver.ClassHierarchyInfo.ofInterface();
        check(ii != null, "ofInterface");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

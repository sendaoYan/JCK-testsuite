/*
 * @test
 * @summary java.lang.classfile.attribute.ModulePackagesAttribute conformance: behavioral.
 * @run main ModulePackagesAttributeTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ModulePackagesAttributeTest {
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
        ModulePackagesAttribute a = ModulePackagesAttribute.ofNames(PackageDesc.of("p"), PackageDesc.of("q"));
        check(a.packages().size() == 2, "packages");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

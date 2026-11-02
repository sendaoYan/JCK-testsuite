/*
 * @test
 * @summary java.lang.classfile.attribute.NestHostAttribute conformance: behavioral.
 * @run main NestHostAttributeTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class NestHostAttributeTest {
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
        NestHostAttribute a = NestHostAttribute.of(ClassDesc.of("pkg.H"));
        check("pkg/H".equals(a.nestHost().asInternalName()), "nestHost");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

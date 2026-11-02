/*
 * @test
 * @summary java.lang.classfile.Superclass conformance: behavioral.
 * @run main SuperclassTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class SuperclassTest {
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
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        Superclass s = Superclass.of(cpb.classEntry(ClassDesc.of("java.lang.Object")));
        check("java/lang/Object".equals(s.superclassEntry().asInternalName()), "superclassEntry");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

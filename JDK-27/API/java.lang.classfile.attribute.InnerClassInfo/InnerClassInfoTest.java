/*
 * @test
 * @summary java.lang.classfile.attribute.InnerClassInfo conformance: behavioral.
 * @run main InnerClassInfoTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class InnerClassInfoTest {
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
        check("pkg/C".equals(i.innerClass().asInternalName()), "innerClass");
        check(i.outerClass().isEmpty(), "outerClass empty");
        check("C".equals(i.innerName().get().stringValue()), "innerName");
        check(i.flags().contains(AccessFlag.PUBLIC), "flags");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

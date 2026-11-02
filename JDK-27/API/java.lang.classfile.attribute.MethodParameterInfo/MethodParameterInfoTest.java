/*
 * @test
 * @summary java.lang.classfile.attribute.MethodParameterInfo conformance: behavioral.
 * @run main MethodParameterInfoTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class MethodParameterInfoTest {
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
        MethodParameterInfo i = MethodParameterInfo.of(Optional.of("p"), AccessFlag.FINAL);
        check("p".equals(i.name().get().stringValue()), "name");
        check(i.has(AccessFlag.FINAL), "has FINAL");
        check(i.flags().contains(AccessFlag.FINAL), "flags");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

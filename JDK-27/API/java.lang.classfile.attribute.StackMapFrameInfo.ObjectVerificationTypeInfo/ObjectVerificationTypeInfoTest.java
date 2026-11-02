/*
 * @test
 * @summary java.lang.classfile.attribute.StackMapFrameInfo.ObjectVerificationTypeInfo conformance: behavioral.
 * @run main ObjectVerificationTypeInfoTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ObjectVerificationTypeInfoTest {
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
        StackMapFrameInfo.ObjectVerificationTypeInfo v = StackMapFrameInfo.ObjectVerificationTypeInfo.of(ClassDesc.of("java.lang.String"));
        check(ClassDesc.of("java.lang.String").equals(v.classSymbol()), "classSymbol");
        check("java/lang/String".equals(v.className().asInternalName()), "className");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

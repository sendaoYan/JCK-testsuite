/*
 * @test
 * @summary java.lang.classfile.MethodSignature conformance: behavioral.
 * @run main MethodSignatureTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class MethodSignatureTest {
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
        MethodSignature s = MethodSignature.of(MethodTypeDesc.of(ConstantDescs.CD_int));
        check("()I".equals(s.signatureString()), "of(MethodTypeDesc)");
        check(s.arguments().isEmpty() && s.typeParameters().isEmpty(), "no args/typeParams");
        check("I".equals(s.result().signatureString()), "result");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.lang.classfile.CodeBuilder conformance: behavioral.
 * @run main CodeBuilderTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class CodeBuilderTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withSuperclass(ClassDesc.of("java.lang.Object")).withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_int), ClassFile.ACC_STATIC, cob -> cob.iconst_0().ireturn()));
        ClassModel cm = ClassFile.of().parse(b);
        boolean hasIReturn = false;
        for (CodeElement e : cm.methods().get(0).code().get()) if (e instanceof Instruction in && in.opcode() == Opcode.IRETURN) hasIReturn = true;
        check(hasIReturn, "code has IRETURN");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

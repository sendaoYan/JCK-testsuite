/*
 * @test
 * @summary java.lang.classfile.CodeModel conformance: behavioral.
 * @run main CodeModelTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class CodeModelTest {
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
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.Sample"), cb -> cb.withSuperclass(ClassDesc.of("java.lang.Object")).withField("x", ConstantDescs.CD_int, ClassFile.ACC_PRIVATE).withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_int), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> cob.iconst_0().ireturn()));
        ClassModel cm = ClassFile.of().parse(b);
        CodeModel cdm = cm.methods().get(0).code().get();
        check(cdm.parent().get() == cm.methods().get(0), "parent");
        check(cdm.exceptionHandlers().isEmpty(), "no exception handlers");
        boolean hasReturn = false;
        for (CodeElement e : cdm) if (e instanceof Instruction in && in.opcode() == Opcode.IRETURN) hasReturn = true;
        check(hasReturn, "code contains IRETURN");
        check(cdm.elementList().size() >= 2, "code element list");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

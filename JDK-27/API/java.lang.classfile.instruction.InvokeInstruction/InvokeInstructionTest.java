/*
 * @test
 * @summary java.lang.classfile.instruction.InvokeInstruction conformance: behavioral.
 * @run main InvokeInstructionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class InvokeInstructionTest {
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
        InvokeInstruction i = InvokeInstruction.of(Opcode.INVOKESTATIC, cpb.classEntry(ClassDesc.of("java.lang.Integer")), cpb.utf8Entry("parseInt"), cpb.utf8Entry("(Ljava/lang/String;)I"), false);
        check(i.opcode() == Opcode.INVOKESTATIC && !i.isInterface(), "invokestatic");
        check("parseInt".equals(i.name().stringValue()), "name");
        check(MethodTypeDesc.of(ConstantDescs.CD_int, ClassDesc.of("java.lang.String")).equals(i.typeSymbol()), "typeSymbol");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.lang.classfile.instruction.TypeCheckInstruction conformance: behavioral.
 * @run main TypeCheckInstructionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class TypeCheckInstructionTest {
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
        TypeCheckInstruction i = TypeCheckInstruction.of(Opcode.CHECKCAST, ClassDesc.of("java.lang.String"));
        check(i.opcode() == Opcode.CHECKCAST && "java/lang/String".equals(i.type().asInternalName()) && i.sizeInBytes() == 3, "checkcast");
        check(TypeCheckInstruction.of(Opcode.INSTANCEOF, ClassDesc.of("java.lang.String")).opcode() == Opcode.INSTANCEOF, "instanceof");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

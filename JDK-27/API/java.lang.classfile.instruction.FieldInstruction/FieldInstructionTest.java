/*
 * @test
 * @summary java.lang.classfile.instruction.FieldInstruction conformance: behavioral.
 * @run main FieldInstructionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class FieldInstructionTest {
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
        FieldInstruction i = FieldInstruction.of(Opcode.GETSTATIC, cpb.classEntry(ClassDesc.of("java.lang.System")), cpb.utf8Entry("out"), cpb.utf8Entry("Ljava/io/PrintStream;"));
        check(i.opcode() == Opcode.GETSTATIC && "java/lang/System".equals(i.owner().asInternalName()), "getstatic owner");
        check("out".equals(i.name().stringValue()), "name");
        check(ClassDesc.of("java.io.PrintStream").equals(i.typeSymbol()), "typeSymbol");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

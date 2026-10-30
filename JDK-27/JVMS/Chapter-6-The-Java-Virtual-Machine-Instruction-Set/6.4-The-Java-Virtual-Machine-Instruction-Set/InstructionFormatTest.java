/*
 * @test
 * @summary JVMS 6.4: each instruction is encoded as a one-byte opcode followed
 *          by zero or more operand bytes.  This test decodes two methods and
 *          asserts their exact opcode sequences (return == 0xB1; iconst_0;
 *          ireturn == 0x03 0xAC).
 * @compile Simple.jasm
 * @run main InstructionFormatTest
 */

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.Opcode;
import java.lang.classfile.Instruction;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class InstructionFormatTest {
    public static void main(String[] args) throws Exception {
        byte[] bytes;
        try (InputStream in = InstructionFormatTest.class.getResourceAsStream("Simple.class")) {
            bytes = in.readAllBytes();
        }

        ClassModel cm = ClassFile.of().parse(bytes);
        assertOps(cm, "ret", List.of(Opcode.RETURN));
        assertOps(cm, "loadRet", List.of(Opcode.ICONST_0, Opcode.IRETURN));

        System.out.println("PASS");
    }

    static void assertOps(ClassModel cm, String name, List<Opcode> expected) {
        for (MethodModel mm : cm.methods()) {
            if (!mm.methodName().stringValue().equals(name)) continue;
            CodeModel code = mm.code().orElseThrow();
            List<Opcode> ops = new ArrayList<>();
            for (CodeElement ce : code) {
                if (ce instanceof Instruction instr) ops.add(instr.opcode());
            }
            if (!ops.equals(expected))
                throw new AssertionError(name + ": expected " + expected + " but got " + ops);
            return;
        }
        throw new AssertionError("method not found: " + name);
    }
}

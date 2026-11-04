/*
 * @test
 * @summary java.lang.classfile.instruction.CharacterRange conformance: white-box structure + black-box behavior.
 * @run main CharacterRangeTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.nio.file.*;

public class CharacterRangeTest {
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
    static final ClassReader[] cap = new ClassReader[1];
    static final BufWriter[] cap2 = new BufWriter[1];
    static final BranchInstruction[] br = new BranchInstruction[1];
    static final SwitchCase[] sc = new SwitchCase[1];
    static final TableSwitchInstruction[] ts = new TableSwitchInstruction[1];
    static final LookupSwitchInstruction[] ls = new LookupSwitchInstruction[1];
    static final ExceptionCatch[] ec = new ExceptionCatch[1];
    static final TypeAnnotation.TypeArgumentTarget[] ta = new TypeAnnotation.TypeArgumentTarget[1];
    static final TypeAnnotation.LocalVarTarget[] lvt = new TypeAnnotation.LocalVarTarget[1];
    static final TypeAnnotation.LocalVarTargetInfo[] lvi = new TypeAnnotation.LocalVarTargetInfo[1];
    static final StackMapFrameInfo.UninitializedVerificationTypeInfo[] uv = new StackMapFrameInfo.UninitializedVerificationTypeInfo[1];
    static final TypeAnnotation.OffsetTarget[] ot = new TypeAnnotation.OffsetTarget[1];

    // ---- custom attribute support for black-box above ----
    static class CA extends CustomAttribute<CA> { CA(AttributeMapper<CA> m) { super(m); } }
    static class CA2 extends CustomAttribute<CA2> { CA2(AttributeMapper<CA2> m) { super(m); } }
    static class CA3 extends CustomAttribute<CA3> { CA3(AttributeMapper<CA3> m) { super(m); } }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.instruction.CharacterRange");
        check(c != null, "java.lang.classfile.instruction.CharacterRange loads");
        check(c.getName().equals("java.lang.classfile.instruction.CharacterRange"), "binary name java.lang.classfile.instruction.CharacterRange");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.CharacterRange is public");
        check(c.isInterface(), "java.lang.classfile.instruction.CharacterRange is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.CharacterRange has no superclass");
        check(declares(c, "startScope"), "declares startScope");
        check(declares(c, "endScope"), "declares endScope");
        check(declares(c, "characterRangeStart"), "declares characterRangeStart");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        final Label[] scopes = new Label[2];
        ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> {
            scopes[0] = cob.newLabel();
            scopes[1] = cob.newLabel();
            CharacterRange cr = CharacterRange.of(scopes[0], scopes[1], 0, 10, CharacterRange.FLAG_BLOCK);
            check(cr.characterRangeStart() == 0, "start");
            check(cr.characterRangeEnd() == 10, "end");
            check(cr.flags() == CharacterRange.FLAG_BLOCK, "flags");
            check(cr.startScope() == scopes[0] && cr.endScope() == scopes[1], "scopes");
            check(CharacterRange.FLAG_STATEMENT == 0x0001 && CharacterRange.FLAG_BLOCK == 0x0002 && CharacterRange.FLAG_ASSIGNMENT == 0x0004, "flag constants");
            cob.return_();
        }));

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

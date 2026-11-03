/*
 * @test
 * @summary java.lang.classfile.Opcode.Kind conformance: white-box structure + black-box behavior.
 * @run main KindTest
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

public class KindTest {
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
        Class<?> c = Class.forName("java.lang.classfile.Opcode$Kind");
        check(c != null, "java.lang.classfile.Opcode.Kind loads");
        check(c.getName().equals("java.lang.classfile.Opcode$Kind"), "binary name java.lang.classfile.Opcode$Kind");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Opcode.Kind is public");
        check(c.isEnum(), "java.lang.classfile.Opcode.Kind is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.Opcode.Kind superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 26, "26 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("LOAD", "STORE", "INCREMENT", "BRANCH", "LOOKUP_SWITCH", "TABLE_SWITCH", "RETURN", "THROW_EXCEPTION", "FIELD_ACCESS", "INVOKE", "INVOKE_DYNAMIC", "NEW_OBJECT", "NEW_PRIMITIVE_ARRAY", "NEW_REF_ARRAY", "NEW_MULTI_ARRAY", "TYPE_CHECK", "ARRAY_LOAD", "ARRAY_STORE", "STACK", "CONVERT", "OPERATOR", "CONSTANT", "MONITOR", "NOP", "DISCONTINUED_JSR", "DISCONTINUED_RET")), "exact enum constant names");
        check(declares(c, "values"), "declares values");
        check(declares(c, "valueOf"), "declares valueOf");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(Opcode.Kind.values().length == 26, "26 kinds");
        check(Opcode.IADD.kind() == Opcode.Kind.OPERATOR, "IADD OPERATOR");
        check(Opcode.ILOAD.kind() == Opcode.Kind.LOAD, "ILOAD LOAD");
        check(Opcode.ISTORE.kind() == Opcode.Kind.STORE, "ISTORE STORE");
        check(Opcode.GOTO.kind() == Opcode.Kind.BRANCH, "GOTO BRANCH");
        check(Opcode.IINC.kind() == Opcode.Kind.INCREMENT, "IINC INCREMENT");
        check(Opcode.LOOKUPSWITCH.kind() == Opcode.Kind.LOOKUP_SWITCH, "LOOKUPSWITCH");
        check(Opcode.RETURN.kind() == Opcode.Kind.RETURN, "RETURN");
        check(Opcode.NOP.kind() == Opcode.Kind.NOP, "NOP");
        check(Opcode.Kind.valueOf("LOAD") == Opcode.Kind.LOAD, "valueOf LOAD");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

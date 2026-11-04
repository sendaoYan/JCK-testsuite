/*
 * @test
 * @summary java.lang.classfile.TypeKind conformance: white-box structure + black-box behavior.
 * @run main TypeKindTest
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

public class TypeKindTest {
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
        Class<?> c = Class.forName("java.lang.classfile.TypeKind");
        check(c != null, "java.lang.classfile.TypeKind loads");
        check(c.getName().equals("java.lang.classfile.TypeKind"), "binary name java.lang.classfile.TypeKind");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeKind is public");
        check(c.isEnum(), "java.lang.classfile.TypeKind is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.TypeKind superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 10, "10 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("BOOLEAN", "BYTE", "CHAR", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE", "REFERENCE", "VOID")), "exact enum constant names");
        check(declares(c, "values"), "declares values");
        check(declares(c, "valueOf"), "declares valueOf");
        check(declares(c, "upperBound"), "declares upperBound");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(TypeKind.INT.slotSize() == 1, "INT slots 1");
        check(TypeKind.LONG.slotSize() == 2, "LONG slots 2");
        check(TypeKind.DOUBLE.slotSize() == 2, "DOUBLE slots 2");
        check(TypeKind.VOID.slotSize() == 0, "VOID slots 0");
        check(TypeKind.REFERENCE.slotSize() == 1, "REFERENCE slots 1");
        check(TypeKind.INT.newarrayCode() == 10, "INT newarray code");
        check(TypeKind.BOOLEAN.newarrayCode() == 4, "BOOLEAN newarray code");
        check(TypeKind.fromNewarrayCode(10) == TypeKind.INT, "fromNewarrayCode INT");
        check(TypeKind.fromDescriptor("J") == TypeKind.LONG, "fromDescriptor J");
        check(TypeKind.from(ConstantDescs.CD_int) == TypeKind.INT, "from ClassDesc int");
        check(TypeKind.INT.asLoadable() == TypeKind.INT, "INT asLoadable");
        check(TypeKind.VOID.asLoadable() == TypeKind.VOID, "VOID asLoadable");
        check(TypeKind.INT.upperBound().equals(ConstantDescs.CD_int), "upperBound int");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

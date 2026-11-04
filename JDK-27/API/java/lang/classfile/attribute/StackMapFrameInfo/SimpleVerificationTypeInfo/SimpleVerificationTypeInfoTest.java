/*
 * @test
 * @summary java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo conformance: white-box structure + black-box behavior.
 * @run main SimpleVerificationTypeInfoTest
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

public class SimpleVerificationTypeInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.StackMapFrameInfo$SimpleVerificationTypeInfo");
        check(c != null, "java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.StackMapFrameInfo$SimpleVerificationTypeInfo"), "binary name java.lang.classfile.attribute.StackMapFrameInfo$SimpleVerificationTypeInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo is public");
        check(c.isEnum(), "java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 7, "7 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("TOP", "INTEGER", "FLOAT", "DOUBLE", "LONG", "NULL", "UNINITIALIZED_THIS")), "exact enum constant names");
        check(declares(c, "values"), "declares values");
        check(declares(c, "valueOf"), "declares valueOf");
        check(declares(c, "tag"), "declares tag");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(StackMapFrameInfo.SimpleVerificationTypeInfo.TOP.tag() == 0, "TOP tag 0");
        check(StackMapFrameInfo.SimpleVerificationTypeInfo.INTEGER.tag() == 1, "INTEGER tag 1");
        check(StackMapFrameInfo.SimpleVerificationTypeInfo.NULL.tag() == 5, "NULL tag 5");
        check(StackMapFrameInfo.SimpleVerificationTypeInfo.UNINITIALIZED_THIS.tag() == 6, "UNINITIALIZED_THIS tag 6");
        check(StackMapFrameInfo.SimpleVerificationTypeInfo.values().length == 7, "7 constants");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

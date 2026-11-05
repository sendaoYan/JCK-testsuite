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

    // ---- Part 3: 26 值 valueOf 全名双向 + 负例（实测锁定） ----
    static void testValueOfSemantics() {
        String[] names = {"LOAD", "STORE", "INCREMENT", "BRANCH", "LOOKUP_SWITCH", "TABLE_SWITCH",
                "RETURN", "THROW_EXCEPTION", "FIELD_ACCESS", "INVOKE", "INVOKE_DYNAMIC", "NEW_OBJECT",
                "NEW_PRIMITIVE_ARRAY", "NEW_REF_ARRAY", "NEW_MULTI_ARRAY", "TYPE_CHECK", "ARRAY_LOAD",
                "ARRAY_STORE", "STACK", "CONVERT", "OPERATOR", "CONSTANT", "MONITOR", "NOP",
                "DISCONTINUED_JSR", "DISCONTINUED_RET"};
        boolean allRoundTrip = true;
        for (String n : names)
            if (Opcode.Kind.valueOf(n).name().equals(n) == false || Opcode.Kind.valueOf(n) != Opcode.Kind.valueOf(n))
                allRoundTrip = false;
        check(allRoundTrip, "valueOf round trip for all 26 kind names");
        check(Opcode.Kind.valueOf("LOAD") == Opcode.Kind.LOAD, "valueOf LOAD");
        check(Opcode.Kind.valueOf("OPERATOR") == Opcode.Kind.OPERATOR, "valueOf OPERATOR");
        check(Opcode.Kind.valueOf("DISCONTINUED_JSR") == Opcode.Kind.DISCONTINUED_JSR, "valueOf DISCONTINUED_JSR");
        check(Opcode.Kind.valueOf("DISCONTINUED_RET") == Opcode.Kind.DISCONTINUED_RET, "valueOf DISCONTINUED_RET");
        try {
            Opcode.Kind.valueOf("NO_SUCH_KIND");
            check(false, "valueOf unknown kind must throw");
        } catch (IllegalArgumentException e) {
            check("No enum constant java.lang.classfile.Opcode.Kind.NO_SUCH_KIND".equals(e.getMessage()),
                    "valueOf unknown kind IAE message: " + e.getMessage());
        }
        try {
            Opcode.Kind.valueOf(null);
            check(false, "valueOf(null) kind must throw");
        } catch (NullPointerException e) {
            check("Name is null".equals(e.getMessage()), "valueOf(null) kind NPE message");
        }
        // values() 防御拷贝 + ordinal 严格递增 + toString==name
        Opcode.Kind[] a1 = Opcode.Kind.values();
        Opcode.Kind[] a2 = Opcode.Kind.values();
        check(a1 != a2, "values() defensive copy");
        boolean ordered = true, namesOk = true;
        for (int i = 0; i < a1.length; i++) {
            if (i > 0 && a1[i - 1].compareTo(a1[i]) >= 0) ordered = false;
            if (!a1[i].toString().equals(a1[i].name())) namesOk = false;
        }
        check(ordered, "kind ordinals strictly ascending in values() order");
        check(namesOk, "toString==name for all kinds");
        check(Opcode.Kind.LOAD.ordinal() == 0, "LOAD ordinal 0");
        check(Opcode.Kind.DISCONTINUED_RET.ordinal() == 25, "DISCONTINUED_RET ordinal 25");
    }

    // ---- Part 4: 每 kind 至少 1 个 opcode + 26 类精确计数表（JDK-27 实测锁定） ----
    static void testKindCoverage() {
        Map<Opcode.Kind, Integer> counts = new EnumMap<>(Opcode.Kind.class);
        for (Opcode o : Opcode.values()) counts.merge(o.kind(), 1, Integer::sum);
        check(counts.size() == 26, "every one of 26 kinds has >= 1 opcode");
        // 完整计数表（与 OpcodeTest 交叉锁定）
        Map<Opcode.Kind, Integer> expected = Map.ofEntries(
                Map.entry(Opcode.Kind.LOAD, 30), Map.entry(Opcode.Kind.STORE, 30),
                Map.entry(Opcode.Kind.INCREMENT, 2), Map.entry(Opcode.Kind.BRANCH, 18),
                Map.entry(Opcode.Kind.LOOKUP_SWITCH, 1), Map.entry(Opcode.Kind.TABLE_SWITCH, 1),
                Map.entry(Opcode.Kind.RETURN, 6), Map.entry(Opcode.Kind.THROW_EXCEPTION, 1),
                Map.entry(Opcode.Kind.FIELD_ACCESS, 4), Map.entry(Opcode.Kind.INVOKE, 4),
                Map.entry(Opcode.Kind.INVOKE_DYNAMIC, 1), Map.entry(Opcode.Kind.NEW_OBJECT, 1),
                Map.entry(Opcode.Kind.NEW_PRIMITIVE_ARRAY, 1), Map.entry(Opcode.Kind.NEW_REF_ARRAY, 1),
                Map.entry(Opcode.Kind.NEW_MULTI_ARRAY, 1), Map.entry(Opcode.Kind.TYPE_CHECK, 2),
                Map.entry(Opcode.Kind.ARRAY_LOAD, 8), Map.entry(Opcode.Kind.ARRAY_STORE, 8),
                Map.entry(Opcode.Kind.STACK, 9), Map.entry(Opcode.Kind.CONVERT, 15),
                Map.entry(Opcode.Kind.OPERATOR, 42), Map.entry(Opcode.Kind.CONSTANT, 20),
                Map.entry(Opcode.Kind.MONITOR, 2), Map.entry(Opcode.Kind.NOP, 1),
                Map.entry(Opcode.Kind.DISCONTINUED_JSR, 2), Map.entry(Opcode.Kind.DISCONTINUED_RET, 2));
        check(counts.equals(expected), "per-kind opcode count table: " + counts);
        int sum = 0;
        for (int v : counts.values()) sum += v;
        check(sum == Opcode.values().length, "kind counts sum equals opcode total 213");
        // 每个 kind 至少能找到一个典型成员（分类可由 kind() 反查）
        check(Opcode.IADD.kind() == Opcode.Kind.OPERATOR && Opcode.RET.kind() == Opcode.Kind.DISCONTINUED_RET
                && Opcode.JSR.kind() == Opcode.Kind.DISCONTINUED_JSR && Opcode.LDC.kind() == Opcode.Kind.CONSTANT,
                "representative members map to expected kinds");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testValueOfSemantics();
        testKindCoverage();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

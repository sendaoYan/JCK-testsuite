/*
 * @test
 * @summary java.lang.classfile.Signature.TypeArg.Bounded.WildcardIndicator conformance: white-box structure + black-box behavior.
 * @run main WildcardIndicatorTest
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

public class WildcardIndicatorTest {
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
        Class<?> c = Class.forName("java.lang.classfile.Signature$TypeArg$Bounded$WildcardIndicator");
        check(c != null, "java.lang.classfile.Signature.TypeArg.Bounded.WildcardIndicator loads");
        check(c.getName().equals("java.lang.classfile.Signature$TypeArg$Bounded$WildcardIndicator"), "binary name java.lang.classfile.Signature$TypeArg$Bounded$WildcardIndicator");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Signature.TypeArg.Bounded.WildcardIndicator is public");
        check(c.isEnum(), "java.lang.classfile.Signature.TypeArg.Bounded.WildcardIndicator is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.Signature.TypeArg.Bounded.WildcardIndicator superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 3, "3 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("NONE", "EXTENDS", "SUPER")), "exact enum constant names");
        check(declares(c, "values"), "declares values");
        check(declares(c, "valueOf"), "declares valueOf");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(Signature.TypeArg.Bounded.WildcardIndicator.NONE != null, "NONE");
        check(Signature.TypeArg.Bounded.WildcardIndicator.EXTENDS != null, "EXTENDS");
        check(Signature.TypeArg.Bounded.WildcardIndicator.SUPER != null, "SUPER");
        check(Signature.TypeArg.Bounded.WildcardIndicator.values().length == 3, "3 constants");
        check(Signature.TypeArg.Bounded.WildcardIndicator.valueOf("NONE") == Signature.TypeArg.Bounded.WildcardIndicator.NONE, "valueOf");

    }

    // ---- Part 3: 枚举值精确表（顺序/ordinal/名字） ----
    static void testEnumValueTable() {
        Signature.TypeArg.Bounded.WildcardIndicator[] vs = Signature.TypeArg.Bounded.WildcardIndicator.values();
        check(vs.length == 3, "exactly 3 values");
        check(vs[0] == Signature.TypeArg.Bounded.WildcardIndicator.NONE
                && vs[1] == Signature.TypeArg.Bounded.WildcardIndicator.EXTENDS
                && vs[2] == Signature.TypeArg.Bounded.WildcardIndicator.SUPER, "declaration order NONE,EXTENDS,SUPER");
        check(Signature.TypeArg.Bounded.WildcardIndicator.NONE.ordinal() == 0, "NONE ordinal 0");
        check(Signature.TypeArg.Bounded.WildcardIndicator.EXTENDS.ordinal() == 1, "EXTENDS ordinal 1");
        check(Signature.TypeArg.Bounded.WildcardIndicator.SUPER.ordinal() == 2, "SUPER ordinal 2");
        for (Signature.TypeArg.Bounded.WildcardIndicator v : vs) {
            check(v.name().equals(v.toString()), "name==toString for " + v);
            check(v.getDeclaringClass() == Signature.TypeArg.Bounded.WildcardIndicator.class, "declaringClass self for " + v);
        }
        check(Signature.TypeArg.Bounded.WildcardIndicator.NONE.compareTo(Signature.TypeArg.Bounded.WildcardIndicator.EXTENDS) < 0,
                "NONE < EXTENDS");
        check(Signature.TypeArg.Bounded.WildcardIndicator.EXTENDS.compareTo(Signature.TypeArg.Bounded.WildcardIndicator.SUPER) < 0,
                "EXTENDS < SUPER");
        check(java.util.EnumSet.allOf(Signature.TypeArg.Bounded.WildcardIndicator.class).size() == 3, "EnumSet.allOf 3");
        check(vs[0].equals(vs[0]) && !vs[0].equals(vs[1]), "equals distinct");
    }

    // ---- Part 4: valueOf 双向 + 假名/空名/null 负例 ----
    static void testValueOfMatrix() {
        for (Signature.TypeArg.Bounded.WildcardIndicator v : Signature.TypeArg.Bounded.WildcardIndicator.values())
            check(Signature.TypeArg.Bounded.WildcardIndicator.valueOf(v.name()) == v, "valueOf round-trip " + v);
        boolean threw = false;
        try {
            Signature.TypeArg.Bounded.WildcardIndicator.valueOf("BOGUS");
        } catch (IllegalArgumentException e) {
            threw = true;
            check(e.getMessage() != null && e.getMessage().contains("BOGUS")
                    && e.getMessage().contains("No enum constant"), "bogus name IAE msg: " + e.getMessage());
        }
        check(threw, "valueOf(BOGUS) IAE");
        threw = false;
        try {
            Signature.TypeArg.Bounded.WildcardIndicator.valueOf("");
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        check(threw, "valueOf(\"\") IAE");
        threw = false;
        try {
            Signature.TypeArg.Bounded.WildcardIndicator.valueOf(null);
        } catch (NullPointerException e) {
            threw = true;
            check(e.getMessage() != null && e.getMessage().contains("Name is null"), "valueOf(null) NPE msg");
        }
        check(threw, "valueOf(null) NPE");
        // 小写名不是合法枚举名（枚举名大小写敏感）
        threw = false;
        try {
            Signature.TypeArg.Bounded.WildcardIndicator.valueOf("extends");
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        check(threw, "valueOf(\"extends\") case-sensitive IAE");
    }

    // ---- Part 5: 语义映射（签名串 +/−/裸/* ↔ 指示器/Unbounded） ----
    static void testSemanticParseMapping() {
        Signature.ClassTypeSig bare = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/List<Ljava/lang/Number;>;");
        check(((Signature.TypeArg.Bounded) bare.typeArgs().get(0)).wildcardIndicator()
                == Signature.TypeArg.Bounded.WildcardIndicator.NONE, "bare -> NONE");
        Signature.ClassTypeSig ext = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/List<+Ljava/lang/Number;>;");
        check(((Signature.TypeArg.Bounded) ext.typeArgs().get(0)).wildcardIndicator()
                == Signature.TypeArg.Bounded.WildcardIndicator.EXTENDS, "+ -> EXTENDS");
        Signature.ClassTypeSig sup = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/List<-Ljava/lang/Number;>;");
        check(((Signature.TypeArg.Bounded) sup.typeArgs().get(0)).wildcardIndicator()
                == Signature.TypeArg.Bounded.WildcardIndicator.SUPER, "- -> SUPER");
        Signature.ClassTypeSig unb = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/List<*>;");
        check(!(unb.typeArgs().get(0) instanceof Signature.TypeArg.Bounded), "* -> NOT Bounded (Unbounded)");
        // 工厂逆映射：bounded(ind, ·) 的指示器经 signatureString 再解析保持
        Signature.RefTypeSig num = (Signature.RefTypeSig) Signature.parseFrom("Ljava/lang/Number;");
        String[][] rows = {{"NONE", "Ljava/util/List<Ljava/lang/Number;>;"},
                {"EXTENDS", "Ljava/util/List<+Ljava/lang/Number;>;"},
                {"SUPER", "Ljava/util/List<-Ljava/lang/Number;>;"}};
        for (String[] row : rows) {
            Signature.TypeArg.Bounded.WildcardIndicator ind = Signature.TypeArg.Bounded.WildcardIndicator.valueOf(row[0]);
            Signature.ClassTypeSig emitted = Signature.ClassTypeSig.of(ClassDesc.of("java.util.List"),
                    Signature.TypeArg.bounded(ind, num));
            check(emitted.signatureString().equals(row[1]), ind + " emits " + row[1]);
            Signature.ClassTypeSig back = (Signature.ClassTypeSig) Signature.parseFrom(emitted.signatureString());
            check(((Signature.TypeArg.Bounded) back.typeArgs().get(0)).wildcardIndicator() == ind, ind + " survives round-trip");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEnumValueTable();
        testValueOfMatrix();
        testSemanticParseMapping();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

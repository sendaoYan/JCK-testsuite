/*
 * @test
 * @summary java.lang.classfile.Signature.TypeArg.Bounded conformance: white-box structure + black-box behavior.
 * @run main BoundedTest
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

public class BoundedTest {
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

    // ---- 真类闭环素材：javac 为 <? extends Number> / <? super String> 字段生成有界通配实参 ----
    static final java.util.List<? extends Number> EXT_FIELD = java.util.List.of();
    static final java.util.List<? super String> SUPER_FIELD = new java.util.ArrayList<>();

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.Signature$TypeArg$Bounded");
        check(c != null, "java.lang.classfile.Signature.TypeArg.Bounded loads");
        check(c.getName().equals("java.lang.classfile.Signature$TypeArg$Bounded"), "binary name java.lang.classfile.Signature$TypeArg$Bounded");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Signature.TypeArg.Bounded is public");
        check(c.isInterface(), "java.lang.classfile.Signature.TypeArg.Bounded is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.Signature.TypeArg.Bounded has no superclass");
        check(declares(c, "wildcardIndicator"), "declares wildcardIndicator");
        check(declares(c, "boundType"), "declares boundType");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        Signature.TypeArg.Bounded b = Signature.TypeArg.superOf(Signature.ClassTypeSig.of(ClassDesc.of("java.lang.String")));
        check(b.wildcardIndicator() == Signature.TypeArg.Bounded.WildcardIndicator.SUPER, "SUPER indicator");
        check(b.boundType().signatureString().equals("Ljava/lang/String;"), "boundType");
        Signature.TypeArg.Bounded b2 = Signature.TypeArg.of(Signature.ClassTypeSig.of(ClassDesc.of("java.lang.Object")));
        check(b2.wildcardIndicator() == Signature.TypeArg.Bounded.WildcardIndicator.NONE, "of() NONE indicator");

    }

    // ---- Part 3: 指示器 × 来源双矩阵（工厂 / +、-、裸形解析） ----
    static void testIndicatorMatrix() {
        Signature.RefTypeSig num = (Signature.RefTypeSig) Signature.parseFrom("Ljava/lang/Number;");
        check(Signature.TypeArg.of(num).wildcardIndicator() == Signature.TypeArg.Bounded.WildcardIndicator.NONE, "factory NONE");
        check(Signature.TypeArg.extendsOf(num).wildcardIndicator() == Signature.TypeArg.Bounded.WildcardIndicator.EXTENDS, "factory EXTENDS");
        check(Signature.TypeArg.superOf(num).wildcardIndicator() == Signature.TypeArg.Bounded.WildcardIndicator.SUPER, "factory SUPER");
        // 解析通道：+/−/裸形
        String[][] rows = {
                {"Ljava/util/List<Ljava/lang/Number;>;", "NONE"},
                {"Ljava/util/List<+Ljava/lang/Number;>;", "EXTENDS"},
                {"Ljava/util/List<-Ljava/lang/Number;>;", "SUPER"},
        };
        for (String[] row : rows) {
            Signature.ClassTypeSig c = (Signature.ClassTypeSig) Signature.parseFrom(row[0]);
            check(c.typeArgs().get(0) instanceof Signature.TypeArg.Bounded, row[0] + " arg Bounded");
            Signature.TypeArg.Bounded b = (Signature.TypeArg.Bounded) c.typeArgs().get(0);
            check(b.wildcardIndicator().name().equals(row[1]), row[0] + " -> " + row[1]);
            check(b.boundType().signatureString().equals("Ljava/lang/Number;"), row[0] + " bound Number");
        }
        // 三个指示器 enum 值逐一经 bounded(...) 工厂
        for (Signature.TypeArg.Bounded.WildcardIndicator ind : Signature.TypeArg.Bounded.WildcardIndicator.values())
            check(Signature.TypeArg.bounded(ind, num).wildcardIndicator() == ind, "bounded passes " + ind);
    }

    // ---- Part 4: boundType 三种 RefTypeSig 形态（类/TypeVar/数组） ----
    static void testBoundTypeKinds() {
        Signature.RefTypeSig str = (Signature.RefTypeSig) Signature.parseFrom("Ljava/lang/String;");
        check(Signature.TypeArg.of(str).boundType() instanceof Signature.ClassTypeSig, "class bound");
        check(Signature.TypeArg.extendsOf((Signature.RefTypeSig) Signature.parseFrom("TT;")).boundType()
                instanceof Signature.TypeVarSig, "typevar bound");
        check(Signature.TypeArg.superOf((Signature.RefTypeSig) Signature.parseFrom("[I")).boundType()
                instanceof Signature.ArrayTypeSig, "array bound");
        // 解析通道：泛型类作 bound / 数组作 bound
        Signature.ClassTypeSig g = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/List<Ljava/util/List<Ljava/lang/String;>;>;");
        check(((Signature.TypeArg.Bounded) g.typeArgs().get(0)).boundType() instanceof Signature.ClassTypeSig, "nested generic bound");
        Signature.ClassTypeSig a = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/List<[I>;");
        check(((Signature.TypeArg.Bounded) a.typeArgs().get(0)).boundType().signatureString().equals("[I"), "array bound string");
    }

    // ---- Part 5: 深嵌套通配（List<List<+Number>> 内层 EXTENDS） ----
    static void testNestedWildcard() {
        Signature.ClassTypeSig c = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/List<Ljava/util/List<+Ljava/lang/Number;>;>;");
        Signature.TypeArg.Bounded outer = (Signature.TypeArg.Bounded) c.typeArgs().get(0);
        check(outer.wildcardIndicator() == Signature.TypeArg.Bounded.WildcardIndicator.NONE, "outer NONE");
        Signature.ClassTypeSig inner = (Signature.ClassTypeSig) outer.boundType();
        check(inner.className().equals("java/util/List"), "inner className");
        Signature.TypeArg.Bounded innerArg = (Signature.TypeArg.Bounded) inner.typeArgs().get(0);
        check(innerArg.wildcardIndicator() == Signature.TypeArg.Bounded.WildcardIndicator.EXTENDS, "inner EXTENDS");
        check(innerArg.boundType().signatureString().equals("Ljava/lang/Number;"), "inner bound Number");
        // 内外层互不相等
        check(!outer.equals(innerArg), "outer/inner args not equals");
    }

    // ---- Part 6: null 负例（实测 NPE） ----
    static void testNullMatrix() {
        Signature.RefTypeSig str = (Signature.RefTypeSig) Signature.parseFrom("Ljava/lang/String;");
        boolean threw = false;
        try {
            Signature.TypeArg.bounded(null, str);
        } catch (NullPointerException e) {
            threw = true;
        }
        check(threw, "bounded(null, sig) NPE");
        threw = false;
        try {
            Signature.TypeArg.bounded(Signature.TypeArg.Bounded.WildcardIndicator.NONE, null);
        } catch (NullPointerException e) {
            threw = true;
        }
        check(threw, "bounded(ind, null) NPE");
        threw = false;
        try {
            Signature.TypeArg.extendsOf(null);
        } catch (NullPointerException e) {
            threw = true;
        }
        check(threw, "extendsOf(null) NPE");
    }

    // ---- Part 7: equals/hashCode 值语义（跨工厂等值） ----
    static void testEqualityContract() {
        Signature.RefTypeSig num = (Signature.RefTypeSig) Signature.parseFrom("Ljava/lang/Number;");
        check(Signature.TypeArg.of(num).equals(Signature.TypeArg.bounded(
                Signature.TypeArg.Bounded.WildcardIndicator.NONE, num)), "of == bounded(NONE)");
        check(Signature.TypeArg.superOf(num).equals(Signature.TypeArg.bounded(
                Signature.TypeArg.Bounded.WildcardIndicator.SUPER, num)), "superOf == bounded(SUPER)");
        check(Signature.TypeArg.extendsOf(num).hashCode() == Signature.TypeArg.bounded(
                Signature.TypeArg.Bounded.WildcardIndicator.EXTENDS, num).hashCode(), "hashCode consistent");
        check(!Signature.TypeArg.extendsOf(num).equals(Signature.TypeArg.extendsOf(
                (Signature.RefTypeSig) Signature.parseFrom("Ljava/lang/Object;"))), "different bound not equals");
        // 与解析产物等值
        Signature.ClassTypeSig c = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/List<+Ljava/lang/Number;>;");
        check(c.typeArgs().get(0).equals(Signature.TypeArg.extendsOf(num)), "parsed arg == factory arg");
    }

    // ---- Part 8: 真类闭环（javac <? extends>/< ? super> 字段 → Bounded EXTENDS/SUPER） ----
    static void testRealClassFieldLoop() throws Exception {
        byte[] self = BoundedTest.class.getResourceAsStream("BoundedTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        String[][] rows = {{"EXT_FIELD", "Ljava/util/List<+Ljava/lang/Number;>;", "EXTENDS"},
                {"SUPER_FIELD", "Ljava/util/List<-Ljava/lang/String;>;", "SUPER"}};
        for (String[] row : rows) {
            FieldModel fm = null;
            for (FieldModel f : cm.fields()) if (f.fieldName().stringValue().equals(row[0])) fm = f;
            check(fm != null, row[0] + " found");
            String fs = fm.findAttribute(Attributes.signature()).orElseThrow().signature().stringValue();
            check(fs.equals(row[1]), row[0] + " sig exact: " + fs);
            Signature.ClassTypeSig model = (Signature.ClassTypeSig) Signature.parseFrom(fs);
            check(model.typeArgs().get(0) instanceof Signature.TypeArg.Bounded, row[0] + " arg Bounded");
            Signature.TypeArg.Bounded b = (Signature.TypeArg.Bounded) model.typeArgs().get(0);
            check(b.wildcardIndicator().name().equals(row[2]), row[0] + " indicator " + row[2]);
            check(b.boundType().signatureString().equals(row[1].contains("+") ? "Ljava/lang/Number;" : "Ljava/lang/String;"),
                    row[0] + " boundType");
            check(Signature.parseFrom(model.signatureString()).equals(model), row[0] + " model idempotent");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testIndicatorMatrix();
        testBoundTypeKinds();
        testNestedWildcard();
        testNullMatrix();
        testEqualityContract();
        testRealClassFieldLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

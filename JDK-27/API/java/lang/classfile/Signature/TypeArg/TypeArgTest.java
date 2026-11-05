/*
 * @test
 * @summary java.lang.classfile.Signature.TypeArg conformance: white-box structure + black-box behavior.
 * @run main TypeArgTest
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

public class TypeArgTest {
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

    // ---- 真类闭环素材：泛型字段（javac 产物 Signature 属性中实参为 TypeArg） ----
    static final java.util.List<String> GENERIC_FIELD = java.util.List.of();
    static final java.util.List<?> WILDCARD_FIELD = java.util.List.of();

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.Signature$TypeArg");
        check(c != null, "java.lang.classfile.Signature.TypeArg loads");
        check(c.getName().equals("java.lang.classfile.Signature$TypeArg"), "binary name java.lang.classfile.Signature$TypeArg");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Signature.TypeArg is public");
        check(c.isInterface(), "java.lang.classfile.Signature.TypeArg is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.Signature.TypeArg has no superclass");
        check(declares(c, "of"), "declares of");
        check(declares(c, "unbounded"), "declares unbounded");
        check(declares(c, "extendsOf"), "declares extendsOf");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        Signature.TypeArg u = Signature.TypeArg.unbounded();
        check(u instanceof Signature.TypeArg.Unbounded, "unbounded type");
        Signature.TypeArg b = Signature.TypeArg.extendsOf(Signature.ClassTypeSig.of(ClassDesc.of("java.lang.String")));
        check(b instanceof Signature.TypeArg.Bounded, "extendsOf bounded");
        check(((Signature.TypeArg.Bounded) b).wildcardIndicator() == Signature.TypeArg.Bounded.WildcardIndicator.EXTENDS, "EXTENDS indicator");

    }

    // ---- Part 3: 工厂全表（of/unbounded/extendsOf/superOf/bounded 指示器矩阵） ----
    static void testFactoryMatrix() {
        Signature.RefTypeSig str = (Signature.RefTypeSig) Signature.parseFrom("Ljava/lang/String;");
        check(Signature.TypeArg.of(str) instanceof Signature.TypeArg.Bounded, "of -> Bounded");
        check(Signature.TypeArg.of(str).wildcardIndicator() == Signature.TypeArg.Bounded.WildcardIndicator.NONE, "of -> NONE");
        check(Signature.TypeArg.extendsOf(str).wildcardIndicator() == Signature.TypeArg.Bounded.WildcardIndicator.EXTENDS, "extendsOf -> EXTENDS");
        check(Signature.TypeArg.superOf(str).wildcardIndicator() == Signature.TypeArg.Bounded.WildcardIndicator.SUPER, "superOf -> SUPER");
        for (Signature.TypeArg.Bounded.WildcardIndicator ind : Signature.TypeArg.Bounded.WildcardIndicator.values()) {
            Signature.TypeArg.Bounded b = Signature.TypeArg.bounded(ind, str);
            check(b.wildcardIndicator() == ind, "bounded(" + ind + ") indicator");
            check(b.boundType().equals(str), "bounded(" + ind + ") boundType");
        }
        Signature.TypeArg u = Signature.TypeArg.unbounded();
        check(u instanceof Signature.TypeArg.Unbounded, "unbounded -> Unbounded");
        check(!(u instanceof Signature.TypeArg.Bounded), "unbounded not Bounded");
        check(Signature.TypeArg.unbounded() == Signature.TypeArg.unbounded(), "unbounded singleton ==");
        check(u.equals(Signature.TypeArg.unbounded()), "unbounded equals");
        // boundType 三种 RefTypeSig 均可
        check(Signature.TypeArg.of((Signature.RefTypeSig) Signature.parseFrom("TT;")).boundType()
                instanceof Signature.TypeVarSig, "TypeVar bound");
        check(Signature.TypeArg.of((Signature.RefTypeSig) Signature.parseFrom("[I")).boundType()
                instanceof Signature.ArrayTypeSig, "Array bound");
    }

    // ---- Part 4: null 负例全表（实测全部 NPE） ----
    static void testNullMatrix() {
        Signature.RefTypeSig str = (Signature.RefTypeSig) Signature.parseFrom("Ljava/lang/String;");
        Runnable[] calls = {
                () -> Signature.TypeArg.of(null),
                () -> Signature.TypeArg.extendsOf(null),
                () -> Signature.TypeArg.superOf(null),
                () -> Signature.TypeArg.bounded(null, str),
                () -> Signature.TypeArg.bounded(Signature.TypeArg.Bounded.WildcardIndicator.EXTENDS, null),
        };
        String[] names = {"of(null)", "extendsOf(null)", "superOf(null)", "bounded(null, sig)", "bounded(ind, null)"};
        for (int i = 0; i < calls.length; i++) {
            final int idx = i;
            boolean threw = false;
            try {
                calls[idx].run();
            } catch (NullPointerException e) {
                threw = true;
            }
            check(threw, names[idx] + " must throw NPE");
        }
    }

    // ---- Part 5: 嵌入解析矩阵（typeArgs() 元素 instanceof 分型） ----
    static void testEmbeddingMatrix() {
        Signature.ClassTypeSig lst = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/List<Ljava/lang/String;>;");
        check(lst.typeArgs().get(0) instanceof Signature.TypeArg.Bounded, "bare arg Bounded");
        check(((Signature.TypeArg.Bounded) lst.typeArgs().get(0)).boundType().signatureString().equals("Ljava/lang/String;"), "bare arg bound");
        Signature.ClassTypeSig un = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/List<*>;");
        check(un.typeArgs().get(0) instanceof Signature.TypeArg.Unbounded, "* arg Unbounded");
        check(!(un.typeArgs().get(0) instanceof Signature.TypeArg.Bounded), "* arg not Bounded");
        Signature.ClassTypeSig ext = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/List<+Ljava/lang/Number;>;");
        check(((Signature.TypeArg.Bounded) ext.typeArgs().get(0)).wildcardIndicator()
                == Signature.TypeArg.Bounded.WildcardIndicator.EXTENDS, "+ arg EXTENDS");
        Signature.ClassTypeSig sup = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/List<-Ljava/lang/Number;>;");
        check(((Signature.TypeArg.Bounded) sup.typeArgs().get(0)).wildcardIndicator()
                == Signature.TypeArg.Bounded.WildcardIndicator.SUPER, "- arg SUPER");
        Signature.ClassTypeSig tv = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/List<TT;>;");
        check(((Signature.TypeArg.Bounded) tv.typeArgs().get(0)).boundType() instanceof Signature.TypeVarSig, "TypeVar arg");
        Signature.ClassTypeSig arr = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/List<[I>;");
        check(((Signature.TypeArg.Bounded) arr.typeArgs().get(0)).boundType() instanceof Signature.ArrayTypeSig, "array arg");
        // 多实参与无实参
        Signature.ClassTypeSig map = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/Map<TK;TV;>;");
        check(map.typeArgs().size() == 2, "Map 2 args");
        check(Signature.parseFrom("Ljava/util/List<Ljava/lang/String;>;") instanceof Signature.ClassTypeSig c2
                && c2.typeArgs().size() == 1, "List 1 arg");
    }

    // ---- Part 6: 组合发射（ClassTypeSig.of 携带各 TypeArg 形态 → 精确串） ----
    static void testCompositionEmission() {
        Signature.RefTypeSig str = (Signature.RefTypeSig) Signature.parseFrom("Ljava/lang/String;");
        check(Signature.ClassTypeSig.of(ClassDesc.of("java.util.List"), Signature.TypeArg.of(str)).signatureString()
                .equals("Ljava/util/List<Ljava/lang/String;>;"), "emit NONE arg");
        check(Signature.ClassTypeSig.of(ClassDesc.of("java.util.List"), Signature.TypeArg.extendsOf(str)).signatureString()
                .equals("Ljava/util/List<+Ljava/lang/String;>;"), "emit EXTENDS arg");
        check(Signature.ClassTypeSig.of(ClassDesc.of("java.util.List"), Signature.TypeArg.superOf(str)).signatureString()
                .equals("Ljava/util/List<-Ljava/lang/String;>;"), "emit SUPER arg");
        check(Signature.ClassTypeSig.of(ClassDesc.of("java.util.List"), Signature.TypeArg.unbounded()).signatureString()
                .equals("Ljava/util/List<*>;"), "emit unbounded arg");
        check(Signature.ClassTypeSig.of(ClassDesc.of("java.util.Map"),
                        Signature.TypeArg.unbounded(), Signature.TypeArg.of(str)).signatureString()
                .equals("Ljava/util/Map<*Ljava/lang/String;>;"), "emit Map mixed args");
        // 发射产物 re-parse 回到等值模型
        Signature.ClassTypeSig emitted = Signature.ClassTypeSig.of(ClassDesc.of("java.util.List"), Signature.TypeArg.unbounded());
        check(Signature.parseFrom(emitted.signatureString()).equals(emitted), "emitted model re-parse equals");
    }

    // ---- Part 7: equals/hashCode 值语义 ----
    static void testEqualityContract() {
        Signature.RefTypeSig num = (Signature.RefTypeSig) Signature.parseFrom("Ljava/lang/Number;");
        check(Signature.TypeArg.of(num).equals(Signature.TypeArg.of(num)), "Bounded value equals");
        check(Signature.TypeArg.of(num).hashCode() == Signature.TypeArg.of(num).hashCode(), "hashCode consistent");
        check(Signature.TypeArg.extendsOf(num).equals(
                Signature.TypeArg.bounded(Signature.TypeArg.Bounded.WildcardIndicator.EXTENDS, num)), "extendsOf == bounded(EXTENDS)");
        check(!Signature.TypeArg.of(num).equals(Signature.TypeArg.extendsOf(num)), "NONE vs EXTENDS not equals");
        check(!Signature.TypeArg.of(num).equals(Signature.TypeArg.unbounded()), "Bounded vs Unbounded not equals");
        check(!Signature.TypeArg.of(num).equals(null), "not equals null");
    }

    // ---- Part 8: 真类泛型闭环（javac 产物 List<String>/List<?> 字段 → TypeArg 实参） ----
    static void testRealClassGenericLoop() throws Exception {
        byte[] self = TypeArgTest.class.getResourceAsStream("TypeArgTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        for (String[] row : new String[][]{{"GENERIC_FIELD", "Ljava/util/List<Ljava/lang/String;>;"},
                {"WILDCARD_FIELD", "Ljava/util/List<*>;"}}) {
            FieldModel fm = null;
            for (FieldModel f : cm.fields()) if (f.fieldName().stringValue().equals(row[0])) fm = f;
            check(fm != null, row[0] + " found");
            String fs = fm.findAttribute(Attributes.signature()).orElseThrow().signature().stringValue();
            check(fs.equals(row[1]), row[0] + " sig exact: " + fs);
            Signature.ClassTypeSig model = (Signature.ClassTypeSig) Signature.parseFrom(fs);
            check(model.typeArgs().size() == 1, row[0] + " 1 arg");
            check(model.typeArgs().get(0) instanceof Signature.TypeArg, row[0] + " arg is TypeArg");
            check(Signature.parseFrom(model.signatureString()).equals(model), row[0] + " model idempotent");
        }
        FieldModel gf = null;
        for (FieldModel f : cm.fields()) if (f.fieldName().stringValue().equals("GENERIC_FIELD")) gf = f;
        Signature.ClassTypeSig gm = (Signature.ClassTypeSig) Signature.parseFrom(
                gf.findAttribute(Attributes.signature()).orElseThrow().signature().stringValue());
        check(gm.typeArgs().get(0) instanceof Signature.TypeArg.Bounded, "GENERIC_FIELD arg Bounded");
        FieldModel wf = null;
        for (FieldModel f : cm.fields()) if (f.fieldName().stringValue().equals("WILDCARD_FIELD")) wf = f;
        Signature.ClassTypeSig wm = (Signature.ClassTypeSig) Signature.parseFrom(
                wf.findAttribute(Attributes.signature()).orElseThrow().signature().stringValue());
        check(wm.typeArgs().get(0) instanceof Signature.TypeArg.Unbounded, "WILDCARD_FIELD arg Unbounded");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryMatrix();
        testNullMatrix();
        testEmbeddingMatrix();
        testCompositionEmission();
        testEqualityContract();
        testRealClassGenericLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

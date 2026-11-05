/*
 * @test
 * @summary java.lang.classfile.AnnotationValue.OfEnum conformance: white-box structure + black-box behavior.
 * @run main OfEnumTest
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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class OfEnumTest {
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
        Class<?> c = Class.forName("java.lang.classfile.AnnotationValue$OfEnum");
        check(c != null, "java.lang.classfile.AnnotationValue.OfEnum loads");
        check(c.getName().equals("java.lang.classfile.AnnotationValue$OfEnum"), "binary name java.lang.classfile.AnnotationValue$OfEnum");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.AnnotationValue.OfEnum is public");
        check(c.isInterface(), "java.lang.classfile.AnnotationValue.OfEnum is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.AnnotationValue.OfEnum has no superclass");
        check(declares(c, "className"), "declares className");
        check(declares(c, "classSymbol"), "declares classSymbol");
        check(declares(c, "constantName"), "declares constantName");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        AnnotationValue.OfEnum v = AnnotationValue.ofEnum(ClassDesc.of("java.lang.annotation.RetentionPolicy"), "RUNTIME");
        check(v.tag() == AnnotationValue.TAG_ENUM, "tag");
        check(v.classSymbol().equals(ClassDesc.of("java.lang.annotation.RetentionPolicy")), "classSymbol");
        check(v.constantName().stringValue().equals("RUNTIME"), "constantName");

    }


    // ===================== 契约深化（2026-11-05 追加，不改动既有内容） =====================

    static final ClassDesc CD_RP = ClassDesc.of("java.lang.annotation.RetentionPolicy");

    // ---- 深化 A: 工厂矩阵（ClassDesc,String 两段式） ----
    static void testDeepFactoryMatrix() {
        AnnotationValue.OfEnum v = AnnotationValue.ofEnum(CD_RP, "RUNTIME");
        check(v.tag() == AnnotationValue.TAG_ENUM, "deep A: ofEnum tag 常量");
        check(v.tag() == 101, "deep A: tag 精确值 101 ('e')");
        check(v.className().stringValue().equals("Ljava/lang/annotation/RetentionPolicy;"), "deep A: className 描述符");
        check(v.classSymbol().descriptorString().equals("Ljava/lang/annotation/RetentionPolicy;"), "deep A: classSymbol 描述符");
        check(v.classSymbol().equals(CD_RP), "deep A: classSymbol == ClassDesc");
        check(v.constantName().stringValue().equals("RUNTIME"), "deep A: constantName");
        check(v.className() instanceof Utf8Entry && v.constantName() instanceof Utf8Entry, "deep A: 两段均为 Utf8Entry");
    }

    // ---- 深化 B: Utf8Entry 重载（两段条目同一性复用） ----
    static void testDeepEntryFactory() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        Utf8Entry cls = cpb.utf8Entry("LE;");
        Utf8Entry nm = cpb.utf8Entry("X");
        AnnotationValue.OfEnum ev = AnnotationValue.ofEnum(cls, nm);
        check(ev.className() == cls, "deep B: className == 原池条目");
        check(ev.constantName() == nm, "deep B: constantName == 原池条目");
        check(ev.classSymbol().descriptorString().equals("LE;"), "deep B: classSymbol 解析自 utf8");
        check(ev.tag() == AnnotationValue.TAG_ENUM, "deep B: 条目形态 tag");
    }

    // ---- 深化 C: null 负例（四组合） ----
    static void testDeepNullNegatives() {
        try { AnnotationValue.ofEnum((ClassDesc) null, "X"); check(false, "deep C: ofEnum(null,..) 必须 NPE"); }
        catch (NullPointerException e) { check(e.getMessage() != null && e.getMessage().contains("desc"), "deep C: null ClassDesc NPE 消息含 desc"); }
        try { AnnotationValue.ofEnum(CD_RP, (String) null); check(false, "deep C: ofEnum(cd,null) 必须 NPE"); }
        catch (NullPointerException e) { check(e.getMessage() != null && e.getMessage().contains("s"), "deep C: null 常量名 NPE 消息含 s"); }
        try { AnnotationValue.ofEnum((Utf8Entry) null, cpbUtf8()); check(false, "deep C: ofEnum(null条目,..) 必须 NPE"); }
        catch (NullPointerException e) { check(true, "deep C: null Utf8Entry 类名 NPE"); }
        try { AnnotationValue.ofEnum(cpbUtf8(), (Utf8Entry) null); check(false, "deep C: ofEnum(ue,null) 必须 NPE"); }
        catch (NullPointerException e) { check(true, "deep C: null Utf8Entry 常量名 NPE"); }
    }

    static Utf8Entry cpbUtf8() { return ConstantPoolBuilder.of().utf8Entry("LE;"); }

    // ---- 深化 D: 层次（OfEnum 不属 OfConstant，编译器可静态证伪） ----
    static void testDeepHierarchy() {
        AnnotationValue v = AnnotationValue.ofEnum(CD_RP, "RUNTIME");
        check(v instanceof AnnotationValue, "deep D: OfEnum instanceof AnnotationValue");
        check(!AnnotationValue.OfConstant.class.isInstance(v), "deep D: OfEnum 不属 OfConstant");
        check(!AnnotationValue.OfClass.class.isInstance(v) && !AnnotationValue.OfArray.class.isInstance(v)
                && !AnnotationValue.OfAnnotation.class.isInstance(v), "deep D: 不是 class/array/annotation 形态");
        check(!AnnotationValue.OfInt.class.isInstance(v) && !AnnotationValue.OfString.class.isInstance(v), "deep D: 不是常量形态");
    }

    // ---- 深化 E: build->re-parse 全等（enum 两段保真） ----
    static void testDeepBoundaryRoundTrip() {
        AnnotationValue r = rtOne("PvOfEnum", AnnotationElement.of("e", AnnotationValue.ofEnum(CD_RP, "SOURCE")));
        check(r instanceof AnnotationValue.OfEnum o && o.className().stringValue().equals("Ljava/lang/annotation/RetentionPolicy;"),
                "deep E: RT className");
        check(((AnnotationValue.OfEnum) r).constantName().stringValue().equals("SOURCE"), "deep E: RT constantName");
        check(((AnnotationValue.OfEnum) r).classSymbol().equals(CD_RP), "deep E: RT classSymbol");
        check(((AnnotationValue.OfEnum) r).tag() == AnnotationValue.TAG_ENUM, "deep E: RT tag");
    }

    // ---- 深化 F: equals/hashCode/toString ----
    static void testDeepEqualsHashCodeToString() {
        check(AnnotationValue.ofEnum(CD_RP, "A").equals(AnnotationValue.ofEnum(CD_RP, "A")), "deep F: 同形 equals");
        check(AnnotationValue.ofEnum(CD_RP, "A").hashCode() == AnnotationValue.ofEnum(CD_RP, "A").hashCode(), "deep F: hashCode 一致");
        check(!AnnotationValue.ofEnum(CD_RP, "A").equals(AnnotationValue.ofEnum(CD_RP, "B")), "deep F: 异常量名不 equals");
        check(!AnnotationValue.ofEnum(CD_RP, "A").equals(AnnotationValue.ofEnum(ClassDesc.of("Other"), "A")), "deep F: 异类不 equals");
        check(!AnnotationValue.ofEnum(CD_RP, "A").equals(null), "deep F: equals(null) false");
        AnnotationValue.OfEnum oe = AnnotationValue.ofEnum(CD_RP, "RUNTIME");
        check(oe.toString().contains("RUNTIME") && oe.toString().contains("Ljava/lang/annotation/RetentionPolicy;"), "deep F: toString 含常量名+描述符");
    }

    // ---- 深化 G: javac 真类产物闭环 ----
    static void testDeepRealJavacProduct() throws Exception {
        byte[] self = OfEnumTest.class.getResourceAsStream("OfEnumTest.class").readAllBytes();
        FieldModel fm = null;
        for (FieldModel f : ClassFile.of().parse(self).fields())
            if (f.fieldName().stringValue().equals("annotatedField")) fm = f;
        check(fm != null, "deep G: annotatedField 找到");
        RuntimeVisibleAnnotationsAttribute rva = fm.findAttribute(Attributes.runtimeVisibleAnnotations()).get();
        Annotation a = rva.annotations().get(0);
        check(a.className().stringValue().equals("LOfEnumTest$EnumAnno;"), "deep G: className 描述符");
        check(a.elements().size() == 2, "deep G: 仅显式元素");
        check(elemOf(a, "e") instanceof AnnotationValue.OfEnum xe && xe.constantName().stringValue().equals("RUNTIME")
                && xe.classSymbol().equals(CD_RP), "deep G: 真类 enum 元素 RUNTIME");
        check(elemOf(a, "f") instanceof AnnotationValue.OfEnum xf && xf.constantName().stringValue().equals("CLASS"), "deep G: 真类 enum 元素 CLASS");
        byte[] ab = OfEnumTest.class.getResourceAsStream("OfEnumTest$EnumAnno.class").readAllBytes();
        MethodModel vm = null, wm = null;
        for (MethodModel mm : ClassFile.of().parse(ab).methods()) {
            if (mm.methodName().stringValue().equals("e")) vm = mm;
            if (mm.methodName().stringValue().equals("f")) wm = mm;
        }
        AnnotationDefaultAttribute da = vm.findAttribute(Attributes.annotationDefault()).get();
        check(da.defaultValue() instanceof AnnotationValue.OfEnum dv && dv.constantName().stringValue().equals("SOURCE"), "deep G: AnnotationDefault e()==SOURCE");
        check(!wm.findAttribute(Attributes.annotationDefault()).isPresent(), "deep G: 无 default 成员无 AnnotationDefault");
    }

    // ---- 真类产物：嵌套 @interface 与被注解字段 ----
    @Retention(RetentionPolicy.RUNTIME)
    @interface EnumAnno {
        RetentionPolicy e() default RetentionPolicy.SOURCE;
        RetentionPolicy f();
    }

    @EnumAnno(e = RetentionPolicy.RUNTIME, f = RetentionPolicy.CLASS)
    static int annotatedField;

    // ---- 契约深化共享工具 ----
    static AnnotationValue rtOne(String holder, AnnotationElement el) {
        byte[] b = ClassFile.of().build(ClassDesc.of(holder),
                cb -> cb.with(RuntimeVisibleAnnotationsAttribute.of(Annotation.of(ClassDesc.of("pkg.RT"), el))));
        return ClassFile.of().parse(b).findAttribute(Attributes.runtimeVisibleAnnotations()).get()
                .annotations().get(0).elements().get(0).value();
    }

    static AnnotationValue elemOf(Annotation a, String name) {
        for (AnnotationElement e : a.elements())
            if (e.name().stringValue().equals(name)) return e.value();
        return null;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepFactoryMatrix();
        testDeepEntryFactory();
        testDeepNullNegatives();
        testDeepHierarchy();
        testDeepBoundaryRoundTrip();
        testDeepEqualsHashCodeToString();
        testDeepRealJavacProduct();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

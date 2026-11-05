/*
 * @test
 * @summary java.lang.classfile.AnnotationValue.OfAnnotation conformance: white-box structure + black-box behavior.
 * @run main OfAnnotationTest
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

public class OfAnnotationTest {
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
        Class<?> c = Class.forName("java.lang.classfile.AnnotationValue$OfAnnotation");
        check(c != null, "java.lang.classfile.AnnotationValue.OfAnnotation loads");
        check(c.getName().equals("java.lang.classfile.AnnotationValue$OfAnnotation"), "binary name java.lang.classfile.AnnotationValue$OfAnnotation");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.AnnotationValue.OfAnnotation is public");
        check(c.isInterface(), "java.lang.classfile.AnnotationValue.OfAnnotation is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.AnnotationValue.OfAnnotation has no superclass");
        check(declares(c, "annotation"), "declares annotation");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        Annotation inner = Annotation.of(ClassDesc.of("java.lang.Deprecated"));
        AnnotationValue.OfAnnotation v = AnnotationValue.ofAnnotation(inner);
        check(v.tag() == AnnotationValue.TAG_ANNOTATION, "tag");
        check(v.annotation() == inner, "annotation()");
        check(v.annotation().classSymbol().equals(ClassDesc.of("java.lang.Deprecated")), "inner classSymbol");

    }


    // ===================== 契约深化（2026-11-05 追加，不改动既有内容） =====================

    // ---- 深化 A: 工厂矩阵（annotation() 同一性） ----
    static void testDeepFactoryMatrix() {
        Annotation inner = Annotation.of(ClassDesc.of("pkg.In"),
                AnnotationElement.ofInt("q", 3), AnnotationElement.ofString("s", "x"));
        AnnotationValue.OfAnnotation v = AnnotationValue.ofAnnotation(inner);
        check(v.tag() == AnnotationValue.TAG_ANNOTATION, "deep A: ofAnnotation tag 常量");
        check(v.tag() == 64, "deep A: tag 精确值 64 ('@')");
        check(v.annotation() == inner, "deep A: annotation() == 原实例（同一性）");
        check(v.annotation().className().stringValue().equals("Lpkg/In;"), "deep A: 嵌套注解 className");
        check(v.annotation().elements().size() == 2, "deep A: 嵌套注解元素数");
    }

    // ---- 深化 B: null 负例 ----
    static void testDeepNullNegatives() {
        try { AnnotationValue.ofAnnotation(null); check(false, "deep B: ofAnnotation(null) 必须 NPE"); }
        catch (NullPointerException e) { check(true, "deep B: ofAnnotation(null) NPE"); }
    }

    // ---- 深化 C: 层次（OfAnnotation 不属 OfConstant） ----
    static void testDeepHierarchy() {
        AnnotationValue v = AnnotationValue.ofAnnotation(Annotation.of(ClassDesc.of("pkg.I")));
        check(v instanceof AnnotationValue, "deep C: OfAnnotation instanceof AnnotationValue");
        check(!AnnotationValue.OfConstant.class.isInstance(v), "deep C: OfAnnotation 不属 OfConstant");
        check(!AnnotationValue.OfEnum.class.isInstance(v) && !AnnotationValue.OfClass.class.isInstance(v)
                && !AnnotationValue.OfArray.class.isInstance(v), "deep C: 不是 enum/class/array 形态");
    }

    // ---- 深化 D: build->re-parse 深度回读（一级/二级嵌套） ----
    static void testDeepRoundTrip() {
        Annotation inner = Annotation.of(ClassDesc.of("pkg.In"),
                AnnotationElement.ofInt("q", 3), AnnotationElement.ofString("s", "x"));
        AnnotationValue rt = rtOne("PvOfAnnotation0", AnnotationElement.ofAnnotation("n", inner));
        check(rt instanceof AnnotationValue.OfAnnotation, "deep D: RT 形态");
        Annotation back = ((AnnotationValue.OfAnnotation) rt).annotation();
        check(back.className().stringValue().equals("Lpkg/In;"), "deep D: RT className");
        check(elemOf(back, "q") instanceof AnnotationValue.OfInt qi && qi.intValue() == 3, "deep D: RT 嵌套元素 q");
        check(elemOf(back, "s") instanceof AnnotationValue.OfString si && si.stringValue().equals("x"), "deep D: RT 嵌套元素 s");
        check(rt.tag() == AnnotationValue.TAG_ANNOTATION, "deep D: RT tag");
        Annotation lvl3 = Annotation.of(ClassDesc.of("pkg.L3"), AnnotationElement.ofInt("z", 1));
        Annotation lvl2 = Annotation.of(ClassDesc.of("pkg.L2"), AnnotationElement.ofAnnotation("down", lvl3));
        Annotation lvl1 = Annotation.of(ClassDesc.of("pkg.L1"), AnnotationElement.ofAnnotation("down", lvl2));
        AnnotationValue rt2 = rtOne("PvOfAnnotation1", AnnotationElement.ofAnnotation("top", lvl1));
        Annotation a1 = ((AnnotationValue.OfAnnotation) rt2).annotation();
        Annotation a2 = ((AnnotationValue.OfAnnotation) elemOf(a1, "down")).annotation();
        Annotation a3 = ((AnnotationValue.OfAnnotation) elemOf(a2, "down")).annotation();
        check(a2.className().stringValue().equals("Lpkg/L2;") && a3.className().stringValue().equals("Lpkg/L3;")
                && ((AnnotationValue.OfInt) elemOf(a3, "z")).intValue() == 1, "deep D: 二级嵌套全等");
    }

    // ---- 深化 E: equals/hashCode/toString ----
    static void testDeepEqualsHashCodeToString() {
        Annotation in = Annotation.of(ClassDesc.of("pkg.I"), AnnotationElement.ofInt("q", 1));
        Annotation same = Annotation.of(ClassDesc.of("pkg.I"), AnnotationElement.ofInt("q", 1));
        check(AnnotationValue.ofAnnotation(in).equals(AnnotationValue.ofAnnotation(same)), "deep E: 同形 equals");
        check(AnnotationValue.ofAnnotation(in).hashCode() == AnnotationValue.ofAnnotation(same).hashCode(), "deep E: hashCode 一致");
        check(!AnnotationValue.ofAnnotation(in).equals(AnnotationValue.ofAnnotation(Annotation.of(ClassDesc.of("pkg.J")))), "deep E: 异内层不 equals");
        check(!AnnotationValue.ofAnnotation(in).equals(null), "deep E: equals(null) false");
        check(AnnotationValue.ofAnnotation(in).toString().contains("Lpkg/I;"), "deep E: toString 含内层描述符");
    }

    // ---- 深化 F: javac 真类产物闭环（嵌套注解成员 + AnnotationDefault） ----
    static void testDeepRealJavacProduct() throws Exception {
        byte[] self = OfAnnotationTest.class.getResourceAsStream("OfAnnotationTest.class").readAllBytes();
        FieldModel fm = null;
        for (FieldModel f : ClassFile.of().parse(self).fields())
            if (f.fieldName().stringValue().equals("annotatedField")) fm = f;
        check(fm != null, "deep F: annotatedField 找到");
        RuntimeVisibleAnnotationsAttribute rva = fm.findAttribute(Attributes.runtimeVisibleAnnotations()).get();
        Annotation a = rva.annotations().get(0);
        check(a.className().stringValue().equals("LOfAnnotationTest$AnnoAnno;"), "deep F: className 描述符");
        check(a.elements().size() == 2, "deep F: 仅显式元素");
        check(elemOf(a, "n") instanceof AnnotationValue.OfAnnotation, "deep F: n 是 OfAnnotation");
        Annotation nBack = ((AnnotationValue.OfAnnotation) elemOf(a, "n")).annotation();
        check(nBack.className().stringValue().equals("LOfAnnotationTest$InnerAnno;"), "deep F: 内层 className");
        check(((AnnotationValue.OfInt) elemOf(nBack, "q")).intValue() == 5, "deep F: 内层 q=5");
        Annotation mBack = ((AnnotationValue.OfAnnotation) elemOf(a, "m")).annotation();
        check(mBack.elements().isEmpty(), "deep F: @InnerAnno 无实参 -> 零元素（default 不序列化）");
        byte[] ab = OfAnnotationTest.class.getResourceAsStream("OfAnnotationTest$AnnoAnno.class").readAllBytes();
        MethodModel vm = null, wm = null;
        for (MethodModel mm : ClassFile.of().parse(ab).methods()) {
            if (mm.methodName().stringValue().equals("n")) vm = mm;
            if (mm.methodName().stringValue().equals("m")) wm = mm;
        }
        AnnotationDefaultAttribute da = vm.findAttribute(Attributes.annotationDefault()).get();
        check(da.defaultValue() instanceof AnnotationValue.OfAnnotation dv
                && dv.annotation().className().stringValue().equals("LOfAnnotationTest$InnerAnno;")
                && ((AnnotationValue.OfInt) elemOf(dv.annotation(), "q")).intValue() == 9,
                "deep F: AnnotationDefault n()==@InnerAnno(q=9)");
        check(!wm.findAttribute(Attributes.annotationDefault()).isPresent(), "deep F: 无 default 成员无 AnnotationDefault");
    }

    // ---- 真类产物：嵌套 @interface 与被注解字段 ----
    @Retention(RetentionPolicy.RUNTIME)
    @interface InnerAnno {
        int q() default 9;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface AnnoAnno {
        InnerAnno n() default @InnerAnno(q = 9);
        InnerAnno m();
    }

    @AnnoAnno(n = @InnerAnno(q = 5), m = @InnerAnno)
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
        testDeepNullNegatives();
        testDeepHierarchy();
        testDeepRoundTrip();
        testDeepEqualsHashCodeToString();
        testDeepRealJavacProduct();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

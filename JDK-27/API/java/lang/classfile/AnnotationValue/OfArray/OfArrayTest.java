/*
 * @test
 * @summary java.lang.classfile.AnnotationValue.OfArray conformance: white-box structure + black-box behavior.
 * @run main OfArrayTest
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

public class OfArrayTest {
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
        Class<?> c = Class.forName("java.lang.classfile.AnnotationValue$OfArray");
        check(c != null, "java.lang.classfile.AnnotationValue.OfArray loads");
        check(c.getName().equals("java.lang.classfile.AnnotationValue$OfArray"), "binary name java.lang.classfile.AnnotationValue$OfArray");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.AnnotationValue.OfArray is public");
        check(c.isInterface(), "java.lang.classfile.AnnotationValue.OfArray is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.AnnotationValue.OfArray has no superclass");
        check(declares(c, "values"), "declares values");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        AnnotationValue.OfArray v = AnnotationValue.ofArray(AnnotationValue.ofInt(1), AnnotationValue.ofInt(2), AnnotationValue.ofInt(3));
        check(v.tag() == AnnotationValue.TAG_ARRAY, "tag");
        check(v.values().size() == 3, "size 3");
        check(((AnnotationValue.OfInt) v.values().get(0)).intValue() == 1, "first int");
        check(((AnnotationValue.OfInt) v.values().get(2)).intValue() == 3, "last int");

    }


    // ===================== 契约深化（2026-11-05 追加，不改动既有内容） =====================

    // ---- 深化 A: 工厂矩阵（varargs/List 两形态 + 空数组 + 只读 + 嵌套） ----
    static void testDeepFactoryMatrix() {
        AnnotationValue.OfArray v = AnnotationValue.ofArray(AnnotationValue.ofInt(1), AnnotationValue.ofString("x"));
        check(v.tag() == AnnotationValue.TAG_ARRAY, "deep A: ofArray tag 常量");
        check(v.tag() == 91, "deep A: tag 精确值 91 ('[')");
        check(v.values().size() == 2, "deep A: 元素数");
        check(((AnnotationValue.OfInt) v.values().get(0)).intValue() == 1, "deep A: 顺序 0");
        check(((AnnotationValue.OfString) v.values().get(1)).stringValue().equals("x"), "deep A: 顺序 1");
        check(v.values() == v.values(), "deep A: values() 稳定同一实例");
        try { v.values().add(AnnotationValue.ofInt(9)); check(false, "deep A: values() add 必须 UOE"); }
        catch (UnsupportedOperationException e) { check(true, "deep A: values() 只读 UOE"); }
        check(AnnotationValue.ofArray().values().isEmpty(), "deep A: varargs 空数组");
        check(AnnotationValue.ofArray(List.of()).values().isEmpty(), "deep A: List 空数组");
        check(AnnotationValue.ofArray(List.of(AnnotationValue.ofInt(1))).equals(AnnotationValue.ofArray(AnnotationValue.ofInt(1))),
                "deep A: List 形态 == varargs 形态");
        AnnotationValue.OfArray nested = AnnotationValue.ofArray(AnnotationValue.ofArray(AnnotationValue.ofInt(1)));
        check(((AnnotationValue.OfArray) nested.values().get(0)).values().size() == 1, "deep A: 嵌套数组");
        AnnotationValue.OfArray withAnno = AnnotationValue.ofArray(AnnotationValue.ofAnnotation(Annotation.of(ClassDesc.of("pkg.Q"))));
        check(((AnnotationValue.OfAnnotation) withAnno.values().get(0)).annotation().classSymbol().equals(ClassDesc.of("pkg.Q")), "deep A: 数组内注解");
    }

    // ---- 深化 B: null 负例（List/数组/含 null 元素，消息各异） ----
    static void testDeepNullNegatives() {
        try { AnnotationValue.ofArray((java.util.List<AnnotationValue>) null); check(false, "deep B: ofArray((List)null) 必须 NPE"); }
        catch (NullPointerException e) { check(e.getMessage() != null && e.getMessage().contains("coll"), "deep B: null List NPE 消息含 coll"); }
        try { AnnotationValue.ofArray((AnnotationValue[]) null); check(false, "deep B: ofArray((AV[])null) 必须 NPE"); }
        catch (NullPointerException e) { check(e.getMessage() != null && e.getMessage().contains("elements"), "deep B: null 数组 NPE 消息含 elements"); }
        try { AnnotationValue.ofArray(java.util.Arrays.asList((AnnotationValue) null)); check(false, "deep B: List 含 null 元素必须 NPE"); }
        catch (NullPointerException e) { check(true, "deep B: List 含 null 元素 NPE"); }
        try { AnnotationValue.ofArray(new AnnotationValue[]{null}); check(false, "deep B: varargs 含 null 元素必须 NPE"); }
        catch (NullPointerException e) { check(true, "deep B: varargs 含 null 元素 NPE"); }
    }

    // ---- 深化 C: 层次（OfArray 不属 OfConstant） ----
    static void testDeepHierarchy() {
        AnnotationValue v = AnnotationValue.ofArray();
        check(v instanceof AnnotationValue, "deep C: OfArray instanceof AnnotationValue");
        check(!AnnotationValue.OfConstant.class.isInstance(v), "deep C: OfArray 不属 OfConstant");
        check(!AnnotationValue.OfEnum.class.isInstance(v) && !AnnotationValue.OfClass.class.isInstance(v)
                && !AnnotationValue.OfAnnotation.class.isInstance(v), "deep C: 不是 enum/class/annotation 形态");
        check(!AnnotationValue.OfInt.class.isInstance(v), "deep C: 不是 OfInt");
    }

    // ---- 深化 D: 全 13 形态混合数组 build->re-parse 保序（tag 序列实测 IsZJBSCFDce@[） ----
    static void testDeepRoundTrip() {
        AnnotationValue mixed = AnnotationValue.ofArray(
                AnnotationValue.ofInt(1), AnnotationValue.ofString("two"), AnnotationValue.ofBoolean(false),
                AnnotationValue.ofLong(4L), AnnotationValue.ofByte((byte) 5), AnnotationValue.ofShort((short) 6),
                AnnotationValue.ofChar('7'), AnnotationValue.ofFloat(8f), AnnotationValue.ofDouble(9d),
                AnnotationValue.ofClass(ClassDesc.of("java.lang.Object")),
                AnnotationValue.ofEnum(ClassDesc.of("java.lang.annotation.RetentionPolicy"), "SOURCE"),
                AnnotationValue.ofAnnotation(Annotation.of(ClassDesc.of("pkg.F"))),
                AnnotationValue.ofArray());
        AnnotationValue r = rtOne("PvOfArray0", AnnotationElement.of("m", mixed));
        check(r instanceof AnnotationValue.OfArray, "deep D: RT 形态");
        java.util.List<AnnotationValue> vals = ((AnnotationValue.OfArray) r).values();
        check(vals.size() == 13, "deep D: 13 元素保序");
        StringBuilder tags = new StringBuilder();
        for (AnnotationValue q : vals) tags.append((char) q.tag());
        check(tags.toString().equals("IsZJBSCFDce@["), "deep D: 混合 tag 序列 " + tags);
        check(((AnnotationValue.OfArray) vals.get(12)).values().isEmpty(), "deep D: 末位空数组嵌套往返");
        AnnotationValue empty = rtOne("PvOfArray1", AnnotationElement.of("e", AnnotationValue.ofArray()));
        check(empty instanceof AnnotationValue.OfArray oe && oe.values().isEmpty() && oe.tag() == AnnotationValue.TAG_ARRAY, "deep D: RT 空数组");
        AnnotationValue nestedRt = rtOne("PvOfArray2", AnnotationElement.of("n",
                AnnotationValue.ofArray(AnnotationValue.ofArray(AnnotationValue.ofInt(1), AnnotationValue.ofInt(2)))));
        check(((AnnotationValue.OfArray) ((AnnotationValue.OfArray) nestedRt).values().get(0)).values().size() == 2, "deep D: RT 嵌套数组");
        AnnotationValue annoInArr = rtOne("PvOfArray3", AnnotationElement.of("a",
                AnnotationValue.ofArray(AnnotationValue.ofAnnotation(Annotation.of(ClassDesc.of("pkg.Z"), AnnotationElement.ofInt("q", 3))))));
        Annotation innerBack = ((AnnotationValue.OfAnnotation) ((AnnotationValue.OfArray) annoInArr).values().get(0)).annotation();
        check(innerBack.className().stringValue().equals("Lpkg/Z;") && ((AnnotationValue.OfInt) elemOf(innerBack, "q")).intValue() == 3,
                "deep D: RT 数组内注解深度回读");
    }

    // ---- 深化 E: equals/hashCode/toString ----
    static void testDeepEqualsHashCodeToString() {
        check(AnnotationValue.ofArray().equals(AnnotationValue.ofArray(List.of())), "deep E: 空数组 equals");
        check(AnnotationValue.ofArray(AnnotationValue.ofInt(1)).hashCode()
                == AnnotationValue.ofArray(AnnotationValue.ofInt(1)).hashCode(), "deep E: hashCode 一致");
        check(!AnnotationValue.ofArray(AnnotationValue.ofInt(1)).equals(AnnotationValue.ofArray(AnnotationValue.ofInt(1), AnnotationValue.ofInt(2))),
                "deep E: 异长度不 equals");
        check(!AnnotationValue.ofArray(AnnotationValue.ofInt(1)).equals(AnnotationValue.ofArray(AnnotationValue.ofLong(1L))), "deep E: 异元素形态不 equals");
        check(!AnnotationValue.ofArray().equals(null), "deep E: equals(null) false");
        check(AnnotationValue.ofArray(AnnotationValue.ofInt(1)).toString().contains("values="), "deep E: toString 含 values=");
    }

    // ---- 深化 F: javac 真类产物闭环 ----
    static void testDeepRealJavacProduct() throws Exception {
        byte[] self = OfArrayTest.class.getResourceAsStream("OfArrayTest.class").readAllBytes();
        FieldModel fm = null;
        for (FieldModel f : ClassFile.of().parse(self).fields())
            if (f.fieldName().stringValue().equals("annotatedField")) fm = f;
        check(fm != null, "deep F: annotatedField 找到");
        RuntimeVisibleAnnotationsAttribute rva = fm.findAttribute(Attributes.runtimeVisibleAnnotations()).get();
        Annotation a = rva.annotations().get(0);
        check(a.className().stringValue().equals("LOfArrayTest$ArrayAnno;"), "deep F: className 描述符");
        check(a.elements().size() == 2, "deep F: 仅显式元素");
        check(elemOf(a, "arr") instanceof AnnotationValue.OfArray xa && xa.values().size() == 3
                && ((AnnotationValue.OfInt) xa.values().get(0)).intValue() == 7
                && ((AnnotationValue.OfInt) xa.values().get(1)).intValue() == -7
                && ((AnnotationValue.OfInt) xa.values().get(2)).intValue() == 0, "deep F: 真类数组 {7,-7,0}");
        check(elemOf(a, "brr") instanceof AnnotationValue.OfArray xb && xb.values().isEmpty(), "deep F: 真类空数组 {}");
        byte[] ab = OfArrayTest.class.getResourceAsStream("OfArrayTest$ArrayAnno.class").readAllBytes();
        MethodModel vm = null, wm = null;
        for (MethodModel mm : ClassFile.of().parse(ab).methods()) {
            if (mm.methodName().stringValue().equals("arr")) vm = mm;
            if (mm.methodName().stringValue().equals("brr")) wm = mm;
        }
        AnnotationDefaultAttribute da = vm.findAttribute(Attributes.annotationDefault()).get();
        check(da.defaultValue() instanceof AnnotationValue.OfArray dv && dv.values().size() == 2
                && ((AnnotationValue.OfInt) dv.values().get(0)).intValue() == 1
                && ((AnnotationValue.OfInt) dv.values().get(1)).intValue() == 2, "deep F: AnnotationDefault arr()=={1,2}");
        check(!wm.findAttribute(Attributes.annotationDefault()).isPresent(), "deep F: 无 default 成员无 AnnotationDefault");
    }

    // ---- 真类产物：嵌套 @interface 与被注解字段 ----
    @Retention(RetentionPolicy.RUNTIME)
    @interface ArrayAnno {
        int[] arr() default {1, 2};
        int[] brr();
    }

    @ArrayAnno(arr = {7, -7, 0}, brr = {})
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

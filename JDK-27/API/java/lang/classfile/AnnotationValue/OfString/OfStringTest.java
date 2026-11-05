/*
 * @test
 * @summary java.lang.classfile.AnnotationValue.OfString conformance: white-box structure + black-box behavior.
 * @run main OfStringTest
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

public class OfStringTest {
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
        Class<?> c = Class.forName("java.lang.classfile.AnnotationValue$OfString");
        check(c != null, "java.lang.classfile.AnnotationValue.OfString loads");
        check(c.getName().equals("java.lang.classfile.AnnotationValue$OfString"), "binary name java.lang.classfile.AnnotationValue$OfString");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.AnnotationValue.OfString is public");
        check(c.isInterface(), "java.lang.classfile.AnnotationValue.OfString is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.AnnotationValue.OfString has no superclass");
        check(declares(c, "constant"), "declares constant");
        check(declares(c, "stringValue"), "declares stringValue");
        check(declares(c, "resolvedValue"), "declares resolvedValue");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        AnnotationValue.OfString v = AnnotationValue.ofString("abc");
        check(v.tag() == AnnotationValue.TAG_STRING, "tag");
        check(v.stringValue().equals("abc"), "stringValue");
        check(v.constant().stringValue().equals("abc"), "constant");
        check(v.resolvedValue().equals("abc"), "resolvedValue");

    }


    // ===================== 契约深化（2026-11-05 追加，不改动既有内容） =====================

    // ---- 深化 A: 工厂矩阵 ----
    static void testDeepFactoryMatrix() {
        AnnotationValue.OfString v = AnnotationValue.ofString("hi");
        check(v.tag() == AnnotationValue.TAG_STRING, "deep A: ofString tag 常量");
        check(v.tag() == 115, "deep A: tag 精确值 115 ('s')");
        check(v.stringValue().equals("hi"), "deep A: stringValue 精确回读");
        check(v.resolvedValue().getClass() == String.class && v.resolvedValue().equals("hi"), "deep A: resolvedValue String");
        check(v.constant() instanceof Utf8Entry, "deep A: constant() is Utf8Entry");
        check(v.constant().tag() == PoolEntry.TAG_UTF8, "deep A: 池条目 tag TAG_UTF8(1)");
        check(v.constant().stringValue().equals("hi"), "deep A: constant().stringValue()");
        check(v.constant() == v.constant(), "deep A: constant() 稳定同一实例");
        check(AnnotationValue.of("x") instanceof AnnotationValue.OfString, "deep A: of(String) 分派");
    }

    // ---- 深化 B: Utf8Entry 重载（同一性复用 + utf8 内容直用不解析） ----
    static void testDeepEntryFactory() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        Utf8Entry ue = cpb.utf8Entry("q");
        AnnotationValue.OfString ev = AnnotationValue.ofString(ue);
        check(ev.stringValue().equals("q"), "deep B: ofString(Utf8Entry).stringValue()");
        check(ev.constant() == ue, "deep B: constant() == 原池条目（== 复用）");
        check(ev.tag() == AnnotationValue.TAG_STRING, "deep B: 条目形态 tag 不变");
        check(AnnotationValue.ofString(cpb.utf8Entry("Ljava/lang/String;")).stringValue().equals("Ljava/lang/String;"),
                "deep B: utf8 内容直用（描述符串不解析，实测记录）");
    }

    // ---- 深化 C: null 负例 ----
    static void testDeepNullNegatives() {
        try { AnnotationValue.ofString((String) null); check(false, "deep C: ofString(null) 必须 NPE"); }
        catch (NullPointerException e) { check(e.getMessage() != null && e.getMessage().contains("s"), "deep C: ofString(null) NPE 消息含 s"); }
        try { AnnotationValue.ofString((Utf8Entry) null); check(false, "deep C: ofString(null条目) 必须 NPE"); }
        catch (NullPointerException e) { check(true, "deep C: ofString(null条目) NPE"); }
    }

    // ---- 深化 D: 层次链（OfString 属 OfConstant，实测） ----
    static void testDeepHierarchy() {
        AnnotationValue v = AnnotationValue.ofString("x");
        check(v instanceof AnnotationValue, "deep D: OfString instanceof AnnotationValue");
        check(v instanceof AnnotationValue.OfConstant, "deep D: OfString instanceof OfConstant（字符串也是常量形态，实测）");
        check(!AnnotationValue.OfEnum.class.isInstance(v) && !AnnotationValue.OfClass.class.isInstance(v)
                && !AnnotationValue.OfArray.class.isInstance(v) && !AnnotationValue.OfAnnotation.class.isInstance(v),
                "deep D: 不是 enum/class/array/annotation 形态");
        check(!AnnotationValue.OfInt.class.isInstance(v), "deep D: 不是 OfInt");
    }

    // ---- 深化 E: 空串/Unicode/转义/内嵌 NUL build->re-parse 全等 ----
    static void testDeepBoundaryRoundTrip() {
        AnnotationValue r0 = rtOne("PvOfString0", AnnotationElement.ofString("e", ""));
        check(r0 instanceof AnnotationValue.OfString o0 && o0.stringValue().isEmpty() && o0.tag() == AnnotationValue.TAG_STRING, "deep E: RT 空串");
        AnnotationValue r1 = rtOne("PvOfString1", AnnotationElement.ofString("u", "héllo 世界"));
        check(r1 instanceof AnnotationValue.OfString o1 && o1.stringValue().equals("héllo 世界") && o1.stringValue().length() == 8, "deep E: RT Unicode 串");
        AnnotationValue r2 = rtOne("PvOfString2", AnnotationElement.ofString("q", "a\"b\\c\n"));
        check(r2 instanceof AnnotationValue.OfString o2 && o2.stringValue().equals("a\"b\\c\n"), "deep E: RT 引号/反斜杠/换行");
        AnnotationValue r3 = rtOne("PvOfString3", AnnotationElement.ofString("n", "\0 end"));
        check(r3 instanceof AnnotationValue.OfString o3 && o3.stringValue().equals("\0 end") && o3.stringValue().length() == 5, "deep E: RT 内嵌 NUL");
    }

    // ---- 深化 F: equals/hashCode/toString ----
    static void testDeepEqualsHashCodeToString() {
        check(AnnotationValue.ofString("a").equals(AnnotationValue.ofString("a")), "deep F: 同值 equals");
        check(AnnotationValue.ofString("a").hashCode() == AnnotationValue.ofString("a").hashCode(), "deep F: hashCode 一致");
        check(!AnnotationValue.ofString("a").equals(AnnotationValue.ofString("b")), "deep F: 异值不 equals");
        check(!AnnotationValue.ofString("").equals(AnnotationValue.ofString("a")), "deep F: 空串与串不 equals");
        check(!AnnotationValue.ofString("a").equals(null), "deep F: equals(null) false");
        check(AnnotationValue.ofString("hi").toString().contains("hi"), "deep F: toString 含值");
    }

    // ---- 深化 G: javac 真类产物闭环 ----
    static void testDeepRealJavacProduct() throws Exception {
        byte[] self = OfStringTest.class.getResourceAsStream("OfStringTest.class").readAllBytes();
        FieldModel fm = null;
        for (FieldModel f : ClassFile.of().parse(self).fields())
            if (f.fieldName().stringValue().equals("annotatedField")) fm = f;
        check(fm != null, "deep G: annotatedField 找到");
        RuntimeVisibleAnnotationsAttribute rva = fm.findAttribute(Attributes.runtimeVisibleAnnotations()).get();
        check(rva.annotations().size() == 1, "deep G: 恰一条注解");
        Annotation a = rva.annotations().get(0);
        check(a.className().stringValue().equals("LOfStringTest$StringAnno;"), "deep G: className 描述符");
        check(a.elements().size() == 2, "deep G: 仅显式元素");
        check(elemOf(a, "s") instanceof AnnotationValue.OfString xs && xs.stringValue().isEmpty(), "deep G: 真类空串元素");
        check(elemOf(a, "t") instanceof AnnotationValue.OfString xt && xt.stringValue().equals("héllo"), "deep G: 真类 Unicode 元素");
        byte[] ab = OfStringTest.class.getResourceAsStream("OfStringTest$StringAnno.class").readAllBytes();
        MethodModel vm = null, wm = null;
        for (MethodModel mm : ClassFile.of().parse(ab).methods()) {
            if (mm.methodName().stringValue().equals("s")) vm = mm;
            if (mm.methodName().stringValue().equals("t")) wm = mm;
        }
        AnnotationDefaultAttribute da = vm.findAttribute(Attributes.annotationDefault()).get();
        check(da.defaultValue() instanceof AnnotationValue.OfString dv && dv.stringValue().equals("dflt"), "deep G: AnnotationDefault s()==\"dflt\"");
        check(!wm.findAttribute(Attributes.annotationDefault()).isPresent(), "deep G: 无 default 成员无 AnnotationDefault");
    }

    // ---- 真类产物：嵌套 @interface 与被注解字段 ----
    @Retention(RetentionPolicy.RUNTIME)
    @interface StringAnno {
        String s() default "dflt";
        String t();
    }

    @StringAnno(s = "", t = "héllo")
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

/*
 * @test
 * @summary java.lang.classfile.AnnotationValue.OfFloat conformance: white-box structure + black-box behavior.
 * @run main OfFloatTest
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

public class OfFloatTest {
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
        Class<?> c = Class.forName("java.lang.classfile.AnnotationValue$OfFloat");
        check(c != null, "java.lang.classfile.AnnotationValue.OfFloat loads");
        check(c.getName().equals("java.lang.classfile.AnnotationValue$OfFloat"), "binary name java.lang.classfile.AnnotationValue$OfFloat");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.AnnotationValue.OfFloat is public");
        check(c.isInterface(), "java.lang.classfile.AnnotationValue.OfFloat is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.AnnotationValue.OfFloat has no superclass");
        check(declares(c, "constant"), "declares constant");
        check(declares(c, "floatValue"), "declares floatValue");
        check(declares(c, "resolvedValue"), "declares resolvedValue");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        AnnotationValue.OfFloat v = AnnotationValue.ofFloat(2.5f);
        check(v.tag() == AnnotationValue.TAG_FLOAT, "tag");
        check(v.constant().floatValue() == 2.5f, "constant");
        check(v.resolvedValue().equals(2.5f), "resolvedValue");

    }


    // ===================== 契约深化（2026-11-05 追加，不改动既有内容） =====================

    // ---- 深化 A: 工厂矩阵 ----
    static void testDeepFactoryMatrix() {
        AnnotationValue.OfFloat v = AnnotationValue.ofFloat(1.5f);
        check(v.tag() == AnnotationValue.TAG_FLOAT, "deep A: ofFloat tag 常量");
        check(v.tag() == 70, "deep A: tag 精确值 70 ('F')");
        check(v.floatValue() == 1.5f, "deep A: floatValue() 精确回读");
        check(v.resolvedValue().getClass() == Float.class, "deep A: resolvedValue 运行时类型");
        check(v.resolvedValue().equals(Float.valueOf(1.5f)), "deep A: resolvedValue 值");
        check(v.constant() instanceof FloatEntry, "deep A: constant() is FloatEntry");
        check(v.constant().tag() == PoolEntry.TAG_FLOAT, "deep A: 池条目 tag");
        check(v.constant() == v.constant(), "deep A: constant() 稳定同一实例");
        check(AnnotationValue.of(1.5f) instanceof AnnotationValue.OfFloat, "deep A: of(Object) 分派");
        // NaN 经 resolvedValue 保持 NaN
        AnnotationValue.OfFloat nan = AnnotationValue.ofFloat(Float.NaN);
        check(Float.isNaN(nan.floatValue()), "deep A: NaN 回读 isNaN");
        check(nan.resolvedValue().equals(Float.valueOf(Float.NaN)), "deep A: NaN resolvedValue");
    }

    // ---- 深化 B: 池条目重载 ----
    static void testDeepEntryFactory() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        FloatEntry fe = cpb.floatEntry(2.5f);
        AnnotationValue.OfFloat ev = AnnotationValue.ofFloat(fe);
        check(ev.floatValue() == 2.5f, "deep B: 条目形态值");
        check(ev.constant() == fe, "deep B: constant() == 原池条目");
        check(ev.tag() == AnnotationValue.TAG_FLOAT, "deep B: 条目形态 tag");
    }

    // ---- 深化 C: null 负例 ----
    static void testDeepNullNegatives() {
        try { AnnotationValue.ofFloat((FloatEntry) null); check(false, "deep C: ofFloat(null条目) 必须 NPE"); }
        catch (NullPointerException e) { check(true, "deep C: ofFloat(null条目) NPE"); }
    }

    // ---- 深化 D: 层次链 + 跨形态不相交 ----
    static void testDeepHierarchy() {
        AnnotationValue v = AnnotationValue.ofFloat(1.5f);
        check(v instanceof AnnotationValue, "deep D: OfFloat instanceof AnnotationValue");
        check(v instanceof AnnotationValue.OfConstant, "deep D: OfFloat instanceof OfConstant");
        check(!AnnotationValue.OfString.class.isInstance(v), "deep D: 不是 OfString");
        check(!AnnotationValue.OfEnum.class.isInstance(v) && !AnnotationValue.OfClass.class.isInstance(v)
                && !AnnotationValue.OfArray.class.isInstance(v) && !AnnotationValue.OfAnnotation.class.isInstance(v),
                "deep D: 不是 enum/class/array/annotation 形态");
    }

    // ---- 深化 E: 特殊值 build->re-parse 全等（raw bits 保真） ----
    static void testDeepSpecialRoundTrip() {
        AnnotationValue r1 = rtOne("PvOfFloat1", AnnotationElement.ofFloat("nz", -0.0f));
        check(r1 instanceof AnnotationValue.OfFloat o1 && Float.floatToRawIntBits(o1.floatValue()) == 0x80000000, "deep E: RT -0.0 raw bits 保真");
        AnnotationValue r2 = rtOne("PvOfFloat2", AnnotationElement.ofFloat("nan", Float.NaN));
        check(r2 instanceof AnnotationValue.OfFloat o2 && Float.isNaN(o2.floatValue()), "deep E: RT NaN");
        AnnotationValue r3 = rtOne("PvOfFloat3", AnnotationElement.ofFloat("pinf", Float.POSITIVE_INFINITY));
        check(r3 instanceof AnnotationValue.OfFloat o3 && o3.floatValue() == Float.POSITIVE_INFINITY, "deep E: RT +Inf");
        AnnotationValue r4 = rtOne("PvOfFloat4", AnnotationElement.ofFloat("ninf", Float.NEGATIVE_INFINITY));
        check(r4 instanceof AnnotationValue.OfFloat o4 && o4.floatValue() == Float.NEGATIVE_INFINITY, "deep E: RT -Inf");
        AnnotationValue r5 = rtOne("PvOfFloat5", AnnotationElement.ofFloat("minv", Float.MIN_VALUE));
        check(r5 instanceof AnnotationValue.OfFloat o5 && o5.floatValue() == Float.MIN_VALUE, "deep E: RT MIN_VALUE");
        AnnotationValue r6 = rtOne("PvOfFloat6", AnnotationElement.ofFloat("maxv", Float.MAX_VALUE));
        check(r6 instanceof AnnotationValue.OfFloat o6 && o6.floatValue() == Float.MAX_VALUE, "deep E: RT MAX_VALUE");
    }

    // ---- 深化 F: equals 语义实测（IEEE == 语义：NaN 不等、-0.0==0.0） ----
    static void testDeepEqualsQuirks() {
        check(AnnotationValue.ofFloat(1.5f).equals(AnnotationValue.ofFloat(1.5f)), "deep F: 同值 equals");
        check(AnnotationValue.ofFloat(1.5f).hashCode() == AnnotationValue.ofFloat(1.5f).hashCode(), "deep F: hashCode 一致");
        check(!AnnotationValue.ofFloat(Float.NaN).equals(AnnotationValue.ofFloat(Float.NaN)), "deep F: NaN equals false（IEEE ==，实测锁定）");
        check(AnnotationValue.ofFloat(Float.NaN).hashCode() == AnnotationValue.ofFloat(Float.NaN).hashCode(), "deep F: NaN hashCode 仍一致");
        check(AnnotationValue.ofFloat(-0.0f).equals(AnnotationValue.ofFloat(0.0f)), "deep F: -0.0 equals 0.0 true（IEEE ==，实测锁定）");
        check(!AnnotationValue.ofFloat(1.5f).equals(AnnotationValue.ofString("x")), "deep F: 跨形态不 equals");
        check(!AnnotationValue.ofFloat(1.5f).equals(null), "deep F: equals(null) false");
    }

    // ---- 深化 G: javac 真类产物闭环（RuntimeVisibleAnnotations + AnnotationDefault） ----
    static void testDeepRealJavacProduct() throws Exception {
        byte[] self = OfFloatTest.class.getResourceAsStream("OfFloatTest.class").readAllBytes();
        FieldModel fm = null;
        for (FieldModel f : ClassFile.of().parse(self).fields())
            if (f.fieldName().stringValue().equals("annotatedField")) fm = f;
        check(fm != null, "deep G: annotatedField 找到");
        RuntimeVisibleAnnotationsAttribute rva = fm.findAttribute(Attributes.runtimeVisibleAnnotations()).get();
        check(rva.annotations().size() == 1, "deep G: 恰一条注解");
        Annotation a = rva.annotations().get(0);
        check(a.className().stringValue().equals("LOfFloatTest$FloatAnno;"), "deep G: className 描述符");
        check(a.elements().size() == 2, "deep G: 仅显式元素");
        check(elemOf(a, "f") instanceof AnnotationValue.OfFloat x_f && Float.floatToRawIntBits(x_f.floatValue()) == 0x80000000, "deep G: 真类元素 f");
        check(elemOf(a, "g") instanceof AnnotationValue.OfFloat x_g && x_g.floatValue() == 3.25f, "deep G: 真类元素 g");
        byte[] ab = OfFloatTest.class.getResourceAsStream("OfFloatTest$FloatAnno.class").readAllBytes();
        MethodModel vm = null, wm = null;
        for (MethodModel mm : ClassFile.of().parse(ab).methods()) {
            if (mm.methodName().stringValue().equals("f")) vm = mm;
            if (mm.methodName().stringValue().equals("g")) wm = mm;
        }
        AnnotationDefaultAttribute da = vm.findAttribute(Attributes.annotationDefault()).get();
        check(da.defaultValue() instanceof AnnotationValue.OfFloat dv && dv.floatValue() == 0.5f, "deep G: AnnotationDefault");
        check(!wm.findAttribute(Attributes.annotationDefault()).isPresent(), "deep G: 无 default 成员无 AnnotationDefault");
    }

    // ---- 真类产物：嵌套 @interface 与被注解字段 ----
    @Retention(RetentionPolicy.RUNTIME)
    @interface FloatAnno {
        float f() default 0.5f;
        float g();
    }

    @FloatAnno(f = -0.0f, g = 3.25f)
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
        testDeepSpecialRoundTrip();
        testDeepEqualsQuirks();
        testDeepRealJavacProduct();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

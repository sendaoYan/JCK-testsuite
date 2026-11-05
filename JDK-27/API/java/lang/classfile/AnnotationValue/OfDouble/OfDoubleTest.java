/*
 * @test
 * @summary java.lang.classfile.AnnotationValue.OfDouble conformance: white-box structure + black-box behavior.
 * @run main OfDoubleTest
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

public class OfDoubleTest {
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
        Class<?> c = Class.forName("java.lang.classfile.AnnotationValue$OfDouble");
        check(c != null, "java.lang.classfile.AnnotationValue.OfDouble loads");
        check(c.getName().equals("java.lang.classfile.AnnotationValue$OfDouble"), "binary name java.lang.classfile.AnnotationValue$OfDouble");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.AnnotationValue.OfDouble is public");
        check(c.isInterface(), "java.lang.classfile.AnnotationValue.OfDouble is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.AnnotationValue.OfDouble has no superclass");
        check(declares(c, "constant"), "declares constant");
        check(declares(c, "doubleValue"), "declares doubleValue");
        check(declares(c, "resolvedValue"), "declares resolvedValue");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        AnnotationValue.OfDouble v = AnnotationValue.ofDouble(3.5);
        check(v.tag() == AnnotationValue.TAG_DOUBLE, "tag");
        check(v.constant().doubleValue() == 3.5, "constant");
        check(v.resolvedValue().equals(3.5), "resolvedValue");

    }


    // ===================== 契约深化（2026-11-05 追加，不改动既有内容） =====================

    // ---- 深化 A: 工厂矩阵 ----
    static void testDeepFactoryMatrix() {
        AnnotationValue.OfDouble v = AnnotationValue.ofDouble(1.5);
        check(v.tag() == AnnotationValue.TAG_DOUBLE, "deep A: ofDouble tag 常量");
        check(v.tag() == 68, "deep A: tag 精确值 68 ('D')");
        check(v.doubleValue() == 1.5, "deep A: doubleValue() 精确回读");
        check(v.resolvedValue().getClass() == Double.class, "deep A: resolvedValue 运行时类型");
        check(v.resolvedValue().equals(Double.valueOf(1.5)), "deep A: resolvedValue 值");
        check(v.constant() instanceof DoubleEntry, "deep A: constant() is DoubleEntry");
        check(v.constant().tag() == PoolEntry.TAG_DOUBLE, "deep A: 池条目 tag");
        check(v.constant() == v.constant(), "deep A: constant() 稳定同一实例");
        check(AnnotationValue.of(1.5) instanceof AnnotationValue.OfDouble, "deep A: of(Object) 分派");
        // NaN 经 resolvedValue 保持 NaN
        AnnotationValue.OfDouble nan = AnnotationValue.ofDouble(Double.NaN);
        check(Double.isNaN(nan.doubleValue()), "deep A: NaN 回读 isNaN");
        check(nan.resolvedValue().equals(Double.valueOf(Double.NaN)), "deep A: NaN resolvedValue");
    }

    // ---- 深化 B: 池条目重载 ----
    static void testDeepEntryFactory() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        DoubleEntry fe = cpb.doubleEntry(2.5);
        AnnotationValue.OfDouble ev = AnnotationValue.ofDouble(fe);
        check(ev.doubleValue() == 2.5, "deep B: 条目形态值");
        check(ev.constant() == fe, "deep B: constant() == 原池条目");
        check(ev.tag() == AnnotationValue.TAG_DOUBLE, "deep B: 条目形态 tag");
    }

    // ---- 深化 C: null 负例 ----
    static void testDeepNullNegatives() {
        try { AnnotationValue.ofDouble((DoubleEntry) null); check(false, "deep C: ofDouble(null条目) 必须 NPE"); }
        catch (NullPointerException e) { check(true, "deep C: ofDouble(null条目) NPE"); }
    }

    // ---- 深化 D: 层次链 + 跨形态不相交 ----
    static void testDeepHierarchy() {
        AnnotationValue v = AnnotationValue.ofDouble(1.5);
        check(v instanceof AnnotationValue, "deep D: OfDouble instanceof AnnotationValue");
        check(v instanceof AnnotationValue.OfConstant, "deep D: OfDouble instanceof OfConstant");
        check(!AnnotationValue.OfString.class.isInstance(v), "deep D: 不是 OfString");
        check(!AnnotationValue.OfEnum.class.isInstance(v) && !AnnotationValue.OfClass.class.isInstance(v)
                && !AnnotationValue.OfArray.class.isInstance(v) && !AnnotationValue.OfAnnotation.class.isInstance(v),
                "deep D: 不是 enum/class/array/annotation 形态");
    }

    // ---- 深化 E: 特殊值 build->re-parse 全等（raw bits 保真） ----
    static void testDeepSpecialRoundTrip() {
        AnnotationValue r1 = rtOne("PvOfDouble1", AnnotationElement.ofDouble("nz", -0.0));
        check(r1 instanceof AnnotationValue.OfDouble o1 && Double.doubleToRawLongBits(o1.doubleValue()) == 0x8000000000000000L, "deep E: RT -0.0 raw bits 保真");
        AnnotationValue r2 = rtOne("PvOfDouble2", AnnotationElement.ofDouble("nan", Double.NaN));
        check(r2 instanceof AnnotationValue.OfDouble o2 && Double.isNaN(o2.doubleValue()), "deep E: RT NaN");
        AnnotationValue r3 = rtOne("PvOfDouble3", AnnotationElement.ofDouble("pinf", Double.POSITIVE_INFINITY));
        check(r3 instanceof AnnotationValue.OfDouble o3 && o3.doubleValue() == Double.POSITIVE_INFINITY, "deep E: RT +Inf");
        AnnotationValue r4 = rtOne("PvOfDouble4", AnnotationElement.ofDouble("ninf", Double.NEGATIVE_INFINITY));
        check(r4 instanceof AnnotationValue.OfDouble o4 && o4.doubleValue() == Double.NEGATIVE_INFINITY, "deep E: RT -Inf");
        AnnotationValue r5 = rtOne("PvOfDouble5", AnnotationElement.ofDouble("minv", Double.MIN_VALUE));
        check(r5 instanceof AnnotationValue.OfDouble o5 && o5.doubleValue() == Double.MIN_VALUE, "deep E: RT MIN_VALUE");
        AnnotationValue r6 = rtOne("PvOfDouble6", AnnotationElement.ofDouble("maxv", Double.MAX_VALUE));
        check(r6 instanceof AnnotationValue.OfDouble o6 && o6.doubleValue() == Double.MAX_VALUE, "deep E: RT MAX_VALUE");
    }

    // ---- 深化 F: equals 语义实测（IEEE == 语义：NaN 不等、-0.0==0.0） ----
    static void testDeepEqualsQuirks() {
        check(AnnotationValue.ofDouble(1.5).equals(AnnotationValue.ofDouble(1.5)), "deep F: 同值 equals");
        check(AnnotationValue.ofDouble(1.5).hashCode() == AnnotationValue.ofDouble(1.5).hashCode(), "deep F: hashCode 一致");
        check(!AnnotationValue.ofDouble(Double.NaN).equals(AnnotationValue.ofDouble(Double.NaN)), "deep F: NaN equals false（IEEE ==，实测锁定）");
        check(AnnotationValue.ofDouble(Double.NaN).hashCode() == AnnotationValue.ofDouble(Double.NaN).hashCode(), "deep F: NaN hashCode 仍一致");
        check(AnnotationValue.ofDouble(-0.0).equals(AnnotationValue.ofDouble(0.0)), "deep F: -0.0 equals 0.0 true（IEEE ==，实测锁定）");
        check(!AnnotationValue.ofDouble(1.5).equals(AnnotationValue.ofString("x")), "deep F: 跨形态不 equals");
        check(!AnnotationValue.ofDouble(1.5).equals(null), "deep F: equals(null) false");
    }

    // ---- 深化 G: javac 真类产物闭环（RuntimeVisibleAnnotations + AnnotationDefault） ----
    static void testDeepRealJavacProduct() throws Exception {
        byte[] self = OfDoubleTest.class.getResourceAsStream("OfDoubleTest.class").readAllBytes();
        FieldModel fm = null;
        for (FieldModel f : ClassFile.of().parse(self).fields())
            if (f.fieldName().stringValue().equals("annotatedField")) fm = f;
        check(fm != null, "deep G: annotatedField 找到");
        RuntimeVisibleAnnotationsAttribute rva = fm.findAttribute(Attributes.runtimeVisibleAnnotations()).get();
        check(rva.annotations().size() == 1, "deep G: 恰一条注解");
        Annotation a = rva.annotations().get(0);
        check(a.className().stringValue().equals("LOfDoubleTest$DoubleAnno;"), "deep G: className 描述符");
        check(a.elements().size() == 2, "deep G: 仅显式元素");
        check(elemOf(a, "d") instanceof AnnotationValue.OfDouble x_d && Double.doubleToRawLongBits(x_d.doubleValue()) == 0x8000000000000000L, "deep G: 真类元素 d");
        check(elemOf(a, "e") instanceof AnnotationValue.OfDouble x_e && x_e.doubleValue() == 3.5, "deep G: 真类元素 e");
        byte[] ab = OfDoubleTest.class.getResourceAsStream("OfDoubleTest$DoubleAnno.class").readAllBytes();
        MethodModel vm = null, wm = null;
        for (MethodModel mm : ClassFile.of().parse(ab).methods()) {
            if (mm.methodName().stringValue().equals("d")) vm = mm;
            if (mm.methodName().stringValue().equals("e")) wm = mm;
        }
        AnnotationDefaultAttribute da = vm.findAttribute(Attributes.annotationDefault()).get();
        check(da.defaultValue() instanceof AnnotationValue.OfDouble dv && dv.doubleValue() == 1.25, "deep G: AnnotationDefault");
        check(!wm.findAttribute(Attributes.annotationDefault()).isPresent(), "deep G: 无 default 成员无 AnnotationDefault");
    }

    // ---- 真类产物：嵌套 @interface 与被注解字段 ----
    @Retention(RetentionPolicy.RUNTIME)
    @interface DoubleAnno {
        double d() default 1.25;
        double e();
    }

    @DoubleAnno(d = -0.0, e = 3.5)
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

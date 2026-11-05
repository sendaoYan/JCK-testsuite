/*
 * @test
 * @summary java.lang.classfile.AnnotationValue.OfLong conformance: white-box structure + black-box behavior.
 * @run main OfLongTest
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

public class OfLongTest {
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
        Class<?> c = Class.forName("java.lang.classfile.AnnotationValue$OfLong");
        check(c != null, "java.lang.classfile.AnnotationValue.OfLong loads");
        check(c.getName().equals("java.lang.classfile.AnnotationValue$OfLong"), "binary name java.lang.classfile.AnnotationValue$OfLong");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.AnnotationValue.OfLong is public");
        check(c.isInterface(), "java.lang.classfile.AnnotationValue.OfLong is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.AnnotationValue.OfLong has no superclass");
        check(declares(c, "constant"), "declares constant");
        check(declares(c, "longValue"), "declares longValue");
        check(declares(c, "resolvedValue"), "declares resolvedValue");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        AnnotationValue.OfLong v = AnnotationValue.ofLong(100L);
        check(v.tag() == AnnotationValue.TAG_LONG, "tag");
        check(v.constant().longValue() == 100L, "constant");
        check(v.resolvedValue().equals(100L), "resolvedValue");

    }


    // ===================== 契约深化（2026-11-05 追加，不改动既有内容） =====================

    // ---- 深化 A: 工厂矩阵：tag 精确字符值 + 类型化访问器 + resolvedValue/constant ----
    static void testDeepFactoryMatrix() {
        AnnotationValue.OfLong v = AnnotationValue.ofLong(5L);
        check(v.tag() == AnnotationValue.TAG_LONG, "deep A: ofLong tag 常量");
        check(v.tag() == 74, "deep A: tag 精确值 74 ('J')");
        check(v.longValue() == 5L, "deep A: longValue() 精确回读");
        check(v.resolvedValue().equals(Long.valueOf(5L)), "deep A: resolvedValue 值");
        check(v.resolvedValue().getClass() == Long.class, "deep A: resolvedValue 运行时类型 Long");
        check(v.constant() instanceof LongEntry, "deep A: constant() is LongEntry");
        check(v.constant().tag() == PoolEntry.TAG_LONG, "deep A: 池条目 tag PoolEntry.TAG_LONG");
        check(v.constant() == v.constant(), "deep A: constant() 稳定同一实例");
        check(AnnotationValue.of(5L) instanceof AnnotationValue.OfLong, "deep A: of(Object) 分派到 OfLong");
    }

    // ---- 深化 B: 池条目重载工厂：同一条目同一性复用 ----
    static void testDeepEntryFactory() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        LongEntry ie = cpb.longEntry(42L);
        AnnotationValue.OfLong ev = AnnotationValue.ofLong(ie);
        check(ev.longValue() == 42L, "deep B: 条目形态值");
        check(ev.constant() == ie, "deep B: constant() == 原池条目（== 复用）");
        check(ev.tag() == AnnotationValue.TAG_LONG, "deep B: 条目形态 tag 不变");
        check(ev.resolvedValue().equals(42L), "deep B: 条目形态 resolvedValue");
    }

    // ---- 深化 C: null 负例（值形态/条目形态） ----
    static void testDeepNullNegatives() {
        try { AnnotationValue.ofLong((LongEntry) null); check(false, "deep C: ofLong(null条目) 必须 NPE"); }
        catch (NullPointerException e) { check(true, "deep C: ofLong(null条目) NPE"); }
    }

    // ---- 深化 D: 层次链 AV<-OfConstant<-OfLong + 跨形态不相交 ----
    static void testDeepHierarchy() {
        AnnotationValue v = AnnotationValue.ofLong(5L);
        check(v instanceof AnnotationValue, "deep D: OfLong instanceof AnnotationValue");
        check(v instanceof AnnotationValue.OfConstant, "deep D: OfLong instanceof OfConstant");
        check(AnnotationValue.OfConstant.class.isInstance(v) && AnnotationValue.class.isInstance(v), "deep D: 层次链完整");
        check(!AnnotationValue.OfString.class.isInstance(v), "deep D: 不是 OfString");
        check(!AnnotationValue.OfEnum.class.isInstance(v) && !AnnotationValue.OfClass.class.isInstance(v)
                && !AnnotationValue.OfArray.class.isInstance(v) && !AnnotationValue.OfAnnotation.class.isInstance(v),
                "deep D: 不是 enum/class/array/annotation 形态");
    }

    // ---- 深化 E: 边界值 build->re-parse 全等（注解真序列化往返） ----
    static void testDeepBoundaryRoundTrip() {
        AnnotationValue r0 = rtOne("PvOfLong", AnnotationElement.ofLong("z", 0L));
        check(r0 instanceof AnnotationValue.OfLong o0 && o0.longValue() == 0L && o0.tag() == AnnotationValue.TAG_LONG, "deep E: RT z=0L");
        AnnotationValue r1 = rtOne("PvOfLong", AnnotationElement.ofLong("neg", -1L));
        check(r1 instanceof AnnotationValue.OfLong o1 && o1.longValue() == -1L && o1.tag() == AnnotationValue.TAG_LONG, "deep E: RT neg=-1L");
        AnnotationValue r2 = rtOne("PvOfLong", AnnotationElement.ofLong("min", Long.MIN_VALUE));
        check(r2 instanceof AnnotationValue.OfLong o2 && o2.longValue() == Long.MIN_VALUE && o2.tag() == AnnotationValue.TAG_LONG, "deep E: RT min=Long.MIN_VALUE");
        AnnotationValue r3 = rtOne("PvOfLong", AnnotationElement.ofLong("max", Long.MAX_VALUE));
        check(r3 instanceof AnnotationValue.OfLong o3 && o3.longValue() == Long.MAX_VALUE && o3.tag() == AnnotationValue.TAG_LONG, "deep E: RT max=Long.MAX_VALUE");
    }

    // ---- 深化 F: equals/hashCode/toString 实测记录 ----
    static void testDeepEqualsHashCodeToString() {
        check(AnnotationValue.ofLong(5L).equals(AnnotationValue.ofLong(5L)), "deep F: 同值结构 equals");
        check(AnnotationValue.ofLong(5L).hashCode() == AnnotationValue.ofLong(5L).hashCode(), "deep F: hashCode 一致");
        check(!AnnotationValue.ofLong(5L).equals(AnnotationValue.ofLong(6L)), "deep F: 异值不 equals");
        check(!AnnotationValue.ofLong(5L).equals(null), "deep F: equals(null) false");
        check(!AnnotationValue.ofLong(5L).equals(AnnotationValue.ofString("x")), "deep F: 跨形态不 equals");
        check(AnnotationValue.ofLong(5L).toString() != null && !AnnotationValue.ofLong(5L).toString().isEmpty(), "deep F: toString 非空");
    }

    // ---- 深化 G: javac 真类产物闭环（RuntimeVisibleAnnotations + AnnotationDefault） ----
    static void testDeepRealJavacProduct() throws Exception {
        byte[] self = OfLongTest.class.getResourceAsStream("OfLongTest.class").readAllBytes();
        FieldModel fm = null;
        for (FieldModel f : ClassFile.of().parse(self).fields())
            if (f.fieldName().stringValue().equals("annotatedField")) fm = f;
        check(fm != null, "deep G: annotatedField 找到");
        RuntimeVisibleAnnotationsAttribute rva = fm.findAttribute(Attributes.runtimeVisibleAnnotations()).get();
        check(rva.annotations().size() == 1, "deep G: 恰一条注解");
        Annotation a = rva.annotations().get(0);
        check(a.className().stringValue().equals("LOfLongTest$LongAnno;"), "deep G: className 描述符（点号->斜杠）");
        check(a.elements().size() == 2, "deep G: 仅显式元素被序列化（default 不出现）");
        check(elemOf(a, "l") instanceof AnnotationValue.OfLong x_l && x_l.longValue() == 5L, "deep G: 真类元素 l");
        check(elemOf(a, "m") instanceof AnnotationValue.OfLong x_m && x_m.longValue() == Long.MIN_VALUE, "deep G: 真类元素 m");
        byte[] ab = OfLongTest.class.getResourceAsStream("OfLongTest$LongAnno.class").readAllBytes();
        MethodModel vm = null, wm = null;
        for (MethodModel mm : ClassFile.of().parse(ab).methods()) {
            if (mm.methodName().stringValue().equals("l")) vm = mm;
            if (mm.methodName().stringValue().equals("m")) wm = mm;
        }
        AnnotationDefaultAttribute da = vm.findAttribute(Attributes.annotationDefault()).get();
        check(da.defaultValue() instanceof AnnotationValue.OfLong dv && dv.longValue() == 7L, "deep G: AnnotationDefault l()");
        check(da.attributeName().stringValue().equals(Attributes.NAME_ANNOTATION_DEFAULT), "deep G: 属性规范名");
        check(!wm.findAttribute(Attributes.annotationDefault()).isPresent(), "deep G: 无 default 成员无 AnnotationDefault");
    }

    // ---- 真类产物：嵌套 @interface 与被注解字段（javac 编译自本文件） ----
    @Retention(RetentionPolicy.RUNTIME)
    @interface LongAnno {
        long l() default 7L;
        long m();
    }

    @LongAnno(l = 5L, m = -9223372036854775808L)
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

/*
 * @test
 * @summary java.lang.classfile.AnnotationValue.OfInt conformance: white-box structure + black-box behavior.
 * @run main OfIntTest
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

public class OfIntTest {
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
        Class<?> c = Class.forName("java.lang.classfile.AnnotationValue$OfInt");
        check(c != null, "java.lang.classfile.AnnotationValue.OfInt loads");
        check(c.getName().equals("java.lang.classfile.AnnotationValue$OfInt"), "binary name java.lang.classfile.AnnotationValue$OfInt");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.AnnotationValue.OfInt is public");
        check(c.isInterface(), "java.lang.classfile.AnnotationValue.OfInt is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.AnnotationValue.OfInt has no superclass");
        check(declares(c, "constant"), "declares constant");
        check(declares(c, "intValue"), "declares intValue");
        check(declares(c, "resolvedValue"), "declares resolvedValue");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        AnnotationValue.OfInt v = AnnotationValue.ofInt(-7);
        check(v.tag() == AnnotationValue.TAG_INT, "tag");
        check(v.intValue() == -7, "intValue");
        check(v.constant().intValue() == -7, "constant");
        check(v.resolvedValue().equals(-7), "resolvedValue");

    }


    // ===================== 契约深化（2026-11-05 追加，不改动既有内容） =====================

    // ---- 深化 A: 工厂矩阵：tag 精确字符值 + 类型化访问器 + resolvedValue/constant ----
    static void testDeepFactoryMatrix() {
        AnnotationValue.OfInt v = AnnotationValue.ofInt(5);
        check(v.tag() == AnnotationValue.TAG_INT, "deep A: ofInt tag 常量");
        check(v.tag() == 73, "deep A: tag 精确值 73 ('I')");
        check(v.intValue() == 5, "deep A: intValue() 精确回读");
        check(v.resolvedValue().equals(Integer.valueOf(5)), "deep A: resolvedValue 值");
        check(v.resolvedValue().getClass() == Integer.class, "deep A: resolvedValue 运行时类型 Integer");
        check(v.constant() instanceof IntegerEntry, "deep A: constant() is IntegerEntry");
        check(v.constant().tag() == PoolEntry.TAG_INTEGER, "deep A: 池条目 tag PoolEntry.TAG_INTEGER");
        check(v.constant() == v.constant(), "deep A: constant() 稳定同一实例");
        check(AnnotationValue.of(5) instanceof AnnotationValue.OfInt, "deep A: of(Object) 分派到 OfInt");
    }

    // ---- 深化 B: 池条目重载工厂：同一条目同一性复用 ----
    static void testDeepEntryFactory() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        IntegerEntry ie = cpb.intEntry(42);
        AnnotationValue.OfInt ev = AnnotationValue.ofInt(ie);
        check(ev.intValue() == 42, "deep B: 条目形态值");
        check(ev.constant() == ie, "deep B: constant() == 原池条目（== 复用）");
        check(ev.tag() == AnnotationValue.TAG_INT, "deep B: 条目形态 tag 不变");
        check(ev.resolvedValue().equals(42), "deep B: 条目形态 resolvedValue");
    }

    // ---- 深化 C: null 负例（值形态/条目形态） ----
    static void testDeepNullNegatives() {
        try { AnnotationValue.ofInt((IntegerEntry) null); check(false, "deep C: ofInt(null条目) 必须 NPE"); }
        catch (NullPointerException e) { check(true, "deep C: ofInt(null条目) NPE"); }
    }

    // ---- 深化 D: 层次链 AV<-OfConstant<-OfInt + 跨形态不相交 ----
    static void testDeepHierarchy() {
        AnnotationValue v = AnnotationValue.ofInt(5);
        check(v instanceof AnnotationValue, "deep D: OfInt instanceof AnnotationValue");
        check(v instanceof AnnotationValue.OfConstant, "deep D: OfInt instanceof OfConstant");
        check(AnnotationValue.OfConstant.class.isInstance(v) && AnnotationValue.class.isInstance(v), "deep D: 层次链完整");
        check(!AnnotationValue.OfString.class.isInstance(v), "deep D: 不是 OfString");
        check(!AnnotationValue.OfEnum.class.isInstance(v) && !AnnotationValue.OfClass.class.isInstance(v)
                && !AnnotationValue.OfArray.class.isInstance(v) && !AnnotationValue.OfAnnotation.class.isInstance(v),
                "deep D: 不是 enum/class/array/annotation 形态");
    }

    // ---- 深化 E: 边界值 build->re-parse 全等（注解真序列化往返） ----
    static void testDeepBoundaryRoundTrip() {
        AnnotationValue r0 = rtOne("PvOfInt", AnnotationElement.ofInt("z", 0));
        check(r0 instanceof AnnotationValue.OfInt o0 && o0.intValue() == 0 && o0.tag() == AnnotationValue.TAG_INT, "deep E: RT z=0");
        AnnotationValue r1 = rtOne("PvOfInt", AnnotationElement.ofInt("neg", -1));
        check(r1 instanceof AnnotationValue.OfInt o1 && o1.intValue() == -1 && o1.tag() == AnnotationValue.TAG_INT, "deep E: RT neg=-1");
        AnnotationValue r2 = rtOne("PvOfInt", AnnotationElement.ofInt("min", Integer.MIN_VALUE));
        check(r2 instanceof AnnotationValue.OfInt o2 && o2.intValue() == Integer.MIN_VALUE && o2.tag() == AnnotationValue.TAG_INT, "deep E: RT min=Integer.MIN_VALUE");
        AnnotationValue r3 = rtOne("PvOfInt", AnnotationElement.ofInt("max", Integer.MAX_VALUE));
        check(r3 instanceof AnnotationValue.OfInt o3 && o3.intValue() == Integer.MAX_VALUE && o3.tag() == AnnotationValue.TAG_INT, "deep E: RT max=Integer.MAX_VALUE");
    }

    // ---- 深化 F: equals/hashCode/toString 实测记录 ----
    static void testDeepEqualsHashCodeToString() {
        check(AnnotationValue.ofInt(5).equals(AnnotationValue.ofInt(5)), "deep F: 同值结构 equals");
        check(AnnotationValue.ofInt(5).hashCode() == AnnotationValue.ofInt(5).hashCode(), "deep F: hashCode 一致");
        check(!AnnotationValue.ofInt(5).equals(AnnotationValue.ofInt(6)), "deep F: 异值不 equals");
        check(!AnnotationValue.ofInt(5).equals(null), "deep F: equals(null) false");
        check(!AnnotationValue.ofInt(5).equals(AnnotationValue.ofString("x")), "deep F: 跨形态不 equals");
        check(AnnotationValue.ofInt(5).toString() != null && !AnnotationValue.ofInt(5).toString().isEmpty(), "deep F: toString 非空");
    }

    // ---- 深化 G: javac 真类产物闭环（RuntimeVisibleAnnotations + AnnotationDefault） ----
    static void testDeepRealJavacProduct() throws Exception {
        byte[] self = OfIntTest.class.getResourceAsStream("OfIntTest.class").readAllBytes();
        FieldModel fm = null;
        for (FieldModel f : ClassFile.of().parse(self).fields())
            if (f.fieldName().stringValue().equals("annotatedField")) fm = f;
        check(fm != null, "deep G: annotatedField 找到");
        RuntimeVisibleAnnotationsAttribute rva = fm.findAttribute(Attributes.runtimeVisibleAnnotations()).get();
        check(rva.annotations().size() == 1, "deep G: 恰一条注解");
        Annotation a = rva.annotations().get(0);
        check(a.className().stringValue().equals("LOfIntTest$IntAnno;"), "deep G: className 描述符（点号->斜杠）");
        check(a.elements().size() == 2, "deep G: 仅显式元素被序列化（default 不出现）");
        check(elemOf(a, "v") instanceof AnnotationValue.OfInt x_v && x_v.intValue() == 5, "deep G: 真类元素 v");
        check(elemOf(a, "w") instanceof AnnotationValue.OfInt x_w && x_w.intValue() == Integer.MIN_VALUE, "deep G: 真类元素 w");
        byte[] ab = OfIntTest.class.getResourceAsStream("OfIntTest$IntAnno.class").readAllBytes();
        MethodModel vm = null, wm = null;
        for (MethodModel mm : ClassFile.of().parse(ab).methods()) {
            if (mm.methodName().stringValue().equals("v")) vm = mm;
            if (mm.methodName().stringValue().equals("w")) wm = mm;
        }
        AnnotationDefaultAttribute da = vm.findAttribute(Attributes.annotationDefault()).get();
        check(da.defaultValue() instanceof AnnotationValue.OfInt dv && dv.intValue() == 7, "deep G: AnnotationDefault v()");
        check(da.attributeName().stringValue().equals(Attributes.NAME_ANNOTATION_DEFAULT), "deep G: 属性规范名");
        check(!wm.findAttribute(Attributes.annotationDefault()).isPresent(), "deep G: 无 default 成员无 AnnotationDefault");
    }

    // ---- 真类产物：嵌套 @interface 与被注解字段（javac 编译自本文件） ----
    @Retention(RetentionPolicy.RUNTIME)
    @interface IntAnno {
        int v() default 7;
        int w();
    }

    @IntAnno(v = 5, w = -2147483648)
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

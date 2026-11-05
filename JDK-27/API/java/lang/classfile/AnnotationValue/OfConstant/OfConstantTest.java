/*
 * @test
 * @summary java.lang.classfile.AnnotationValue.OfConstant conformance: white-box structure + black-box behavior.
 * @run main OfConstantTest
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

public class OfConstantTest {
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
        Class<?> c = Class.forName("java.lang.classfile.AnnotationValue$OfConstant");
        check(c != null, "java.lang.classfile.AnnotationValue.OfConstant loads");
        check(c.getName().equals("java.lang.classfile.AnnotationValue$OfConstant"), "binary name java.lang.classfile.AnnotationValue$OfConstant");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.AnnotationValue.OfConstant is public");
        check(c.isInterface(), "java.lang.classfile.AnnotationValue.OfConstant is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.AnnotationValue.OfConstant has no superclass");
        check(declares(c, "constant"), "declares constant");
        check(declares(c, "resolvedValue"), "declares resolvedValue");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        AnnotationValue.OfConstant v = AnnotationValue.ofInt(42);
        check(v.tag() == AnnotationValue.TAG_INT, "tag");
        check(((java.lang.classfile.constantpool.IntegerEntry) v.constant()).intValue() == 42, "constant int");
        check(v.resolvedValue().equals(42), "resolvedValue");
        AnnotationValue.OfConstant v2 = AnnotationValue.ofString("s");
        check(((java.lang.classfile.constantpool.Utf8Entry) v2.constant()).stringValue().equals("s"), "string constant");
        check(v2.resolvedValue().equals("s"), "string resolvedValue");

    }


    // ===================== 契约深化（2026-11-05 追加，不改动既有内容） =====================

    // ---- 深化 A: 统一面 constant()/resolvedValue() 覆盖九种常量形态 ----
    static void testDeepUnifiedFace() {
        AnnotationValue.OfInt vi = AnnotationValue.ofInt(3);
        AnnotationValue.OfLong vl = AnnotationValue.ofLong(4L);
        AnnotationValue.OfByte vb = AnnotationValue.ofByte((byte) 1);
        AnnotationValue.OfShort vs = AnnotationValue.ofShort((short) 2);
        AnnotationValue.OfChar vc = AnnotationValue.ofChar('Z');
        AnnotationValue.OfBoolean vz = AnnotationValue.ofBoolean(true);
        AnnotationValue.OfFloat vf = AnnotationValue.ofFloat(1.5f);
        AnnotationValue.OfDouble vd = AnnotationValue.ofDouble(2.5);
        AnnotationValue.OfString vst = AnnotationValue.ofString("q");
        check(vi instanceof AnnotationValue.OfConstant && vl instanceof AnnotationValue.OfConstant
                && vb instanceof AnnotationValue.OfConstant && vs instanceof AnnotationValue.OfConstant
                && vc instanceof AnnotationValue.OfConstant && vz instanceof AnnotationValue.OfConstant
                && vf instanceof AnnotationValue.OfConstant && vd instanceof AnnotationValue.OfConstant
                && vst instanceof AnnotationValue.OfConstant, "deep A: 九种常量形态均 instanceof OfConstant");
        check(vi.constant().tag() == PoolEntry.TAG_INTEGER, "deep A: int -> CONSTANT_Integer(3)");
        check(vb.constant().tag() == PoolEntry.TAG_INTEGER && vs.constant().tag() == PoolEntry.TAG_INTEGER
                && vc.constant().tag() == PoolEntry.TAG_INTEGER && vz.constant().tag() == PoolEntry.TAG_INTEGER,
                "deep A: byte/short/char/boolean 共用 CONSTANT_Integer 槽");
        check(vl.constant().tag() == PoolEntry.TAG_LONG, "deep A: long -> CONSTANT_Long(5)");
        check(vf.constant().tag() == PoolEntry.TAG_FLOAT, "deep A: float -> CONSTANT_Float(4)");
        check(vd.constant().tag() == PoolEntry.TAG_DOUBLE, "deep A: double -> CONSTANT_Double(6)");
        check(vst.constant().tag() == PoolEntry.TAG_UTF8, "deep A: string -> CONSTANT_Utf8(1)");
        check(vi.resolvedValue().getClass() == Integer.class && vl.resolvedValue().getClass() == Long.class
                && vb.resolvedValue().getClass() == Byte.class && vs.resolvedValue().getClass() == Short.class
                && vc.resolvedValue().getClass() == Character.class && vz.resolvedValue().getClass() == Boolean.class
                && vf.resolvedValue().getClass() == Float.class && vd.resolvedValue().getClass() == Double.class
                && vst.resolvedValue().getClass() == String.class, "deep A: resolvedValue 运行时类型矩阵");
        check(vi.resolvedValue().equals(3) && vl.resolvedValue().equals(4L) && vb.resolvedValue().equals((byte) 1)
                && vs.resolvedValue().equals((short) 2) && vc.resolvedValue().equals('Z') && vz.resolvedValue().equals(true)
                && vf.resolvedValue().equals(1.5f) && vd.resolvedValue().equals(2.5) && vst.resolvedValue().equals("q"),
                "deep A: resolvedValue 值矩阵");
        check(vi.constant() instanceof AnnotationConstantValueEntry && vst.constant() instanceof AnnotationConstantValueEntry,
                "deep A: constant() 返回 AnnotationConstantValueEntry");
        // 非常量形态不属于 OfConstant
        check(!AnnotationValue.OfConstant.class.isInstance(AnnotationValue.ofEnum(ClassDesc.of("E"), "X")), "deep A: OfEnum 不属 OfConstant");
        check(!AnnotationValue.OfConstant.class.isInstance(AnnotationValue.ofClass(ClassDesc.of("E"))), "deep A: OfClass 不属 OfConstant");
        check(!AnnotationValue.OfConstant.class.isInstance(AnnotationValue.ofArray()), "deep A: OfArray 不属 OfConstant");
        check(!AnnotationValue.OfConstant.class.isInstance(AnnotationValue.ofAnnotation(Annotation.of(ClassDesc.of("E")))),
                "deep A: OfAnnotation 不属 OfConstant");
        // byte 与 short 同池条目、tag 互异（语义区分在 tag 而非池条目）
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        IntegerEntry ie = cpb.intEntry(7);
        check(AnnotationValue.ofByte(ie).constant() == AnnotationValue.ofShort(ie).constant(), "deep A: B/S 共享同池条目");
        check(AnnotationValue.ofByte(ie).tag() != AnnotationValue.ofShort(ie).tag(), "deep A: tag 区分 B/S");
    }

    // ---- 深化 B: 九种常量形态 build->re-parse 后仍为 OfConstant 且值保真 ----
    static void testDeepRoundTripAllConstantKinds() {
        Annotation mega = Annotation.of(ClassDesc.of("pkg.AllC"),
                AnnotationElement.ofByte("b", (byte) -128),
                AnnotationElement.ofShort("s", Short.MAX_VALUE),
                AnnotationElement.ofChar("c", '\0'),
                AnnotationElement.ofBoolean("z", true),
                AnnotationElement.ofInt("i", Integer.MIN_VALUE),
                AnnotationElement.ofLong("j", Long.MAX_VALUE),
                AnnotationElement.ofFloat("f", -0.0f),
                AnnotationElement.ofDouble("d", Double.NaN),
                AnnotationElement.ofString("t", ""));
        byte[] b = ClassFile.of().build(ClassDesc.of("PvOfConstant"),
                cb -> cb.with(RuntimeVisibleAnnotationsAttribute.of(mega)));
        Annotation back = ClassFile.of().parse(b)
                .findAttribute(Attributes.runtimeVisibleAnnotations()).get().annotations().get(0);
        check(back.elements().size() == 9, "deep B: RT 9 常量元素");
        for (AnnotationElement e : back.elements())
            check(e.value() instanceof AnnotationValue.OfConstant, "deep B: RT " + e.name().stringValue() + " 仍为 OfConstant");
        check(((AnnotationValue.OfByte) elemOf(back, "b")).byteValue() == (byte) -128, "deep B: RT byte");
        check(((AnnotationValue.OfShort) elemOf(back, "s")).shortValue() == Short.MAX_VALUE, "deep B: RT short");
        check(((AnnotationValue.OfChar) elemOf(back, "c")).charValue() == '\0', "deep B: RT char");
        check(((AnnotationValue.OfBoolean) elemOf(back, "z")).booleanValue(), "deep B: RT boolean");
        check(((AnnotationValue.OfInt) elemOf(back, "i")).intValue() == Integer.MIN_VALUE, "deep B: RT int");
        check(((AnnotationValue.OfLong) elemOf(back, "j")).longValue() == Long.MAX_VALUE, "deep B: RT long");
        check(Float.floatToRawIntBits(((AnnotationValue.OfFloat) elemOf(back, "f")).floatValue()) == 0x80000000, "deep B: RT float -0.0 bits");
        check(Double.isNaN(((AnnotationValue.OfDouble) elemOf(back, "d")).doubleValue()), "deep B: RT double NaN");
        check(((AnnotationValue.OfString) elemOf(back, "t")).stringValue().isEmpty(), "deep B: RT string 空串");
        StringBuilder tags = new StringBuilder();
        for (AnnotationElement e : back.elements()) tags.append((char) e.value().tag());
        check(tags.toString().equals("BSCZIJFDs"), "deep B: tag 序列 " + tags);
    }

    // ---- 深化 C: 层次 + of(Object) 全分派落到 OfConstant 家族 ----
    static void testDeepHierarchyDispatch() {
        check(AnnotationValue.of(1) instanceof AnnotationValue.OfConstant, "deep C: of(Integer) 落到 OfConstant 家族");
        check(AnnotationValue.of(1L) instanceof AnnotationValue.OfConstant, "deep C: of(Long)");
        check(AnnotationValue.of((byte) 1) instanceof AnnotationValue.OfConstant, "deep C: of(Byte)");
        check(AnnotationValue.of((short) 1) instanceof AnnotationValue.OfConstant, "deep C: of(Short)");
        check(AnnotationValue.of('c') instanceof AnnotationValue.OfConstant, "deep C: of(Character)");
        check(AnnotationValue.of(true) instanceof AnnotationValue.OfConstant, "deep C: of(Boolean)");
        check(AnnotationValue.of(1f) instanceof AnnotationValue.OfConstant, "deep C: of(Float)");
        check(AnnotationValue.of(1d) instanceof AnnotationValue.OfConstant, "deep C: of(Double)");
        check(AnnotationValue.of("s") instanceof AnnotationValue.OfConstant, "deep C: of(String)");
        check(!(AnnotationValue.of(ClassDesc.of("E")) instanceof AnnotationValue.OfConstant), "deep C: of(ClassDesc) 不落 OfConstant");
    }

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
        testDeepUnifiedFace();
        testDeepRoundTripAllConstantKinds();
        testDeepHierarchyDispatch();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

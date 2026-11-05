/*
 * @test
 * @summary java.lang.classfile.AnnotationValue conformance: white-box structure + black-box behavior.
 * @run main AnnotationValueTest
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

public class AnnotationValueTest {
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
        Class<?> c = Class.forName("java.lang.classfile.AnnotationValue");
        check(c != null, "java.lang.classfile.AnnotationValue loads");
        check(c.getName().equals("java.lang.classfile.AnnotationValue"), "binary name java.lang.classfile.AnnotationValue");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.AnnotationValue is public");
        check(c.isInterface(), "java.lang.classfile.AnnotationValue is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.AnnotationValue has no superclass");
        check(declares(c, "tag"), "declares tag");
        check(declares(c, "ofEnum"), "declares ofEnum");
        check(declares(c, "ofClass"), "declares ofClass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(AnnotationValue.TAG_INT == 73 && AnnotationValue.TAG_BYTE == 66 && AnnotationValue.TAG_CHAR == 67 && AnnotationValue.TAG_BOOLEAN == 90, "tag constants (ASCII)");
        check(AnnotationValue.TAG_STRING == 115 && AnnotationValue.TAG_ENUM == 101 && AnnotationValue.TAG_CLASS == 99 && AnnotationValue.TAG_ANNOTATION == 64 && AnnotationValue.TAG_ARRAY == 91, "tag constants 2");
        check(AnnotationValue.ofInt(5).tag() == AnnotationValue.TAG_INT, "ofInt tag");
        check(AnnotationValue.ofString("s").tag() == AnnotationValue.TAG_STRING, "ofString tag");
        check(AnnotationValue.ofBoolean(true).tag() == AnnotationValue.TAG_BOOLEAN, "ofBoolean tag");
        check(AnnotationValue.ofClass(ClassDesc.of("java.lang.String")).tag() == AnnotationValue.TAG_CLASS, "ofClass tag");
        check(AnnotationValue.ofEnum(ClassDesc.of("java.lang.annotation.RetentionPolicy"), "RUNTIME").tag() == AnnotationValue.TAG_ENUM, "ofEnum tag");
        check(AnnotationValue.ofArray(AnnotationValue.ofInt(1), AnnotationValue.ofInt(2)).tag() == AnnotationValue.TAG_ARRAY, "ofArray tag");
        check(AnnotationValue.of(5).tag() == AnnotationValue.TAG_INT, "of(Object) box int");
        check(AnnotationValue.of("s").tag() == AnnotationValue.TAG_STRING, "of(Object) string");
        check(AnnotationValue.of(true).tag() == AnnotationValue.TAG_BOOLEAN, "of(Object) boolean");

    }


    // ===================== 契约深化（2026-11-05 追加，不改动既有内容） =====================

    // java.lang.constant 初始化次序 warm-up（本 JDK 构建 quirk，见 OfClass 深化注释）
    static final ClassDesc CD_WARM = ConstantDescs.CD_Object;

    // ---- 深化 A: of(ConstantDesc) 统一工厂分派矩阵（10 种接受 + 3 种拒绝） ----
    static void testDeepOfDispatch() {
        check(CD_WARM.descriptorString().equals("Ljava/lang/Object;"), "deep A: ConstantDescs warm-up");
        check(AnnotationValue.of(5) instanceof AnnotationValue.OfInt, "deep A: Integer->OfInt");
        check(AnnotationValue.of("x") instanceof AnnotationValue.OfString, "deep A: String->OfString");
        check(AnnotationValue.of(true) instanceof AnnotationValue.OfBoolean, "deep A: Boolean->OfBoolean");
        check(AnnotationValue.of((byte) 1) instanceof AnnotationValue.OfByte, "deep A: Byte->OfByte");
        check(AnnotationValue.of((short) 1) instanceof AnnotationValue.OfShort, "deep A: Short->OfShort");
        check(AnnotationValue.of('c') instanceof AnnotationValue.OfChar, "deep A: Character->OfChar");
        check(AnnotationValue.of(1L) instanceof AnnotationValue.OfLong, "deep A: Long->OfLong");
        check(AnnotationValue.of(1.5f) instanceof AnnotationValue.OfFloat, "deep A: Float->OfFloat");
        check(AnnotationValue.of(1.5d) instanceof AnnotationValue.OfDouble, "deep A: Double->OfDouble");
        check(AnnotationValue.of(ClassDesc.of("java.lang.String")) instanceof AnnotationValue.OfClass, "deep A: ClassDesc->OfClass");
        try { AnnotationValue.of(null); check(false, "deep A: of(null) 必须 NPE"); }
        catch (NullPointerException e) { check(true, "deep A: of(null) NPE"); }
        try { AnnotationValue.of(new Object()); check(false, "deep A: of(未知类型) 必须 IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage() != null && e.getMessage().contains("Illegal annotation constant value type"),
                "deep A: of(Object) IAE 消息含 Illegal annotation constant value type"); }
        try { AnnotationValue.of(MethodTypeDesc.ofDescriptor("()V")); check(false, "deep A: of(MethodTypeDesc) 必须 IAE"); }
        catch (IllegalArgumentException e) { check(true, "deep A: of(MethodTypeDesc) IAE"); }
        try { AnnotationValue.of(java.lang.Enum.EnumDesc.of(ClassDesc.of("java.lang.annotation.RetentionPolicy"), "RUNTIME")); check(false, "deep A: of(EnumDesc) 必须 IAE"); }
        catch (IllegalArgumentException e) { check(true, "deep A: of(EnumDesc) IAE（枚举须 ofEnum 两段式）"); }
    }

    // ---- 深化 B: 13 形态工厂 tag 字符表（字符语义精确锁定） ----
    static void testDeepTagCharTable() {
        check(AnnotationValue.ofByte((byte) 1).tag() == 'B', "deep B: B");
        check(AnnotationValue.ofChar('c').tag() == 'C', "deep B: C");
        check(AnnotationValue.ofDouble(1d).tag() == 'D', "deep B: D");
        check(AnnotationValue.ofFloat(1f).tag() == 'F', "deep B: F");
        check(AnnotationValue.ofInt(1).tag() == 'I', "deep B: I");
        check(AnnotationValue.ofLong(1L).tag() == 'J', "deep B: J");
        check(AnnotationValue.ofShort((short) 1).tag() == 'S', "deep B: S");
        check(AnnotationValue.ofBoolean(true).tag() == 'Z', "deep B: Z");
        check(AnnotationValue.ofString("x").tag() == 's', "deep B: s");
        check(AnnotationValue.ofEnum(ClassDesc.of("E"), "X").tag() == 'e', "deep B: e");
        check(AnnotationValue.ofClass(ClassDesc.of("E")).tag() == 'c', "deep B: c");
        check(AnnotationValue.ofAnnotation(Annotation.of(ClassDesc.of("E"))).tag() == '@', "deep B: @");
        check(AnnotationValue.ofArray().tag() == '[', "deep B: [");
    }

    // ---- 深化 C: 层次谱系（OfString 属 OfConstant 反直觉点 + 四形态不属 + 全形态属 AV） ----
    static void testDeepHierarchyMap() {
        check(AnnotationValue.ofString("x") instanceof AnnotationValue.OfConstant, "deep C: OfString 属 OfConstant（实测）");
        check(AnnotationValue.ofInt(1) instanceof AnnotationValue.OfConstant && AnnotationValue.ofLong(1L) instanceof AnnotationValue.OfConstant
                && AnnotationValue.ofByte((byte) 1) instanceof AnnotationValue.OfConstant && AnnotationValue.ofShort((short) 1) instanceof AnnotationValue.OfConstant
                && AnnotationValue.ofChar('c') instanceof AnnotationValue.OfConstant && AnnotationValue.ofBoolean(true) instanceof AnnotationValue.OfConstant
                && AnnotationValue.ofFloat(1f) instanceof AnnotationValue.OfConstant && AnnotationValue.ofDouble(1d) instanceof AnnotationValue.OfConstant,
                "deep C: 九常量形态均属 OfConstant");
        check(!AnnotationValue.OfConstant.class.isInstance(AnnotationValue.ofEnum(ClassDesc.of("E"), "X")), "deep C: OfEnum 不属");
        check(!AnnotationValue.OfConstant.class.isInstance(AnnotationValue.ofClass(ClassDesc.of("E"))), "deep C: OfClass 不属");
        check(!AnnotationValue.OfConstant.class.isInstance(AnnotationValue.ofArray()), "deep C: OfArray 不属");
        check(!AnnotationValue.OfConstant.class.isInstance(AnnotationValue.ofAnnotation(Annotation.of(ClassDesc.of("E")))), "deep C: OfAnnotation 不属");
        AnnotationValue[] all = { AnnotationValue.ofByte((byte) 1), AnnotationValue.ofChar('c'), AnnotationValue.ofDouble(1d),
                AnnotationValue.ofFloat(1f), AnnotationValue.ofInt(1), AnnotationValue.ofLong(1L), AnnotationValue.ofShort((short) 1),
                AnnotationValue.ofBoolean(true), AnnotationValue.ofString("x"), AnnotationValue.ofEnum(ClassDesc.of("E"), "X"),
                AnnotationValue.ofClass(ClassDesc.of("E")), AnnotationValue.ofAnnotation(Annotation.of(ClassDesc.of("E"))), AnnotationValue.ofArray() };
        int cnt = 0;
        for (AnnotationValue v : all) if (v instanceof AnnotationValue) cnt++;
        check(cnt == 13, "deep C: 全部 13 形态 instanceof AnnotationValue");
    }

    // ---- 深化 D: 池条目重载矩阵（同一性复用 + B/S/C/Z 共享 IntegerEntry） ----
    static void testDeepEntryFactories() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        IntegerEntry ie = cpb.intEntry(42);
        check(AnnotationValue.ofInt(ie).constant() == ie, "deep D: ofInt(entry) 复用");
        LongEntry le = cpb.longEntry(42L);
        check(AnnotationValue.ofLong(le).constant() == le, "deep D: ofLong(entry) 复用");
        FloatEntry fe = cpb.floatEntry(1.5f);
        check(AnnotationValue.ofFloat(fe).constant() == fe, "deep D: ofFloat(entry) 复用");
        DoubleEntry de = cpb.doubleEntry(2.5);
        check(AnnotationValue.ofDouble(de).constant() == de, "deep D: ofDouble(entry) 复用");
        Utf8Entry ue = cpb.utf8Entry("q");
        check(AnnotationValue.ofString(ue).constant() == ue, "deep D: ofString(entry) 复用");
        check(AnnotationValue.ofByte(ie).constant() == ie && AnnotationValue.ofShort(ie).constant() == ie
                && AnnotationValue.ofChar(ie).constant() == ie && AnnotationValue.ofBoolean(ie).constant() == ie,
                "deep D: B/S/C/Z 共享 IntegerEntry（池只有 int 槽）");
    }

    // ---- 深化 E: 全 13 形态混合数组真序列化往返（tag 保序实测 IsZJBSCFDce@[） ----
    static void testDeepMixedArrayRoundTrip() {
        AnnotationValue mixed = AnnotationValue.ofArray(
                AnnotationValue.ofInt(1), AnnotationValue.ofString("two"), AnnotationValue.ofBoolean(false),
                AnnotationValue.ofLong(4L), AnnotationValue.ofByte((byte) 5), AnnotationValue.ofShort((short) 6),
                AnnotationValue.ofChar('7'), AnnotationValue.ofFloat(8f), AnnotationValue.ofDouble(9d),
                AnnotationValue.ofClass(ClassDesc.of("java.lang.Object")),
                AnnotationValue.ofEnum(ClassDesc.of("E"), "X"),
                AnnotationValue.ofAnnotation(Annotation.of(ClassDesc.of("F"))),
                AnnotationValue.ofArray());
        byte[] b = ClassFile.of().build(ClassDesc.of("PvAVMixed"), cb -> cb.with(RuntimeVisibleAnnotationsAttribute.of(
                Annotation.of(ClassDesc.of("pkg.M"), AnnotationElement.of("m", mixed)))));
        Annotation back = ClassFile.of().parse(b).findAttribute(Attributes.runtimeVisibleAnnotations()).get().annotations().get(0);
        List<AnnotationValue> vals = ((AnnotationValue.OfArray) elemOf(back, "m")).values();
        check(vals.size() == 13, "deep E: 13 元素保序");
        StringBuilder tags = new StringBuilder();
        for (AnnotationValue q : vals) tags.append((char) q.tag());
        check(tags.toString().equals("IsZJBSCFDce@["), "deep E: 混合 tag 序列 " + tags);
        check(((AnnotationValue.OfArray) vals.get(12)).values().isEmpty(), "deep E: 末位空数组嵌套");
        check(((AnnotationValue.OfEnum) vals.get(10)).constantName().stringValue().equals("X"), "deep E: 数组内 enum 回读");
    }

    // ---- 深化 F: equals 语义实测（IEEE == 语义：NaN 不等、-0.0==0.0） ----
    static void testDeepEqualsQuirks() {
        check(!AnnotationValue.ofFloat(Float.NaN).equals(AnnotationValue.ofFloat(Float.NaN)), "deep F: ofFloat(NaN) equals false（IEEE ==）");
        check(AnnotationValue.ofFloat(Float.NaN).hashCode() == AnnotationValue.ofFloat(Float.NaN).hashCode(), "deep F: NaN hashCode 仍一致");
        check(!AnnotationValue.ofDouble(Double.NaN).equals(AnnotationValue.ofDouble(Double.NaN)), "deep F: ofDouble(NaN) equals false");
        check(AnnotationValue.ofDouble(Double.NaN).hashCode() == AnnotationValue.ofDouble(Double.NaN).hashCode(), "deep F: double NaN hashCode 一致");
        check(AnnotationValue.ofFloat(-0.0f).equals(AnnotationValue.ofFloat(0.0f)), "deep F: ofFloat(-0.0)==ofFloat(0.0)（IEEE ==）");
        check(AnnotationValue.ofDouble(-0.0).equals(AnnotationValue.ofDouble(0.0)), "deep F: ofDouble(-0.0)==ofDouble(0.0)");
        check(AnnotationValue.ofDouble(1.5).equals(AnnotationValue.ofDouble(1.5)), "deep F: 常规 double equals");
        check(!AnnotationValue.ofInt(1).equals(AnnotationValue.ofLong(1L)), "deep F: int/long 跨形态不 equals");
        check(!AnnotationValue.ofByte((byte) 1).equals(AnnotationValue.ofShort((short) 1)), "deep F: byte/short 跨形态不 equals");
    }

    // ---- 深化 G: toString 稳定位（值/描述符可见性因形态而异，实测记录） ----
    static void testDeepToStringStableBits() {
        check(AnnotationValue.ofString("hi").toString().contains("hi"), "deep G: OfString toString 含值");
        AnnotationValue.OfEnum oe = AnnotationValue.ofEnum(ClassDesc.of("java.lang.annotation.RetentionPolicy"), "RUNTIME");
        check(oe.toString().contains("RUNTIME") && oe.toString().contains("Ljava/lang/annotation/RetentionPolicy;"), "deep G: OfEnum 含常量名+描述符");
        check(AnnotationValue.ofClass(ClassDesc.of("java.lang.String")).toString().contains("Ljava/lang/String;"), "deep G: OfClass 含描述符");
        check(AnnotationValue.ofInt(5).toString().contains("constant="), "deep G: OfInt 记录形态 constant=");
        Annotation nested = AnnotationValue.ofAnnotation(Annotation.of(ClassDesc.of("pkg.Nx"))).annotation();
        check(nested.toString().contains("Lpkg/Nx;"), "deep G: Annotation toString 含描述符");
    }

    // ---- 深化 H: javac 真类产物（字段注解模型 + AnnotationDefault） ----
    static void testDeepRealJavacProduct() throws Exception {
        byte[] self = AnnotationValueTest.class.getResourceAsStream("AnnotationValueTest.class").readAllBytes();
        FieldModel fm = null;
        for (FieldModel f : ClassFile.of().parse(self).fields())
            if (f.fieldName().stringValue().equals("avField")) fm = f;
        check(fm != null, "deep H: avField 找到");
        Annotation a = fm.findAttribute(Attributes.runtimeVisibleAnnotations()).get().annotations().get(0);
        check(a.className().stringValue().equals("LAnnotationValueTest$AVAnno;"), "deep H: 真类字段注解 className");
        check(a.elements().size() == 1, "deep H: 仅显式元素");
        check(elemOf(a, "v") instanceof AnnotationValue.OfInt ov && ov.intValue() == 3, "deep H: v=3 OfInt");
        byte[] ab = AnnotationValueTest.class.getResourceAsStream("AnnotationValueTest$AVAnno.class").readAllBytes();
        boolean sawDefault = false;
        for (MethodModel m : ClassFile.of().parse(ab).methods())
            if (m.methodName().stringValue().equals("v")) {
                AnnotationDefaultAttribute da = m.findAttribute(Attributes.annotationDefault()).get();
                check(da.defaultValue() instanceof AnnotationValue.OfInt dv && dv.intValue() == 7, "deep H: AnnotationDefault v()==7");
                sawDefault = true;
            }
        check(sawDefault, "deep H: v() 成员存在");
    }

    // ---- 真类产物：嵌套 @interface 与被注解字段 ----
    @Retention(RetentionPolicy.RUNTIME)
    @interface AVAnno {
        int v() default 7;
    }

    @AVAnno(v = 3)
    static int avField;

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
        testDeepOfDispatch();
        testDeepTagCharTable();
        testDeepHierarchyMap();
        testDeepEntryFactories();
        testDeepMixedArrayRoundTrip();
        testDeepEqualsQuirks();
        testDeepToStringStableBits();
        testDeepRealJavacProduct();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

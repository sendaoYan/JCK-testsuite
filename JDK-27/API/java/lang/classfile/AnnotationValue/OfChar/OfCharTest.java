/*
 * @test
 * @summary java.lang.classfile.AnnotationValue.OfChar conformance: white-box structure + black-box behavior.
 * @run main OfCharTest
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

public class OfCharTest {
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
        Class<?> c = Class.forName("java.lang.classfile.AnnotationValue$OfChar");
        check(c != null, "java.lang.classfile.AnnotationValue.OfChar loads");
        check(c.getName().equals("java.lang.classfile.AnnotationValue$OfChar"), "binary name java.lang.classfile.AnnotationValue$OfChar");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.AnnotationValue.OfChar is public");
        check(c.isInterface(), "java.lang.classfile.AnnotationValue.OfChar is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.AnnotationValue.OfChar has no superclass");
        check(declares(c, "constant"), "declares constant");
        check(declares(c, "charValue"), "declares charValue");
        check(declares(c, "resolvedValue"), "declares resolvedValue");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        AnnotationValue.OfChar v = AnnotationValue.ofChar('x');
        check(v.tag() == AnnotationValue.TAG_CHAR, "tag");
        check(v.resolvedValue().equals('x'), "resolvedValue");
        check(v.constant().intValue() == 'x', "constant int");

    }


    // ===================== 契约深化（2026-11-05 追加，不改动既有内容） =====================

    // ---- 深化 A: 工厂矩阵：tag 精确字符值 + 类型化访问器 + resolvedValue/constant ----
    static void testDeepFactoryMatrix() {
        AnnotationValue.OfChar v = AnnotationValue.ofChar('Z');
        check(v.tag() == AnnotationValue.TAG_CHAR, "deep A: ofChar tag 常量");
        check(v.tag() == 67, "deep A: tag 精确值 67 ('C')");
        check(v.charValue() == 'Z', "deep A: charValue() 精确回读");
        check(v.resolvedValue().equals(Character.valueOf('Z')), "deep A: resolvedValue 值");
        check(v.resolvedValue().getClass() == Character.class, "deep A: resolvedValue 运行时类型 Character");
        check(v.constant() instanceof IntegerEntry, "deep A: constant() is IntegerEntry");
        check(v.constant().tag() == PoolEntry.TAG_INTEGER, "deep A: 池条目 tag PoolEntry.TAG_INTEGER");
        check(v.constant() == v.constant(), "deep A: constant() 稳定同一实例");
        check(AnnotationValue.of('Z') instanceof AnnotationValue.OfChar, "deep A: of(Object) 分派到 OfChar");
    }

    // ---- 深化 B: 池条目重载工厂：同一条目同一性复用 ----
    static void testDeepEntryFactory() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        IntegerEntry ie = cpb.intEntry(42);
        AnnotationValue.OfChar ev = AnnotationValue.ofChar(ie);
        check(ev.charValue() == 42, "deep B: 条目形态值");
        check(ev.constant() == ie, "deep B: constant() == 原池条目（== 复用）");
        check(ev.tag() == AnnotationValue.TAG_CHAR, "deep B: 条目形态 tag 不变");
        check(ev.resolvedValue().equals((char) 42), "deep B: 条目形态 resolvedValue");
    }

    // ---- 深化 C: null 负例（值形态/条目形态） ----
    static void testDeepNullNegatives() {
        try { AnnotationValue.ofChar((IntegerEntry) null); check(false, "deep C: ofChar(null条目) 必须 NPE"); }
        catch (NullPointerException e) { check(true, "deep C: ofChar(null条目) NPE"); }
    }

    // ---- 深化 D: 层次链 AV<-OfConstant<-OfChar + 跨形态不相交 ----
    static void testDeepHierarchy() {
        AnnotationValue v = AnnotationValue.ofChar('Z');
        check(v instanceof AnnotationValue, "deep D: OfChar instanceof AnnotationValue");
        check(v instanceof AnnotationValue.OfConstant, "deep D: OfChar instanceof OfConstant");
        check(AnnotationValue.OfConstant.class.isInstance(v) && AnnotationValue.class.isInstance(v), "deep D: 层次链完整");
        check(!AnnotationValue.OfString.class.isInstance(v), "deep D: 不是 OfString");
        check(!AnnotationValue.OfEnum.class.isInstance(v) && !AnnotationValue.OfClass.class.isInstance(v)
                && !AnnotationValue.OfArray.class.isInstance(v) && !AnnotationValue.OfAnnotation.class.isInstance(v),
                "deep D: 不是 enum/class/array/annotation 形态");
    }

    // ---- 深化 E: 边界值 build->re-parse 全等（注解真序列化往返） ----
    static void testDeepBoundaryRoundTrip() {
        AnnotationValue r0 = rtOne("PvOfChar", AnnotationElement.ofChar("nul", '\0'));
        check(r0 instanceof AnnotationValue.OfChar o0 && o0.charValue() == '\0' && o0.tag() == AnnotationValue.TAG_CHAR, "deep E: RT nul='\0'");
        AnnotationValue r1 = rtOne("PvOfChar", AnnotationElement.ofChar("max", '\uffff'));
        check(r1 instanceof AnnotationValue.OfChar o1 && o1.charValue() == '\uffff' && o1.tag() == AnnotationValue.TAG_CHAR, "deep E: RT max='\uffff'");
        AnnotationValue r2 = rtOne("PvOfChar", AnnotationElement.ofChar("mid", 'Z'));
        check(r2 instanceof AnnotationValue.OfChar o2 && o2.charValue() == 'Z' && o2.tag() == AnnotationValue.TAG_CHAR, "deep E: RT mid='Z'");
    }

    // ---- 深化 F: equals/hashCode/toString 实测记录 ----
    static void testDeepEqualsHashCodeToString() {
        check(AnnotationValue.ofChar('Z').equals(AnnotationValue.ofChar('Z')), "deep F: 同值结构 equals");
        check(AnnotationValue.ofChar('Z').hashCode() == AnnotationValue.ofChar('Z').hashCode(), "deep F: hashCode 一致");
        check(!AnnotationValue.ofChar('Z').equals(AnnotationValue.ofChar('Y')), "deep F: 异值不 equals");
        check(!AnnotationValue.ofChar('Z').equals(null), "deep F: equals(null) false");
        check(!AnnotationValue.ofChar('Z').equals(AnnotationValue.ofString("x")), "deep F: 跨形态不 equals");
        check(AnnotationValue.ofChar('Z').toString() != null && !AnnotationValue.ofChar('Z').toString().isEmpty(), "deep F: toString 非空");
    }

    // ---- 深化 G: javac 真类产物闭环（RuntimeVisibleAnnotations + AnnotationDefault） ----
    static void testDeepRealJavacProduct() throws Exception {
        byte[] self = OfCharTest.class.getResourceAsStream("OfCharTest.class").readAllBytes();
        FieldModel fm = null;
        for (FieldModel f : ClassFile.of().parse(self).fields())
            if (f.fieldName().stringValue().equals("annotatedField")) fm = f;
        check(fm != null, "deep G: annotatedField 找到");
        RuntimeVisibleAnnotationsAttribute rva = fm.findAttribute(Attributes.runtimeVisibleAnnotations()).get();
        check(rva.annotations().size() == 1, "deep G: 恰一条注解");
        Annotation a = rva.annotations().get(0);
        check(a.className().stringValue().equals("LOfCharTest$CharAnno;"), "deep G: className 描述符（点号->斜杠）");
        check(a.elements().size() == 2, "deep G: 仅显式元素被序列化（default 不出现）");
        check(elemOf(a, "c") instanceof AnnotationValue.OfChar x_c && x_c.charValue() == '\0', "deep G: 真类元素 c");
        check(elemOf(a, "d") instanceof AnnotationValue.OfChar x_d && x_d.charValue() == 'Z', "deep G: 真类元素 d");
        byte[] ab = OfCharTest.class.getResourceAsStream("OfCharTest$CharAnno.class").readAllBytes();
        MethodModel vm = null, wm = null;
        for (MethodModel mm : ClassFile.of().parse(ab).methods()) {
            if (mm.methodName().stringValue().equals("c")) vm = mm;
            if (mm.methodName().stringValue().equals("d")) wm = mm;
        }
        AnnotationDefaultAttribute da = vm.findAttribute(Attributes.annotationDefault()).get();
        check(da.defaultValue() instanceof AnnotationValue.OfChar dv && dv.charValue() == 'x', "deep G: AnnotationDefault c()");
        check(da.attributeName().stringValue().equals(Attributes.NAME_ANNOTATION_DEFAULT), "deep G: 属性规范名");
        check(!wm.findAttribute(Attributes.annotationDefault()).isPresent(), "deep G: 无 default 成员无 AnnotationDefault");
    }

    // ---- 真类产物：嵌套 @interface 与被注解字段（javac 编译自本文件） ----
    @Retention(RetentionPolicy.RUNTIME)
    @interface CharAnno {
        char c() default 'x';
        char d();
    }

    @CharAnno(c = '\0', d = 'Z')
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

    // ---- 深化 B2: B/S/C/Z 形态共享同一 IntegerEntry（池只有 int 槽，语义区分在 tag） ----
    static void testDeepEntrySharing() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        IntegerEntry ie = cpb.intEntry(7);
        check(AnnotationValue.ofByte(ie).constant() == ie && AnnotationValue.ofShort(ie).constant() == ie
                && AnnotationValue.ofChar(ie).constant() == ie && AnnotationValue.ofBoolean(ie).constant() == ie,
                "deep B2: 四形态 constant() 同一条目（==）");
        check(AnnotationValue.ofByte(ie).tag() != AnnotationValue.ofShort(ie).tag()
                && AnnotationValue.ofByte(ie).tag() != AnnotationValue.ofChar(ie).tag()
                && AnnotationValue.ofShort(ie).tag() != AnnotationValue.ofInt(ie).tag(),
                "deep B2: tag 互异（B/S/C/I）");
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
        testDeepEntrySharing();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.lang.classfile.AnnotationValue.OfClass conformance: white-box structure + black-box behavior.
 * @run main OfClassTest
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

public class OfClassTest {
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
        Class<?> c = Class.forName("java.lang.classfile.AnnotationValue$OfClass");
        check(c != null, "java.lang.classfile.AnnotationValue.OfClass loads");
        check(c.getName().equals("java.lang.classfile.AnnotationValue$OfClass"), "binary name java.lang.classfile.AnnotationValue$OfClass");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.AnnotationValue.OfClass is public");
        check(c.isInterface(), "java.lang.classfile.AnnotationValue.OfClass is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.AnnotationValue.OfClass has no superclass");
        check(declares(c, "className"), "declares className");
        check(declares(c, "classSymbol"), "declares classSymbol");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        AnnotationValue.OfClass v = AnnotationValue.ofClass(ClassDesc.of("java.lang.String"));
        check(v.tag() == AnnotationValue.TAG_CLASS, "tag");
        check(v.classSymbol().equals(ClassDesc.of("java.lang.String")), "classSymbol");
        check(v.className().stringValue().equals("Ljava/lang/String;"), "className");

    }


    // ===================== 契约深化（2026-11-05 追加，不改动既有内容） =====================

    // ---- 深化 A: 工厂矩阵（注意：先 ClassDesc.of warm-up，规避本 JDK 构建的 java.lang.constant 初始化次序问题） ----
    static void testDeepFactoryMatrix() {
        ClassDesc warm = ClassDesc.of("java.lang.String");
        check(warm.descriptorString().equals("Ljava/lang/String;"), "deep A: warm-up ClassDesc");
        AnnotationValue.OfClass v = AnnotationValue.ofClass(ClassDesc.of("java.lang.String"));
        check(v.tag() == AnnotationValue.TAG_CLASS, "deep A: ofClass tag 常量");
        check(v.tag() == 99, "deep A: tag 精确值 99 ('c')");
        check(v.className().stringValue().equals("Ljava/lang/String;"), "deep A: className 描述符");
        check(v.className() instanceof Utf8Entry, "deep A: className is Utf8Entry");
        check(v.classSymbol().equals(ClassDesc.of("java.lang.String")), "deep A: classSymbol == ClassDesc");
        check(v.classSymbol().descriptorString().equals("Ljava/lang/String;"), "deep A: classSymbol 描述符");
        check(AnnotationValue.of(ClassDesc.of("java.lang.Object")) instanceof AnnotationValue.OfClass, "deep A: of(ClassDesc) 分派");
    }

    // ---- 深化 B: Utf8Entry 重载（同一性复用 + 描述符解析） ----
    static void testDeepEntryFactory() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        Utf8Entry ue = cpb.utf8Entry("Ljava/util/List;");
        AnnotationValue.OfClass ev = AnnotationValue.ofClass(ue);
        check(ev.className() == ue, "deep B: className == 原池条目");
        check(ev.classSymbol().descriptorString().equals("Ljava/util/List;"), "deep B: classSymbol 解析自 utf8");
        check(ev.tag() == AnnotationValue.TAG_CLASS, "deep B: 条目形态 tag");
    }

    // ---- 深化 C: null 负例 ----
    static void testDeepNullNegatives() {
        try { AnnotationValue.ofClass((ClassDesc) null); check(false, "deep C: ofClass(null) 必须 NPE"); }
        catch (NullPointerException e) { check(e.getMessage() != null && e.getMessage().contains("desc"), "deep C: null ClassDesc NPE 消息含 desc"); }
        try { AnnotationValue.ofClass((Utf8Entry) null); check(false, "deep C: ofClass(null条目) 必须 NPE"); }
        catch (NullPointerException e) { check(true, "deep C: null Utf8Entry NPE"); }
    }

    // ---- 深化 D: 层次（OfClass 不属 OfConstant） ----
    static void testDeepHierarchy() {
        AnnotationValue v = AnnotationValue.ofClass(ClassDesc.of("java.lang.String"));
        check(v instanceof AnnotationValue, "deep D: OfClass instanceof AnnotationValue");
        check(!AnnotationValue.OfConstant.class.isInstance(v), "deep D: OfClass 不属 OfConstant");
        check(!AnnotationValue.OfEnum.class.isInstance(v) && !AnnotationValue.OfArray.class.isInstance(v)
                && !AnnotationValue.OfAnnotation.class.isInstance(v), "deep D: 不是 enum/array/annotation 形态");
    }

    // 本 JDK 构建的类初始化次序 quirk（实测锁定）：ClassDesc.ofDescriptor("I") 若是
    // java.lang.constant 体系的第一个触点，会陷入 PrimitiveClassDescImpl<->
    // ConstantDescs 循环初始化并抛 ExceptionInInitializerError。用静态字段直读
    // ConstantDescs.CD_Object 先完整初始化 ConstantDescs（直接触发时 clint 可成功）。
    static final ClassDesc CD_WARM = ConstantDescs.CD_Object;

    // ---- 深化 E: 描述符形态（基元/void/数组）+ build->re-parse 全等 ----
    static void testDeepDescriptorForms() {
        check(CD_WARM.descriptorString().equals("Ljava/lang/Object;"), "deep E: ConstantDescs warm-up");
        AnnotationValue.OfClass prim = AnnotationValue.ofClass(ClassDesc.ofDescriptor("I"));
        check(prim.className().stringValue().equals("I"), "deep E: 基元描述符 I");
        AnnotationValue.OfClass vd = AnnotationValue.ofClass(ClassDesc.ofDescriptor("V"));
        check(vd.className().stringValue().equals("V"), "deep E: void 描述符 V");
        ClassDesc arrD = ClassDesc.of("java.lang.String").arrayType();
        AnnotationValue.OfClass arr = AnnotationValue.ofClass(arrD);
        check(arr.className().stringValue().equals("[Ljava/lang/String;"), "deep E: 数组描述符");
        AnnotationValue r0 = rtOne("PvOfClass0", AnnotationElement.ofClass("p", ClassDesc.ofDescriptor("I")));
        check(r0 instanceof AnnotationValue.OfClass o0 && o0.className().stringValue().equals("I")
                && o0.classSymbol().equals(ClassDesc.ofDescriptor("I")), "deep E: RT 基元描述符");
        AnnotationValue r1 = rtOne("PvOfClass1", AnnotationElement.ofClass("a", arrD));
        check(r1 instanceof AnnotationValue.OfClass o1 && o1.classSymbol().equals(arrD), "deep E: RT 数组描述符");
        AnnotationValue r2 = rtOne("PvOfClass2", AnnotationElement.ofClass("r", ClassDesc.of("java.util.List")));
        check(r2 instanceof AnnotationValue.OfClass o2 && o2.className().stringValue().equals("Ljava/util/List;")
                && o2.classSymbol().equals(ClassDesc.of("java.util.List")) && o2.tag() == AnnotationValue.TAG_CLASS, "deep E: RT 引用描述符");
    }

    // ---- 深化 F: equals/hashCode/toString ----
    static void testDeepEqualsHashCodeToString() {
        check(AnnotationValue.ofClass(ClassDesc.of("A")).equals(AnnotationValue.ofClass(ClassDesc.of("A"))), "deep F: 同形 equals");
        check(AnnotationValue.ofClass(ClassDesc.of("A")).hashCode() == AnnotationValue.ofClass(ClassDesc.of("A")).hashCode(), "deep F: hashCode 一致");
        check(!AnnotationValue.ofClass(ClassDesc.of("A")).equals(AnnotationValue.ofClass(ClassDesc.of("B"))), "deep F: 异类不 equals");
        check(!AnnotationValue.ofClass(ClassDesc.of("A")).equals(null), "deep F: equals(null) false");
        check(AnnotationValue.ofClass(ClassDesc.of("java.lang.String")).toString().contains("Ljava/lang/String;"), "deep F: toString 含描述符");
    }

    // ---- 深化 G: javac 真类产物闭环 ----
    static void testDeepRealJavacProduct() throws Exception {
        byte[] self = OfClassTest.class.getResourceAsStream("OfClassTest.class").readAllBytes();
        FieldModel fm = null;
        for (FieldModel f : ClassFile.of().parse(self).fields())
            if (f.fieldName().stringValue().equals("annotatedField")) fm = f;
        check(fm != null, "deep G: annotatedField 找到");
        RuntimeVisibleAnnotationsAttribute rva = fm.findAttribute(Attributes.runtimeVisibleAnnotations()).get();
        Annotation a = rva.annotations().get(0);
        check(a.className().stringValue().equals("LOfClassTest$ClassAnno;"), "deep G: className 描述符");
        check(a.elements().size() == 2, "deep G: 仅显式元素");
        check(elemOf(a, "cl") instanceof AnnotationValue.OfClass xc && xc.className().stringValue().equals("Ljava/util/List;"), "deep G: 真类 List.class 元素");
        check(elemOf(a, "cm") instanceof AnnotationValue.OfClass xm && xm.classSymbol().equals(ClassDesc.of("java.lang.String")), "deep G: 真类 String.class 元素");
        byte[] ab = OfClassTest.class.getResourceAsStream("OfClassTest$ClassAnno.class").readAllBytes();
        MethodModel vm = null, wm = null;
        for (MethodModel mm : ClassFile.of().parse(ab).methods()) {
            if (mm.methodName().stringValue().equals("cl")) vm = mm;
            if (mm.methodName().stringValue().equals("cm")) wm = mm;
        }
        AnnotationDefaultAttribute da = vm.findAttribute(Attributes.annotationDefault()).get();
        check(da.defaultValue() instanceof AnnotationValue.OfClass dv && dv.className().stringValue().equals("Ljava/lang/Runnable;"),
                "deep G: AnnotationDefault cl()==Runnable");
        check(!wm.findAttribute(Attributes.annotationDefault()).isPresent(), "deep G: 无 default 成员无 AnnotationDefault");
    }

    // ---- 真类产物：嵌套 @interface 与被注解字段 ----
    @Retention(RetentionPolicy.RUNTIME)
    @interface ClassAnno {
        Class<?> cl() default Runnable.class;
        Class<?> cm();
    }

    @ClassAnno(cl = java.util.List.class, cm = String.class)
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
        testDeepDescriptorForms();
        testDeepEqualsHashCodeToString();
        testDeepRealJavacProduct();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

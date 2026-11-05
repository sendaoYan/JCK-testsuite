/*
 * @test
 * @summary java.lang.classfile.Annotation conformance: white-box structure + black-box behavior.
 * @run main AnnotationTest
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

public class AnnotationTest {
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
        Class<?> c = Class.forName("java.lang.classfile.Annotation");
        check(c != null, "java.lang.classfile.Annotation loads");
        check(c.getName().equals("java.lang.classfile.Annotation"), "binary name java.lang.classfile.Annotation");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Annotation is public");
        check(c.isInterface(), "java.lang.classfile.Annotation is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.Annotation has no superclass");
        check(declares(c, "className"), "declares className");
        check(declares(c, "classSymbol"), "declares classSymbol");
        check(declares(c, "elements"), "declares elements");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        Annotation a = Annotation.of(ClassDesc.of("java.lang.Deprecated"), AnnotationElement.ofBoolean("forRemoval", false));
        check(a.className().stringValue().equals("Ljava/lang/Deprecated;"), "className descriptor");
        check(a.classSymbol().equals(ClassDesc.of("java.lang.Deprecated")), "classSymbol");
        check(a.elements().size() == 1, "one element");
        check(a.elements().get(0).name().stringValue().equals("forRemoval"), "element name");
        Annotation a2 = Annotation.of(ClassDesc.of("java.lang.SuppressWarnings"), AnnotationElement.ofString("value", "unchecked"));
        check(a2.classSymbol().equals(ClassDesc.of("java.lang.SuppressWarnings")), "SuppressWarnings");

    }

    // ---- Part 3: of(ClassDesc)/of(ClassDesc, List.of()) 空元素 + null 负例 + elements() 只读 ----
    static void testOfOverloadsAndNulls() {
        Annotation empty = Annotation.of(ClassDesc.of("pkg.Empty"));
        check(empty.className().stringValue().equals("Lpkg/Empty;"), "of(cd) empty elements className descriptor");
        check(empty.elements().isEmpty(), "of(cd) varargs zero elements");
        Annotation emptyList = Annotation.of(ClassDesc.of("pkg.Empty2"), List.of());
        check(emptyList.elements().isEmpty(), "of(cd, List.of()) empty");
        check(emptyList.classSymbol().equals(ClassDesc.of("pkg.Empty2")), "of(cd,list) classSymbol");
        // className 是 Utf8Entry（池条目形态），descriptorString 经 classSymbol
        check(empty.className() instanceof Utf8Entry, "className is Utf8Entry");
        check(empty.classSymbol().descriptorString().equals("Lpkg/Empty;"), "classSymbol descriptor");
        // null 负例（实测 NPE）
        try { Annotation.of((ClassDesc) null); check(false, "of((ClassDesc)null) must throw"); }
        catch (NullPointerException expected) { check(true, "of((ClassDesc)null) NPE"); }
        try { Annotation.of(ClassDesc.of("pkg.X"), (List<AnnotationElement>) null); check(false, "of(cd, (List)null) must throw"); }
        catch (NullPointerException expected) { check(true, "of(cd, (List)null) NPE"); }
        // elements() 只读快照（实测 UOE）
        Annotation withEl = Annotation.of(ClassDesc.of("pkg.X"), AnnotationElement.ofInt("n", 1));
        try { withEl.elements().add(AnnotationElement.ofInt("m", 2)); check(false, "elements() add must throw"); }
        catch (UnsupportedOperationException expected) { check(true, "elements() UOE"); }
        // Utf8Entry 重载：池条目名直用
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        Utf8Entry name = cpb.utf8Entry("Lpkg/FromPool;");
        Annotation pooled = Annotation.of(name, AnnotationElement.ofInt("q", 3));
        check(pooled.className() == name, "of(Utf8Entry,...) reuses pool entry (==)");
        check(pooled.elements().get(0).name().stringValue().equals("q"), "of(Utf8Entry, varargs) element");
        Annotation pooled2 = Annotation.of(cpb.utf8Entry("Lpkg/FromPool2;"), List.of(AnnotationElement.ofString("s", "v")));
        check(pooled2.elements().size() == 1, "of(Utf8Entry, List) element count");
    }

    // ---- Part 4: build with(RuntimeVisibleAnnotationsAttribute) → re-parse 回读 className/elements 全等 + 顺序保持 ----
    static void testBuildReparseRoundTrip() throws Exception {
        ClassFile cf = ClassFile.of();
        Annotation anno = Annotation.of(ClassDesc.of("pkg.Marker"),
                AnnotationElement.ofString("s", "hi"),
                AnnotationElement.ofInt("n", 7),
                AnnotationElement.ofLong("l", 123456789012345L),
                AnnotationElement.ofBoolean("b", true));
        byte[] b = cf.build(ClassDesc.of("ANR"), cb -> cb.with(RuntimeVisibleAnnotationsAttribute.of(List.of(anno))));
        ClassModel cm = cf.parse(b);
        RuntimeVisibleAnnotationsAttribute rva = cm.findAttribute(Attributes.runtimeVisibleAnnotations()).get();
        check(rva != null, "RuntimeVisibleAnnotationsAttribute present");
        check(rva.annotations().size() == 1, "one annotation");
        Annotation back = rva.annotations().get(0);
        check(back.className().stringValue().equals("Lpkg/Marker;"), "roundtrip className");
        check(back.classSymbol().equals(ClassDesc.of("pkg.Marker")), "roundtrip classSymbol");
        check(back.elements().size() == 4, "roundtrip element count: " + back.elements().size());
        // 顺序保持（实测 s,n,l,b）
        check(back.elements().get(0).name().stringValue().equals("s"), "element order 0: s");
        check(back.elements().get(1).name().stringValue().equals("n"), "element order 1: n");
        check(back.elements().get(2).name().stringValue().equals("l"), "element order 2: l");
        check(back.elements().get(3).name().stringValue().equals("b"), "element order 3: b");
        // 逐值类型+值全等
        check(back.elements().get(0).value() instanceof AnnotationValue.OfString s0 && s0.stringValue().equals("hi"), "roundtrip string value");
        check(back.elements().get(1).value() instanceof AnnotationValue.OfInt i0 && i0.intValue() == 7, "roundtrip int value");
        check(back.elements().get(2).value() instanceof AnnotationValue.OfLong l0 && l0.longValue() == 123456789012345L, "roundtrip long value");
        check(back.elements().get(3).value() instanceof AnnotationValue.OfBoolean b0 && b0.booleanValue(), "roundtrip boolean value");
        // 多注解同属性：两条注解顺序回读
        Annotation a2 = Annotation.of(ClassDesc.of("pkg.Second"), AnnotationElement.ofInt("k", 1));
        byte[] b2 = cf.build(ClassDesc.of("ANR2"), cb -> cb.with(RuntimeVisibleAnnotationsAttribute.of(List.of(anno, a2))));
        List<Annotation> annos = cf.parse(b2).findAttribute(Attributes.runtimeVisibleAnnotations()).get().annotations();
        check(annos.size() == 2, "two annotations round-trip");
        check(annos.get(0).className().stringValue().equals("Lpkg/Marker;") && annos.get(1).className().stringValue().equals("Lpkg/Second;"), "annotation order preserved");
    }

    // ---- Part 5: 嵌套注解（ofAnnotation）回读 ----
    static void testNestedAnnotation() throws Exception {
        ClassFile cf = ClassFile.of();
        Annotation inner = Annotation.of(ClassDesc.of("pkg.Inner"), AnnotationElement.ofBoolean("flag", true));
        Annotation outer = Annotation.of(ClassDesc.of("pkg.Outer"),
                AnnotationElement.ofAnnotation("nested", inner));
        byte[] b = cf.build(ClassDesc.of("ANN"), cb -> cb.with(RuntimeVisibleAnnotationsAttribute.of(List.of(outer))));
        Annotation back = cf.parse(b).findAttribute(Attributes.runtimeVisibleAnnotations()).get().annotations().get(0);
        check(back.elements().size() == 1, "outer one element");
        AnnotationValue v = back.elements().get(0).value();
        check(v instanceof AnnotationValue.OfAnnotation, "nested value is OfAnnotation");
        Annotation innerBack = ((AnnotationValue.OfAnnotation) v).annotation();
        check(innerBack.className().stringValue().equals("Lpkg/Inner;"), "nested className");
        check(innerBack.elements().size() == 1, "nested element count");
        check(innerBack.elements().get(0).name().stringValue().equals("flag"), "nested element name");
        check(innerBack.elements().get(0).value() instanceof AnnotationValue.OfBoolean ob && ob.booleanValue(), "nested boolean value");
        check(v.tag() == AnnotationValue.TAG_ANNOTATION, "nested tag '@' == TAG_ANNOTATION");
    }

    // ---- Part 6: 真类 @Deprecated 方法解析回读（facts §6）----
    @Deprecated
    public static void markedDeprecated() {
    }

    static void testRealDeprecatedMethod() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] self = AnnotationTest.class.getResourceAsStream("AnnotationTest.class").readAllBytes();
        ClassModel cm = cf.parse(self);
        MethodModel target = null;
        for (MethodModel m : cm.methods())
            if (m.methodName().stringValue().equals("markedDeprecated")) target = m;
        check(target != null, "markedDeprecated method found in self bytes");
        RuntimeVisibleAnnotationsAttribute rva = target.findAttribute(Attributes.runtimeVisibleAnnotations()).get();
        check(rva.annotations().size() == 1, "@Deprecated emits exactly one annotation");
        Annotation dep = rva.annotations().get(0);
        check(dep.className().stringValue().equals("Ljava/lang/Deprecated;"), "real @Deprecated className");
        check(dep.classSymbol().equals(ClassDesc.of("java.lang.Deprecated")), "real @Deprecated classSymbol");
        check(dep.elements().isEmpty(), "@Deprecated with no arguments has zero elements");
        // 方法同时有 DeprecatedAttribute（结构属性）
        check(target.findAttribute(Attributes.deprecated()).isPresent(), "DeprecatedAttribute also present");
    }


    // ===================== 契约深化（2026-11-05 追加，不改动既有内容） =====================

    // ---- 深化 A: of 重载 null 全矩阵（含精确消息；注意 of(null, varargs) 不转型无法编译——两重载歧义，实测记录） ----
    static void testDeepOfNullMatrix() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        try { Annotation.of((Utf8Entry) null); check(false, "deep A: of((Utf8Entry)null) 必须 NPE"); }
        catch (NullPointerException e) { check(true, "deep A: of((Utf8Entry)null) NPE"); }
        try { Annotation.of(cpb.utf8Entry("Lx;"), (AnnotationElement) null); check(false, "deep A: of(ue, null元素) 必须 NPE"); }
        catch (NullPointerException e) { check(true, "deep A: of(ue, null元素) NPE"); }
        try { Annotation.of(ClassDesc.of("pkg.X"), (java.util.List<AnnotationElement>) null); check(false, "deep A: of(cd,(List)null) 必须 NPE"); }
        catch (NullPointerException e) { check(true, "deep A: of(cd,(List)null) NPE（既有用例已含）"); }
        try { Annotation.of(ClassDesc.of("pkg.X"), java.util.Arrays.asList((AnnotationElement) null)); check(false, "deep A: List 含 null 元素必须 NPE"); }
        catch (NullPointerException e) { check(true, "deep A: List 含 null 元素 NPE"); }
        try { Annotation.of(ClassDesc.of("pkg.X"), AnnotationElement.ofInt("a", 1), null); check(false, "deep A: varargs 含 null 必须 NPE"); }
        catch (NullPointerException e) { check(true, "deep A: varargs 含 null NPE"); }
    }

    // ---- 深化 B: equals/hashCode/toString 实测（结构化 equals：类名+元素名+元素值） ----
    static void testDeepEqualsHashCodeToString() {
        Annotation x = Annotation.of(ClassDesc.of("pkg.Eq"), AnnotationElement.ofInt("n", 1), AnnotationElement.ofString("s", "a"));
        Annotation y = Annotation.of(ClassDesc.of("pkg.Eq"), AnnotationElement.ofInt("n", 1), AnnotationElement.ofString("s", "a"));
        check(x.equals(y) && y.equals(x), "deep B: 同形 equals 对称");
        check(x.hashCode() == y.hashCode(), "deep B: hashCode 一致");
        check(!x.equals(Annotation.of(ClassDesc.of("pkg.Eq2"), AnnotationElement.ofInt("n", 1), AnnotationElement.ofString("s", "a"))), "deep B: 类名不同不 equals");
        check(!x.equals(Annotation.of(ClassDesc.of("pkg.Eq"), AnnotationElement.ofInt("m", 1), AnnotationElement.ofString("s", "a"))), "deep B: 元素名不同不 equals");
        check(!x.equals(Annotation.of(ClassDesc.of("pkg.Eq"), AnnotationElement.ofInt("n", 2), AnnotationElement.ofString("s", "a"))), "deep B: 元素值不同不 equals");
        check(!x.equals(Annotation.of(ClassDesc.of("pkg.Eq"), AnnotationElement.ofInt("n", 1))), "deep B: 元素数不同不 equals");
        check(!x.equals(Annotation.of(ClassDesc.of("pkg.Eq"))), "deep B: 对空元素注解不 equals");
        check(!x.equals(null), "deep B: equals(null) false");
        check(x.toString().contains("Lpkg/Eq;"), "deep B: toString 含类描述符");
        check(Annotation.of(ClassDesc.of("pkg.E2")).toString().contains("Lpkg/E2;"), "deep B: 空元素 toString 含类描述符");
        check(AnnotationElement.ofInt("n", 1).equals(AnnotationElement.ofInt("n", 1)), "deep B: AnnotationElement 同形 equals");
        check(!AnnotationElement.ofInt("n", 1).equals(AnnotationElement.ofInt("n", 2)), "deep B: AnnotationElement 异值不 equals");
        check(AnnotationElement.ofInt("n", 1).toString().startsWith("n="), "deep B: AnnotationElement toString 形如 name=...");
    }

    // ---- 深化 C: 全 13 形态 + 边界值 mega 注解真序列化往返（tag 序列保序实测 BSCZIJFDsce[@） ----
    static void testDeepMegaRoundTrip() {
        ClassFile cf = ClassFile.of();
        Annotation mega = Annotation.of(ClassDesc.of("pkg.Mega"),
                AnnotationElement.ofByte("b", (byte) -128),
                AnnotationElement.ofShort("s", Short.MIN_VALUE),
                AnnotationElement.ofChar("c", '\0'),
                AnnotationElement.ofBoolean("z", false),
                AnnotationElement.ofInt("i", Integer.MAX_VALUE),
                AnnotationElement.ofLong("j", Long.MIN_VALUE),
                AnnotationElement.ofFloat("f", Float.NaN),
                AnnotationElement.ofDouble("d", Double.POSITIVE_INFINITY),
                AnnotationElement.ofString("t", ""),
                AnnotationElement.ofClass("k", ClassDesc.of("java.util.List")),
                AnnotationElement.of("e", AnnotationValue.ofEnum(ClassDesc.of("java.lang.annotation.RetentionPolicy"), "SOURCE")),
                AnnotationElement.ofArray("a", AnnotationValue.ofInt(1), AnnotationValue.ofString("x")),
                AnnotationElement.ofAnnotation("n", Annotation.of(ClassDesc.of("pkg.Sub"), AnnotationElement.ofLong("q", 8L))));
        Annotation marker = Annotation.of(ClassDesc.of("pkg.Mk"));
        byte[] b = cf.build(ClassDesc.of("PvAnnoMega"), cb -> cb.with(RuntimeVisibleAnnotationsAttribute.of(List.of(mega, marker))));
        List<Annotation> back = cf.parse(b).findAttribute(Attributes.runtimeVisibleAnnotations()).get().annotations();
        check(back.size() == 2, "deep C: 两条注解往返");
        Annotation m = back.get(0);
        check(m.className().stringValue().equals("Lpkg/Mega;"), "deep C: 点号->斜杠描述符");
        check(m.classSymbol().equals(ClassDesc.of("pkg.Mega")), "deep C: classSymbol");
        check(m.elements().size() == 13, "deep C: 13 元素全保序往返");
        StringBuilder tags = new StringBuilder();
        for (AnnotationElement e : m.elements()) tags.append((char) e.value().tag());
        check(tags.toString().equals("BSCZIJFDsce[@"), "deep C: 13 形态 tag 序列 " + tags);
        check(((AnnotationValue.OfByte) elemOf(m, "b")).byteValue() == (byte) -128, "deep C: RT byte MIN");
        check(((AnnotationValue.OfShort) elemOf(m, "s")).shortValue() == Short.MIN_VALUE, "deep C: RT short MIN");
        check(((AnnotationValue.OfChar) elemOf(m, "c")).charValue() == '\0', "deep C: RT char NUL");
        check(!((AnnotationValue.OfBoolean) elemOf(m, "z")).booleanValue(), "deep C: RT boolean false");
        check(((AnnotationValue.OfInt) elemOf(m, "i")).intValue() == Integer.MAX_VALUE, "deep C: RT int MAX");
        check(((AnnotationValue.OfLong) elemOf(m, "j")).longValue() == Long.MIN_VALUE, "deep C: RT long MIN");
        check(Float.isNaN(((AnnotationValue.OfFloat) elemOf(m, "f")).floatValue()), "deep C: RT float NaN");
        check(((AnnotationValue.OfDouble) elemOf(m, "d")).doubleValue() == Double.POSITIVE_INFINITY, "deep C: RT double +Inf");
        check(((AnnotationValue.OfString) elemOf(m, "t")).stringValue().isEmpty(), "deep C: RT 空串");
        check(((AnnotationValue.OfClass) elemOf(m, "k")).classSymbol().equals(ClassDesc.of("java.util.List")), "deep C: RT class");
        AnnotationValue.OfEnum oe = (AnnotationValue.OfEnum) elemOf(m, "e");
        check(oe.classSymbol().equals(ClassDesc.of("java.lang.annotation.RetentionPolicy")) && oe.constantName().stringValue().equals("SOURCE"), "deep C: RT enum");
        AnnotationValue.OfArray oa = (AnnotationValue.OfArray) elemOf(m, "a");
        check(oa.values().size() == 2 && ((AnnotationValue.OfInt) oa.values().get(0)).intValue() == 1
                && ((AnnotationValue.OfString) oa.values().get(1)).stringValue().equals("x"), "deep C: RT 数组保序");
        AnnotationValue.OfAnnotation on = (AnnotationValue.OfAnnotation) elemOf(m, "n");
        check(on.annotation().className().stringValue().equals("Lpkg/Sub;")
                && ((AnnotationValue.OfLong) elemOf(on.annotation(), "q")).longValue() == 8L, "deep C: RT 嵌套注解");
        check(back.get(1).elements().isEmpty(), "deep C: 空元素注解往返");
        check(back.get(1).className().stringValue().equals("Lpkg/Mk;"), "deep C: 空元素注解 className");
    }

    // ---- 深化 D: builder 挂载到字段/方法（注解不止类级） ----
    static void testDeepBuilderAttachment() {
        ClassFile cf = ClassFile.of();
        Annotation fa = Annotation.of(ClassDesc.of("pkg.Fld"), AnnotationElement.ofInt("x", 42));
        byte[] b = cf.build(ClassDesc.of("PvAnnoAttach"), cb -> cb
                .withField("fld", ClassDesc.of("java.lang.String"), fb -> fb.with(RuntimeVisibleAnnotationsAttribute.of(fa)))
                .withMethod("mth", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        mb -> mb.withCode(c -> c.return_()).with(RuntimeVisibleAnnotationsAttribute.of(fa))));
        ClassModel cm = cf.parse(b);
        FieldModel fm = cm.fields().get(0);
        MethodModel mm = cm.methods().get(0);
        Annotation rf = fm.findAttribute(Attributes.runtimeVisibleAnnotations()).get().annotations().get(0);
        check(rf.className().stringValue().equals("Lpkg/Fld;"), "deep D: 字段注解往返");
        check(((AnnotationValue.OfInt) elemOf(rf, "x")).intValue() == 42, "deep D: 字段注解值");
        Annotation rm = mm.findAttribute(Attributes.runtimeVisibleAnnotations()).get().annotations().get(0);
        check(rm.classSymbol().equals(ClassDesc.of("pkg.Fld")), "deep D: 方法注解往返");
    }

    // ---- 深化 E: javac 真类全形态闭环（RuntimeVisible/Invisible 双侧 + AnnotationDefault 全类型） ----
    static void testDeepRealJavacFullMatrix() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] self = AnnotationTest.class.getResourceAsStream("AnnotationTest.class").readAllBytes();
        ClassModel cm = cf.parse(self);
        FieldModel full = null, inv = null;
        for (FieldModel f : cm.fields()) {
            if (f.fieldName().stringValue().equals("fullField")) full = f;
            if (f.fieldName().stringValue().equals("invisibleField")) inv = f;
        }
        check(full != null && inv != null, "deep E: 两个被注解字段找到");
        Annotation a = full.findAttribute(Attributes.runtimeVisibleAnnotations()).get().annotations().get(0);
        check(a.className().stringValue().equals("LAnnotationTest$FullAnno;"), "deep E: className 描述符（点号->斜杠）");
        check(a.elements().size() == 12, "deep E: 仅 12 个显式元素（by/sh 用 default，不序列化）");
        check(elemOf(a, "by") == null && elemOf(a, "sh") == null, "deep E: default 成员不出现在 RuntimeVisibleAnnotations");
        check(elemOf(a, "req") instanceof AnnotationValue.OfString xr && xr.stringValue().equals("r"), "deep E: req=r");
        check(elemOf(a, "v") instanceof AnnotationValue.OfInt xv && xv.intValue() == 9, "deep E: v=9");
        check(elemOf(a, "s") instanceof AnnotationValue.OfString xs && xs.stringValue().equals("hi"), "deep E: s=hi");
        check(elemOf(a, "b") instanceof AnnotationValue.OfBoolean xb && xb.booleanValue(), "deep E: b=true");
        check(elemOf(a, "c") instanceof AnnotationValue.OfChar xc && xc.charValue() == '\0', "deep E: c=NUL");
        check(elemOf(a, "l") instanceof AnnotationValue.OfLong xl && xl.longValue() == Long.MIN_VALUE, "deep E: l=Long.MIN");
        check(elemOf(a, "d") instanceof AnnotationValue.OfDouble xd && xd.doubleValue() == 3.5, "deep E: d=3.5");
        check(elemOf(a, "f") instanceof AnnotationValue.OfFloat xf
                && Float.floatToRawIntBits(xf.floatValue()) == 0x80000000, "deep E: f=-0.0f bits");
        check(elemOf(a, "cl") instanceof AnnotationValue.OfClass xcl && xcl.className().stringValue().equals("Ljava/util/List;"), "deep E: cl=List.class");
        check(elemOf(a, "e") instanceof AnnotationValue.OfEnum xe && xe.constantName().stringValue().equals("RUNTIME"), "deep E: e=RUNTIME");
        check(elemOf(a, "arr") instanceof AnnotationValue.OfArray xa && xa.values().size() == 3
                && ((AnnotationValue.OfInt) xa.values().get(2)).intValue() == 0, "deep E: arr={7,-7,0}");
        check(elemOf(a, "n") instanceof AnnotationValue.OfAnnotation xn
                && ((AnnotationValue.OfInt) elemOf(xn.annotation(), "q")).intValue() == 5, "deep E: n=@InnerAnno(q=5)");
        // CLASS retention -> RuntimeInvisibleAnnotations（默认 retention 不进 visible）
        check(!inv.findAttribute(Attributes.runtimeVisibleAnnotations()).isPresent(), "deep E: CLASS retention 无 RuntimeVisible");
        Annotation ia = inv.findAttribute(Attributes.runtimeInvisibleAnnotations()).get().annotations().get(0);
        check(ia.className().stringValue().equals("LAnnotationTest$ClassRet;"), "deep E: RuntimeInvisible className");
        check(ia.elements().size() == 1 && ((AnnotationValue.OfInt) elemOf(ia, "w")).intValue() == 6, "deep E: invisible w=6");
        // @interface 自身的 AnnotationDefault 全类型矩阵
        byte[] fb = AnnotationTest.class.getResourceAsStream("AnnotationTest$FullAnno.class").readAllBytes();
        ClassModel fm = cf.parse(fb);
        check(fm.methods().size() == 14, "deep E: FullAnno 14 个成员方法");
        java.util.Map<String, AnnotationValue> defaults = new java.util.HashMap<>();
        for (MethodModel mm : fm.methods()) {
            var d = mm.findAttribute(Attributes.annotationDefault());
            if (d.isPresent()) defaults.put(mm.methodName().stringValue(), d.get().defaultValue());
        }
        check(defaults.size() == 13, "deep E: 13 个带 default 成员（req 无）");
        check(defaults.get("v") instanceof AnnotationValue.OfInt dv && dv.intValue() == 7, "deep E: default v=7");
        check(defaults.get("s") instanceof AnnotationValue.OfString ds && ds.stringValue().equals("dflt"), "deep E: default s=dflt");
        check(defaults.get("b") instanceof AnnotationValue.OfBoolean db && !db.booleanValue(), "deep E: default b=false");
        check(defaults.get("c") instanceof AnnotationValue.OfChar dc && dc.charValue() == 'x', "deep E: default c=x");
        check(defaults.get("l") instanceof AnnotationValue.OfLong dl && dl.longValue() == 7L, "deep E: default l=7L");
        check(defaults.get("d") instanceof AnnotationValue.OfDouble dd && dd.doubleValue() == 1.25, "deep E: default d=1.25");
        check(defaults.get("f") instanceof AnnotationValue.OfFloat df && df.floatValue() == 0.5f, "deep E: default f=0.5f");
        check(defaults.get("cl") instanceof AnnotationValue.OfClass dcl && dcl.className().stringValue().equals("Ljava/lang/Runnable;"), "deep E: default cl=Runnable");
        check(defaults.get("e") instanceof AnnotationValue.OfEnum de && de.constantName().stringValue().equals("SOURCE"), "deep E: default e=SOURCE");
        check(defaults.get("arr") instanceof AnnotationValue.OfArray da && da.values().size() == 2
                && ((AnnotationValue.OfInt) da.values().get(1)).intValue() == 2, "deep E: default arr={1,2}");
        check(defaults.get("by") instanceof AnnotationValue.OfByte dby && dby.byteValue() == (byte) 3, "deep E: default by=3");
        check(defaults.get("sh") instanceof AnnotationValue.OfShort dsh && dsh.shortValue() == (short) 300, "deep E: default sh=300");
        check(defaults.get("n") instanceof AnnotationValue.OfAnnotation dn
                && dn.annotation().className().stringValue().equals("LAnnotationTest$InnerAnno;")
                && ((AnnotationValue.OfInt) elemOf(dn.annotation(), "q")).intValue() == 9, "deep E: default n=@InnerAnno(q=9)");
        // @Retention(RUNTIME) 元注解落在 FullAnno 自身的 RuntimeVisibleAnnotations
        var metaVis = fm.findAttribute(Attributes.runtimeVisibleAnnotations());
        check(metaVis.isPresent() && metaVis.get().annotations().get(0).className().stringValue().equals("Ljava/lang/annotation/Retention;"),
                "deep E: @Retention 元注解可见");
    }

    // ---- 真类产物：全形态 @interface（13 defaulted 成员 + req）+ CLASS retention 对照 ----
    @Retention(RetentionPolicy.RUNTIME)
    @interface InnerAnno {
        int q() default 1;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface FullAnno {
        int v() default 7;
        String s() default "dflt";
        boolean b() default false;
        char c() default 'x';
        long l() default 7L;
        double d() default 1.25;
        float f() default 0.5f;
        Class<?> cl() default Runnable.class;
        RetentionPolicy e() default RetentionPolicy.SOURCE;
        int[] arr() default {1, 2};
        byte by() default 3;
        short sh() default 300;
        InnerAnno n() default @InnerAnno(q = 9);
        String req();
    }

    @interface ClassRet {
        int w() default 5;
    }

    @FullAnno(req = "r", v = 9, s = "hi", b = true, c = '\0', l = -9223372036854775808L,
            d = 3.5, f = -0.0f, cl = java.util.List.class, e = RetentionPolicy.RUNTIME,
            arr = {7, -7, 0}, n = @InnerAnno(q = 5))
    static int fullField;

    @ClassRet(w = 6)
    static int invisibleField;

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
        testOfOverloadsAndNulls();
        testBuildReparseRoundTrip();
        testNestedAnnotation();
        testRealDeprecatedMethod();
        testDeepOfNullMatrix();
        testDeepEqualsHashCodeToString();
        testDeepMegaRoundTrip();
        testDeepBuilderAttachment();
        testDeepRealJavacFullMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation conformance: white-box structure + black-box behavior.
 * @run main TypeAnnotationTest
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

public class TypeAnnotationTest {
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

    // ===== 深化：真 javac 产物注解类型与载体类 =====
    static final String TA_DESC = "L" + TypeAnnotationTest.class.getName().replace('.', '/') + "$TA;";
    static final String TB_DESC = "L" + TypeAnnotationTest.class.getName().replace('.', '/') + "$TB;";

    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE_USE, java.lang.annotation.ElementType.LOCAL_VARIABLE})
    @interface TA { String value() default "x"; }

    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.CLASS)
    @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE_USE, java.lang.annotation.ElementType.LOCAL_VARIABLE})
    @interface TB { }

    static class TABase { }
    static interface TAIF1 { }

    /** 各类型位置真产物载体：字段/方法签名/方法体全覆盖。 */
    static class Holder extends @TA("sup") TABase implements @TA("if0") Cloneable, @TA("if1") TAIF1 {
        @TA("f0") String f0;                                     // FIELD, path=[]
        java.util.List<@TA("f1") String> f1;                     // FIELD, [TYPE_ARGUMENT 0]
        java.util.List<? extends @TA("f2") Number> f2;           // FIELD, [TYPE_ARGUMENT 0, WILDCARD]
        String @TA("a0") [] @TA("a1") [] f3;                     // FIELD x2, [] 与 [ARRAY]
        java.util.List<@TA("f4") String[]> f4;                   // FIELD, [TYPE_ARGUMENT 0, ARRAY]
        @TB String fb;                                           // RuntimeInvisible 版
        <@TA("c0") T, @TA("c1") U extends @TA("b0") Number & @TA("b1") Runnable> void gc() { }
        <@TA("m0") T> void gm(@TA("p0") String p, int q, @TA("p2") long r) { }
        java.util.@TA("ret") List<@TA("reta") String> mr() { return null; }
        void th() throws @TA("t0") IllegalArgumentException, @TA("t1") IllegalStateException { }
        void code(Object o) throws Exception {
            @TA("lv") String lv = "s";
            java.util.List<@TA("lvta") String> gl = java.util.List.of();
            String cs = (@TA("cast") String) o;
            @SuppressWarnings("unchecked") java.util.List<@TA("gcta") String> gcs =
                    (java.util.List<@TA("gcast") String>) (Object) java.util.List.of();
            boolean ib = o instanceof @TA("inst") String;
            Object nw = new @TA("new") TABase();
            java.util.Set<String> s = java.util.Collections.<@TA("mita") String>singleton("x");
            java.util.function.Predicate<String> pr = @TA("mref") String::isEmpty;
            try { lv.length(); } catch (@TA("exc") RuntimeException e) { }
            try (@TA("res") AutoCloseable rr = null) { }
            if (ib && cs.isEmpty() && gl.isEmpty() && s.isEmpty() && pr.test(lv) && nw != null && gcs.isEmpty()) System.out.print("");
        }
    }

    // ===== 深化辅助 =====
    static String aval(TypeAnnotation t) {
        for (AnnotationElement e : t.annotation().elements())
            if (e.name().stringValue().equals("value"))
                return ((AnnotationValue.OfString) e.value()).stringValue();
        return "?";
    }
    /** 按 value 元素唯一锁定一条真产物注解（对 javac 输出顺序鲁棒）。 */
    static TypeAnnotation findOne(java.util.List<TypeAnnotation> l, String val) {
        TypeAnnotation r = null;
        for (TypeAnnotation t : l) {
            if (aval(t).equals(val)) {
                if (r != null) throw new AssertionError("duplicate " + val);
                r = t;
            }
        }
        if (r == null) throw new AssertionError("missing " + val);
        return r;
    }
    static void checkPath(TypeAnnotation t, TypeAnnotation.TypePathComponent.Kind[] kinds, int[] idxs, String msg) {
        java.util.List<TypeAnnotation.TypePathComponent> p = t.targetPath();
        check(p.size() == kinds.length, msg + ": path length " + p.size());
        for (int i = 0; i < kinds.length; i++) {
            check(p.get(i).typePathKind() == kinds[i], msg + ": kind@" + i);
            check(p.get(i).typeArgumentIndex() == idxs[i], msg + ": typeArgumentIndex@" + i);
        }
    }
    static FieldModel field(ClassModel cm, String name) {
        for (FieldModel f : cm.fields())
            if (f.fieldName().stringValue().equals(name)) return f;
        throw new AssertionError("field not found: " + name);
    }
    static MethodModel method(ClassModel cm, String name) {
        for (MethodModel m : cm.methods())
            if (m.methodName().stringValue().equals(name)) return m;
        throw new AssertionError("method not found: " + name);
    }
    static int nthBci(CodeAttribute ca, Opcode op, int nth) {
        int bci = 0, seen = 0;
        for (CodeElement e : ca.elementList()) {
            if (e instanceof Instruction i) {
                if (i.opcode() == op) { if (seen++ == nth) return bci; }
                bci += i.sizeInBytes();
            }
        }
        return -1;
    }


    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.TypeAnnotation");
        check(c != null, "java.lang.classfile.TypeAnnotation loads");
        check(c.getName().equals("java.lang.classfile.TypeAnnotation"), "binary name java.lang.classfile.TypeAnnotation");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeAnnotation is public");
        check(c.isInterface(), "java.lang.classfile.TypeAnnotation is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.TypeAnnotation has no superclass");
        check(declares(c, "targetInfo"), "declares targetInfo");
        check(declares(c, "targetPath"), "declares targetPath");
        check(declares(c, "annotation"), "declares annotation");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        TypeAnnotation ta = TypeAnnotation.of(TypeAnnotation.TargetInfo.ofField(), java.util.List.of(TypeAnnotation.TypePathComponent.ARRAY), Annotation.of(ClassDesc.of("java.lang.Deprecated")));
        check(ta.targetInfo().targetType() == TypeAnnotation.TargetType.FIELD, "target FIELD");
        check(ta.targetPath().size() == 1 && ta.targetPath().get(0) == TypeAnnotation.TypePathComponent.ARRAY, "targetPath");
        check(ta.annotation().classSymbol().equals(ClassDesc.of("java.lang.Deprecated")), "annotation");

    }

    // ---- Part 3: of(...) 工厂矩阵 + null 语义（实测锁定） ----
    static void testFactoryNulls() {
        Annotation an = Annotation.of(ClassDesc.of("pkg.Ann"), AnnotationElement.ofString("value", "v"));
        // 全参回读
        TypeAnnotation t1 = TypeAnnotation.of(TypeAnnotation.TargetInfo.ofField(), java.util.List.of(), an);
        check(t1.targetInfo().targetType() == TypeAnnotation.TargetType.FIELD, "of: targetType FIELD");
        check(t1.targetPath().isEmpty(), "of: empty targetPath");
        check(t1.annotation().className().stringValue().equals("Lpkg/Ann;"), "of: className");
        check(t1.annotation().elements().size() == 1, "of: one element");
        AnnotationElement el = t1.annotation().elements().get(0);
        check(el.name().stringValue().equals("value"), "of: element name");
        check(((AnnotationValue.OfString) el.value()).stringValue().equals("v"), "of: element value");
        // 多步 targetPath 回读
        TypeAnnotation t2 = TypeAnnotation.of(TypeAnnotation.TargetInfo.ofMethodReturn(),
                java.util.List.of(TypeAnnotation.TypePathComponent.of(TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT, 5),
                                  TypeAnnotation.TypePathComponent.ARRAY),
                an);
        checkPath(t2,
                new TypeAnnotation.TypePathComponent.Kind[]{TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT, TypeAnnotation.TypePathComponent.Kind.ARRAY},
                new int[]{5, 0}, "of: two-step path");
        // targetPath 视图不可变
        try {
            t2.targetPath().add(TypeAnnotation.TypePathComponent.ARRAY);
            check(false, "targetPath must be immutable");
        } catch (UnsupportedOperationException e) { check(true, "targetPath UOE"); }
        // null 矩阵（实测：三个参数各自 NPE；path 为 null 的消息含 coll）
        try { TypeAnnotation.of(null, java.util.List.of(), an); check(false, "of(null,..) must NPE"); }
        catch (NullPointerException e) { check(true, "of(null,..) NPE"); }
        try { TypeAnnotation.of(TypeAnnotation.TargetInfo.ofField(), null, an); check(false, "of(..,null,..) must NPE"); }
        catch (NullPointerException e) { check(e.getMessage() != null && e.getMessage().contains("coll"), "of(..,null,..) NPE msg coll"); }
        try { TypeAnnotation.of(TypeAnnotation.TargetInfo.ofField(), java.util.List.of(), null); check(false, "of(..,null) must NPE"); }
        catch (NullPointerException e) { check(true, "of(..,null) NPE"); }
    }

    // ---- Part 4: 真 javac 产物闭环（核心：12 种 targetInfo 形态 + 5 种 path 形态 + Invisible 版） ----
    static void testRealJavacProducts() throws Exception {
        byte[] hb = TypeAnnotationTest.class.getResourceAsStream("TypeAnnotationTest$Holder.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(hb);

        // 类级：extends(65535) + implements(0/1)
        var clsAttr = cm.findAttribute(Attributes.runtimeVisibleTypeAnnotations());
        check(clsAttr.isPresent(), "class-level RVTA present");
        java.util.List<TypeAnnotation> cls = clsAttr.get().annotations();
        check(cls.size() == 3, "class-level RVTA count 3, got " + cls.size());
        TypeAnnotation sup = findOne(cls, "sup");
        check(sup.targetInfo() instanceof TypeAnnotation.SupertypeTarget, "extends is SupertypeTarget");
        check(((TypeAnnotation.SupertypeTarget) sup.targetInfo()).supertypeIndex() == 65535, "superclass supertypeIndex 65535");
        check(sup.annotation().className().stringValue().equals(TA_DESC), "extends ann descriptor");
        checkPath(sup, new TypeAnnotation.TypePathComponent.Kind[0], new int[0], "extends path empty");
        TypeAnnotation if0 = findOne(cls, "if0");
        check(((TypeAnnotation.SupertypeTarget) if0.targetInfo()).supertypeIndex() == 0, "first interface index 0");
        TypeAnnotation if1 = findOne(cls, "if1");
        check(((TypeAnnotation.SupertypeTarget) if1.targetInfo()).supertypeIndex() == 1, "second interface index 1");
        check(if0.targetInfo().targetType() == TypeAnnotation.TargetType.CLASS_EXTENDS, "interface ann also CLASS_EXTENDS");

        // 字段级：FIELD 空 path / TYPE_ARGUMENT / WILDCARD / ARRAY / TYPE_ARGUMENT+ARRAY / Invisible
        java.util.List<TypeAnnotation> f0s = field(cm, "f0").findAttribute(Attributes.runtimeVisibleTypeAnnotations()).get().annotations();
        check(f0s.size() == 1, "f0 count 1");
        check(f0s.get(0).targetInfo() instanceof TypeAnnotation.EmptyTarget, "f0 is EmptyTarget");
        check(f0s.get(0).targetInfo().targetType() == TypeAnnotation.TargetType.FIELD, "f0 FIELD");
        check(f0s.get(0).annotation().elements().size() == 1, "f0 annotation has value element");
        checkPath(findOne(f0s, "f0"), new TypeAnnotation.TypePathComponent.Kind[0], new int[0], "f0 path empty");
        java.util.List<TypeAnnotation> f1s = field(cm, "f1").findAttribute(Attributes.runtimeVisibleTypeAnnotations()).get().annotations();
        checkPath(findOne(f1s, "f1"),
                new TypeAnnotation.TypePathComponent.Kind[]{TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT}, new int[]{0}, "f1 path");
        java.util.List<TypeAnnotation> f2s = field(cm, "f2").findAttribute(Attributes.runtimeVisibleTypeAnnotations()).get().annotations();
        checkPath(findOne(f2s, "f2"),
                new TypeAnnotation.TypePathComponent.Kind[]{TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT, TypeAnnotation.TypePathComponent.Kind.WILDCARD},
                new int[]{0, 0}, "f2 path TYPE_ARGUMENT+WILDCARD");
        java.util.List<TypeAnnotation> f3s = field(cm, "f3").findAttribute(Attributes.runtimeVisibleTypeAnnotations()).get().annotations();
        check(f3s.size() == 2, "f3 count 2");
        checkPath(findOne(f3s, "a0"), new TypeAnnotation.TypePathComponent.Kind[0], new int[0], "f3 outer array path empty");
        checkPath(findOne(f3s, "a1"),
                new TypeAnnotation.TypePathComponent.Kind[]{TypeAnnotation.TypePathComponent.Kind.ARRAY}, new int[]{0}, "f3 inner array path");
        java.util.List<TypeAnnotation> f4s = field(cm, "f4").findAttribute(Attributes.runtimeVisibleTypeAnnotations()).get().annotations();
        checkPath(findOne(f4s, "f4"),
                new TypeAnnotation.TypePathComponent.Kind[]{TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT, TypeAnnotation.TypePathComponent.Kind.ARRAY},
                new int[]{0, 0}, "f4 path TYPE_ARGUMENT+ARRAY");
        check(!field(cm, "fb").findAttribute(Attributes.runtimeVisibleTypeAnnotations()).isPresent(), "fb has no RVTA");
        java.util.List<TypeAnnotation> fbs = field(cm, "fb").findAttribute(Attributes.runtimeInvisibleTypeAnnotations()).get().annotations();
        check(fbs.size() == 1, "fb RITA count 1");
        check(fbs.get(0).targetInfo().targetType() == TypeAnnotation.TargetType.FIELD, "fb RITA FIELD");
        check(fbs.get(0).annotation().className().stringValue().equals(TB_DESC), "fb RITA descriptor");
        check(fbs.get(0).annotation().elements().isEmpty(), "fb RITA no elements");

        // 方法级：类型参数 / bound / 形参 / 返回值 / throws
        java.util.List<TypeAnnotation> gcs = method(cm, "gc").findAttribute(Attributes.runtimeVisibleTypeAnnotations()).get().annotations();
        check(gcs.size() == 4, "gc count 4");
        check(findOne(gcs, "c0").targetInfo() instanceof TypeAnnotation.TypeParameterTarget, "gc c0 TypeParameterTarget");
        check(((TypeAnnotation.TypeParameterTarget) findOne(gcs, "c0").targetInfo()).typeParameterIndex() == 0, "gc c0 idx 0");
        check(((TypeAnnotation.TypeParameterTarget) findOne(gcs, "c1").targetInfo()).typeParameterIndex() == 1, "gc c1 idx 1");
        TypeAnnotation b0 = findOne(gcs, "b0");
        check(b0.targetInfo() instanceof TypeAnnotation.TypeParameterBoundTarget, "gc b0 BoundTarget");
        check(((TypeAnnotation.TypeParameterBoundTarget) b0.targetInfo()).typeParameterIndex() == 1, "gc b0 tpIdx 1");
        check(((TypeAnnotation.TypeParameterBoundTarget) b0.targetInfo()).boundIndex() == 0, "gc b0 boundIdx 0");
        TypeAnnotation b1 = findOne(gcs, "b1");
        check(((TypeAnnotation.TypeParameterBoundTarget) b1.targetInfo()).boundIndex() == 1, "gc b1 boundIdx 1");
        java.util.List<TypeAnnotation> gms = method(cm, "gm").findAttribute(Attributes.runtimeVisibleTypeAnnotations()).get().annotations();
        check(gms.size() == 3, "gm count 3");
        check(((TypeAnnotation.TypeParameterTarget) findOne(gms, "m0").targetInfo()).typeParameterIndex() == 0, "gm m0 idx 0");
        check(findOne(gms, "p0").targetInfo() instanceof TypeAnnotation.FormalParameterTarget, "gm p0 FormalParameterTarget");
        check(((TypeAnnotation.FormalParameterTarget) findOne(gms, "p0").targetInfo()).formalParameterIndex() == 0, "gm p0 fpIdx 0");
        check(((TypeAnnotation.FormalParameterTarget) findOne(gms, "p2").targetInfo()).formalParameterIndex() == 2, "gm p2 fpIdx 2（声明序而非槽位序）");
        java.util.List<TypeAnnotation> mrs = method(cm, "mr").findAttribute(Attributes.runtimeVisibleTypeAnnotations()).get().annotations();
        check(mrs.size() == 2, "mr count 2");
        check(findOne(mrs, "ret").targetInfo() instanceof TypeAnnotation.EmptyTarget, "mr ret EmptyTarget");
        check(findOne(mrs, "ret").targetInfo().targetType() == TypeAnnotation.TargetType.METHOD_RETURN, "mr ret METHOD_RETURN");
        checkPath(findOne(mrs, "ret"), new TypeAnnotation.TypePathComponent.Kind[0], new int[0], "mr ret path empty");
        checkPath(findOne(mrs, "reta"),
                new TypeAnnotation.TypePathComponent.Kind[]{TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT}, new int[]{0}, "mr reta path");
        java.util.List<TypeAnnotation> ths = method(cm, "th").findAttribute(Attributes.runtimeVisibleTypeAnnotations()).get().annotations();
        check(ths.size() == 2, "th count 2");
        check(findOne(ths, "t0").targetInfo() instanceof TypeAnnotation.ThrowsTarget, "th t0 ThrowsTarget");
        check(((TypeAnnotation.ThrowsTarget) findOne(ths, "t0").targetInfo()).throwsTargetIndex() == 0, "th t0 idx 0");
        check(((TypeAnnotation.ThrowsTarget) findOne(ths, "t1").targetInfo()).throwsTargetIndex() == 1, "th t1 idx 1");

        // 代码级：CAST x2 / INSTANCEOF / NEW / 方法调用类型实参 / 方法引用 / 局部变量 x2 / catch / 资源变量
        CodeAttribute ca = (CodeAttribute) method(cm, "code").code().get();
        java.util.List<TypeAnnotation> cs = ca.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).get().annotations();
        check(cs.size() == 11, "code count 11, got " + cs.size());
        TypeAnnotation cast = findOne(cs, "cast");
        check(cast.targetInfo() instanceof TypeAnnotation.TypeArgumentTarget, "cast TypeArgumentTarget");
        check(((TypeAnnotation.TypeArgumentTarget) cast.targetInfo()).typeArgumentIndex() == 0, "cast taIdx 0");
        check(cast.targetInfo().targetType() == TypeAnnotation.TargetType.CAST, "cast targetType CAST");
        checkPath(cast, new TypeAnnotation.TypePathComponent.Kind[0], new int[0], "cast path empty");
        check(ca.labelToBci(((TypeAnnotation.TypeArgumentTarget) cast.targetInfo()).target()) == nthBci(ca, Opcode.CHECKCAST, 0),
                "cast label anchors at 1st CHECKCAST");
        TypeAnnotation gcast = findOne(cs, "gcast");
        check(gcast.targetInfo().targetType() == TypeAnnotation.TargetType.CAST, "gcast targetType CAST");
        checkPath(gcast,
                new TypeAnnotation.TypePathComponent.Kind[]{TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT}, new int[]{0}, "gcast path");
        check(ca.labelToBci(((TypeAnnotation.TypeArgumentTarget) gcast.targetInfo()).target()) == nthBci(ca, Opcode.CHECKCAST, 1),
                "gcast label anchors at 2nd CHECKCAST");
        TypeAnnotation inst = findOne(cs, "inst");
        check(inst.targetInfo() instanceof TypeAnnotation.OffsetTarget, "inst OffsetTarget");
        check(inst.targetInfo().targetType() == TypeAnnotation.TargetType.INSTANCEOF, "inst targetType INSTANCEOF");
        check(ca.labelToBci(((TypeAnnotation.OffsetTarget) inst.targetInfo()).target()) == nthBci(ca, Opcode.INSTANCEOF, 0),
                "inst label anchors at INSTANCEOF");
        TypeAnnotation nw = findOne(cs, "new");
        check(nw.targetInfo().targetType() == TypeAnnotation.TargetType.NEW, "new targetType NEW");
        check(ca.labelToBci(((TypeAnnotation.OffsetTarget) nw.targetInfo()).target()) == nthBci(ca, Opcode.NEW, 0),
                "new label anchors at NEW");
        TypeAnnotation mita = findOne(cs, "mita");
        check(mita.targetInfo() instanceof TypeAnnotation.TypeArgumentTarget, "mita TypeArgumentTarget");
        check(mita.targetInfo().targetType() == TypeAnnotation.TargetType.METHOD_INVOCATION_TYPE_ARGUMENT, "mita targetType");
        check(((TypeAnnotation.TypeArgumentTarget) mita.targetInfo()).typeArgumentIndex() == 0, "mita taIdx 0");
        check(ca.labelToBci(((TypeAnnotation.TypeArgumentTarget) mita.targetInfo()).target()) >= 0, "mita label bound");
        TypeAnnotation mref = findOne(cs, "mref");
        check(mref.targetInfo().targetType() == TypeAnnotation.TargetType.METHOD_REFERENCE, "mref targetType METHOD_REFERENCE");
        check(ca.labelToBci(((TypeAnnotation.OffsetTarget) mref.targetInfo()).target()) == nthBci(ca, Opcode.INVOKEDYNAMIC, 0),
                "mref label anchors at INVOKEDYNAMIC");
        TypeAnnotation lv = findOne(cs, "lv");
        check(lv.targetInfo() instanceof TypeAnnotation.LocalVarTarget, "lv LocalVarTarget");
        check(lv.targetInfo().targetType() == TypeAnnotation.TargetType.LOCAL_VARIABLE, "lv targetType LOCAL_VARIABLE");
        checkPath(lv, new TypeAnnotation.TypePathComponent.Kind[0], new int[0], "lv path empty");
        java.util.List<TypeAnnotation.LocalVarTargetInfo> ltbl = ((TypeAnnotation.LocalVarTarget) lv.targetInfo()).table();
        check(ltbl.size() == 1, "lv table size 1");
        int lvSlot = ltbl.get(0).index();
        check(lvSlot >= 1, "lv slot after param, got " + lvSlot);
        check(ca.labelToBci(ltbl.get(0).startLabel()) < ca.labelToBci(ltbl.get(0).endLabel()), "lv range start<end");
        TypeAnnotation gcta = findOne(cs, "gcta");
        check(gcta.targetInfo().targetType() == TypeAnnotation.TargetType.LOCAL_VARIABLE, "gcta LOCAL_VARIABLE");
        checkPath(gcta,
                new TypeAnnotation.TypePathComponent.Kind[]{TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT}, new int[]{0}, "gcta path");
        TypeAnnotation lvta = findOne(cs, "lvta");
        check(lvta.targetInfo().targetType() == TypeAnnotation.TargetType.LOCAL_VARIABLE, "lvta targetType");
        checkPath(lvta,
                new TypeAnnotation.TypePathComponent.Kind[]{TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT}, new int[]{0}, "lvta path");
        int lvtaSlot = ((TypeAnnotation.LocalVarTarget) lvta.targetInfo()).table().get(0).index();
        check(lvtaSlot != lvSlot, "lv/lvta different slots");
        TypeAnnotation exc = findOne(cs, "exc");
        check(exc.targetInfo() instanceof TypeAnnotation.CatchTarget, "exc CatchTarget");
        check(((TypeAnnotation.CatchTarget) exc.targetInfo()).exceptionTableIndex() == 0, "exc excIdx 0");
        TypeAnnotation res = findOne(cs, "res");
        check(res.targetInfo() instanceof TypeAnnotation.LocalVarTarget, "res LocalVarTarget");
        check(res.targetInfo().targetType() == TypeAnnotation.TargetType.RESOURCE_VARIABLE, "res targetType RESOURCE_VARIABLE");
        check(((TypeAnnotation.LocalVarTarget) res.targetInfo()).table().size() == 1, "res table size 1");
    }

    // ---- Part 5: build 注入 → re-parse 回读闭环（RVTA/RITA + 代码级 Label 解析） ----
    static void testBuildInjection() {
        Annotation an = Annotation.of(ClassDesc.of("pkg.Inj"), AnnotationElement.ofString("value", "iv"));
        TypeAnnotation fieldTa = TypeAnnotation.of(TypeAnnotation.TargetInfo.ofField(),
                java.util.List.of(TypeAnnotation.TypePathComponent.of(TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT, 3)), an);
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.TAInj"), cb -> cb
            .withField("fx", ConstantDescs.CD_String, fb -> fb.with(RuntimeVisibleTypeAnnotationsAttribute.of(java.util.List.of(fieldTa))))
            .withMethodBody("m", MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;)Ljava/lang/String;"), 0, cob -> {
                Label lb = cob.newLabel();
                cob.aload(0).labelBinding(lb).checkcast(ConstantDescs.CD_String).areturn();
                cob.with(RuntimeVisibleTypeAnnotationsAttribute.of(
                        TypeAnnotation.of(TypeAnnotation.TargetInfo.ofCastExpr(lb, 0), java.util.List.of(), an),
                        TypeAnnotation.of(TypeAnnotation.TargetInfo.ofInstanceofExpr(lb), java.util.List.of(), an)));
            }));
        ClassModel m = ClassFile.of().parse(b);
        // 字段级注入
        RuntimeVisibleTypeAnnotationsAttribute fa = field(m, "fx").findAttribute(Attributes.runtimeVisibleTypeAnnotations()).get();
        check(fa.attributeName().stringValue().equals(Attributes.NAME_RUNTIME_VISIBLE_TYPE_ANNOTATIONS), "attributeName");
        check(fa.attributeName().stringValue().equals("RuntimeVisibleTypeAnnotations"), "attributeName literal");
        check(fa.attributeMapper() == Attributes.runtimeVisibleTypeAnnotations(), "attributeMapper registry identity");
        check(fa.annotations().size() == 1, "injected field count 1");
        TypeAnnotation it = fa.annotations().get(0);
        check(it.targetInfo().targetType() == TypeAnnotation.TargetType.FIELD, "injected field targetType");
        checkPath(it,
                new TypeAnnotation.TypePathComponent.Kind[]{TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT}, new int[]{3}, "injected path");
        check(it.annotation().className().stringValue().equals("Lpkg/Inj;"), "injected className");
        check(aval(it).equals("iv"), "injected element value");
        try { fa.annotations().add(it); check(false, "annotations() must be immutable"); }
        catch (UnsupportedOperationException e) { check(true, "annotations() UOE"); }
        // 代码级注入（Label 已绑定 → offset 解析）
        CodeAttribute ca = (CodeAttribute) method(m, "m").code().get();
        RuntimeVisibleTypeAnnotationsAttribute ma = ca.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).get();
        check(ma.annotations().size() == 2, "injected code count 2");
        TypeAnnotation c0 = ma.annotations().get(0);
        check(c0.targetInfo().targetType() == TypeAnnotation.TargetType.CAST, "injected CAST targetType");
        check(((TypeAnnotation.TypeArgumentTarget) c0.targetInfo()).typeArgumentIndex() == 0, "injected CAST taIdx");
        TypeAnnotation c1 = ma.annotations().get(1);
        check(c1.targetInfo().targetType() == TypeAnnotation.TargetType.INSTANCEOF, "injected INSTANCEOF targetType");
        check(ca.labelToBci(((TypeAnnotation.OffsetTarget) c1.targetInfo()).target()) == nthBci(ca, Opcode.CHECKCAST, 0),
                "injected label binds to anchored bci");
        check(ClassFile.of().verify(m).isEmpty(), "injected class verifies clean");
        // varargs 工厂
        check(RuntimeVisibleTypeAnnotationsAttribute.of(c0, c1).annotations().size() == 2, "varargs of count");
        // RITA 注入
        byte[] b2 = ClassFile.of().build(ClassDesc.of("pkg.TAInj2"), cb -> cb
            .withField("fy", ConstantDescs.CD_String, fb -> fb.with(RuntimeInvisibleTypeAnnotationsAttribute.of(java.util.List.of(fieldTa)))));
        RuntimeInvisibleTypeAnnotationsAttribute ia = field(ClassFile.of().parse(b2), "fy")
                .findAttribute(Attributes.runtimeInvisibleTypeAnnotations()).get();
        check(ia.attributeName().stringValue().equals(Attributes.NAME_RUNTIME_INVISIBLE_TYPE_ANNOTATIONS), "RITA attributeName");
        check(ia.annotations().size() == 1, "RITA injected count");
        check(ia.annotations().get(0).targetInfo().targetType() == TypeAnnotation.TargetType.FIELD, "RITA injected targetType");
        checkPath(ia.annotations().get(0),
                new TypeAnnotation.TypePathComponent.Kind[]{TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT}, new int[]{3}, "RITA injected path");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryNulls();
        testRealJavacProducts();
        testBuildInjection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

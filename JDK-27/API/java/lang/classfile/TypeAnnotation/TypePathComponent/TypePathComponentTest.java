/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.TypePathComponent conformance: white-box structure + black-box behavior.
 * @run main TypePathComponentTest
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

public class TypePathComponentTest {
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
        Class<?> c = Class.forName("java.lang.classfile.TypeAnnotation$TypePathComponent");
        check(c != null, "java.lang.classfile.TypeAnnotation.TypePathComponent loads");
        check(c.getName().equals("java.lang.classfile.TypeAnnotation$TypePathComponent"), "binary name java.lang.classfile.TypeAnnotation$TypePathComponent");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeAnnotation.TypePathComponent is public");
        check(c.isInterface(), "java.lang.classfile.TypeAnnotation.TypePathComponent is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.TypeAnnotation.TypePathComponent has no superclass");
        check(declares(c, "typePathKind"), "declares typePathKind");
        check(declares(c, "typeArgumentIndex"), "declares typeArgumentIndex");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(TypeAnnotation.TypePathComponent.ARRAY.typePathKind() == TypeAnnotation.TypePathComponent.Kind.ARRAY, "ARRAY kind");
        check(TypeAnnotation.TypePathComponent.ARRAY.typeArgumentIndex() == 0, "ARRAY typeArgumentIndex");
        check(TypeAnnotation.TypePathComponent.INNER_TYPE.typePathKind() == TypeAnnotation.TypePathComponent.Kind.INNER_TYPE, "INNER_TYPE kind");
        check(TypeAnnotation.TypePathComponent.WILDCARD.typePathKind() == TypeAnnotation.TypePathComponent.Kind.WILDCARD, "WILDCARD kind");
        TypeAnnotation.TypePathComponent ta = TypeAnnotation.TypePathComponent.of(TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT, 1);
        check(ta.typePathKind() == TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT, "of kind");
        check(ta.typeArgumentIndex() == 1, "of index");

    }

    // ===== 深化：真 javac 产物载体（path 全形态矩阵） =====
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE_USE, java.lang.annotation.ElementType.LOCAL_VARIABLE})
    @interface PCA { String value() default "x"; }

    static class Holder {
        java.util.List<@PCA("ta") String> f1;                     // [TYPE_ARGUMENT 0]
        java.util.List<? extends @PCA("wild") Number> f2;         // [TYPE_ARGUMENT 0, WILDCARD]
        String @PCA("a0") [] @PCA("a1") [] f3;                    // [] 与 [ARRAY]（外/内维度）
        java.util.List<@PCA("taarr") String[]> f4;                // [TYPE_ARGUMENT 0, ARRAY]
        java.util.@PCA("ret") List<@PCA("reta") String> mr() { return null; }
        void code(Object o) {
            @SuppressWarnings("unchecked") java.util.List<@PCA("gcta") String> gcs =
                    (java.util.List<@PCA("gcast") String>) (Object) java.util.List.of();
            if (gcs.isEmpty()) System.out.print("");
        }
    }

    static String aval(TypeAnnotation t) {
        for (AnnotationElement e : t.annotation().elements())
            if (e.name().stringValue().equals("value"))
                return ((AnnotationValue.OfString) e.value()).stringValue();
        return "?";
    }
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
    static void checkPath(java.util.List<TypeAnnotation.TypePathComponent> p,
                          TypeAnnotation.TypePathComponent.Kind[] kinds, int[] idxs, String msg) {
        if (p.size() != kinds.length) throw new AssertionError(msg + ": size " + p.size() + " != " + kinds.length);
        for (int i = 0; i < kinds.length; i++) {
            if (p.get(i).typePathKind() != kinds[i] || p.get(i).typeArgumentIndex() != idxs[i])
                throw new AssertionError(msg + "@" + i + ": " + p.get(i).typePathKind() + "/" + p.get(i).typeArgumentIndex());
        }
        passed++;
    }

    // ---- Part 3: 工厂语义（常量身份 + 非 TYPE_ARGUMENT 索引归零 + null 校验） ----
    static void testFactoryMatrix() {
        TypeAnnotation.TypePathComponent K = TypeAnnotation.TypePathComponent.ARRAY;
        // of(非 TYPE_ARGUMENT, 0) 返回常量本身（身份相等，探针实测）
        check(TypeAnnotation.TypePathComponent.of(TypeAnnotation.TypePathComponent.Kind.ARRAY, 0) == K, "of(ARRAY,0)==ARRAY identity");
        check(TypeAnnotation.TypePathComponent.of(TypeAnnotation.TypePathComponent.Kind.INNER_TYPE, 0) == TypeAnnotation.TypePathComponent.INNER_TYPE,
                "of(INNER_TYPE,0)==INNER_TYPE identity");
        check(TypeAnnotation.TypePathComponent.of(TypeAnnotation.TypePathComponent.Kind.WILDCARD, 0) == TypeAnnotation.TypePathComponent.WILDCARD,
                "of(WILDCARD,0)==WILDCARD identity");
        // 非 TYPE_ARGUMENT kind 时 index 归零
        check(TypeAnnotation.TypePathComponent.of(TypeAnnotation.TypePathComponent.Kind.WILDCARD, 3).typeArgumentIndex() == 0,
                "of(WILDCARD,3) index normalized to 0");
        check(TypeAnnotation.TypePathComponent.of(TypeAnnotation.TypePathComponent.Kind.ARRAY, 7).typeArgumentIndex() == 0,
                "of(ARRAY,7) index normalized to 0");
        // 常量的 typeArgumentIndex 恒 0
        check(TypeAnnotation.TypePathComponent.INNER_TYPE.typeArgumentIndex() == 0, "INNER_TYPE constant index 0");
        check(TypeAnnotation.TypePathComponent.WILDCARD.typeArgumentIndex() == 0, "WILDCARD constant index 0");
        // TYPE_ARGUMENT 任意 index 回读
        for (int i : new int[]{0, 1, 255}) {
            TypeAnnotation.TypePathComponent p = TypeAnnotation.TypePathComponent.of(TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT, i);
            check(p.typePathKind() == TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT && p.typeArgumentIndex() == i,
                    "of(TYPE_ARGUMENT," + i + ") readback");
        }
        try { TypeAnnotation.TypePathComponent.of(null, 0); check(false, "of(null,0) must NPE"); }
        catch (NullPointerException e) { check(e.getMessage() != null && e.getMessage().contains("typePathKind"), "of(null,0) NPE msg"); }
        // toString 稳定含 kind
        check(K.toString() != null && !K.toString().isEmpty(), "toString non-empty");
    }

    // ---- Part 4: 真 javac 产物（type_path 各 kind 全出现 + 多步组合顺序） ----
    static void testRealProduct() throws Exception {
        byte[] hb = TypePathComponentTest.class.getResourceAsStream("TypePathComponentTest$Holder.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(hb);
        // 字段级
        FieldModel f1m = cm.fields().stream().filter(f -> f.fieldName().stringValue().equals("f1")).findFirst().orElseThrow();
        java.util.List<TypeAnnotation> f1s = f1m.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations();
        checkPath(findOne(f1s, "ta").targetPath(),
                new TypeAnnotation.TypePathComponent.Kind[]{TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT}, new int[]{0},
                "f1 [TYPE_ARGUMENT 0]");
        FieldModel f2m = cm.fields().stream().filter(f -> f.fieldName().stringValue().equals("f2")).findFirst().orElseThrow();
        java.util.List<TypeAnnotation> f2s = f2m.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations();
        checkPath(findOne(f2s, "wild").targetPath(),
                new TypeAnnotation.TypePathComponent.Kind[]{TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT, TypeAnnotation.TypePathComponent.Kind.WILDCARD},
                new int[]{0, 0}, "f2 [TYPE_ARGUMENT 0, WILDCARD]");
        FieldModel f3m = cm.fields().stream().filter(f -> f.fieldName().stringValue().equals("f3")).findFirst().orElseThrow();
        java.util.List<TypeAnnotation> f3s = f3m.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations();
        checkPath(findOne(f3s, "a0").targetPath(), new TypeAnnotation.TypePathComponent.Kind[0], new int[0], "f3 外层维度 path empty");
        checkPath(findOne(f3s, "a1").targetPath(),
                new TypeAnnotation.TypePathComponent.Kind[]{TypeAnnotation.TypePathComponent.Kind.ARRAY}, new int[]{0}, "f3 内层维度 [ARRAY]");
        FieldModel f4m = cm.fields().stream().filter(f -> f.fieldName().stringValue().equals("f4")).findFirst().orElseThrow();
        java.util.List<TypeAnnotation> f4s = f4m.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations();
        checkPath(findOne(f4s, "taarr").targetPath(),
                new TypeAnnotation.TypePathComponent.Kind[]{TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT, TypeAnnotation.TypePathComponent.Kind.ARRAY},
                new int[]{0, 0}, "f4 [TYPE_ARGUMENT 0, ARRAY]");
        // 方法返回值：外层泛型与内层类型实参各一条
        MethodModel mr = cm.methods().stream().filter(m -> m.methodName().stringValue().equals("mr")).findFirst().orElseThrow();
        java.util.List<TypeAnnotation> mrs = mr.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations();
        checkPath(findOne(mrs, "ret").targetPath(), new TypeAnnotation.TypePathComponent.Kind[0], new int[0], "mr 外层 path empty");
        checkPath(findOne(mrs, "reta").targetPath(),
                new TypeAnnotation.TypePathComponent.Kind[]{TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT}, new int[]{0}, "mr 内层 [TYPE_ARGUMENT 0]");
        // 代码级：泛型强转与泛型局部声明
        MethodModel cm2 = cm.methods().stream().filter(m -> m.methodName().stringValue().equals("code")).findFirst().orElseThrow();
        CodeAttribute ca = (CodeAttribute) cm2.code().get();
        java.util.List<TypeAnnotation> cas = ca.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations();
        checkPath(findOne(cas, "gcast").targetPath(),
                new TypeAnnotation.TypePathComponent.Kind[]{TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT}, new int[]{0}, "gcast [TYPE_ARGUMENT 0]");
        checkPath(findOne(cas, "gcta").targetPath(),
                new TypeAnnotation.TypePathComponent.Kind[]{TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT}, new int[]{0}, "gcta [TYPE_ARGUMENT 0]");
    }

    // ---- Part 5: INNER_TYPE 注入回读（javac 源码无法产生限定名非末位注解，用 build 注入） ----
    static void testInnerTypeInjection() {
        Annotation an = Annotation.of(ClassDesc.of("p.PCAnn"),
                AnnotationElement.ofString("value", "inj"));
        java.util.List<TypeAnnotation.TypePathComponent> path = java.util.List.of(
                TypeAnnotation.TypePathComponent.INNER_TYPE,
                TypeAnnotation.TypePathComponent.of(TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT, 2),
                TypeAnnotation.TypePathComponent.ARRAY);
        TypeAnnotation tann = TypeAnnotation.of(TypeAnnotation.TargetInfo.ofField(), path, an);
        byte[] b = ClassFile.of().build(ClassDesc.of("p.PCInj"), cb -> cb
                .withField("f", ClassDesc.ofDescriptor("Ljava/util/List;"),
                        fb -> fb.with(RuntimeVisibleTypeAnnotationsAttribute.of(tann))));
        FieldModel fm = ClassFile.of().parse(b).fields().get(0);
        TypeAnnotation back = fm.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations().get(0);
        check(back.targetInfo().targetType() == TypeAnnotation.TargetType.FIELD, "injected FIELD");
        checkPath(back.targetPath(),
                new TypeAnnotation.TypePathComponent.Kind[]{TypeAnnotation.TypePathComponent.Kind.INNER_TYPE,
                        TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT, TypeAnnotation.TypePathComponent.Kind.ARRAY},
                new int[]{0, 2, 0}, "injected [INNER_TYPE, TYPE_ARGUMENT 2, ARRAY] round-trip");
        // 注入属性不可变
        try { back.targetPath().add(TypeAnnotation.TypePathComponent.ARRAY); check(false, "targetPath add must UOE"); }
        catch (UnsupportedOperationException e) { check(true, "targetPath immutable"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryMatrix();
        testRealProduct();
        testInnerTypeInjection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.TypeArgumentTarget conformance: white-box structure + black-box behavior.
 * @run main TypeArgumentTargetTest
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

public class TypeArgumentTargetTest {
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
        Class<?> c = Class.forName("java.lang.classfile.TypeAnnotation$TypeArgumentTarget");
        check(c != null, "java.lang.classfile.TypeAnnotation.TypeArgumentTarget loads");
        check(c.getName().equals("java.lang.classfile.TypeAnnotation$TypeArgumentTarget"), "binary name java.lang.classfile.TypeAnnotation$TypeArgumentTarget");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeAnnotation.TypeArgumentTarget is public");
        check(c.isInterface(), "java.lang.classfile.TypeAnnotation.TypeArgumentTarget is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.TypeAnnotation.TypeArgumentTarget has no superclass");
        check(declares(c, "target"), "declares target");
        check(declares(c, "typeArgumentIndex"), "declares typeArgumentIndex");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> {
            Label lb = cob.newLabel();
            ta[0] = TypeAnnotation.TargetInfo.ofCastExpr(lb, 1);
            cob.labelBinding(lb).return_();
        }));
        TypeAnnotation.TypeArgumentTarget t = ta[0];
        check(t != null, "ofCastExpr created");
        check(t.typeArgumentIndex() == 1, "typeArgumentIndex");
        check(t.targetType() == TypeAnnotation.TargetType.CAST, "targetType CAST");
        check(t.target() != null, "target label");

    }

    // ===== 深化：真 javac 产物载体（普通/泛型强转 + 泛型方法调用显式类型实参） =====
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE_USE, java.lang.annotation.ElementType.LOCAL_VARIABLE})
    @interface TAA { String value() default "x"; }

    static class Holder {
        void code(Object o) {
            String cs = (@TAA("cast") String) o;
            @SuppressWarnings("unchecked") java.util.List<@TAA("gcta") String> gcs =
                    (java.util.List<@TAA("gcast") String>) (Object) java.util.List.of();
            java.util.Set<String> s = java.util.Collections.<@TAA("mita") String>singleton("x");
            System.out.print(cs + gcs + s);
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

    // ---- Part 3: 六个命名工厂 + ofTypeArgument 通用工厂（targetType 矩阵 + 访问器 + 负例） ----
    static void testFactoryMatrix() {
        ClassFile.of().build(ClassDesc.of("pkg.TAFab"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), 0, cob -> {
            Label lb = cob.newLabel();
            TypeAnnotation.TypeArgumentTarget[] ts = {
                    TypeAnnotation.TargetInfo.ofCastExpr(lb, 0),
                    TypeAnnotation.TargetInfo.ofConstructorInvocationTypeArgument(lb, 1),
                    TypeAnnotation.TargetInfo.ofMethodInvocationTypeArgument(lb, 2),
                    TypeAnnotation.TargetInfo.ofConstructorReferenceTypeArgument(lb, 3),
                    TypeAnnotation.TargetInfo.ofMethodReferenceTypeArgument(lb, 4),
                    TypeAnnotation.TargetInfo.ofTypeArgument(TypeAnnotation.TargetType.CAST, lb, 5),
                    TypeAnnotation.TargetInfo.ofTypeArgument(TypeAnnotation.TargetType.CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT, lb, 0),
                    TypeAnnotation.TargetInfo.ofTypeArgument(TypeAnnotation.TargetType.METHOD_INVOCATION_TYPE_ARGUMENT, lb, 1),
                    TypeAnnotation.TargetInfo.ofTypeArgument(TypeAnnotation.TargetType.CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT, lb, 2),
                    TypeAnnotation.TargetInfo.ofTypeArgument(TypeAnnotation.TargetType.METHOD_REFERENCE_TYPE_ARGUMENT, lb, 3)};
            TypeAnnotation.TargetType[] et = {TypeAnnotation.TargetType.CAST,
                    TypeAnnotation.TargetType.CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT,
                    TypeAnnotation.TargetType.METHOD_INVOCATION_TYPE_ARGUMENT,
                    TypeAnnotation.TargetType.CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT,
                    TypeAnnotation.TargetType.METHOD_REFERENCE_TYPE_ARGUMENT,
                    TypeAnnotation.TargetType.CAST,
                    TypeAnnotation.TargetType.CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT,
                    TypeAnnotation.TargetType.METHOD_INVOCATION_TYPE_ARGUMENT,
                    TypeAnnotation.TargetType.CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT,
                    TypeAnnotation.TargetType.METHOD_REFERENCE_TYPE_ARGUMENT};
            int[] eidx = {0, 1, 2, 3, 4, 5, 0, 1, 2, 3};
            for (int i = 0; i < ts.length; i++) {
                check(ts[i].targetType() == et[i], "shape#" + i + " targetType");
                check(ts[i].typeArgumentIndex() == eidx[i], "shape#" + i + " typeArgumentIndex");
                check(ts[i].target() == lb, "shape#" + i + " label identity");
                check(ts[i].size() == 3, "shape#" + i + " size 3");
            }
            try { TypeAnnotation.TargetInfo.ofCastExpr(null, 0); check(false, "ofCastExpr(null,..) must NPE"); }
            catch (NullPointerException ex) { check(true, "ofCastExpr(null,0) NPE"); }
            try { TypeAnnotation.TargetInfo.ofMethodInvocationTypeArgument(null, 0); check(false, "ofMethodInvocationTypeArgument(null,..) must NPE"); }
            catch (NullPointerException ex) { check(true, "ofMethodInvocationTypeArgument(null,0) NPE"); }
            try { TypeAnnotation.TargetInfo.ofTypeArgument(TypeAnnotation.TargetType.FIELD, lb, 0); check(false, "ofTypeArgument(FIELD,..) must IAE"); }
            catch (IllegalArgumentException ex) { check(ex.getMessage() != null && ex.getMessage().contains("Wrong target type"), "ofTypeArgument(FIELD) IAE msg"); }
            try { TypeAnnotation.TargetInfo.ofTypeArgument(TypeAnnotation.TargetType.INSTANCEOF, lb, 0); check(false, "ofTypeArgument(INSTANCEOF,..) must IAE"); }
            catch (IllegalArgumentException ex) { check(true, "ofTypeArgument(INSTANCEOF) IAE"); }
            cob.labelBinding(lb).return_();
        }));
    }

    // ---- Part 4: 真 javac 产物（CAST 普通/泛型 + METHOD_INVOCATION_TYPE_ARGUMENT 锚点） ----
    static void testRealProduct() throws Exception {
        byte[] hb = TypeArgumentTargetTest.class.getResourceAsStream("TypeArgumentTargetTest$Holder.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(hb);
        MethodModel mm = cm.methods().stream().filter(m -> m.methodName().stringValue().equals("code")).findFirst().orElseThrow();
        CodeAttribute ca = (CodeAttribute) mm.code().get();
        java.util.List<TypeAnnotation> anns = ca.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations();
        check(anns.size() == 4, "4 annotations (3 type-argument + 1 local decl), got " + anns.size());
        for (TypeAnnotation t : anns) {
            if (t.targetInfo().targetType() == TypeAnnotation.TargetType.LOCAL_VARIABLE) continue;
            check(t.targetInfo() instanceof TypeAnnotation.TypeArgumentTarget, "ann is TypeArgumentTarget");
            check(t.targetInfo().size() == 3, "size 3");
        }
        TypeAnnotation gcta = findOne(anns, "gcta");
        check(gcta.targetInfo().targetType() == TypeAnnotation.TargetType.LOCAL_VARIABLE,
                "泛型强转赋值的局部变量声明同时产生 LOCAL_VARIABLE 注解");
        TypeAnnotation cast = findOne(anns, "cast");
        check(cast.targetInfo().targetType() == TypeAnnotation.TargetType.CAST, "cast CAST");
        check(((TypeAnnotation.TypeArgumentTarget) cast.targetInfo()).typeArgumentIndex() == 0, "非泛型强转 typeArgumentIndex 恒 0");
        check(cast.targetPath().isEmpty(), "cast path empty");
        check(ca.labelToBci(((TypeAnnotation.TypeArgumentTarget) cast.targetInfo()).target()) == nthBci(ca, Opcode.CHECKCAST, 0),
                "cast anchored at 1st CHECKCAST");
        TypeAnnotation gcast = findOne(anns, "gcast");
        check(gcast.targetInfo().targetType() == TypeAnnotation.TargetType.CAST, "gcast CAST");
        check(gcast.targetPath().size() == 1
                && gcast.targetPath().get(0).typePathKind() == TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT
                && gcast.targetPath().get(0).typeArgumentIndex() == 0, "泛型强转 path [TYPE_ARGUMENT 0]");
        check(ca.labelToBci(((TypeAnnotation.TypeArgumentTarget) gcast.targetInfo()).target()) == nthBci(ca, Opcode.CHECKCAST, 1),
                "gcast anchored at 2nd CHECKCAST");
        TypeAnnotation mita = findOne(anns, "mita");
        check(mita.targetInfo().targetType() == TypeAnnotation.TargetType.METHOD_INVOCATION_TYPE_ARGUMENT, "mita targetType");
        check(((TypeAnnotation.TypeArgumentTarget) mita.targetInfo()).typeArgumentIndex() == 0, "mita taIdx 0");
        check(ca.labelToBci(((TypeAnnotation.TypeArgumentTarget) mita.targetInfo()).target()) >= 0, "mita label bound in range");
        check(mita.targetPath().isEmpty(), "mita path empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryMatrix();
        testRealProduct();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

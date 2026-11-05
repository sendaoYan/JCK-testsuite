/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.TypeParameterBoundTarget conformance: white-box structure + black-box behavior.
 * @run main TypeParameterBoundTargetTest
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

public class TypeParameterBoundTargetTest {
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
        Class<?> c = Class.forName("java.lang.classfile.TypeAnnotation$TypeParameterBoundTarget");
        check(c != null, "java.lang.classfile.TypeAnnotation.TypeParameterBoundTarget loads");
        check(c.getName().equals("java.lang.classfile.TypeAnnotation$TypeParameterBoundTarget"), "binary name java.lang.classfile.TypeAnnotation$TypeParameterBoundTarget");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeAnnotation.TypeParameterBoundTarget is public");
        check(c.isInterface(), "java.lang.classfile.TypeAnnotation.TypeParameterBoundTarget is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.TypeAnnotation.TypeParameterBoundTarget has no superclass");
        check(declares(c, "typeParameterIndex"), "declares typeParameterIndex");
        check(declares(c, "boundIndex"), "declares boundIndex");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        TypeAnnotation.TypeParameterBoundTarget t = TypeAnnotation.TargetInfo.ofClassTypeParameterBound(1, 2);
        check(t.typeParameterIndex() == 1, "typeParameterIndex");
        check(t.boundIndex() == 2, "boundIndex");
        check(t.targetType() == TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER_BOUND, "targetType");

    }

    // ===== 深化：真 javac 产物载体（方法类型参数 bound：多 bound 逐位序） =====
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE_USE, java.lang.annotation.ElementType.LOCAL_VARIABLE})
    @interface TPBA { String value() default "x"; }

    static class Holder {
        <T, U extends @TPBA("b0") Number & @TPBA("b1") Runnable> void m(U u) { }
        <T, U extends @TPBA("sb") Number> void single(U u) { }
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

    // ---- Part 3: 工厂族（ofClass/ofMethod/ofTypeParameterBound 三入口 + u1 边界） ----
    static void testFactoryMatrix() {
        TypeAnnotation.TypeParameterBoundTarget a = TypeAnnotation.TargetInfo.ofClassTypeParameterBound(1, 2);
        check(a.targetType() == TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER_BOUND, "ofClassTypeParameterBound type");
        check(a.typeParameterIndex() == 1 && a.boundIndex() == 2 && a.size() == 2, "ofClassTypeParameterBound idx/size");
        TypeAnnotation.TypeParameterBoundTarget b = TypeAnnotation.TargetInfo.ofMethodTypeParameterBound(0, 1);
        check(b.targetType() == TypeAnnotation.TargetType.METHOD_TYPE_PARAMETER_BOUND, "ofMethodTypeParameterBound type");
        check(b.typeParameterIndex() == 0 && b.boundIndex() == 1, "ofMethodTypeParameterBound idx");
        TypeAnnotation.TypeParameterBoundTarget c = TypeAnnotation.TargetInfo.ofTypeParameterBound(TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER_BOUND, 3, 4);
        check(c.targetType() == TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER_BOUND && c.typeParameterIndex() == 3 && c.boundIndex() == 4,
                "ofTypeParameterBound(CTPB,3,4)");
        check(TypeAnnotation.TargetInfo.ofTypeParameterBound(TypeAnnotation.TargetType.METHOD_TYPE_PARAMETER_BOUND, 5, 6).boundIndex() == 6,
                "ofTypeParameterBound(MTPB,5,6)");
        // 形态不符 → IAE
        try { TypeAnnotation.TargetInfo.ofTypeParameterBound(TypeAnnotation.TargetType.FIELD, 0, 0); check(false, "ofTypeParameterBound(FIELD) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage() != null && e.getMessage().contains("Wrong target type"), "ofTypeParameterBound(FIELD) IAE msg"); }
        // u1 边界（typeParameterIndex / boundIndex 各自校验）
        try { TypeAnnotation.TargetInfo.ofClassTypeParameterBound(-1, 0); check(false, "(-1,0) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("type parameter index out of range of u1"), "(-1,0) type param IAE msg"); }
        try { TypeAnnotation.TargetInfo.ofClassTypeParameterBound(0, -1); check(false, "(0,-1) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("bound index out of range of u1"), "(0,-1) bound IAE msg"); }
        try { TypeAnnotation.TargetInfo.ofMethodTypeParameterBound(0, 300); check(false, "(0,300) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("bound index out of range of u1"), "(0,300) IAE msg"); }
        check(TypeAnnotation.TargetInfo.ofClassTypeParameterBound(255, 255).boundIndex() == 255, "(255,255) u1 maxima legal");
        check(TypeAnnotation.TargetInfo.ofClassTypeParameterBound(1, 2) instanceof TypeAnnotation.TargetInfo, "BoundTarget instanceof TargetInfo");
    }

    // ---- Part 4: 真 javac 产物（typeParameterIndex=声明序；boundIndex=bound 列表序含首个类 bound） ----
    static void testRealProduct() throws Exception {
        byte[] hb = TypeParameterBoundTargetTest.class.getResourceAsStream("TypeParameterBoundTargetTest$Holder.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(hb);
        MethodModel mm = cm.methods().stream().filter(m -> m.methodName().stringValue().equals("m")).findFirst().orElseThrow();
        java.util.List<TypeAnnotation> anns = mm.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations();
        check(anns.size() == 2, "m count 2");
        for (TypeAnnotation t : anns) {
            check(t.targetInfo() instanceof TypeAnnotation.TypeParameterBoundTarget, "ann is TypeParameterBoundTarget");
            check(t.targetInfo().targetType() == TypeAnnotation.TargetType.METHOD_TYPE_PARAMETER_BOUND, "METHOD_TYPE_PARAMETER_BOUND");
            check(t.targetPath().isEmpty(), "path empty");
            check(t.targetInfo().size() == 2, "size 2");
        }
        TypeAnnotation b0 = findOne(anns, "b0");
        check(((TypeAnnotation.TypeParameterBoundTarget) b0.targetInfo()).typeParameterIndex() == 1, "b0 tpIdx 1（第二个类型参数）");
        check(((TypeAnnotation.TypeParameterBoundTarget) b0.targetInfo()).boundIndex() == 0, "b0 boundIdx 0（首个 bound=类 bound）");
        TypeAnnotation b1 = findOne(anns, "b1");
        check(((TypeAnnotation.TypeParameterBoundTarget) b1.targetInfo()).typeParameterIndex() == 1, "b1 tpIdx 1");
        check(((TypeAnnotation.TypeParameterBoundTarget) b1.targetInfo()).boundIndex() == 1, "b1 boundIdx 1（第二个 bound=接口）");
        MethodModel sm = cm.methods().stream().filter(m -> m.methodName().stringValue().equals("single")).findFirst().orElseThrow();
        java.util.List<TypeAnnotation> sanns = sm.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations();
        check(((TypeAnnotation.TypeParameterBoundTarget) findOne(sanns, "sb").targetInfo()).typeParameterIndex() == 1, "single tpIdx 1");
        check(((TypeAnnotation.TypeParameterBoundTarget) findOne(sanns, "sb").targetInfo()).boundIndex() == 0, "single boundIdx 0");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryMatrix();
        testRealProduct();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

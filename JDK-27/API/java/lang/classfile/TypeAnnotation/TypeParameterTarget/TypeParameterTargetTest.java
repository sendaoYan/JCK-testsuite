/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.TypeParameterTarget conformance: white-box structure + black-box behavior.
 * @run main TypeParameterTargetTest
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

public class TypeParameterTargetTest {
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
        Class<?> c = Class.forName("java.lang.classfile.TypeAnnotation$TypeParameterTarget");
        check(c != null, "java.lang.classfile.TypeAnnotation.TypeParameterTarget loads");
        check(c.getName().equals("java.lang.classfile.TypeAnnotation$TypeParameterTarget"), "binary name java.lang.classfile.TypeAnnotation$TypeParameterTarget");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeAnnotation.TypeParameterTarget is public");
        check(c.isInterface(), "java.lang.classfile.TypeAnnotation.TypeParameterTarget is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.TypeAnnotation.TypeParameterTarget has no superclass");
        check(declares(c, "typeParameterIndex"), "declares typeParameterIndex");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        TypeAnnotation.TypeParameterTarget t = TypeAnnotation.TargetInfo.ofClassTypeParameter(3);
        check(t.typeParameterIndex() == 3, "typeParameterIndex");
        check(t.targetType() == TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER, "targetType");

    }

    // ===== 深化：真 javac 产物载体（类类型参数 + 方法类型参数两个位点） =====
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE_USE, java.lang.annotation.ElementType.LOCAL_VARIABLE})
    @interface TPA { String value() default "x"; }

    static class GHolder<@TPA("c0") T, @TPA("c1") U> { }

    static class Holder {
        <@TPA("m0") T, @TPA("m1") U> void m(T t, U u) { }
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
        TypeAnnotation.TypeParameterTarget a = TypeAnnotation.TargetInfo.ofClassTypeParameter(0);
        check(a.targetType() == TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER && a.typeParameterIndex() == 0 && a.size() == 1,
                "ofClassTypeParameter(0)");
        TypeAnnotation.TypeParameterTarget b = TypeAnnotation.TargetInfo.ofMethodTypeParameter(1);
        check(b.targetType() == TypeAnnotation.TargetType.METHOD_TYPE_PARAMETER && b.typeParameterIndex() == 1,
                "ofMethodTypeParameter(1)");
        TypeAnnotation.TypeParameterTarget c = TypeAnnotation.TargetInfo.ofTypeParameter(TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER, 2);
        check(c.targetType() == TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER && c.typeParameterIndex() == 2, "ofTypeParameter(CTP,2)");
        check(TypeAnnotation.TargetInfo.ofTypeParameter(TypeAnnotation.TargetType.METHOD_TYPE_PARAMETER, 3).typeParameterIndex() == 3,
                "ofTypeParameter(MTP,3)");
        check(TypeAnnotation.TargetInfo.ofClassTypeParameter(255).typeParameterIndex() == 255, "(255) u1 max legal");
        try { TypeAnnotation.TargetInfo.ofClassTypeParameter(-1); check(false, "(-1) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage().equals("type parameter index out of range of u1: -1"), "(-1) IAE msg"); }
        try { TypeAnnotation.TargetInfo.ofMethodTypeParameter(300); check(false, "(300) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage().equals("type parameter index out of range of u1: 300"), "(300) IAE msg"); }
        try { TypeAnnotation.TargetInfo.ofTypeParameter(TypeAnnotation.TargetType.FIELD, 0); check(false, "ofTypeParameter(FIELD) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage() != null && e.getMessage().contains("Wrong target type"), "ofTypeParameter(FIELD) IAE msg"); }
        check(TypeAnnotation.TargetInfo.ofClassTypeParameter(0) instanceof TypeAnnotation.TargetInfo, "TypeParameterTarget instanceof TargetInfo");
    }

    // ---- Part 4: 真 javac 产物（类级 CLASS_TYPE_PARAMETER / 方法级 METHOD_TYPE_PARAMETER 声明序） ----
    static void testRealProduct() throws Exception {
        // 类级：泛型类自身的类型参数注解位于 GHolder 的类文件
        byte[] gb = TypeParameterTargetTest.class.getResourceAsStream("TypeParameterTargetTest$GHolder.class").readAllBytes();
        ClassModel gcm = ClassFile.of().parse(gb);
        java.util.List<TypeAnnotation> ganns = gcm.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations();
        check(ganns.size() == 2, "class-level count 2");
        for (TypeAnnotation t : ganns) {
            check(t.targetInfo() instanceof TypeAnnotation.TypeParameterTarget, "class ann is TypeParameterTarget");
            check(t.targetInfo().targetType() == TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER, "CLASS_TYPE_PARAMETER");
            check(t.targetPath().isEmpty(), "path empty");
            check(t.targetInfo().size() == 1, "size 1");
        }
        check(((TypeAnnotation.TypeParameterTarget) findOne(ganns, "c0").targetInfo()).typeParameterIndex() == 0, "c0 idx 0");
        check(((TypeAnnotation.TypeParameterTarget) findOne(ganns, "c1").targetInfo()).typeParameterIndex() == 1, "c1 idx 1");
        // 方法级
        byte[] hb = TypeParameterTargetTest.class.getResourceAsStream("TypeParameterTargetTest$Holder.class").readAllBytes();
        ClassModel hcm = ClassFile.of().parse(hb);
        MethodModel mm = hcm.methods().stream().filter(m -> m.methodName().stringValue().equals("m")).findFirst().orElseThrow();
        java.util.List<TypeAnnotation> manns = mm.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations();
        check(manns.size() == 2, "method-level count 2");
        for (TypeAnnotation t : manns) {
            check(t.targetInfo().targetType() == TypeAnnotation.TargetType.METHOD_TYPE_PARAMETER, "METHOD_TYPE_PARAMETER");
        }
        check(((TypeAnnotation.TypeParameterTarget) findOne(manns, "m0").targetInfo()).typeParameterIndex() == 0, "m0 idx 0");
        check(((TypeAnnotation.TypeParameterTarget) findOne(manns, "m1").targetInfo()).typeParameterIndex() == 1, "m1 idx 1");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryMatrix();
        testRealProduct();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

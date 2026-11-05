/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.FormalParameterTarget conformance: white-box structure + black-box behavior.
 * @run main FormalParameterTargetTest
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

public class FormalParameterTargetTest {
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
        Class<?> c = Class.forName("java.lang.classfile.TypeAnnotation$FormalParameterTarget");
        check(c != null, "java.lang.classfile.TypeAnnotation.FormalParameterTarget loads");
        check(c.getName().equals("java.lang.classfile.TypeAnnotation$FormalParameterTarget"), "binary name java.lang.classfile.TypeAnnotation$FormalParameterTarget");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeAnnotation.FormalParameterTarget is public");
        check(c.isInterface(), "java.lang.classfile.TypeAnnotation.FormalParameterTarget is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.TypeAnnotation.FormalParameterTarget has no superclass");
        check(declares(c, "formalParameterIndex"), "declares formalParameterIndex");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        TypeAnnotation.FormalParameterTarget t = TypeAnnotation.TargetInfo.ofMethodFormalParameter(1);
        check(t.formalParameterIndex() == 1, "formalParameterIndex");
        check(t.targetType() == TypeAnnotation.TargetType.METHOD_FORMAL_PARAMETER, "targetType");

    }

    // ===== 深化：真 javac 产物载体（形参类型位置 vs 声明位置、声明序语义） =====
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE_USE, java.lang.annotation.ElementType.LOCAL_VARIABLE})
    @interface FPA { String value() default "x"; }

    static class Holder {
        void m(@FPA("p0") String a, int b, @FPA("p2") long c, java.util.List<@FPA("p3") String> d) { }
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

    // ---- Part 3: 工厂边界（u1：0/1/255 合法，-1/300 IAE 精确消息） ----
    static void testFactoryBounds() {
        for (int ok : new int[]{0, 1, 255}) {
            TypeAnnotation.FormalParameterTarget t = TypeAnnotation.TargetInfo.ofMethodFormalParameter(ok);
            check(t.formalParameterIndex() == ok, "ofMethodFormalParameter(" + ok + ") readback");
            check(t.targetType() == TypeAnnotation.TargetType.METHOD_FORMAL_PARAMETER, "targetType @" + ok);
            check(t.size() == 1, "size 1 @" + ok);
        }
        try { TypeAnnotation.TargetInfo.ofMethodFormalParameter(-1); check(false, "(-1) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage().equals("formal parameter index out of range of u1: -1"), "(-1) IAE msg"); }
        try { TypeAnnotation.TargetInfo.ofMethodFormalParameter(300); check(false, "(300) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage().equals("formal parameter index out of range of u1: 300"), "(300) IAE msg"); }
        check(TypeAnnotation.TargetInfo.ofMethodFormalParameter(0) instanceof TypeAnnotation.TargetInfo, "FormalParameterTarget instanceof TargetInfo");
    }

    // ---- Part 4: 真 javac 产物（formalParameterIndex 是声明参数序而非槽位序；类型实参 path） ----
    static void testRealProduct() throws Exception {
        byte[] hb = FormalParameterTargetTest.class.getResourceAsStream("FormalParameterTargetTest$Holder.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(hb);
        MethodModel mm = cm.methods().stream().filter(m -> m.methodName().stringValue().equals("m")).findFirst().orElseThrow();
        java.util.List<TypeAnnotation> anns = mm.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations();
        check(anns.size() == 3, "3 param type annotations, got " + anns.size());
        for (TypeAnnotation t : anns) {
            check(t.targetInfo() instanceof TypeAnnotation.FormalParameterTarget, "param ann is FormalParameterTarget");
            check(t.targetInfo().targetType() == TypeAnnotation.TargetType.METHOD_FORMAL_PARAMETER, "METHOD_FORMAL_PARAMETER");
            check(t.targetInfo().size() == 1, "size 1");
        }
        check(((TypeAnnotation.FormalParameterTarget) findOne(anns, "p0").targetInfo()).formalParameterIndex() == 0, "p0 idx 0");
        // p2 是第 3 个参数（long 占 2 槽）：index==2 证明按声明序计数而非局部槽位
        check(((TypeAnnotation.FormalParameterTarget) findOne(anns, "p2").targetInfo()).formalParameterIndex() == 2, "p2 idx 2 (declared order, not slot)");
        TypeAnnotation p3 = findOne(anns, "p3");
        check(((TypeAnnotation.FormalParameterTarget) p3.targetInfo()).formalParameterIndex() == 3, "p3 idx 3");
        check(p3.targetPath().size() == 1
                && p3.targetPath().get(0).typePathKind() == TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT
                && p3.targetPath().get(0).typeArgumentIndex() == 0, "p3 path TYPE_ARGUMENT 0");
        check(findOne(anns, "p0").targetPath().isEmpty(), "p0 path empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryBounds();
        testRealProduct();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

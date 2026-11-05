/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.CatchTarget conformance: white-box structure + black-box behavior.
 * @run main CatchTargetTest
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
import java.io.IOException;

public class CatchTargetTest {
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
        Class<?> c = Class.forName("java.lang.classfile.TypeAnnotation$CatchTarget");
        check(c != null, "java.lang.classfile.TypeAnnotation.CatchTarget loads");
        check(c.getName().equals("java.lang.classfile.TypeAnnotation$CatchTarget"), "binary name java.lang.classfile.TypeAnnotation$CatchTarget");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeAnnotation.CatchTarget is public");
        check(c.isInterface(), "java.lang.classfile.TypeAnnotation.CatchTarget is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.TypeAnnotation.CatchTarget has no superclass");
        check(declares(c, "exceptionTableIndex"), "declares exceptionTableIndex");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        TypeAnnotation.CatchTarget t = TypeAnnotation.TargetInfo.ofExceptionParameter(2);
        check(t.exceptionTableIndex() == 2, "exceptionTableIndex");
        check(t.targetType() == TypeAnnotation.TargetType.EXCEPTION_PARAMETER, "targetType");

    }

    // ===== 深化：真 javac 产物载体（multi-catch + catch 链 → exceptionTableIndex 语义） =====
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE_USE, java.lang.annotation.ElementType.LOCAL_VARIABLE})
    @interface CTA { String value() default "x"; }

    static class Holder {
        void ex() throws Exception { }
        void catches() throws Exception {
            try { ex(); } catch (@CTA("mc0") IOException | @CTA("mc1") IllegalStateException e) { }
            try { ex(); } catch (@CTA("c2") RuntimeException e) { } catch (@CTA("c3") Error e) { }
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

    // ---- Part 3: 工厂边界（u2：0/1/255/65535 合法，-1/65536 IAE 精确消息） ----
    static void testFactoryBounds() {
        for (int ok : new int[]{0, 1, 255, 65535}) {
            TypeAnnotation.CatchTarget t = TypeAnnotation.TargetInfo.ofExceptionParameter(ok);
            check(t.exceptionTableIndex() == ok, "ofExceptionParameter(" + ok + ") readback");
            check(t.targetType() == TypeAnnotation.TargetType.EXCEPTION_PARAMETER, "targetType EXCEPTION_PARAMETER @" + ok);
            check(t.size() == 2, "size 2 @" + ok);
        }
        try { TypeAnnotation.TargetInfo.ofExceptionParameter(-1); check(false, "ofExceptionParameter(-1) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage().equals("exception table index out of range of u2: -1"), "(-1) IAE msg"); }
        try { TypeAnnotation.TargetInfo.ofExceptionParameter(65536); check(false, "ofExceptionParameter(65536) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage().equals("exception table index out of range of u2: 65536"), "(65536) IAE msg"); }
        // 多态：CatchTarget 是 TargetInfo（经 Object 视图排除无关形态）
        check(TypeAnnotation.TargetInfo.ofExceptionParameter(0) instanceof TypeAnnotation.TargetInfo, "CatchTarget instanceof TargetInfo");
        check(!(((Object) TypeAnnotation.TargetInfo.ofExceptionParameter(0)) instanceof TypeAnnotation.OffsetTarget), "CatchTarget not OffsetTarget");
    }

    // ---- Part 4: 真 javac 产物（multi-catch 每个 alternative 各占一个异常表项；catch 链按表序递增） ----
    static void testRealProduct() throws Exception {
        byte[] hb = CatchTargetTest.class.getResourceAsStream("CatchTargetTest$Holder.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(hb);
        MethodModel mm = null;
        for (MethodModel m : cm.methods())
            if (m.methodName().stringValue().equals("catches")) mm = m;
        check(mm != null, "Holder.catches found");
        CodeAttribute ca = (CodeAttribute) mm.code().get();
        java.util.List<TypeAnnotation> anns = ca.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations();
        check(anns.size() == 4, "4 catch type annotations, got " + anns.size());
        for (TypeAnnotation t : anns) {
            check(t.targetInfo() instanceof TypeAnnotation.CatchTarget, "code-level ann is CatchTarget");
            check(t.targetInfo().targetType() == TypeAnnotation.TargetType.EXCEPTION_PARAMETER, "EXCEPTION_PARAMETER");
            check(t.targetPath().isEmpty(), "catch path empty");
        }
        check(((TypeAnnotation.CatchTarget) findOne(anns, "mc0").targetInfo()).exceptionTableIndex() == 0, "multi-catch first alternative idx 0");
        check(((TypeAnnotation.CatchTarget) findOne(anns, "mc1").targetInfo()).exceptionTableIndex() == 1, "multi-catch second alternative idx 1");
        check(((TypeAnnotation.CatchTarget) findOne(anns, "c2").targetInfo()).exceptionTableIndex() == 2, "chained catch idx 2");
        check(((TypeAnnotation.CatchTarget) findOne(anns, "c3").targetInfo()).exceptionTableIndex() == 3, "chained catch idx 3");
        // 异常表长度覆盖注解引用的最大下标（表至少 4 项）
        check(ca.exceptionHandlers().size() >= 4, "exception table has >= 4 entries");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryBounds();
        testRealProduct();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

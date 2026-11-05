/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.ThrowsTarget conformance: white-box structure + black-box behavior.
 * @run main ThrowsTargetTest
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

public class ThrowsTargetTest {
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
        Class<?> c = Class.forName("java.lang.classfile.TypeAnnotation$ThrowsTarget");
        check(c != null, "java.lang.classfile.TypeAnnotation.ThrowsTarget loads");
        check(c.getName().equals("java.lang.classfile.TypeAnnotation$ThrowsTarget"), "binary name java.lang.classfile.TypeAnnotation$ThrowsTarget");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeAnnotation.ThrowsTarget is public");
        check(c.isInterface(), "java.lang.classfile.TypeAnnotation.ThrowsTarget is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.TypeAnnotation.ThrowsTarget has no superclass");
        check(declares(c, "throwsTargetIndex"), "declares throwsTargetIndex");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        TypeAnnotation.ThrowsTarget t = TypeAnnotation.TargetInfo.ofThrows(0);
        check(t.throwsTargetIndex() == 0, "throwsTargetIndex");
        check(t.targetType() == TypeAnnotation.TargetType.THROWS, "targetType");

    }

    // ===== 深化：真 javac 产物载体（throws 子句类型位置注解 → throwsTargetIndex 序） =====
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE_USE, java.lang.annotation.ElementType.LOCAL_VARIABLE})
    @interface THA { String value() default "x"; }

    static class Holder {
        void th() throws @THA("t0") IllegalArgumentException, @THA("t1") IllegalStateException { }
        void plain() throws @THA("single") InterruptedException { }
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

    // ---- Part 3: 工厂边界（u2：0/1/65535 合法，-1/65536 IAE 精确消息） ----
    static void testFactoryBounds() {
        for (int ok : new int[]{0, 1, 65535}) {
            TypeAnnotation.ThrowsTarget t = TypeAnnotation.TargetInfo.ofThrows(ok);
            check(t.throwsTargetIndex() == ok, "ofThrows(" + ok + ") readback");
            check(t.targetType() == TypeAnnotation.TargetType.THROWS, "targetType THROWS @" + ok);
            check(t.size() == 2, "size 2 @" + ok);
        }
        try { TypeAnnotation.TargetInfo.ofThrows(-1); check(false, "ofThrows(-1) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage().equals("throws type index out of range of u2: -1"), "(-1) IAE msg"); }
        try { TypeAnnotation.TargetInfo.ofThrows(65536); check(false, "ofThrows(65536) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage().equals("throws type index out of range of u2: 65536"), "(65536) IAE msg"); }
        check(TypeAnnotation.TargetInfo.ofThrows(0) instanceof TypeAnnotation.TargetInfo, "ThrowsTarget instanceof TargetInfo");
    }

    // ---- Part 4: 真 javac 产物（throwsTargetIndex = throws 列表序；与 ExceptionsAttribute 异常表对照） ----
    static void testRealProduct() throws Exception {
        byte[] hb = ThrowsTargetTest.class.getResourceAsStream("ThrowsTargetTest$Holder.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(hb);
        MethodModel th = cm.methods().stream().filter(m -> m.methodName().stringValue().equals("th")).findFirst().orElseThrow();
        java.util.List<TypeAnnotation> anns = th.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations();
        check(anns.size() == 2, "th count 2");
        for (TypeAnnotation t : anns) {
            check(t.targetInfo() instanceof TypeAnnotation.ThrowsTarget, "ann is ThrowsTarget");
            check(t.targetInfo().targetType() == TypeAnnotation.TargetType.THROWS, "targetType THROWS");
            check(t.targetPath().isEmpty(), "path empty");
            check(t.targetInfo().size() == 2, "size 2");
        }
        check(((TypeAnnotation.ThrowsTarget) findOne(anns, "t0").targetInfo()).throwsTargetIndex() == 0, "t0 idx 0");
        check(((TypeAnnotation.ThrowsTarget) findOne(anns, "t1").targetInfo()).throwsTargetIndex() == 1, "t1 idx 1");
        // 与 ExceptionsAttribute 的异常顺序一一对应
        java.util.List<ClassEntry> exc = th.findAttribute(Attributes.exceptions()).orElseThrow().exceptions();
        check(exc.size() == 2, "ExceptionsAttribute count 2");
        check(exc.get(0).asInternalName().equals("java/lang/IllegalArgumentException"), "exception[0]");
        check(exc.get(1).asInternalName().equals("java/lang/IllegalStateException"), "exception[1]");
        // 单异常 throws → 单注解 idx 0
        MethodModel pl = cm.methods().stream().filter(m -> m.methodName().stringValue().equals("plain")).findFirst().orElseThrow();
        java.util.List<TypeAnnotation> panns = pl.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations();
        check(panns.size() == 1, "plain count 1");
        check(((TypeAnnotation.ThrowsTarget) findOne(panns, "single").targetInfo()).throwsTargetIndex() == 0, "single idx 0");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryBounds();
        testRealProduct();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

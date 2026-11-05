/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.SupertypeTarget conformance: white-box structure + black-box behavior.
 * @run main SupertypeTargetTest
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

public class SupertypeTargetTest {
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
        Class<?> c = Class.forName("java.lang.classfile.TypeAnnotation$SupertypeTarget");
        check(c != null, "java.lang.classfile.TypeAnnotation.SupertypeTarget loads");
        check(c.getName().equals("java.lang.classfile.TypeAnnotation$SupertypeTarget"), "binary name java.lang.classfile.TypeAnnotation$SupertypeTarget");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeAnnotation.SupertypeTarget is public");
        check(c.isInterface(), "java.lang.classfile.TypeAnnotation.SupertypeTarget is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.TypeAnnotation.SupertypeTarget has no superclass");
        check(declares(c, "supertypeIndex"), "declares supertypeIndex");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        TypeAnnotation.SupertypeTarget t = TypeAnnotation.TargetInfo.ofClassExtends(0);
        check(t.supertypeIndex() == 0, "supertypeIndex");
        check(t.targetType() == TypeAnnotation.TargetType.CLASS_EXTENDS, "targetType");

    }

    // ===== 深化：真 javac 产物载体（extends 父类=65535，implements 接口=0/1） =====
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE_USE, java.lang.annotation.ElementType.LOCAL_VARIABLE})
    @interface STA { String value() default "x"; }

    static class STBase { }
    static interface STIF1 { }

    static class Holder extends @STA("sup") STBase implements @STA("if0") Cloneable, @STA("if1") STIF1 { }

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

    // ---- Part 3: 工厂边界（u2：65535=父类哨兵合法，-1/65536 IAE 精确消息） ----
    static void testFactoryBounds() {
        for (int ok : new int[]{0, 1, 65535}) {
            TypeAnnotation.SupertypeTarget t = TypeAnnotation.TargetInfo.ofClassExtends(ok);
            check(t.supertypeIndex() == ok, "ofClassExtends(" + ok + ") readback");
            check(t.targetType() == TypeAnnotation.TargetType.CLASS_EXTENDS, "targetType CLASS_EXTENDS @" + ok);
            check(t.size() == 2, "size 2 @" + ok);
        }
        try { TypeAnnotation.TargetInfo.ofClassExtends(-1); check(false, "(-1) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage().equals("supertype index out of range of u2: -1"), "(-1) IAE msg"); }
        try { TypeAnnotation.TargetInfo.ofClassExtends(65536); check(false, "(65536) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage().equals("supertype index out of range of u2: 65536"), "(65536) IAE msg"); }
        check(TypeAnnotation.TargetInfo.ofClassExtends(0) instanceof TypeAnnotation.TargetInfo, "SupertypeTarget instanceof TargetInfo");
    }

    // ---- Part 4: 真 javac 产物（父类 65535 哨兵 vs 接口 implements 序） + 注入回读 ----
    static void testRealProduct() throws Exception {
        byte[] hb = SupertypeTargetTest.class.getResourceAsStream("SupertypeTargetTest$Holder.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(hb);
        java.util.List<TypeAnnotation> anns = cm.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations();
        check(anns.size() == 3, "3 supertype annotations, got " + anns.size());
        for (TypeAnnotation t : anns) {
            check(t.targetInfo() instanceof TypeAnnotation.SupertypeTarget, "ann is SupertypeTarget");
            check(t.targetInfo().targetType() == TypeAnnotation.TargetType.CLASS_EXTENDS, "CLASS_EXTENDS");
            check(t.targetPath().isEmpty(), "path empty");
            check(t.targetInfo().size() == 2, "size 2");
        }
        check(((TypeAnnotation.SupertypeTarget) findOne(anns, "sup").targetInfo()).supertypeIndex() == 65535,
                "superclass annotation encoded as 65535 (JVMS 4.7.20.1)");
        check(((TypeAnnotation.SupertypeTarget) findOne(anns, "if0").targetInfo()).supertypeIndex() == 0, "first interface index 0");
        check(((TypeAnnotation.SupertypeTarget) findOne(anns, "if1").targetInfo()).supertypeIndex() == 1, "second interface index 1");
        check(cm.superclass().orElseThrow().asInternalName().endsWith("STBase"), "superclass name");
        check(cm.interfaces().size() == 2, "interfaces count 2");
        // 注入闭环：类级 CLASS_EXTENDS 属性 build → re-parse 回读
        Annotation an = Annotation.of(ClassDesc.of("p.ST"));
        byte[] b = ClassFile.of().build(ClassDesc.of("p.STInj"), cb -> cb
            .withSuperclass(ClassDesc.of("java.lang.Object"))
            .with(RuntimeVisibleTypeAnnotationsAttribute.of(TypeAnnotation.of(
                    TypeAnnotation.TargetInfo.ofClassExtends(65535), java.util.List.of(), an))));
        TypeAnnotation back = ClassFile.of().parse(b).findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations().get(0);
        check(back.targetInfo().targetType() == TypeAnnotation.TargetType.CLASS_EXTENDS, "injected CLASS_EXTENDS");
        check(((TypeAnnotation.SupertypeTarget) back.targetInfo()).supertypeIndex() == 65535, "injected 65535 round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryBounds();
        testRealProduct();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

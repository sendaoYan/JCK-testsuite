/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.OffsetTarget conformance: white-box structure + black-box behavior.
 * @run main OffsetTargetTest
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

public class OffsetTargetTest {
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
        Class<?> c = Class.forName("java.lang.classfile.TypeAnnotation$OffsetTarget");
        check(c != null, "java.lang.classfile.TypeAnnotation.OffsetTarget loads");
        check(c.getName().equals("java.lang.classfile.TypeAnnotation$OffsetTarget"), "binary name java.lang.classfile.TypeAnnotation$OffsetTarget");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeAnnotation.OffsetTarget is public");
        check(c.isInterface(), "java.lang.classfile.TypeAnnotation.OffsetTarget is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.TypeAnnotation.OffsetTarget has no superclass");
        check(declares(c, "target"), "declares target");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ClassFile.of().build(ClassDesc.of("pkg.U"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> {
            Label lb = cob.newLabel();
            ot[0] = TypeAnnotation.TargetInfo.ofNewExpr(lb);
            cob.labelBinding(lb).return_();
        }));
        check(ot[0] != null, "ofNewExpr target created");
        check(ot[0].targetType() == TypeAnnotation.TargetType.NEW, "targetType NEW");
        check(ot[0].target() != null, "target label");

    }

    // ===== 深化：真 javac 产物载体（instanceof / new 两个位点，锚定到指令偏移） =====
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE_USE, java.lang.annotation.ElementType.LOCAL_VARIABLE})
    @interface OTA { String value() default "x"; }

    static class OTBase { }

    static class Holder {
        void code(Object o) {
            boolean b = o instanceof @OTA("inst") String;
            Object nw = new @OTA("new") OTBase();
            java.util.function.Predicate<String> pr = @OTA("mref") String::isEmpty;
            if (b && nw != null && pr != null) System.out.print("");
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

    // ---- Part 3: 四个命名工厂 + ofOffset 通用工厂（targetType 矩阵 + Label 同一性 + 负例） ----
    static void testFactoryMatrix() {
        ClassFile.of().build(ClassDesc.of("pkg.OTFab"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), 0, cob -> {
            Label lb = cob.newLabel();
            TypeAnnotation.OffsetTarget[] ts = {
                    TypeAnnotation.TargetInfo.ofInstanceofExpr(lb),
                    TypeAnnotation.TargetInfo.ofNewExpr(lb),
                    TypeAnnotation.TargetInfo.ofConstructorReference(lb),
                    TypeAnnotation.TargetInfo.ofMethodReference(lb),
                    TypeAnnotation.TargetInfo.ofOffset(TypeAnnotation.TargetType.INSTANCEOF, lb),
                    TypeAnnotation.TargetInfo.ofOffset(TypeAnnotation.TargetType.NEW, lb),
                    TypeAnnotation.TargetInfo.ofOffset(TypeAnnotation.TargetType.CONSTRUCTOR_REFERENCE, lb),
                    TypeAnnotation.TargetInfo.ofOffset(TypeAnnotation.TargetType.METHOD_REFERENCE, lb)};
            TypeAnnotation.TargetType[] et = {TypeAnnotation.TargetType.INSTANCEOF, TypeAnnotation.TargetType.NEW,
                    TypeAnnotation.TargetType.CONSTRUCTOR_REFERENCE, TypeAnnotation.TargetType.METHOD_REFERENCE,
                    TypeAnnotation.TargetType.INSTANCEOF, TypeAnnotation.TargetType.NEW,
                    TypeAnnotation.TargetType.CONSTRUCTOR_REFERENCE, TypeAnnotation.TargetType.METHOD_REFERENCE};
            for (int i = 0; i < ts.length; i++) {
                check(ts[i].targetType() == et[i], "shape#" + i + " targetType");
                check(ts[i].target() == lb, "shape#" + i + " label identity");
                check(ts[i].size() == 2, "shape#" + i + " size 2");
            }
            // 非 offset 目标类型 → IAE（类型校验先于 Label 判空，实测）
            try {
                TypeAnnotation.TargetInfo.ofOffset(TypeAnnotation.TargetType.FIELD, null);
                check(false, "ofOffset(FIELD,..) must IAE");
            } catch (IllegalArgumentException ex) { check(ex.getMessage() != null && ex.getMessage().contains("Wrong target type"), "ofOffset(FIELD) IAE msg"); }
            try {
                TypeAnnotation.TargetInfo.ofOffset(TypeAnnotation.TargetType.CAST, lb);
                check(false, "ofOffset(CAST,..) must IAE");
            } catch (IllegalArgumentException ex) { check(true, "ofOffset(CAST) IAE"); }
            // null Label → NPE
            try { TypeAnnotation.TargetInfo.ofNewExpr(null); check(false, "ofNewExpr(null) must NPE"); }
            catch (NullPointerException ex) { check(true, "ofNewExpr(null) NPE"); }
            try { TypeAnnotation.TargetInfo.ofInstanceofExpr(null); check(false, "ofInstanceofExpr(null) must NPE"); }
            catch (NullPointerException ex) { check(true, "ofInstanceofExpr(null) NPE"); }
            cob.labelBinding(lb).return_();
        }));
    }

    // ---- Part 4: 真 javac 产物（offset 锚点 == 对应指令的 bci） ----
    static void testRealProduct() throws Exception {
        byte[] hb = OffsetTargetTest.class.getResourceAsStream("OffsetTargetTest$Holder.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(hb);
        MethodModel mm = cm.methods().stream().filter(m -> m.methodName().stringValue().equals("code")).findFirst().orElseThrow();
        CodeAttribute ca = (CodeAttribute) mm.code().get();
        java.util.List<TypeAnnotation> anns = ca.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations();
        check(anns.size() == 3, "3 offset annotations, got " + anns.size());
        for (TypeAnnotation t : anns) {
            check(t.targetInfo() instanceof TypeAnnotation.OffsetTarget, "ann is OffsetTarget");
            check(t.targetPath().isEmpty(), "path empty");
            check(t.targetInfo().size() == 2, "size 2");
        }
        TypeAnnotation inst = findOne(anns, "inst");
        check(inst.targetInfo().targetType() == TypeAnnotation.TargetType.INSTANCEOF, "inst INSTANCEOF");
        check(ca.labelToBci(((TypeAnnotation.OffsetTarget) inst.targetInfo()).target()) == nthBci(ca, Opcode.INSTANCEOF, 0),
                "inst label anchored at INSTANCEOF bci");
        TypeAnnotation nw = findOne(anns, "new");
        check(nw.targetInfo().targetType() == TypeAnnotation.TargetType.NEW, "new NEW");
        check(ca.labelToBci(((TypeAnnotation.OffsetTarget) nw.targetInfo()).target()) == nthBci(ca, Opcode.NEW, 0),
                "new label anchored at NEW bci");
        TypeAnnotation mref = findOne(anns, "mref");
        check(mref.targetInfo().targetType() == TypeAnnotation.TargetType.METHOD_REFERENCE, "mref METHOD_REFERENCE");
        check(ca.labelToBci(((TypeAnnotation.OffsetTarget) mref.targetInfo()).target()) == nthBci(ca, Opcode.INVOKEDYNAMIC, 0),
                "mref label anchored at INVOKEDYNAMIC bci");
        // 注入闭环：OffsetTarget 随代码属性写出并保持锚点
        Annotation an = Annotation.of(ClassDesc.of("p.OT"));
        Label[] ls = new Label[1];
        byte[] b = ClassFile.of().build(ClassDesc.of("p.OTInj"), cb -> cb
            .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), 0, cob -> {
                ls[0] = cob.newLabel();
                cob.with(RuntimeVisibleTypeAnnotationsAttribute.of(TypeAnnotation.of(
                        TypeAnnotation.TargetInfo.ofNewExpr(ls[0]), java.util.List.of(), an)));
                cob.labelBinding(ls[0]).return_();
            }));
        CodeAttribute back = (CodeAttribute) ClassFile.of().parse(b).methods().get(0).code().get();
        TypeAnnotation injected = back.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations().get(0);
        check(injected.targetInfo().targetType() == TypeAnnotation.TargetType.NEW, "injected NEW targetType");
        check(back.labelToBci(((TypeAnnotation.OffsetTarget) injected.targetInfo()).target()) == nthBci(back, Opcode.RETURN, 0),
                "injected label binds at its bound position");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryMatrix();
        testRealProduct();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

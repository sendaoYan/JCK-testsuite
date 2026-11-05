/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.LocalVarTarget conformance: white-box structure + black-box behavior.
 * @run main LocalVarTargetTest
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

public class LocalVarTargetTest {
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
        Class<?> c = Class.forName("java.lang.classfile.TypeAnnotation$LocalVarTarget");
        check(c != null, "java.lang.classfile.TypeAnnotation.LocalVarTarget loads");
        check(c.getName().equals("java.lang.classfile.TypeAnnotation$LocalVarTarget"), "binary name java.lang.classfile.TypeAnnotation$LocalVarTarget");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeAnnotation.LocalVarTarget is public");
        check(c.isInterface(), "java.lang.classfile.TypeAnnotation.LocalVarTarget is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.TypeAnnotation.LocalVarTarget has no superclass");
        check(declares(c, "table"), "declares table");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> {
            Label s = cob.newLabel();
            Label e = cob.newLabel();
            java.util.List<TypeAnnotation.LocalVarTargetInfo> tbl = java.util.List.of(TypeAnnotation.LocalVarTargetInfo.of(s, e, 0));
            lvt[0] = TypeAnnotation.TargetInfo.ofLocalVariable(tbl);
            cob.labelBinding(s).labelBinding(e).return_();
        }));
        TypeAnnotation.LocalVarTarget t = lvt[0];
        check(t != null, "ofLocalVariable created");
        check(t.table().size() == 1, "table size 1");
        check(t.table().get(0).index() == 0, "index 0");
        check(t.targetType() == TypeAnnotation.TargetType.LOCAL_VARIABLE, "targetType");

    }

    // ===== 深化：真 javac 产物载体（局部变量声明 + try-with-resources 资源变量） =====
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE_USE, java.lang.annotation.ElementType.LOCAL_VARIABLE})
    @interface LVA { String value() default "x"; }

    static class Holder {
        void code() throws Exception {
            @LVA("lv") String s = "x";
            java.util.List<@LVA("lvta") String> gl = java.util.List.of();
            try (@LVA("res") AutoCloseable r = null) { }
            System.out.print(s + gl);
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

    // ---- Part 3: 工厂族（ofVariable/ofLocalVariable/ofResourceVariable + 变长 size 公式 2+6n） ----
    static void testFactoryMatrix() {
        ClassFile.of().build(ClassDesc.of("pkg.LVT"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), 0, cob -> {
            Label s = cob.newLabel();
            Label e = cob.newLabel();
            TypeAnnotation.LocalVarTargetInfo i1 = TypeAnnotation.LocalVarTargetInfo.of(s, e, 1);
            TypeAnnotation.LocalVarTargetInfo i2 = TypeAnnotation.LocalVarTargetInfo.of(s, e, 2);
            TypeAnnotation.LocalVarTargetInfo i3 = TypeAnnotation.LocalVarTargetInfo.of(s, e, 3);
            TypeAnnotation.LocalVarTarget empty = TypeAnnotation.TargetInfo.ofLocalVariable(java.util.List.of());
            check(empty.targetType() == TypeAnnotation.TargetType.LOCAL_VARIABLE && empty.table().isEmpty() && empty.size() == 2,
                    "ofLocalVariable(empty) size 2（空表合法，仅 u2 计数字节）");
            TypeAnnotation.LocalVarTarget one = TypeAnnotation.TargetInfo.ofLocalVariable(java.util.List.of(i1));
            check(one.table().size() == 1 && one.table().get(0).index() == 1 && one.size() == 8, "table1 size 8");
            TypeAnnotation.LocalVarTarget three = TypeAnnotation.TargetInfo.ofLocalVariable(java.util.List.of(i1, i2, i3));
            check(three.table().size() == 3 && three.size() == 2 + 6 * 3, "table3 size 2+6*3=20");
            TypeAnnotation.LocalVarTarget rv = TypeAnnotation.TargetInfo.ofResourceVariable(java.util.List.of(i1));
            check(rv.targetType() == TypeAnnotation.TargetType.RESOURCE_VARIABLE && rv.size() == 8, "ofResourceVariable");
            check(TypeAnnotation.TargetInfo.ofVariable(TypeAnnotation.TargetType.LOCAL_VARIABLE, java.util.List.of(i1)).table().size() == 1,
                    "ofVariable(LOCAL_VARIABLE,..) table");
            check(TypeAnnotation.TargetInfo.ofVariable(TypeAnnotation.TargetType.RESOURCE_VARIABLE, java.util.List.of(i1)).targetType()
                    == TypeAnnotation.TargetType.RESOURCE_VARIABLE, "ofVariable(RESOURCE_VARIABLE,..)");
            // targetType 与表格形态不符 → IAE（实测 Wrong target type specified）
            try {
                TypeAnnotation.TargetInfo.ofVariable(TypeAnnotation.TargetType.FIELD, java.util.List.of(i1));
                check(false, "ofVariable(FIELD) must IAE");
            } catch (IllegalArgumentException ex) { check(ex.getMessage() != null && ex.getMessage().contains("Wrong target type"), "ofVariable(FIELD) IAE msg"); }
            cob.labelBinding(s).labelBinding(e).return_();
        }));
    }

    // ---- Part 4: 真 javac 产物（LOCAL_VARIABLE / RESOURCE_VARIABLE 表格 + 不可变视图） ----
    static void testRealProduct() throws Exception {
        byte[] hb = LocalVarTargetTest.class.getResourceAsStream("LocalVarTargetTest$Holder.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(hb);
        MethodModel mm = cm.methods().stream().filter(m -> m.methodName().stringValue().equals("code")).findFirst().orElseThrow();
        CodeAttribute ca = (CodeAttribute) mm.code().get();
        java.util.List<TypeAnnotation> anns = ca.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations();
        check(anns.size() == 3, "3 local annotations, got " + anns.size());
        TypeAnnotation lv = findOne(anns, "lv");
        check(lv.targetInfo() instanceof TypeAnnotation.LocalVarTarget, "lv LocalVarTarget");
        check(lv.targetInfo().targetType() == TypeAnnotation.TargetType.LOCAL_VARIABLE, "lv LOCAL_VARIABLE");
        check(lv.targetPath().isEmpty(), "lv path empty");
        java.util.List<TypeAnnotation.LocalVarTargetInfo> tbl = ((TypeAnnotation.LocalVarTarget) lv.targetInfo()).table();
        check(tbl.size() == 1, "lv table size 1");
        int lvSlot = tbl.get(0).index();
        check(lv.targetInfo().size() == 2 + 6 * tbl.size(), "lv size 2+6n matches encoding");
        try { tbl.add(null); check(false, "table() must be immutable"); }
        catch (UnsupportedOperationException e) { check(true, "table() UOE"); }
        try { tbl.remove(0); check(false, "table() remove must fail"); }
        catch (UnsupportedOperationException e) { check(true, "table() remove UOE"); }
        TypeAnnotation lvta = findOne(anns, "lvta");
        check(lvta.targetInfo().targetType() == TypeAnnotation.TargetType.LOCAL_VARIABLE, "lvta LOCAL_VARIABLE");
        check(lvta.targetPath().size() == 1
                && lvta.targetPath().get(0).typePathKind() == TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT, "lvta path TYPE_ARGUMENT");
        check(((TypeAnnotation.LocalVarTarget) lvta.targetInfo()).table().get(0).index() != lvSlot, "lv/lvta different slots");
        TypeAnnotation res = findOne(anns, "res");
        check(res.targetInfo() instanceof TypeAnnotation.LocalVarTarget, "res LocalVarTarget");
        check(res.targetInfo().targetType() == TypeAnnotation.TargetType.RESOURCE_VARIABLE, "res RESOURCE_VARIABLE");
        java.util.List<TypeAnnotation.LocalVarTargetInfo> rtbl = ((TypeAnnotation.LocalVarTarget) res.targetInfo()).table();
        check(rtbl.size() == 1, "res table size 1");
        check(rtbl.get(0).index() != lvSlot, "res separate slot");
        check(ca.labelToBci(rtbl.get(0).startLabel()) >= 0, "res startLabel bound");
        check(ca.labelToBci(rtbl.get(0).endLabel()) > ca.labelToBci(rtbl.get(0).startLabel()), "res start<end");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryMatrix();
        testRealProduct();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

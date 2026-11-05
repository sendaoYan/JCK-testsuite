/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.LocalVarTargetInfo conformance: white-box structure + black-box behavior.
 * @run main LocalVarTargetInfoTest
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

public class LocalVarTargetInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.TypeAnnotation$LocalVarTargetInfo");
        check(c != null, "java.lang.classfile.TypeAnnotation.LocalVarTargetInfo loads");
        check(c.getName().equals("java.lang.classfile.TypeAnnotation$LocalVarTargetInfo"), "binary name java.lang.classfile.TypeAnnotation$LocalVarTargetInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeAnnotation.LocalVarTargetInfo is public");
        check(c.isInterface(), "java.lang.classfile.TypeAnnotation.LocalVarTargetInfo is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.TypeAnnotation.LocalVarTargetInfo has no superclass");
        check(declares(c, "startLabel"), "declares startLabel");
        check(declares(c, "endLabel"), "declares endLabel");
        check(declares(c, "index"), "declares index");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> {
            Label s = cob.newLabel();
            Label e = cob.newLabel();
            lvi[0] = TypeAnnotation.LocalVarTargetInfo.of(s, e, 2);
            cob.labelBinding(s).labelBinding(e).return_();
        }));
        TypeAnnotation.LocalVarTargetInfo i = lvi[0];
        check(i != null, "of created");
        check(i.index() == 2, "index 2");
        check(i.startLabel() != null && i.endLabel() != null, "labels non-null");

    }

    // ===== 深化：真 javac 产物载体 =====
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE_USE, java.lang.annotation.ElementType.LOCAL_VARIABLE})
    @interface LVIA { String value() default "x"; }

    static class Holder {
        void code() {
            @LVIA("lv") String s = "x";
            System.out.print(s);
        }
    }

    // ---- Part 3: 工厂 null/负索引矩阵（实测：Label 判 NPE、slot<0 IAE "Invalid slot index"） ----
    static void testFactoryBounds() {
        ClassFile.of().build(ClassDesc.of("pkg.LVTI"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), 0, cob -> {
            Label s = cob.newLabel();
            Label e = cob.newLabel();
            TypeAnnotation.LocalVarTargetInfo ok = TypeAnnotation.LocalVarTargetInfo.of(s, e, 0);
            check(ok.index() == 0, "of(..,0) index 0");
            check(ok.startLabel() == s && ok.endLabel() == e, "labels identity preserved");
            TypeAnnotation.LocalVarTargetInfo big = TypeAnnotation.LocalVarTargetInfo.of(s, e, 65535);
            check(big.index() == 65535, "of(..,65535) index 65535");
            try { TypeAnnotation.LocalVarTargetInfo.of(null, e, 0); check(false, "of(null,..) must NPE"); }
            catch (NullPointerException ex) { check(true, "of(null,e,0) NPE"); }
            try { TypeAnnotation.LocalVarTargetInfo.of(s, null, 0); check(false, "of(..,null..) must NPE"); }
            catch (NullPointerException ex) { check(true, "of(s,null,0) NPE"); }
            try { TypeAnnotation.LocalVarTargetInfo.of(null, null, 0); check(false, "of(null,null,0) must NPE"); }
            catch (NullPointerException ex) { check(true, "of(null,null,0) NPE"); }
            try { TypeAnnotation.LocalVarTargetInfo.of(s, e, -1); check(false, "of(..,-1) must IAE"); }
            catch (IllegalArgumentException ex) { check(ex.getMessage() != null && ex.getMessage().contains("Invalid slot index"), "of(..,-1) IAE Invalid slot index"); }
            cob.labelBinding(s).labelBinding(e).return_();
        }));
    }

    // ---- Part 4: 真 javac 产物回读（slot/起止 Label 与 code 偏移的一致性） ----
    static void testRealProduct() throws Exception {
        byte[] hb = LocalVarTargetInfoTest.class.getResourceAsStream("LocalVarTargetInfoTest$Holder.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(hb);
        MethodModel mm = cm.methods().stream().filter(m -> m.methodName().stringValue().equals("code")).findFirst().orElseThrow();
        CodeAttribute ca = (CodeAttribute) mm.code().get();
        java.util.List<TypeAnnotation> anns = ca.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations();
        check(anns.size() == 1, "one local annotation");
        TypeAnnotation.LocalVarTarget lvt = (TypeAnnotation.LocalVarTarget) anns.get(0).targetInfo();
        check(lvt.table().size() == 1, "single entry");
        TypeAnnotation.LocalVarTargetInfo ent = lvt.table().get(0);
        check(ent.index() >= 0, "slot non-negative, got " + ent.index());
        check(ent.startLabel() != null && ent.endLabel() != null, "labels non-null from product");
        check(ent.startLabel() != ent.endLabel(), "start/end are distinct labels");
        int sb = ca.labelToBci(ent.startLabel());
        int eb = ca.labelToBci(ent.endLabel());
        check(sb >= 0 && eb > sb, "startBci >= 0 and endBci > startBci: " + sb + ".." + eb);
        check(eb <= ca.codeLength(), "endBci within code length");
        // 同一变量多次范围（分支写法）→ 表格多行；这里单行已足够，补注入闭环：
        Annotation an = Annotation.of(ClassDesc.of("p.LV"));
        Label[] ls = new Label[2];
        byte[] b = ClassFile.of().build(ClassDesc.of("p.LVTIInj"), cb -> cb
            .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), 0, cob -> {
                ls[0] = cob.newLabel();
                ls[1] = cob.newLabel();
                TypeAnnotation.LocalVarTargetInfo e1 = TypeAnnotation.LocalVarTargetInfo.of(ls[0], ls[1], 4);
                TypeAnnotation.LocalVarTargetInfo e2 = TypeAnnotation.LocalVarTargetInfo.of(ls[0], ls[1], 5);
                cob.with(RuntimeVisibleTypeAnnotationsAttribute.of(TypeAnnotation.of(
                        TypeAnnotation.TargetInfo.ofLocalVariable(java.util.List.of(e1, e2)), java.util.List.of(), an)));
                cob.labelBinding(ls[0]).labelBinding(ls[1]).return_();
            }));
        CodeAttribute back = (CodeAttribute) ClassFile.of().parse(b).methods().get(0).code().get();
        java.util.List<TypeAnnotation.LocalVarTargetInfo> t2 =
                ((TypeAnnotation.LocalVarTarget) back.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow()
                        .annotations().get(0).targetInfo()).table();
        check(t2.size() == 2, "injected table 2 entries");
        check(t2.get(0).index() == 4 && t2.get(1).index() == 5, "injected slots 4/5 round-trip");
        check(back.labelToBci(t2.get(0).startLabel()) == back.labelToBci(t2.get(1).startLabel()), "both rows share bound labels");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryBounds();
        testRealProduct();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

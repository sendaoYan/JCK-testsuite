/*
 * @test
 * @summary java.lang.classfile.attribute.StackMapFrameInfo.ObjectVerificationTypeInfo conformance: white-box structure + black-box behavior.
 * @run main ObjectVerificationTypeInfoTest
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

public class ObjectVerificationTypeInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.StackMapFrameInfo$ObjectVerificationTypeInfo");
        check(c != null, "java.lang.classfile.attribute.StackMapFrameInfo.ObjectVerificationTypeInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.StackMapFrameInfo$ObjectVerificationTypeInfo"), "binary name java.lang.classfile.attribute.StackMapFrameInfo$ObjectVerificationTypeInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.StackMapFrameInfo.ObjectVerificationTypeInfo is public");
        check(c.isInterface(), "java.lang.classfile.attribute.StackMapFrameInfo.ObjectVerificationTypeInfo is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.StackMapFrameInfo.ObjectVerificationTypeInfo has no superclass");
        check(declares(c, "of"), "declares of");
        check(declares(c, "className"), "declares className");
        check(declares(c, "classSymbol"), "declares classSymbol");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        StackMapFrameInfo.ObjectVerificationTypeInfo v = StackMapFrameInfo.ObjectVerificationTypeInfo.of(ClassDesc.of("java.lang.String"));
        check(v.tag() == 7, "tag 7");
        check(v.className().asInternalName().equals("java/lang/String"), "className");
        check(v.classSymbol().equals(ClassDesc.of("java.lang.String")), "classSymbol");

    }


    // ---- real-product member（引用局部变量跨分支 → frame 出现 Object vti）----
    static class OVReal { static String pick(boolean c, String a, String b) { String r = a; if (c) r = b; return r; } }

    // ---- Part 3: 工厂 of(ClassDesc)/of(ClassEntry) ----
    static void testFactoryOverloads() {
        var v = StackMapFrameInfo.ObjectVerificationTypeInfo.of(ClassDesc.of("java.lang.String"));
        check(v.className().asInternalName().equals("java/lang/String"), "of(ClassDesc) className");
        check(v.className().asSymbol().descriptorString().equals("Ljava/lang/String;"), "className asSymbol");
        check(v.classSymbol().descriptorString().equals("Ljava/lang/String;"), "classSymbol default method");
        check(v.tag() == StackMapFrameInfo.VerificationTypeInfo.ITEM_OBJECT, "tag ITEM_OBJECT 7");
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        ClassEntry ce = cpb.classEntry(ClassDesc.of("p.C"));
        check(StackMapFrameInfo.ObjectVerificationTypeInfo.of(ce).className() == ce, "of(ClassEntry) identity");
        check(v instanceof StackMapFrameInfo.VerificationTypeInfo, "is VerificationTypeInfo");
        try { StackMapFrameInfo.ObjectVerificationTypeInfo.of((ClassDesc) null); check(false, "of(null) must throw"); }
        catch (NullPointerException e) { check(true, "of(null ClassDesc) NPE"); }
        try { StackMapFrameInfo.ObjectVerificationTypeInfo.of((ClassEntry) null); check(false, "of(null entry) must throw"); }
        catch (NullPointerException e) { check(true, "of(null ClassEntry) NPE"); }
    }

    // ---- Part 4: javac 真产物 frame 中的 Object vti ----
    static void testRealFrames() throws Exception {
        byte[] rb = ObjectVerificationTypeInfoTest.class.getResourceAsStream("ObjectVerificationTypeInfoTest$OVReal.class").readAllBytes();
        ClassModel m = ClassFile.of().parse(rb);
        boolean found = false;
        for (MethodModel mm : m.methods()) {
            if (!mm.methodName().stringValue().equals("pick") || mm.code().isEmpty()) continue;
            CodeAttribute ca = (CodeAttribute) mm.code().get();
            for (StackMapFrameInfo f : ca.findAttribute(Attributes.stackMapTable()).get().entries())
                for (StackMapFrameInfo.VerificationTypeInfo v : f.locals())
                    if (v instanceof StackMapFrameInfo.ObjectVerificationTypeInfo o
                            && o.className().asInternalName().equals("java/lang/String")) found = true;
        }
        check(found, "String local appears as ObjectVerificationTypeInfo java/lang/String");
    }

    // ---- Part 5: 经 StackMapTableAttribute.of 组装回读 ----
    static void testAssemblyReadback() throws Exception {
        byte[] bb = ClassFile.of().build(ClassDesc.of("DynOVT"), cb -> cb.withMethodBody("m",
                MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC,
                cob -> { Label l = cob.newLabel(); cob.iconst_0().iload(0).if_icmpge(l); cob.iconst_1().ireturn(); cob.labelBinding(l).iconst_m1().ireturn(); }));
        CodeAttribute ca = (CodeAttribute) ClassFile.of().parse(bb).methods().get(0).code().get();
        Label lbl = ca.findAttribute(Attributes.stackMapTable()).get().entries().get(0).target();
        var v = StackMapFrameInfo.ObjectVerificationTypeInfo.of(ClassDesc.of("java.lang.Integer"));
        var smt = StackMapTableAttribute.of(List.of(StackMapFrameInfo.of(lbl, List.of(v), List.of(v))));
        var back = smt.entries().get(0);
        check(back.locals().get(0).tag() == StackMapFrameInfo.VerificationTypeInfo.ITEM_OBJECT, "assembled locals tag");
        check(((StackMapFrameInfo.ObjectVerificationTypeInfo) back.stack().get(0)).className().asInternalName().equals("java/lang/Integer"), "assembled stack className");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryOverloads();
        testRealFrames();
        testAssemblyReadback();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

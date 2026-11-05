/*
 * @test
 * @summary java.lang.classfile.attribute.StackMapFrameInfo conformance: white-box structure + black-box behavior.
 * @run main StackMapFrameInfoTest
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

public class StackMapFrameInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.StackMapFrameInfo");
        check(c != null, "java.lang.classfile.attribute.StackMapFrameInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.StackMapFrameInfo"), "binary name java.lang.classfile.attribute.StackMapFrameInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.StackMapFrameInfo is public");
        check(c.isInterface(), "java.lang.classfile.attribute.StackMapFrameInfo is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.StackMapFrameInfo has no superclass");
        check(declares(c, "frameType"), "declares frameType");
        check(declares(c, "target"), "declares target");
        check(declares(c, "locals"), "declares locals");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_int), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> {
            Label l = cob.newLabel();
            cob.iconst_0().ifeq(l).iconst_1().ireturn().labelBinding(l).iconst_2().ireturn();
        }));
        StackMapFrameInfo f = ClassFile.of().parse(b).methods().get(0).code().get().findAttribute(Attributes.stackMapTable()).get().entries().get(0);
        check(f.frameType() == 6, "frameType");
        check(f.locals().isEmpty(), "locals empty");
        check(f.stack().isEmpty(), "stack empty");

    }


    // ---- real-product member ----
    static class SFReal { static String pick(boolean c, String a, String b) { String r = a; if (c) r = b; return r; } }

    static byte[] buildBranchy() {
        return ClassFile.of().build(ClassDesc.of("DynSFI"), cb -> cb.withMethodBody("m",
                MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC,
                cob -> { Label l = cob.newLabel(); cob.iconst_0().iload(0).if_icmpge(l); cob.iconst_1().ireturn(); cob.labelBinding(l).iconst_m1().ireturn(); }));
    }

    // ---- Part 3: build 产物 frame 四访问器 ----
    static void testBuildGeneratedFrames() throws Exception {
        CodeAttribute ca = (CodeAttribute) ClassFile.of().parse(buildBranchy()).methods().get(0).code().get();
        StackMapFrameInfo f = ca.findAttribute(Attributes.stackMapTable()).get().entries().get(0);
        check(f.target() != null, "target label non-null");
        check(ca.labelToBci(f.target()) >= 0, "target resolvable to bci");
        check(f.frameType() >= 0 && f.frameType() <= 255, "frameType u1");
        check(f.locals().size() == 1, "locals expanded full list");
        check(f.locals().get(0) instanceof StackMapFrameInfo.SimpleVerificationTypeInfo, "int local is simple vti");
        check(f.stack().isEmpty(), "empty stack");
    }

    // ---- Part 4: javac 真产物（局部变量展开 + 引用类型 vti）----
    static void testRealJavacFrames() throws Exception {
        byte[] rb = StackMapFrameInfoTest.class.getResourceAsStream("StackMapFrameInfoTest$SFReal.class").readAllBytes();
        ClassModel m = ClassFile.of().parse(rb);
        boolean sawObj = false, sawSimple = false;
        for (MethodModel mm : m.methods()) {
            if (!mm.methodName().stringValue().equals("pick") || mm.code().isEmpty()) continue;
            CodeAttribute ca = (CodeAttribute) mm.code().get();
            for (StackMapFrameInfo f : ca.findAttribute(Attributes.stackMapTable()).get().entries()) {
                check(f.frameType() >= 0 && f.frameType() <= 255, "javac frameType u1");
                for (StackMapFrameInfo.VerificationTypeInfo v : f.locals()) {
                    if (v instanceof StackMapFrameInfo.ObjectVerificationTypeInfo) sawObj = true;
                    if (v instanceof StackMapFrameInfo.SimpleVerificationTypeInfo) sawSimple = true;
                }
            }
        }
        check(sawObj && sawSimple, "locals mix of Object/Simple vti");
    }

    // ---- Part 5: of(Label, locals, stack) 工厂 + null 负例 ----
    static void testOfFactory() throws Exception {
        CodeAttribute ca = (CodeAttribute) ClassFile.of().parse(buildBranchy()).methods().get(0).code().get();
        Label lbl = ca.findAttribute(Attributes.stackMapTable()).get().entries().get(0).target();
        var obj = StackMapFrameInfo.ObjectVerificationTypeInfo.of(ClassDesc.of("java.lang.String"));
        var simple = StackMapFrameInfo.SimpleVerificationTypeInfo.INTEGER;
        StackMapFrameInfo f = StackMapFrameInfo.of(lbl, List.of(obj, simple), List.of(simple));
        check(f.target() == lbl, "of target identity");
        check(f.locals().size() == 2 && f.locals().get(0) == obj, "of locals identity");
        check(f.stack().size() == 1 && f.stack().get(0) == simple, "of stack identity");
        check(f.frameType() == 255, "constructed frame full-frame type 255");
        try { StackMapFrameInfo.of(null, null, null); check(false, "of(nulls) must throw"); }
        catch (NullPointerException e) { check(true, "of(nulls) NPE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testBuildGeneratedFrames();
        testRealJavacFrames();
        testOfFactory();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

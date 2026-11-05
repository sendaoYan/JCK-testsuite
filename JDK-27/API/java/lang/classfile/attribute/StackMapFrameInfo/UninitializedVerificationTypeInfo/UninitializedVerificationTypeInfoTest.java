/*
 * @test
 * @summary java.lang.classfile.attribute.StackMapFrameInfo.UninitializedVerificationTypeInfo conformance: white-box structure + black-box behavior.
 * @run main UninitializedVerificationTypeInfoTest
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

public class UninitializedVerificationTypeInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.StackMapFrameInfo$UninitializedVerificationTypeInfo");
        check(c != null, "java.lang.classfile.attribute.StackMapFrameInfo.UninitializedVerificationTypeInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.StackMapFrameInfo$UninitializedVerificationTypeInfo"), "binary name java.lang.classfile.attribute.StackMapFrameInfo$UninitializedVerificationTypeInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.StackMapFrameInfo.UninitializedVerificationTypeInfo is public");
        check(c.isInterface(), "java.lang.classfile.attribute.StackMapFrameInfo.UninitializedVerificationTypeInfo is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.StackMapFrameInfo.UninitializedVerificationTypeInfo has no superclass");
        check(declares(c, "newTarget"), "declares newTarget");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> {
            Label l = cob.newLabel();
            uv[0] = StackMapFrameInfo.UninitializedVerificationTypeInfo.of(l);
            cob.labelBinding(l).return_();
        }));
        StackMapFrameInfo.UninitializedVerificationTypeInfo v = uv[0];
        check(v != null, "of created");
        check(v.tag() == 8, "tag 8");
        check(v.newTarget() != null, "newTarget");

    }


    // ---- real-product member（NEW 与 <init> 之间的分支 → uninit vti，newTarget 指向 NEW）----
    static class UVReal {
        UVReal(int i) {}
        static Object g(boolean c) { return new UVReal(c ? 1 : 2); }
    }

    // ---- Part 3: of(Label) + tag ----
    static void testOfFactory() throws Exception {
        byte[] rb = UninitializedVerificationTypeInfoTest.class.getResourceAsStream("UninitializedVerificationTypeInfoTest$UVReal.class").readAllBytes();
        ClassModel m = ClassFile.of().parse(rb);
        CodeAttribute ca = null;
        for (MethodModel mm : m.methods())
            if (mm.methodName().stringValue().equals("g")) ca = (CodeAttribute) mm.code().get();
        StackMapFrameInfo.UninitializedVerificationTypeInfo parsed = null;
        outer:
        for (StackMapFrameInfo f : ca.findAttribute(Attributes.stackMapTable()).get().entries())
            for (StackMapFrameInfo.VerificationTypeInfo v : f.stack())
                if (v instanceof StackMapFrameInfo.UninitializedVerificationTypeInfo u) { parsed = u; break outer; }
        check(parsed != null, "javac uninit vti found in frames");
        check(parsed.tag() == StackMapFrameInfo.VerificationTypeInfo.ITEM_UNINITIALIZED, "tag ITEM_UNINITIALIZED 8");
        int bci = ca.labelToBci(parsed.newTarget());
        check(bci >= 0 && bci < ca.codeLength(), "newTarget bci in range");
        check((ca.codeArray()[bci] & 0xff) == 187, "newTarget points at NEW opcode (187)");
        var made = StackMapFrameInfo.UninitializedVerificationTypeInfo.of(parsed.newTarget());
        check(made.newTarget() == parsed.newTarget(), "of(Label) target identity");
        check(made.tag() == 8, "of tag 8");
        check(made instanceof StackMapFrameInfo.VerificationTypeInfo, "is VerificationTypeInfo");
        try { StackMapFrameInfo.UninitializedVerificationTypeInfo.of(null); check(false, "of(null) must throw"); }
        catch (NullPointerException e) { check(true, "of(null) NPE"); }
    }

    // ---- Part 4: javac 真产物（两个分支各自 NEW 偏移）----
    static void testRealFramesDistinctOffsets() throws Exception {
        byte[] rb = UninitializedVerificationTypeInfoTest.class.getResourceAsStream("UninitializedVerificationTypeInfoTest$UVReal.class").readAllBytes();
        ClassModel m = ClassFile.of().parse(rb);
        CodeAttribute ca = null;
        for (MethodModel mm : m.methods())
            if (mm.methodName().stringValue().equals("g")) ca = (CodeAttribute) mm.code().get();
        Set<Integer> offsets = new HashSet<>();
        int uninitCount = 0;
        for (StackMapFrameInfo f : ca.findAttribute(Attributes.stackMapTable()).get().entries())
            for (StackMapFrameInfo.VerificationTypeInfo v : f.stack())
                if (v instanceof StackMapFrameInfo.UninitializedVerificationTypeInfo u) {
                    uninitCount++;
                    offsets.add(ca.labelToBci(u.newTarget()));
                }
        check(uninitCount >= 2, "stack carries two uninit copies (new+dup): " + uninitCount);
        check(offsets.size() == 1, "both copies share one NEW offset");
        for (int off : offsets) check((ca.codeArray()[off] & 0xff) == 187, "offset is NEW");
    }

    // ---- Part 5: build 侧对照（fresh build 不产 uninit vti——builder 类型推导只有引用类型）----
    static void testBuildSideNoUninit() throws Exception {
        byte[] bb = ClassFile.of().build(ClassDesc.of("DynUVT"), cb -> cb.withMethodBody("m",
                MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC,
                cob -> { Label l = cob.newLabel(); cob.iconst_0().iload(0).if_icmpge(l); cob.iconst_1().ireturn(); cob.labelBinding(l).iconst_m1().ireturn(); }));
        CodeAttribute ca = (CodeAttribute) ClassFile.of().parse(bb).methods().get(0).code().get();
        boolean sawUninit = false;
        for (StackMapFrameInfo f : ca.findAttribute(Attributes.stackMapTable()).get().entries())
            for (StackMapFrameInfo.VerificationTypeInfo v : f.stack())
                if (v instanceof StackMapFrameInfo.UninitializedVerificationTypeInfo) sawUninit = true;
        check(!sawUninit, "int-only branch frames carry no uninit vti");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testOfFactory();
        testRealFramesDistinctOffsets();
        testBuildSideNoUninit();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

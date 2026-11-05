/*
 * @test
 * @summary java.lang.classfile.attribute.CodeAttribute conformance: white-box structure + black-box behavior.
 * @run main CodeAttributeTest
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

public class CodeAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.CodeAttribute");
        check(c != null, "java.lang.classfile.attribute.CodeAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.CodeAttribute"), "binary name java.lang.classfile.attribute.CodeAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.CodeAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.CodeAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.CodeAttribute has no superclass");
        check(declares(c, "maxLocals"), "declares maxLocals");
        check(declares(c, "maxStack"), "declares maxStack");
        check(declares(c, "codeLength"), "declares codeLength");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_int), 0, cob -> cob.iconst_0().iconst_1().iadd().ireturn()));
        CodeAttribute ca = (CodeAttribute) ClassFile.of().parse(b).methods().get(0).code().get();
        check(ca.maxStack() == 2, "maxStack 2");
        check(ca.maxLocals() == 1, "maxLocals 1");
        check(ca.codeLength() == 4, "codeLength 4");
        check(ca.codeArray().length == 4, "codeArray length");
        check(ca.attributeName().stringValue().equals("Code"), "attributeName");

    }

    // ---- Part 3: build → parse 全访问器回读 ----
    static void testBuildReadback() {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("CAT1"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC, c -> {
            Label l = c.newLabel();
            c.iconst_0().iload(0).if_icmpge(l);
            c.bipush(7).ireturn();
            c.labelBinding(l);
            c.iconst_m1().ireturn();
        }));
        MethodModel mm = cf.parse(b).methods().get(0);
        check(mm.code().isPresent(), "code present");
        check(mm.code().get() instanceof CodeAttribute, "CodeModel instanceof CodeAttribute");
        CodeAttribute ca = (CodeAttribute) mm.code().get();
        check(ca.maxStack() == 2, "maxStack 2 (two ints on stack)");
        check(ca.maxLocals() == 1, "maxLocals 1 (one int param)");
        check(ca.codeLength() == 10, "codeLength 10");
        check(ca.codeArray().length == ca.codeLength(), "codeArray.length==codeLength");
        check(ca.codeArray()[0] == Opcode.ICONST_0.bytecode(), "byte 0 is iconst_0");
        check((ca.codeArray()[ca.codeLength() - 1] & 0xff) == Opcode.IRETURN.bytecode(), "last byte ireturn");
        // labelToBci：从解析模型的 LabelTarget 取 label 映射回 bci
        int labels = 0;
        for (CodeElement el : ca.elementList())
            if (el instanceof LabelTarget lt) { labels++; check(ca.labelToBci(lt.label()) == 8, "labelToBci==8"); }
        check(labels == 1, "one label target");
        // Code 属性经 findAttribute(Attributes.code()) 获取，与 code() 同一实例
        var viaAttr = mm.findAttribute(Attributes.code());
        check(viaAttr.isPresent(), "findAttribute(Attributes.code()) present");
        check(viaAttr.get() == ca, "findAttribute(code()) == method.code() instance");
        check(ca.attributeName().stringValue().equals(Attributes.NAME_CODE), "attributeName==NAME_CODE");
        check(ca.attributeName().stringValue().equals("Code"), "attributeName literal");
        check(ca.attributeMapper() == Attributes.code(), "attributeMapper==Attributes.code()");
        check(ca.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!ca.attributeMapper().allowMultiple(), "Code allowMultiple false");
        check(cf.verify(b).isEmpty(), "branch class verifies");
    }

    // ---- Part 4: try-catch → exceptionHandlers ----
    static void testExceptionHandlers() {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("CAT2"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC, c -> {
            Label start = c.newLabel(), end = c.newLabel(), handler = c.newLabel();
            c.labelBinding(start).nop().labelBinding(end).return_();
            c.labelBinding(handler).athrow();
            c.exceptionCatch(start, end, handler, ClassDesc.of("java.lang.Exception"));
        }));
        CodeAttribute ca = (CodeAttribute) cf.parse(b).methods().get(0).code().get();
        check(ca.exceptionHandlers().size() == 1, "1 handler");
        ExceptionCatch ec0 = ca.exceptionHandlers().get(0);
        check(ec0.catchType().isPresent() && ec0.catchType().get().asInternalName().equals("java/lang/Exception"), "catchType Exception");
        check(ca.labelToBci(ec0.handler()) == 2, "handler bci 2");
        check(ca.codeLength() > 0 && ca.maxStack() >= 1, "try-catch code shape");
        // catch-all（无类型）handler
        byte[] b2 = cf.build(ClassDesc.of("CAT3"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC, c -> {
            Label start = c.newLabel(), end = c.newLabel(), handler = c.newLabel();
            c.labelBinding(start).nop().labelBinding(end).return_();
            c.labelBinding(handler).athrow();
            c.exceptionCatch(start, end, handler, java.util.Optional.<ClassEntry>empty());
        }));
        CodeAttribute ca2 = (CodeAttribute) cf.parse(b2).methods().get(0).code().get();
        check(!ca2.exceptionHandlers().get(0).catchType().isPresent(), "catch-all has empty catchType");
    }

    // ---- Part 5: javac 真类产物 ----
    static int loopSum(int n) {
        int s = 0;
        for (int i = 0; i < n; i++) s += i;
        return s;
    }

    static void testRealClass() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] self = CodeAttributeTest.class.getResourceAsStream("CodeAttributeTest.class").readAllBytes();
        ClassModel cm = cf.parse(self);
        boolean found = false;
        for (MethodModel mm : cm.methods()) {
            if (mm.methodName().stringValue().equals("loopSum") && mm.code().isPresent()
                    && mm.code().get() instanceof CodeAttribute ca) {
                found = true;
                check(ca.codeLength() > 0, "loopSum codeLength>0");
                check(ca.codeArray().length == ca.codeLength(), "real codeArray==codeLength");
                check(ca.maxLocals() >= 3, "real maxLocals covers n,s,i");
                check(ca.maxStack() >= 2, "real maxStack");
                check(ca.exceptionHandlers().isEmpty(), "loopSum no handlers");
                check(!ca.elementList().isEmpty(), "elementList non-empty");
            }
        }
        check(found, "loopSum method found with CodeAttribute");
        check(cf.verify(cm).isEmpty(), "real class verifies");
        // 每个有代码的方法都有 Code 属性
        for (MethodModel mm : cm.methods())
            if (mm.code().isPresent())
                check(mm.findAttribute(Attributes.code()).isPresent(), "method " + mm.methodName().stringValue() + " has Code attr");
        // 抽象/原生方法无 Code：动态 build 一个
        byte[] abs = cf.build(ClassDesc.of("CAT4"), cb -> cb
                .withMethod("a", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_PUBLIC | ClassFile.ACC_ABSTRACT, mb -> {}));
        check(ClassFile.of().parse(abs).methods().get(0).code().isEmpty(), "abstract method has no Code");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testBuildReadback();
        testExceptionHandlers();
        testRealClass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.lang.classfile.CodeModel conformance: white-box structure + black-box behavior.
 * @run main CodeModelTest
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

public class CodeModelTest {
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
        Class<?> c = Class.forName("java.lang.classfile.CodeModel");
        check(c != null, "java.lang.classfile.CodeModel loads");
        check(c.getName().equals("java.lang.classfile.CodeModel"), "binary name java.lang.classfile.CodeModel");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.CodeModel is public");
        check(c.isInterface(), "java.lang.classfile.CodeModel is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.CodeModel has no superclass");
        check(declares(c, "parent"), "declares parent");
        check(declares(c, "exceptionHandlers"), "declares exceptionHandlers");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_int), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> cob.iconst_1().iconst_2().iadd().ireturn()));
        ClassModel cm = ClassFile.of().parse(b);
        CodeModel code = cm.methods().get(0).code().get();
        check(code.parent().isPresent() && code.parent().get().methodName().stringValue().equals("m"), "parent");
        check(code.exceptionHandlers().isEmpty(), "no handlers");
        int n = 0;
        for (CodeElement ce : code) { if (ce instanceof Instruction) n++; }
        check(n == 4, "4 instructions");
        check(code instanceof java.lang.classfile.attribute.CodeAttribute, "code attribute");
        check(code.elementList().size() == n, "elementList == instructions");

    }


    // ---- Part 3: try-catch + 分支 + 局部变量 发射 → exceptionHandlers/elementList/CodeAttribute 全断言 ----
    static void testTryCatchReadback() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("pkg.TC"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC, cob -> {
            Label s = cob.newLabel(), e = cob.newLabel(), h = cob.newLabel(), end = cob.newLabel();
            cob.labelBinding(s).iconst_1().pop().goto_(end);
            cob.labelBinding(e);
            cob.labelBinding(h).pop().return_();
            cob.labelBinding(end).return_();
            cob.exceptionCatch(s, e, h, ClassDesc.of("java.lang.Exception"));
            cob.exceptionCatchAll(s, e, h);
            cob.localVariable(0, "lv", ConstantDescs.CD_int, s, e);
            cob.lineNumber(7);
        }));
        ClassModel cm = cf.parse(b);
        MethodModel mm = cm.methods().get(0);
        CodeModel code = mm.code().get();
        // parent 双向身份
        check(code.parent().isPresent() && code.parent().get() == mm, "code parent identity == method");
        check(mm.code().get() == code, "method.code() identity == code");
        check(code instanceof MethodElement, "CodeModel instanceof MethodElement");
        check(code instanceof CompoundElement, "CodeModel instanceof CompoundElement");
        check(!(ClassElement.class.isInstance(code)), "CodeModel NOT ClassElement");
        // exceptionHandlers 表
        List<ExceptionCatch> handlers = code.exceptionHandlers();
        check(handlers.size() == 2, "2 handlers");
        CodeAttribute ca = (CodeAttribute) code;
        ExceptionCatch typed = handlers.get(0), any = handlers.get(1);
        check(typed.catchType().isPresent() && typed.catchType().get().asInternalName().equals("java/lang/Exception"), "typed catchType Exception");
        check(any.catchType().isEmpty(), "exceptionCatchAll catchType empty (any)");
        check(typed.handler() != null && typed.tryStart() != null && typed.tryEnd() != null, "typed labels non-null");
        int tsBci = ca.labelToBci(typed.tryStart()), teBci = ca.labelToBci(typed.tryEnd()), hBci = ca.labelToBci(typed.handler());
        check(tsBci == 0, "tryStart bci 0");
        check(teBci == 5, "tryEnd bci 5 (exclusive)");
        check(hBci == 5, "handler bci 5");
        check(teBci >= tsBci, "tryEnd >= tryStart");
        check(hBci >= 0 && hBci < ca.codeLength(), "handler bci in range");
        check(typed.tryStart() != typed.handler(), "tryStart != handler object");
        // elementList：指令/伪指令计数 + ExceptionCatch 以 CodeElement 出现（语义等价而非同对象）
        int instr = 0, pseudo = 0, ecElems = 0, lv = 0, ln = 0, lt = 0;
        for (CodeElement el : code.elementList()) {
            if (Instruction.class.isInstance(el)) instr++;
            if (el instanceof PseudoInstruction) pseudo++;
            if (el instanceof ExceptionCatch) ecElems++;
            if (el instanceof LocalVariable) lv++;
            if (el instanceof LineNumber) ln++;
            if (el instanceof LabelTarget) lt++;
            check(CodeElement.class.isInstance(el), "code element instanceof CodeElement");
        }
        check(instr == 6, "6 instructions (iconst_1,pop,goto_,pop,return_,return_): " + instr);
        check(pseudo >= 2, "pseudo count >= 2 (ExceptionCatch x2): " + pseudo);
        check(ecElems == 2, "2 ExceptionCatch in elementList: " + ecElems);
        check(lv == 1, "1 LocalVariable pseudo: " + lv);
        check(ln == 1, "1 LineNumber pseudo: " + ln);
        check(lt == 3, "3 LabelTarget pseudos (e/h/end coalesce at same bci): " + lt);
        int iter = 0; for (CodeElement el : code) iter++;
        check(iter == code.elementList().size(), "iteration count == elementList size");
        // 语义等价：elementList 中的 EC 与 handlers() 中 bci 一致（不同包装对象，实测）
        boolean matched = false;
        for (CodeElement el : code.elementList())
            if (el instanceof ExceptionCatch le && ca.labelToBci(le.tryStart()) == tsBci && ca.labelToBci(le.handler()) == hBci) matched = true;
        check(matched, "elementList EC bci-matches handlers() entry");
        // CodeAttribute 面
        check(ca.maxStack() > 0, "maxStack > 0");
        check(ca.maxLocals() == 0, "maxLocals 0 (static void, no real locals)");
        check(ca.codeLength() > 0, "codeLength > 0");
        check(ca.codeArray().length == ca.codeLength(), "codeArray().length == codeLength");
        check((ca.codeArray()[0] & 0xff) == Opcode.ICONST_1.bytecode(), "codeArray[0] == ICONST_1 bytecode");
        int sum = 0;
        for (CodeElement el : code.elementList()) if (Instruction.class.isInstance(el)) sum += ((Instruction) el).sizeInBytes();
        check(sum == ca.codeLength(), "sum(Instruction.sizeInBytes) == codeLength (" + sum + "/" + ca.codeLength() + ")");
        // 发射伪指令 → 属性
        check(code.findAttribute(Attributes.localVariableTable()).isPresent(), "LVT attribute present");
        check(code.findAttribute(Attributes.lineNumberTable()).isPresent(), "LNT attribute present");
        check(code.findAttribute(Attributes.stackMapTable()).isPresent(), "StackMapTable present (branches)");
        // LNT 属性值与伪指令一致
        LineNumberTableAttribute lnt = code.findAttribute(Attributes.lineNumberTable()).get();
        check(lnt.lineNumbers().size() == 1 && lnt.lineNumbers().get(0).lineNumber() == 7, "LNT line 7");
        LocalVariableTableAttribute lvtAttr = code.findAttribute(Attributes.localVariableTable()).get();
        check(lvtAttr.localVariables().size() == 1 && lvtAttr.localVariables().get(0).name().stringValue().equals("lv"), "LVT name lv");
        check(lvtAttr.localVariables().get(0).slot() == 0, "LVT slot 0");
        check(lvtAttr.localVariables().get(0).type().stringValue().equals("I"), "LVT type I");
        check(cf.verify(cm).isEmpty(), "try-catch verify clean");
    }

    // ---- Part 4: 真类方法 code 回读 + 无 code 方法 ----
    static void testSelfCode() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] self = CodeModelTest.class.getResourceAsStream("CodeModelTest.class").readAllBytes();
        ClassModel cm = cf.parse(self);
        MethodModel main = null;
        for (MethodModel m : cm.methods()) if (m.methodName().stringValue().equals("main")) main = m;
        check(main != null && main.code().isPresent(), "self main has code");
        CodeModel code = main.code().get();
        CodeAttribute ca = (CodeAttribute) code;
        check(ca.codeLength() > 10, "main codeLength > 10");
        check(ca.maxStack() >= 1 && ca.maxLocals() >= 1, "main maxStack/maxLocals >= 1");
        check(code.exceptionHandlers().isEmpty(), "main no handlers");
        int ret = 0;
        for (CodeElement el : code.elementList()) { if (Instruction.class.isInstance(el) && ((Instruction) el).opcode() == Opcode.RETURN) ret++; }
        check(ret >= 1, "main has >=1 RETURN");
        // 自身类全部方法均有 code；抽象/原生方法经 build 验证 code() empty
        boolean allCode = true;
        for (MethodModel m : cm.methods()) if (m.code().isEmpty()) allCode = false;
        check(allCode, "self all methods have code");
        byte[] ab = ClassFile.of().build(ClassDesc.of("pkg.NoCode"), cb -> cb
                .withMethod("am", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_ABSTRACT, mb -> {}));
        MethodModel am = ClassFile.of().parse(ab).methods().get(0);
        check(am.code().isEmpty(), "abstract method code() empty");
        check(am.elementList().size() == 1, "abstract method elementList [AccessFlags]");
        check(cf.verify(cm).isEmpty(), "self verify clean");
    }

    // ---- Part 5: defineClass 运行闭环（try-catch 真运行） ----
    static int dynSeq;
    static void testDefineClassLoop() throws Exception {
        ClassFile cf = ClassFile.of();
        String name = "DynCodeModel" + (++dynSeq);
        byte[] b = cf.build(ClassDesc.of(name), cb -> cb.withMethodBody("div", MethodTypeDesc.ofDescriptor("(II)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                cob -> cob.iload(0).iload(1).idiv().ireturn()));
        ClassModel rm = cf.parse(b);
        CodeModel code = rm.methods().get(0).code().get();
        check(code.parent().isPresent(), "dyn code parent");
        check(code.elementList().size() == 4, "dyn code 4 elements (iload,iload,idiv,ireturn)");
        CodeAttribute dca = (CodeAttribute) code;
        check(dca.maxLocals() == 2, "dyn div maxLocals 2 (2 int params)");
        check(dca.maxStack() == 2, "dyn div maxStack 2");
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getMethod("div", int.class, int.class).invoke(null, 84, 2).equals(42), "dyn div(84,2)==42");
        check(cf.verify(rm).isEmpty(), "dyn verify clean");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testTryCatchReadback();
        testSelfCode();
        testDefineClassLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

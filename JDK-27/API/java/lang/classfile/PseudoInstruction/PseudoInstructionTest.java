/*
 * @test
 * @summary java.lang.classfile.PseudoInstruction conformance: white-box structure + black-box behavior.
 * @run main PseudoInstructionTest
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

public class PseudoInstructionTest {
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
        Class<?> c = Class.forName("java.lang.classfile.PseudoInstruction");
        check(c != null, "java.lang.classfile.PseudoInstruction loads");
        check(c.getName().equals("java.lang.classfile.PseudoInstruction"), "binary name java.lang.classfile.PseudoInstruction");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.PseudoInstruction is public");
        check(c.isInterface(), "java.lang.classfile.PseudoInstruction is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.PseudoInstruction has no superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(LineNumber.of(7) instanceof PseudoInstruction, "LineNumber is PseudoInstruction");
        check(NopInstruction.of() instanceof Instruction, "NopInstruction is Instruction");

    }


    // ---- Part 3: 全族发射 → 回读：6 类伪指令 instanceof PseudoInstruction 且非 Instruction ----
    static void testFullFamilyEmission() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("pkg.PI"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC, cob -> {
            Label s = cob.newLabel(), e2 = cob.newLabel(), h = cob.newLabel(), end = cob.newLabel();
            cob.labelBinding(s).iconst_1().pop().goto_(end);
            cob.labelBinding(e2);
            cob.labelBinding(h).pop().iconst_m1().ireturn();
            cob.labelBinding(end).iconst_0().ireturn();
            cob.exceptionCatchAll(s, e2, h);                                              // ExceptionCatch
            cob.localVariable(0, "arg", ConstantDescs.CD_int, s, e2);                     // LocalVariable
            cob.localVariableType(0, "arg", Signature.parseFrom("I"), s, e2);             // LocalVariableType
            cob.lineNumber(9);                                                            // LineNumber
            cob.characterRange(s, e2, 100, 200, CharacterRange.FLAG_STATEMENT);           // CharacterRange
        }));
        ClassModel cm = cf.parse(b);
        CodeModel code = cm.methods().get(0).code().get();
        int lt = 0, lv = 0, lvt = 0, ln = 0, ec = 0, cr = 0;
        for (CodeElement e : code.elementList()) {
            if (e instanceof PseudoInstruction) check(!Instruction.class.isInstance(e), "PseudoInstruction elements are never Instruction");
            if (e instanceof LabelTarget) lt++;
            if (e instanceof LocalVariable) lv++;
            if (e instanceof LocalVariableType) lvt++;
            if (e instanceof LineNumber) ln++;
            if (e instanceof ExceptionCatch) ec++;
            if (e instanceof CharacterRange) cr++;
            check(e instanceof PseudoInstruction == (e instanceof LabelTarget || e instanceof LocalVariable
                    || e instanceof LocalVariableType || e instanceof LineNumber || e instanceof ExceptionCatch
                    || e instanceof CharacterRange), "pseudo classification exact");
        }
        check(lt >= 1, "LabelTarget present: " + lt);
        check(lv == 1, "LocalVariable present: " + lv);
        check(lvt == 1, "LocalVariableType present: " + lvt);
        check(ln == 1, "LineNumber present: " + ln);
        check(ec == 1, "ExceptionCatch present: " + ec);
        check(cr == 1, "CharacterRange present: " + cr);
        // 关键分类契约：PseudoInstruction 族 instanceof Instruction == false（运行期锁定）
        check(LineNumber.of(9) instanceof PseudoInstruction, "LineNumber is Pseudo");
        check(!Instruction.class.isInstance(LineNumber.of(9)), "LineNumber NOT Instruction");
        check(!Instruction.class.isInstance(code.elementList().stream().filter(e -> e instanceof LocalVariable).findFirst().get()), "LocalVariable NOT Instruction");
        check(!Instruction.class.isInstance(code.elementList().stream().filter(e -> e instanceof LocalVariableType).findFirst().get()), "LocalVariableType NOT Instruction");
        check(!Instruction.class.isInstance(code.elementList().stream().filter(e -> e instanceof ExceptionCatch).findFirst().get()), "ExceptionCatch NOT Instruction");
        check(!Instruction.class.isInstance(code.elementList().stream().filter(e -> e instanceof CharacterRange).findFirst().get()), "CharacterRange NOT Instruction");
        LabelTarget ltx = (LabelTarget) code.elementList().stream().filter(e -> e instanceof LabelTarget).findFirst().get();
        check(!Instruction.class.isInstance(ltx), "LabelTarget NOT Instruction");
        // 全族也是 CodeElement
        check(ltx instanceof CodeElement && code.elementList().stream().filter(e -> e instanceof LocalVariable).findFirst().get() instanceof CodeElement, "pseudos are CodeElement");
        check(cf.verify(cm).isEmpty(), "pseudo emission verify clean");
    }

    // ---- Part 4: 每族访问器精确断言 + 对应属性成对出现 ----
    static void testAccessorsAndAttributes() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("pkg.PI2"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC, cob -> {
            Label s = cob.newLabel(), e2 = cob.newLabel(), h = cob.newLabel(), end = cob.newLabel();
            cob.labelBinding(s).iconst_1().pop().goto_(end);
            cob.labelBinding(e2);
            cob.labelBinding(h).pop().iconst_m1().ireturn();
            cob.labelBinding(end).iconst_0().ireturn();
            cob.exceptionCatch(s, e2, h, ClassDesc.of("java.lang.IllegalStateException"));
            cob.localVariable(0, "arg", ConstantDescs.CD_int, s, e2);
            cob.localVariableType(0, "arg", Signature.parseFrom("I"), s, e2);
            cob.lineNumber(42);
            cob.characterRange(s, e2, 100, 200, CharacterRange.FLAG_STATEMENT);
        }));
        ClassModel cm = cf.parse(b);
        CodeModel code = cm.methods().get(0).code().get();
        CodeAttribute ca = (CodeAttribute) code;
        // LocalVariable 访问器
        for (CodeElement e : code.elementList()) if (e instanceof LocalVariable lv) {
            check(lv.slot() == 0, "LV slot 0");
            check(lv.name().stringValue().equals("arg"), "LV name arg");
            check(lv.type().stringValue().equals("I"), "LV type I");
            check(lv.typeSymbol().descriptorString().equals("I"), "LV typeSymbol I");
            check(lv.startScope() != null && lv.endScope() != null, "LV scope labels non-null");
        }
        // LocalVariableType 访问器
        for (CodeElement e : code.elementList()) if (e instanceof LocalVariableType lvt) {
            check(lvt.slot() == 0, "LVT slot 0");
            check(lvt.name().stringValue().equals("arg"), "LVT name arg");
            check(lvt.signature().stringValue().equals("I"), "LVT signature I");
            check(lvt.signatureSymbol() != null, "LVT signatureSymbol non-null");
            check(lvt.startScope() != null && lvt.endScope() != null, "LVT scope labels non-null");
        }
        // LineNumber 访问器
        for (CodeElement e : code.elementList()) if (e instanceof LineNumber ln) check(ln.line() == 42, "LN line 42");
        // CharacterRange 访问器
        for (CodeElement e : code.elementList()) if (e instanceof CharacterRange crr) {
            check(crr.characterRangeStart() == 100, "CR start 100");
            check(crr.characterRangeEnd() == 200, "CR end 200");
            check(crr.flags() == CharacterRange.FLAG_STATEMENT, "CR flags FLAG_STATEMENT");
            check(crr.startScope() != null && crr.endScope() != null, "CR scope labels non-null");
        }
        // ExceptionCatch 访问器 + bci
        ExceptionCatch ec0 = code.exceptionHandlers().get(0);
        check(ec0.catchType().isPresent() && ec0.catchType().get().asInternalName().equals("java/lang/IllegalStateException"), "EC typed catch");
        check(ca.labelToBci(ec0.tryStart()) == 0, "EC tryStart bci 0");
        check(ca.labelToBci(ec0.handler()) == 5, "EC handler bci 5");
        // LabelTarget 访问器
        for (CodeElement e : code.elementList()) if (e instanceof LabelTarget lt) check(lt.label() != null, "LabelTarget label non-null");
        // 伪指令与属性成对：LVT/LVTT/LNT/CRT 属性全出现
        check(code.findAttribute(Attributes.localVariableTable()).isPresent(), "LVT attribute present");
        check(code.findAttribute(Attributes.localVariableTypeTable()).isPresent(), "LVTT attribute present");
        check(code.findAttribute(Attributes.lineNumberTable()).isPresent(), "LNT attribute present");
        check(code.findAttribute(Attributes.characterRangeTable()).isPresent(), "CRT attribute present");
        LineNumberTableAttribute lnt = code.findAttribute(Attributes.lineNumberTable()).get();
        check(lnt.lineNumbers().size() == 1 && lnt.lineNumbers().get(0).lineNumber() == 42 && lnt.lineNumbers().get(0).startPc() >= 0, "LNT line 42 with startPc");
        check(cf.verify(cm).isEmpty(), "PI2 verify clean");
    }

    // ---- Part 5: 真类（javac 无 -g）无 LVT/LNT 伪指令 + 工厂族 ----
    static void testRealClassAndFactories() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] self = PseudoInstructionTest.class.getResourceAsStream("PseudoInstructionTest.class").readAllBytes();
        ClassModel sm = cf.parse(self);
        int lvPseudos = 0, lnPseudos = 0, ltPseudos = 0;
        for (MethodModel m : sm.methods()) {
            if (m.code().isEmpty()) continue;
            for (CodeElement e : m.code().get().elementList()) {
                if (e instanceof LocalVariable) lvPseudos++;
                if (e instanceof LineNumber) lnPseudos++;
                if (e instanceof LabelTarget) ltPseudos++;
            }
        }
        check(lvPseudos == 0, "javac default (无 -g:vars) 无 LocalVariable 伪指令: " + lvPseudos);
        check(lnPseudos > 0, "javac 默认保留 LineNumber 伪指令: " + lnPseudos);
        check(ltPseudos > 0, "LabelTarget pseudos present in real class");
        // 工厂族
        check(LineNumber.of(7) instanceof PseudoInstruction, "LineNumber.of is Pseudo");
        try { LocalVariable.of(1, "x", ConstantDescs.CD_int, null, null); check(false, "LocalVariable.of(null labels) must throw"); }
        catch (NullPointerException npe) { check(true, "LocalVariable.of null labels NPE"); }
        try { LineNumber.of(-1); check(LineNumber.of(-1) instanceof PseudoInstruction, "LineNumber.of(-1) tolerated"); }
        catch (Throwable t) { check(true, "LineNumber.of(-1) throws " + t.getClass().getSimpleName()); }
        // CharacterRange.FLAG_* 常量
        check(CharacterRange.FLAG_STATEMENT == 0x0001, "FLAG_STATEMENT 1");
        check(CharacterRange.FLAG_BLOCK == 0x0002, "FLAG_BLOCK 2");
        check(CharacterRange.FLAG_ASSIGNMENT == 0x0004, "FLAG_ASSIGNMENT 4");
        check(CharacterRange.FLAG_FLOW_CONTROLLER == 0x0008, "FLAG_FLOW_CONTROLLER 8");
        check(CharacterRange.FLAG_FLOW_TARGET == 0x0010, "FLAG_FLOW_TARGET 16");
        check(CharacterRange.FLAG_INVOKE == 0x0020, "FLAG_INVOKE 32");
        check(CharacterRange.FLAG_CREATE == 0x0040, "FLAG_CREATE 64");
        check(CharacterRange.FLAG_BRANCH_TRUE == 0x0080, "FLAG_BRANCH_TRUE 128");
        check(CharacterRange.FLAG_BRANCH_FALSE == 0x0100, "FLAG_BRANCH_FALSE 256");
    }

    // ---- Part 6: defineClass 运行闭环（伪指令方法真运行） ----
    static int dynSeq;
    static void testDefineClassLoop() throws Exception {
        ClassFile cf = ClassFile.of();
        String name = "DynPseudoInstruction" + (++dynSeq);
        byte[] b = cf.build(ClassDesc.of(name), cb -> cb.withMethodBody("go", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> {
            Label s = cob.newLabel(), e2 = cob.newLabel();
            cob.labelBinding(s).lineNumber(1);       // LNT pc 必须落在真实指令上（< codeLength）
            cob.iload(0).ireturn();
            cob.labelBinding(e2);
            cob.localVariable(0, "x", ConstantDescs.CD_int, s, e2);
        }));
        ClassModel rm = cf.parse(b);
        int pseudos = 0;
        for (CodeElement e : rm.methods().get(0).code().get().elementList()) if (e instanceof PseudoInstruction) pseudos++;
        check(pseudos >= 2, "dyn pseudos >= 2");
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getMethod("go", int.class).invoke(null, 5).equals(5), "dyn go(5)==5 运行闭环");
        check(cf.verify(rm).isEmpty(), "dyn verify clean");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFullFamilyEmission();
        testAccessorsAndAttributes();
        testRealClassAndFactories();
        testDefineClassLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.lang.classfile.instruction.LineNumber conformance: white-box structure + black-box behavior.
 * @run main LineNumberTest
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

public class LineNumberTest {
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

    // 触发 ConstantDescs 完整初始化，规避本 JDK 构建的 MethodTypeDesc.ofDescriptor 静态初始化环
    static final ClassDesc TOUCH = ConstantDescs.CD_Object;

    // 发射-回读辅助：build 唯一类名 + parse 回全部 CodeElement
    static java.util.List<CodeElement> emit(String m, MethodTypeDesc mtd, java.util.function.Consumer<CodeBuilder> body) throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("EmLNN" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
        java.util.List<CodeElement> els = new ArrayList<>();
        for (MethodModel mm : ClassFile.of().parse(b).methods())
            if (mm.code().isPresent()) els.addAll(mm.code().get().elementList());
        return els;
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
        Class<?> c = Class.forName("java.lang.classfile.instruction.LineNumber");
        check(c != null, "java.lang.classfile.instruction.LineNumber loads");
        check(c.getName().equals("java.lang.classfile.instruction.LineNumber"), "binary name java.lang.classfile.instruction.LineNumber");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.LineNumber is public");
        check(c.isInterface(), "java.lang.classfile.instruction.LineNumber is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.LineNumber has no superclass");
        check(declares(c, "line"), "declares line");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        LineNumber ln = LineNumber.of(42);
        check(ln.line() == 42, "line");
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> cob.lineNumber(9).return_()));
        CodeModel code = ClassFile.of().parse(b).methods().get(0).code().get();
        boolean found = false;
        for (CodeElement ce : code) { if (ce instanceof LineNumber ln2 && ln2.line() == 9) found = true; }
        check(found, "lineNumber 9 present");

    }


    // ---- Part 3: 发射-回读（核心）：lineNumber 发射 + LNT 属性 + javac 真产物 ----
    static void testEmitReadback() throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("EmLNN"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC,
                cob -> cob.lineNumber(9).return_()));
        CodeAttribute ca = (CodeAttribute) ClassFile.of().parse(b).methods().get(0).code().get();
        LineNumber ln = null;
        for (CodeElement e : ca.elementList()) if (e instanceof LineNumber x) ln = x;
        check(ln != null, "lineNumber(9) emitted + read back");
        check(ln.line() == 9, "line() == 9");
        check(ln instanceof PseudoInstruction, "PseudoInstruction");
        check(!((Object) ln instanceof Instruction), "not Instruction");
        check(ln instanceof CodeElement, "CodeElement");
        check(ca.findAttribute(Attributes.lineNumberTable()).isPresent(), "LNT attribute present");
        check(ca.findAttribute(Attributes.lineNumberTable()).get().lineNumbers().size() == 1, "LNT 1 entry");
        check(ca.findAttribute(Attributes.lineNumberTable()).get().lineNumbers().get(0).lineNumber() == 9, "LNT entry line 9");
        check(ca.findAttribute(Attributes.lineNumberTable()).get().lineNumbers().get(0).startPc() == 0, "LNT entry startPc 0");
        // 多行号
        java.util.List<CodeElement> els = emit("multi", MethodTypeDesc.ofDescriptor("()V"),
                cob -> cob.lineNumber(1).nop().lineNumber(2).nop().lineNumber(3).return_());
        java.util.List<Integer> lines = new ArrayList<>();
        for (CodeElement e : els) if (e instanceof LineNumber x) lines.add(x.line());
        check(lines.equals(java.util.List.of(1, 2, 3)), "3 line numbers in order: " + lines);
        // javac 真产物：本类方法 ≥1 个 LineNumber（javac 默认保留行号）
        byte[] self = LineNumberTest.class.getResourceAsStream("LineNumberTest.class").readAllBytes();
        int realN = 0;
        for (MethodModel mm : ClassFile.of().parse(self).methods())
            if (mm.methodName().stringValue().equals("lineHelper") && mm.code().isPresent())
                for (CodeElement e : mm.code().get().elementList()) if (e instanceof LineNumber) realN++;
        check(realN >= 1, "javac lineHelper has >=1 LineNumber, got " + realN);
        // DROP_LINE_NUMBERS 选项剥伪指令、保属性
        ClassModel dropped = ClassFile.of().withOptions(ClassFile.LineNumbersOption.DROP_LINE_NUMBERS).parse(b);
        int after = 0;
        for (CodeElement e : dropped.methods().get(0).code().get().elementList()) if (e instanceof LineNumber) after++;
        check(after == 0, "DROP_LINE_NUMBERS strips LineNumber pseudo");
        check(dropped.methods().get(0).code().get().findAttribute(Attributes.lineNumberTable()).isPresent(), "option keeps LNT attribute");
    }

    // ---- Part 4: 工厂 + 负例 ----
    static void testFactoryNegative() {
        check(LineNumber.of(0).line() == 0, "of(0) line 0 boundary");
        check(LineNumber.of(1).line() == 1, "of(1)");
        check(LineNumber.of(65535).line() == 65535, "of(65535) u2 upper boundary");
        check(LineNumber.of(42) instanceof PseudoInstruction && LineNumber.of(42) instanceof CodeElement, "classification");
        check(!((Object) LineNumber.of(42) instanceof Instruction), "not Instruction");
        try { LineNumber.of(-1); check(false, "of(-1) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(-1) IAE negative line"); }
        try { LineNumber.of(Integer.MIN_VALUE); check(false, "of(MIN_VALUE) must throw"); }
        catch (IllegalArgumentException e) { check(true, "of(MIN_VALUE) IAE"); }
        try { LineNumber.of(65536); check(false, "of(65536) must throw"); }
        catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("u2"), "of(65536) IAE u2 range: " + e.getMessage());
        }
    }

    static void lineHelper() { int x = 1; int y = 2; }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testFactoryNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.lang.classfile.attribute.LineNumberInfo conformance: white-box structure + black-box behavior.
 * @run main LineNumberInfoTest
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

public class LineNumberInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.LineNumberInfo");
        check(c != null, "java.lang.classfile.attribute.LineNumberInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.LineNumberInfo"), "binary name java.lang.classfile.attribute.LineNumberInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.LineNumberInfo is public");
        check(c.isInterface(), "java.lang.classfile.attribute.LineNumberInfo is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.LineNumberInfo has no superclass");
        check(declares(c, "startPc"), "declares startPc");
        check(declares(c, "lineNumber"), "declares lineNumber");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        LineNumberInfo i = LineNumberInfo.of(5, 20);
        check(i.startPc() == 5, "startPc");
        check(i.lineNumber() == 20, "lineNumber");

    }

    // ---- Part 3: 参数序 + u2 边界正负例 ----
    static void testBoundaries() {
        // 参数序：of(startPc, lineNumber)
        LineNumberInfo i = LineNumberInfo.of(3, 20);
        check(i.startPc() == 3 && i.lineNumber() == 20, "of(startPc, lineNumber)");
        // 合法边界：0/0 与 65535/65535
        LineNumberInfo z = LineNumberInfo.of(0, 0);
        check(z.startPc() == 0 && z.lineNumber() == 0, "0,0 legal");
        LineNumberInfo m = LineNumberInfo.of(65535, 65535);
        check(m.startPc() == 65535 && m.lineNumber() == 65535, "u2 max edge legal");
        // 负例：startPc
        try { LineNumberInfo.of(-1, 5); check(false, "neg startPc must IAE"); }
        catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("start pc"), "startPc IAE msg: " + e.getMessage());
        }
        // 负例：lineNumber 溢出
        try { LineNumberInfo.of(0, 65536); check(false, "line 65536 must IAE"); }
        catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("line number"), "line IAE msg: " + e.getMessage());
        }
        try { LineNumberInfo.of(65536, 1); check(false, "startPc 65536 must IAE"); }
        catch (IllegalArgumentException e) { check(true, "startPc 65536 IAE"); }
        try { LineNumberInfo.of(1, -1); check(false, "neg line must IAE"); }
        catch (IllegalArgumentException e) { check(true, "neg line IAE"); }
    }

    // ---- Part 4: 发射（cob.lineNumber）→ re-parse LNT 属性回读 ----
    static void testEmissionRoundtrip() {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("LNIT1"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), 0, c -> {
                    c.lineNumber(9);
                    c.nop();
                    c.lineNumber(12);
                    c.return_();
                }));
        CodeModel code = cf.parse(b).methods().get(0).code().get();
        var lnt = code.findAttribute(Attributes.lineNumberTable());
        check(lnt.isPresent(), "lineNumber emission produces LNT");
        List<LineNumberInfo> ls = lnt.get().lineNumbers();
        check(ls.size() == 2, "2 entries: " + ls.size());
        check(ls.get(0).lineNumber() == 9 && ls.get(0).startPc() == 0, "line 9 @pc 0");
        check(ls.get(1).lineNumber() == 12 && ls.get(1).startPc() == 1, "line 12 @pc 1");
        // LineNumber 伪指令（elementList）与属性表一致
        int pseudos = 0;
        for (CodeElement el : code.elementList()) if (el instanceof java.lang.classfile.instruction.LineNumber) pseudos++;
        check(pseudos == 2, "2 LineNumber pseudos match attribute");
    }

    // ---- Part 5: javac 真类产物 ----
    static void testRealClass() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] self = LineNumberInfoTest.class.getResourceAsStream("LineNumberInfoTest.class").readAllBytes();
        boolean found = false;
        for (MethodModel mm : cf.parse(self).methods()) {
            if (mm.methodName().stringValue().equals("main") && mm.code().isPresent()) {
                var lnt = mm.code().get().findAttribute(Attributes.lineNumberTable());
                check(lnt.isPresent(), "main has LNT (javac default)");
                List<LineNumberInfo> ls = lnt.get().lineNumbers();
                check(!ls.isEmpty(), "LNT non-empty");
                check(ls.get(0).startPc() == 0, "first entry pc 0");
                check(ls.stream().allMatch(i -> i.startPc() >= 0 && i.startPc() <= 65535), "all pc u2");
                check(ls.stream().allMatch(i -> i.lineNumber() >= 1), "all source lines >= 1");
                check(ls.stream().mapToInt(LineNumberInfo::lineNumber).min().getAsInt() >= 1, "min line >= 1");
                found = true;
            }
        }
        check(found, "main method found");
        check(cf.verify(self).isEmpty(), "real class verifies");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testBoundaries();
        testEmissionRoundtrip();
        testRealClass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

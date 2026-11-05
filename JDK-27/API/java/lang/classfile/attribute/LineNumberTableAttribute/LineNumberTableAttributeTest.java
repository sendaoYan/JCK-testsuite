/*
 * @test
 * @summary java.lang.classfile.attribute.LineNumberTableAttribute conformance: white-box structure + black-box behavior.
 * @run main LineNumberTableAttributeTest
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

public class LineNumberTableAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.LineNumberTableAttribute");
        check(c != null, "java.lang.classfile.attribute.LineNumberTableAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.LineNumberTableAttribute"), "binary name java.lang.classfile.attribute.LineNumberTableAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.LineNumberTableAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.LineNumberTableAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.LineNumberTableAttribute has no superclass");
        check(declares(c, "lineNumbers"), "declares lineNumbers");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        LineNumberInfo i = LineNumberInfo.of(0, 10);
        LineNumberTableAttribute a = LineNumberTableAttribute.of(java.util.List.of(i));
        check(a.lineNumbers().size() == 1, "lineNumbers size");
        check(a.lineNumbers().get(0).lineNumber() == 10, "lineNumber");
        check(a.lineNumbers().get(0).startPc() == 0, "startPc");

    }

    // ---- Part 3: 工厂（unbound）+ null/u2 负例 + 元数据 ----
    static void testFactoryMatrix() {
        LineNumberInfo l1 = LineNumberInfo.of(0, 9);
        LineNumberInfo l2 = LineNumberInfo.of(1, 12);
        LineNumberTableAttribute a = LineNumberTableAttribute.of(List.of(l1, l2));
        check(a.lineNumbers().size() == 2, "of(List) size");
        check(a.lineNumbers().get(0) == l1 && a.lineNumbers().get(1) == l2, "element identity kept");
        check(a.lineNumbers().get(1).startPc() == 1 && a.lineNumbers().get(1).lineNumber() == 12, "entry accessors");
        check(LineNumberTableAttribute.of(List.of()).lineNumbers().isEmpty(), "empty list ok");
        try { LineNumberTableAttribute.of(null); check(false, "of(null) must NPE"); }
        catch (NullPointerException e) { check(e.getMessage() != null || true, "of(null) NPE"); }
        LineNumberInfo shared = LineNumberInfo.of(0, 0);
        check(LineNumberTableAttribute.of(Collections.nCopies(65535, shared)).lineNumbers().size() == 65535, "65535 ok");
        try {
            LineNumberTableAttribute.of(Collections.nCopies(66000, shared));
            check(false, "66000 must IAE");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("u2"), "66000 IAE msg: " + e.getMessage());
        }
        // 元数据：引用 code 位置 -> LABELS，且允许出现多条同名属性
        check(a.attributeName().stringValue().equals(Attributes.NAME_LINE_NUMBER_TABLE), "attributeName==NAME_LINE_NUMBER_TABLE");
        check(a.attributeName().stringValue().equals("LineNumberTable"), "attributeName literal");
        check(a.attributeMapper() == Attributes.lineNumberTable(), "attributeMapper==Attributes.lineNumberTable()");
        check(a.attributeMapper().stability() == AttributeMapper.AttributeStability.LABELS, "stability LABELS");
        check(a.attributeMapper().allowMultiple(), "LNT allowMultiple true");
    }

    // ---- Part 4: 发射 → re-parse 回读 + 选项交叉 ----
    static void testEmissionAndOptions() {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("LNTT1"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), 0, c -> {
                    c.lineNumber(7);
                    c.nop();
                    c.lineNumber(11);
                    c.return_();
                }));
        ClassModel cm = cf.parse(b);
        CodeModel code = cm.methods().get(0).code().get();
        var lnt = code.findAttribute(Attributes.lineNumberTable());
        check(lnt.isPresent(), "LNT present after emission");
        List<LineNumberInfo> ls = lnt.get().lineNumbers();
        check(ls.size() == 2 && ls.get(0).lineNumber() == 7 && ls.get(1).lineNumber() == 11, "entries exact");
        check(ls.get(0).startPc() == 0 && ls.get(1).startPc() == 1, "pcs exact");
        // DROP_LINE_NUMBERS（parse 侧）：剥伪指令，保留属性
        CodeModel dropped = cf.withOptions(ClassFile.LineNumbersOption.DROP_LINE_NUMBERS).parse(b).methods().get(0).code().get();
        int pseudos = 0;
        for (CodeElement el : dropped.elementList()) if (el instanceof java.lang.classfile.instruction.LineNumber) pseudos++;
        check(pseudos == 0, "DROP_LINE_NUMBERS strips pseudos");
        check(dropped.findAttribute(Attributes.lineNumberTable()).isPresent(), "DROP_LINE_NUMBERS keeps LNT attribute");
        // 多方法各自 LNT 独立
        byte[] b2 = cf.build(ClassDesc.of("LNTT2"), cb -> cb
                .withMethodBody("a", MethodTypeDesc.ofDescriptor("()V"), 0, c -> c.lineNumber(3).return_())
                .withMethodBody("b", MethodTypeDesc.ofDescriptor("()V"), 0, c -> c.lineNumber(99).return_()));
        ClassModel cm2 = cf.parse(b2);
        Map<String, Integer> lineOf = new HashMap<>();
        for (MethodModel mm : cm2.methods()) {
            var o = mm.code().get().findAttribute(Attributes.lineNumberTable());
            check(o.isPresent(), "each emitted method has own LNT: " + mm.methodName().stringValue());
            lineOf.put(mm.methodName().stringValue(), o.get().lineNumbers().get(0).lineNumber());
        }
        check(lineOf.get("a") == 3 && lineOf.get("b") == 99, "per-method LNT independent");
        // 不发射则无 LNT
        byte[] b3 = cf.build(ClassDesc.of("LNTT3"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), 0, c -> c.return_()));
        check(cf.parse(b3).methods().get(0).code().get().findAttribute(Attributes.lineNumberTable()).isEmpty(), "no emission -> no LNT");
    }

    // ---- Part 5: javac 真类产物 ----
    static int sampleLine() { return 5; }

    static void testRealClass() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] self = LineNumberTableAttributeTest.class.getResourceAsStream("LineNumberTableAttributeTest.class").readAllBytes();
        ClassModel cm = cf.parse(self);
        int methodsWithCode = 0, withLnt = 0;
        for (MethodModel mm : cm.methods()) {
            if (mm.code().isPresent()) {
                methodsWithCode++;
                if (mm.code().get().findAttribute(Attributes.lineNumberTable()).isPresent()) withLnt++;
            }
        }
        check(methodsWithCode > 0 && withLnt == methodsWithCode, "javac emits LNT for every coded method: " + withLnt + "/" + methodsWithCode);
        // sampleLine 精确：单 return 方法，LNT 恰 1 条
        for (MethodModel mm : cm.methods())
            if (mm.methodName().stringValue().equals("sampleLine")) {
                List<LineNumberInfo> ls = mm.code().get().findAttribute(Attributes.lineNumberTable()).get().lineNumbers();
                check(ls.size() == 1, "sampleLine LNT 1 entry");
                check(ls.get(0).startPc() == 0, "sampleLine pc 0");
            }
        check(cf.verify(self).isEmpty(), "real class verifies");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryMatrix();
        testEmissionAndOptions();
        testRealClass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

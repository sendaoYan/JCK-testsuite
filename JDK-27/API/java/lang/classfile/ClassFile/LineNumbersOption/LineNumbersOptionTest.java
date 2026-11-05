/*
 * @test
 * @summary java.lang.classfile.ClassFile.LineNumbersOption conformance: white-box structure + black-box behavior.
 * @run main LineNumbersOptionTest
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

public class LineNumbersOptionTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassFile$LineNumbersOption");
        check(c != null, "java.lang.classfile.ClassFile.LineNumbersOption loads");
        check(c.getName().equals("java.lang.classfile.ClassFile$LineNumbersOption"), "binary name java.lang.classfile.ClassFile$LineNumbersOption");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassFile.LineNumbersOption is public");
        check(c.isEnum(), "java.lang.classfile.ClassFile.LineNumbersOption is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.ClassFile.LineNumbersOption superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 2, "2 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("PASS_LINE_NUMBERS", "DROP_LINE_NUMBERS")), "exact enum constant names");
        check(declares(c, "values"), "declares values");
        check(declares(c, "valueOf"), "declares valueOf");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(ClassFile.LineNumbersOption.PASS_LINE_NUMBERS != ClassFile.LineNumbersOption.DROP_LINE_NUMBERS, "two constants");
        check(ClassFile.LineNumbersOption.values().length == 2, "2 constants");
        ClassFile cf = ClassFile.of(ClassFile.LineNumbersOption.DROP_LINE_NUMBERS);
        check(cf != null, "usable as option");

    }

    // ---- Part 3: option semantics measured on JDK 27 ----
    static byte[] buildLnClass(ClassFile cf, String name) {
        return cf.build(ClassDesc.of(name), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, cob -> {
            Label s = cob.newLabel(), e = cob.newLabel();
            cob.labelBinding(s).return_();
            cob.labelBinding(e);
            cob.localVariable(1, "lv", ConstantDescs.CD_int, s, e);
            cob.lineNumber(9);
        }));
    }

    static int[] pseudoCounts(CodeModel code) {
        int lv = 0, ln = 0;
        for (CodeElement el : code.elementList()) {
            if (el instanceof LocalVariable) lv++;
            if (el instanceof LineNumber) ln++;
        }
        return new int[]{lv, ln};
    }

    static void testOptionSemantics() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] bytes = buildLnClass(cf, "LnEO1");

        // default parse == PASS_LINE_NUMBERS element stream
        CodeModel def = cf.parse(bytes).methods().get(0).code().get();
        int[] cDefault = pseudoCounts(def);
        check(cDefault[0] == 1 && cDefault[1] == 1, "default parse keeps LocalVariable + LineNumber");
        CodeModel pass = cf.withOptions(ClassFile.LineNumbersOption.PASS_LINE_NUMBERS).parse(bytes).methods().get(0).code().get();
        check(Arrays.equals(pseudoCounts(pass), cDefault), "PASS_LINE_NUMBERS identical pseudo counts");

        // DROP_LINE_NUMBERS: strips LineNumber pseudo only, LocalVariable untouched
        CodeModel drop = cf.withOptions(ClassFile.LineNumbersOption.DROP_LINE_NUMBERS).parse(bytes).methods().get(0).code().get();
        int[] cDrop = pseudoCounts(drop);
        check(cDrop[1] == 0, "DROP_LINE_NUMBERS strips LineNumber pseudo");
        check(cDrop[0] == 1, "DROP_LINE_NUMBERS keeps LocalVariable pseudo (governed by DebugElementsOption)");
        // no LabelTarget count change sanity: instructions still present
        int ins = 0;
        for (CodeElement el : drop.elementList()) if (el instanceof Instruction) ins++;
        check(ins >= 1, "instructions intact under DROP_LINE_NUMBERS");

        // raw attributes survive every parse-side state
        for (ClassFile.LineNumbersOption o : ClassFile.LineNumbersOption.values()) {
            CodeModel cm = cf.withOptions(o).parse(bytes).methods().get(0).code().get();
            check(cm.findAttribute(Attributes.lineNumberTable()).isPresent(), o + " keeps LNT attribute");
            check(cm.findAttribute(Attributes.localVariableTable()).isPresent(), o + " keeps LVT attribute");
        }

        // build side: LineNumbersOption does NOT suppress LNT writing (unlike DebugElementsOption which does)
        byte[] builtDrop = buildLnClass(cf.withOptions(ClassFile.LineNumbersOption.DROP_LINE_NUMBERS), "LnEO2");
        CodeModel bd = cf.parse(builtDrop).methods().get(0).code().get();
        check(bd.findAttribute(Attributes.lineNumberTable()).isPresent(),
                "build with DROP_LINE_NUMBERS still writes LNT attribute (parse-side-only option)");
        check(pseudoCounts(bd)[1] == 1, "LineNumber pseudo present after building with DROP_LINE_NUMBERS");
        LineNumberTableAttribute lnt = bd.findAttribute(Attributes.lineNumberTable()).get();
        check(lnt.lineNumbers().size() == 1 && lnt.lineNumbers().get(0).lineNumber() == 9,
                "LNT row survives build with DROP_LINE_NUMBERS with line 9");

        // LNT row precise readback under PASS state
        LineNumberTableAttribute lntDef = def.findAttribute(Attributes.lineNumberTable()).get();
        check(lntDef.lineNumbers().size() == 1, "default LNT one row");
        check(lntDef.lineNumbers().get(0).lineNumber() == 9, "default LNT line 9");
        check(lntDef.lineNumbers().get(0).startPc() >= 0, "default LNT startPc non-negative");

        // enum matrix
        check(ClassFile.LineNumbersOption.values()[0] == ClassFile.LineNumbersOption.PASS_LINE_NUMBERS,
                "ordinal 0 is PASS_LINE_NUMBERS (the default semantic)");
        for (ClassFile.LineNumbersOption o : ClassFile.LineNumbersOption.values())
            check(ClassFile.LineNumbersOption.valueOf(o.name()) == o, "valueOf round-trip " + o);
        check(ClassFile.LineNumbersOption.PASS_LINE_NUMBERS.compareTo(ClassFile.LineNumbersOption.DROP_LINE_NUMBERS) < 0,
                "PASS_LINE_NUMBERS < DROP_LINE_NUMBERS by declaration order");
        check(cf.withOptions(ClassFile.LineNumbersOption.DROP_LINE_NUMBERS) != cf, "withOptions returns new instance");
        // orthogonal combination with DebugElementsOption: both pseudo families stripped
        CodeModel both = cf.withOptions(ClassFile.LineNumbersOption.DROP_LINE_NUMBERS,
                                        ClassFile.DebugElementsOption.DROP_DEBUG)
                          .parse(bytes).methods().get(0).code().get();
        int[] cBoth = pseudoCounts(both);
        check(cBoth[0] == 0 && cBoth[1] == 0, "DROP_LINE_NUMBERS + DROP_DEBUG strips both pseudo families");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testOptionSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

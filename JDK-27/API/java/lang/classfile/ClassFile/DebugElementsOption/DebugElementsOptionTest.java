/*
 * @test
 * @summary java.lang.classfile.ClassFile.DebugElementsOption conformance: white-box structure + black-box behavior.
 * @run main DebugElementsOptionTest
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

public class DebugElementsOptionTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassFile$DebugElementsOption");
        check(c != null, "java.lang.classfile.ClassFile.DebugElementsOption loads");
        check(c.getName().equals("java.lang.classfile.ClassFile$DebugElementsOption"), "binary name java.lang.classfile.ClassFile$DebugElementsOption");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassFile.DebugElementsOption is public");
        check(c.isEnum(), "java.lang.classfile.ClassFile.DebugElementsOption is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.ClassFile.DebugElementsOption superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 2, "2 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("PASS_DEBUG", "DROP_DEBUG")), "exact enum constant names");
        check(declares(c, "values"), "declares values");
        check(declares(c, "valueOf"), "declares valueOf");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(ClassFile.DebugElementsOption.PASS_DEBUG != ClassFile.DebugElementsOption.DROP_DEBUG, "two constants");
        check(ClassFile.DebugElementsOption.values().length == 2, "2 constants");
        check(ClassFile.DebugElementsOption.valueOf("DROP_DEBUG") == ClassFile.DebugElementsOption.DROP_DEBUG, "valueOf");
        ClassFile cf = ClassFile.of(ClassFile.DebugElementsOption.DROP_DEBUG);
        check(cf != null, "usable as option");

    }

    // ---- Part 3: option semantics measured on JDK 27 ----
    // Emits localVariable + localVariableType + characterRange + lineNumber pseudos,
    // then re-parses under each DebugElementsOption state and counts per pseudo type.
    static byte[] buildDebugClass(ClassFile cf, String name) {
        return cf.build(ClassDesc.of(name), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, cob -> {
            Label s = cob.newLabel(), e = cob.newLabel();
            cob.labelBinding(s).return_();
            cob.labelBinding(e);
            cob.localVariable(1, "lv", ConstantDescs.CD_int, s, e);
            cob.localVariableType(1, "lvt", Signature.of(ConstantDescs.CD_int), s, e);
            cob.characterRange(s, e, 1, 2, 3);
            cob.lineNumber(9);
        }));
    }

    static int[] pseudoCounts(CodeModel code) {
        int lv = 0, lvt = 0, cr = 0, ln = 0;
        for (CodeElement el : code.elementList()) {
            if (el instanceof LocalVariable) lv++;
            if (el instanceof LocalVariableType) lvt++;
            if (el instanceof CharacterRange) cr++;
            if (el instanceof LineNumber) ln++;
        }
        return new int[]{lv, lvt, cr, ln};
    }

    static void testOptionSemantics() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] bytes = buildDebugClass(cf, "DbgEO1");

        // default (no option) == PASS_DEBUG element stream
        CodeModel def = cf.parse(bytes).methods().get(0).code().get();
        int[] cDefault = pseudoCounts(def);
        check(cDefault[0] == 1 && cDefault[1] == 1 && cDefault[2] == 1 && cDefault[3] == 1,
                "default parse keeps all debug pseudos: " + Arrays.toString(cDefault));

        CodeModel pass = cf.withOptions(ClassFile.DebugElementsOption.PASS_DEBUG).parse(bytes).methods().get(0).code().get();
        check(Arrays.equals(pseudoCounts(pass), cDefault), "PASS_DEBUG parse identical pseudo counts");

        // DROP_DEBUG: strips LocalVariable/LocalVariableType/CharacterRange pseudos, keeps LineNumber
        CodeModel drop = cf.withOptions(ClassFile.DebugElementsOption.DROP_DEBUG).parse(bytes).methods().get(0).code().get();
        int[] cDrop = pseudoCounts(drop);
        check(cDrop[0] == 0, "DROP_DEBUG strips LocalVariable pseudo");
        check(cDrop[1] == 0, "DROP_DEBUG strips LocalVariableType pseudo");
        check(cDrop[2] == 0, "DROP_DEBUG strips CharacterRange pseudo");
        check(cDrop[3] == 1, "DROP_DEBUG keeps LineNumber pseudo (line numbers governed by LineNumbersOption)");

        // attributes survive all parse-side option states (options affect the element model, not raw attributes)
        for (ClassFile.DebugElementsOption o : ClassFile.DebugElementsOption.values()) {
            CodeModel cm = cf.withOptions(o).parse(bytes).methods().get(0).code().get();
            check(cm.findAttribute(Attributes.localVariableTable()).isPresent(), o + " keeps LVT attribute");
            check(cm.findAttribute(Attributes.localVariableTypeTable()).isPresent(), o + " keeps LocalVariableTypeTable attribute");
            check(cm.findAttribute(Attributes.characterRangeTable()).isPresent(), o + " keeps CharacterRangeTable attribute");
            check(cm.findAttribute(Attributes.lineNumberTable()).isPresent(), o + " keeps LNT attribute");
        }

        // build side: with DROP_DEBUG the debug attributes are not written at all, but LNT still is
        byte[] builtDrop = buildDebugClass(cf.withOptions(ClassFile.DebugElementsOption.DROP_DEBUG), "DbgEO2");
        CodeModel bd = cf.parse(builtDrop).methods().get(0).code().get();
        check(!bd.findAttribute(Attributes.localVariableTable()).isPresent(), "build with DROP_DEBUG omits LVT attribute");
        check(!bd.findAttribute(Attributes.localVariableTypeTable()).isPresent(), "build with DROP_DEBUG omits LocalVariableTypeTable attribute");
        check(!bd.findAttribute(Attributes.characterRangeTable()).isPresent(), "build with DROP_DEBUG omits CharacterRangeTable attribute");
        check(bd.findAttribute(Attributes.lineNumberTable()).isPresent(), "build with DROP_DEBUG still writes LNT attribute");
        int[] cBuilt = pseudoCounts(bd);
        check(cBuilt[0] == 0 && cBuilt[1] == 0 && cBuilt[2] == 0 && cBuilt[3] == 1,
                "build with DROP_DEBUG emits no debug pseudos but keeps line numbers");
        byte[] builtPass = buildDebugClass(cf.withOptions(ClassFile.DebugElementsOption.PASS_DEBUG), "DbgEO3");
        CodeModel bp = cf.parse(builtPass).methods().get(0).code().get();
        check(bp.findAttribute(Attributes.localVariableTable()).isPresent(), "build with PASS_DEBUG writes LVT attribute");
        check(Arrays.equals(pseudoCounts(bp), cDefault), "build with PASS_DEBUG keeps all pseudos");

        // LVT attribute content precise readback under PASS state
        LocalVariableTableAttribute lvt = def.findAttribute(Attributes.localVariableTable()).get();
        check(lvt.localVariables().size() == 1, "LVT one row");
        LocalVariableInfo info = lvt.localVariables().get(0);
        check(info.slot() == 1 && info.name().stringValue().equals("lv") && info.typeSymbol().descriptorString().equals("I"),
                "LVT row slot/name/type");
        LineNumberTableAttribute lnt = def.findAttribute(Attributes.lineNumberTable()).get();
        check(lnt.lineNumbers().get(0).lineNumber() == 9, "LNT row line 9");

        // enum matrix
        check(ClassFile.DebugElementsOption.values()[0] == ClassFile.DebugElementsOption.PASS_DEBUG,
                "ordinal 0 is PASS_DEBUG (the default semantic)");
        for (ClassFile.DebugElementsOption o : ClassFile.DebugElementsOption.values())
            check(ClassFile.DebugElementsOption.valueOf(o.name()) == o, "valueOf round-trip " + o);
        check(ClassFile.DebugElementsOption.PASS_DEBUG.compareTo(ClassFile.DebugElementsOption.DROP_DEBUG) < 0,
                "PASS_DEBUG < DROP_DEBUG by declaration order");
        check(cf.withOptions(ClassFile.DebugElementsOption.DROP_DEBUG) != cf, "withOptions returns new instance");
        check(cf.withOptions(ClassFile.DebugElementsOption.DROP_DEBUG, ClassFile.DebugElementsOption.PASS_DEBUG) != cf,
                "last-wins combination accepted, new instance");
        // chained combination really applies the last DebugElementsOption state (last wins)
        ClassFile chained = cf.withOptions(ClassFile.DebugElementsOption.DROP_DEBUG).withOptions(ClassFile.DebugElementsOption.PASS_DEBUG);
        check(Arrays.equals(pseudoCounts(chained.parse(bytes).methods().get(0).code().get()), cDefault),
                "chained PASS_DEBUG overrides earlier DROP_DEBUG");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testOptionSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

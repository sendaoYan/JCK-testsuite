/*
 * @test
 * @summary java.lang.classfile.ClassFile.DeadLabelsOption conformance: white-box structure + black-box behavior.
 * @run main DeadLabelsOptionTest
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

public class DeadLabelsOptionTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassFile$DeadLabelsOption");
        check(c != null, "java.lang.classfile.ClassFile.DeadLabelsOption loads");
        check(c.getName().equals("java.lang.classfile.ClassFile$DeadLabelsOption"), "binary name java.lang.classfile.ClassFile$DeadLabelsOption");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassFile.DeadLabelsOption is public");
        check(c.isEnum(), "java.lang.classfile.ClassFile.DeadLabelsOption is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.ClassFile.DeadLabelsOption superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 2, "2 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("FAIL_ON_DEAD_LABELS", "DROP_DEAD_LABELS")), "exact enum constant names");
        check(declares(c, "values"), "declares values");
        check(declares(c, "valueOf"), "declares valueOf");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(ClassFile.DeadLabelsOption.FAIL_ON_DEAD_LABELS != ClassFile.DeadLabelsOption.DROP_DEAD_LABELS, "two constants");
        check(ClassFile.DeadLabelsOption.values().length == 2, "2 constants");
        ClassFile cf = ClassFile.of(ClassFile.DeadLabelsOption.DROP_DEAD_LABELS);
        check(cf != null, "usable as option");

    }

    // ---- Part 3: option semantics measured on JDK 27 ----
    // Each fragment references a never-bound label from a debug pseudo-instruction (JDK-8361908 fragments)
    interface Fragment { void emit(CodeBuilder cob); }
    static java.util.List<Fragment> deadLabelFragments() {
        return java.util.List.of(
                cob -> cob.exceptionCatchAll(cob.newLabel(), cob.startLabel(), cob.endLabel()),
                cob -> cob.exceptionCatchAll(cob.startLabel(), cob.newLabel(), cob.endLabel()),
                cob -> cob.exceptionCatchAll(cob.startLabel(), cob.endLabel(), cob.newLabel()),
                cob -> cob.localVariable(0, "v", ConstantDescs.CD_int, cob.startLabel(), cob.newLabel()),
                cob -> cob.localVariable(0, "v", ConstantDescs.CD_int, cob.newLabel(), cob.endLabel()),
                cob -> cob.localVariableType(0, "v", Signature.of(ConstantDescs.CD_int), cob.startLabel(), cob.newLabel()),
                cob -> cob.localVariableType(0, "v", Signature.of(ConstantDescs.CD_int), cob.newLabel(), cob.endLabel()),
                cob -> cob.characterRange(cob.newLabel(), cob.endLabel(), 0, 0, 0));
    }

    static void buildWith(ClassFile cf, String name, java.util.List<Fragment> frags) {
        cf.build(ClassDesc.of(name), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, 0, cob -> {
            cob.return_();
            for (Fragment f : frags) f.emit(cob);
        }));
    }

    static void testOptionSemantics() throws Exception {
        ClassFile cf = ClassFile.of();

        // enum matrix
        check(ClassFile.DeadLabelsOption.values()[0] == ClassFile.DeadLabelsOption.FAIL_ON_DEAD_LABELS,
                "ordinal 0 is FAIL_ON_DEAD_LABELS (the default semantic)");
        for (ClassFile.DeadLabelsOption o : ClassFile.DeadLabelsOption.values())
            check(ClassFile.DeadLabelsOption.valueOf(o.name()) == o, "valueOf round-trip " + o);
        check(cf.withOptions(ClassFile.DeadLabelsOption.DROP_DEAD_LABELS) != cf, "withOptions returns new instance");

        // FAIL_ON_DEAD_LABELS (default): every dead-label fragment aborts the build with IAE
        int fragIndex = 0;
        for (Fragment f : deadLabelFragments()) {
            boolean threw = false;
            String msg = null;
            try {
                final int idx = fragIndex;
                cf.build(ClassDesc.of("DloF" + idx), cb -> cb.withMethodBody("m",
                        ConstantDescs.MTD_void, 0, cob -> { cob.return_(); f.emit(cob); }));
            } catch (IllegalArgumentException iae) {
                threw = true;
                msg = iae.getMessage();
            }
            check(threw, "FAIL_ON_DEAD_LABELS rejects fragment " + fragIndex);
            check(msg != null, "fragment " + fragIndex + " failure message non-null");
            fragIndex++;
        }
        // precise message for the exception-handler shape
        try {
            cf.build(ClassDesc.of("DloFExact"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, 0, cob -> {
                cob.return_();
                cob.exceptionCatchAll(cob.newLabel(), cob.startLabel(), cob.endLabel());
            }));
            check(false, "dead handler label must fail");
        } catch (IllegalArgumentException iae) {
            check(iae.getMessage() != null && iae.getMessage().contains("Unbound label in exception handler"),
                    "exact unbound-handler message: " + iae.getMessage());
        }

        // DROP_DEAD_LABELS: the same fragments build cleanly and the affected metadata is emptied
        ClassFile drop = ClassFile.of(ClassFile.DeadLabelsOption.DROP_DEAD_LABELS);
        byte[] bytes = null;
        try {
            bytes = drop.build(ClassDesc.of("DloD1"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, 0, cob -> {
                cob.return_();
                for (Fragment f : deadLabelFragments()) f.emit(cob);
            }));
            check(true, "DROP_DEAD_LABELS builds with all dead-label fragments");
        } catch (IllegalArgumentException iae) {
            check(false, "DROP_DEAD_LABELS must tolerate dead labels: " + iae.getMessage());
        }
        CodeModel code = drop.parse(bytes).methods().get(0).code().get();
        check(code.exceptionHandlers().isEmpty(), "dead exception handlers dropped");
        check(code.findAttribute(Attributes.localVariableTable()).isPresent(), "LVT attribute still written");
        check(code.findAttribute(Attributes.localVariableTable()).get().localVariables().isEmpty(),
                "LVT rows referencing dead labels dropped");
        check(code.findAttribute(Attributes.localVariableTypeTable()).isPresent(), "LVTy attribute still written");
        check(code.findAttribute(Attributes.localVariableTypeTable()).get().localVariableTypes().isEmpty(),
                "LVTy rows referencing dead labels dropped");
        check(code.findAttribute(Attributes.characterRangeTable()).isPresent(), "CRT attribute still written");
        check(code.findAttribute(Attributes.characterRangeTable()).get().characterRangeTable().isEmpty(),
                "CRT rows referencing dead labels dropped");
        check(cf.verify(bytes).isEmpty(), "DROP_DEAD_LABELS product verifies clean");

        // mixed live/dead handlers: only the live one survives
        byte[] mixed = drop.build(ClassDesc.of("DloD2"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, 0, cob -> {
            cob.return_();
            Label l = cob.newBoundLabel();
            cob.pop().return_();
            cob.exceptionCatch(cob.startLabel(), l, l, java.util.Optional.empty());
            cob.exceptionCatch(cob.newLabel(), l, l, ConstantDescs.CD_Exception);
        }));
        CodeModel mixedCode = drop.parse(mixed).methods().get(0).code().get();
        check(mixedCode.exceptionHandlers().size() == 1, "mixed case keeps exactly the live handler");
        check(mixedCode.exceptionHandlers().get(0).catchType().isEmpty(), "live handler keeps catchType=any");

        // a dangling branch target is NOT rescuable: both option states fail
        for (ClassFile.DeadLabelsOption o : ClassFile.DeadLabelsOption.values()) {
            boolean threw = false;
            try {
                cf.withOptions(o).build(ClassDesc.of("DloX" + o.ordinal()), cb -> cb.withMethodBody("m",
                        ConstantDescs.MTD_void, 0, cob -> {
                            Label dead = cob.newLabel();
                            cob.goto_(dead);
                            cob.return_();
                        }));
            } catch (IllegalArgumentException iae) {
                threw = iae.getMessage() != null && iae.getMessage().contains("branch target out of bytecode range");
            }
            check(threw, o + " rejects branch to never-bound label");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testOptionSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

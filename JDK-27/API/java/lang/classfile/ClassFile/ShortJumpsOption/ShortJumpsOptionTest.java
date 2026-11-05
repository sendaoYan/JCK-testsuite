/*
 * @test
 * @summary java.lang.classfile.ClassFile.ShortJumpsOption conformance: white-box structure + black-box behavior.
 * @run main ShortJumpsOptionTest
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

public class ShortJumpsOptionTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassFile$ShortJumpsOption");
        check(c != null, "java.lang.classfile.ClassFile.ShortJumpsOption loads");
        check(c.getName().equals("java.lang.classfile.ClassFile$ShortJumpsOption"), "binary name java.lang.classfile.ClassFile$ShortJumpsOption");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassFile.ShortJumpsOption is public");
        check(c.isEnum(), "java.lang.classfile.ClassFile.ShortJumpsOption is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.ClassFile.ShortJumpsOption superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 2, "2 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("FIX_SHORT_JUMPS", "FAIL_ON_SHORT_JUMPS")), "exact enum constant names");
        check(declares(c, "values"), "declares values");
        check(declares(c, "valueOf"), "declares valueOf");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(ClassFile.ShortJumpsOption.FIX_SHORT_JUMPS != ClassFile.ShortJumpsOption.FAIL_ON_SHORT_JUMPS, "two constants");
        check(ClassFile.ShortJumpsOption.values().length == 2, "2 constants");
        ClassFile cf = ClassFile.of(ClassFile.ShortJumpsOption.FIX_SHORT_JUMPS);
        check(cf != null, "usable as option");

    }

    // ---- Part 3: option semantics measured on JDK 27 ----
    // backward jump over n instructions: no dead code, jump distance = n + jump size
    static byte[] buildBackwardJump(ClassFile cf, String name, int nops) {
        return cf.build(ClassDesc.of(name), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, 0, cob -> {
            Label l = cob.newLabel();
            cob.labelBinding(l);
            for (int i = 0; i < nops; i++) cob.nop();
            cob.goto_(l);
        }));
    }

    static java.util.List<Opcode> ops(ClassFile cf, byte[] bytes) {
        java.util.List<Opcode> r = new java.util.ArrayList<>();
        for (CodeElement el : cf.parse(bytes).methods().get(0).code().get().elementList())
            if (el instanceof Instruction i) r.add(i.opcode());
        return r;
    }

    static void testOptionSemantics() throws Exception {
        ClassFile cf = ClassFile.of();

        // enum matrix
        check(ClassFile.ShortJumpsOption.values()[0] == ClassFile.ShortJumpsOption.FIX_SHORT_JUMPS,
                "ordinal 0 is FIX_SHORT_JUMPS (the default semantic)");
        for (ClassFile.ShortJumpsOption o : ClassFile.ShortJumpsOption.values())
            check(ClassFile.ShortJumpsOption.valueOf(o.name()) == o, "valueOf round-trip " + o);
        check(cf.withOptions(ClassFile.ShortJumpsOption.FAIL_ON_SHORT_JUMPS) != cf,
                "withOptions returns new instance");

        // short backward jump fits the 16-bit signed offset: identical product under both options
        for (ClassFile.ShortJumpsOption o : ClassFile.ShortJumpsOption.values()) {
            byte[] bytes = buildBackwardJump(cf.withOptions(o), "SjoS" + o.ordinal(), 1);
            java.util.List<Opcode> opcodes = ops(cf, bytes);
            check(opcodes.equals(java.util.List.of(Opcode.NOP, Opcode.GOTO)),
                    o + " short jump stays GOTO: " + opcodes);
        }

        // long backward jump (40000 nops): FIX silently widens GOTO to GOTO_W
        byte[] fixed = buildBackwardJump(cf, "SjoL1", 40000);
        java.util.List<Opcode> fixedOps = ops(cf, fixed);
        check(fixedOps.size() == 40001, "40000 NOPs + one jump instruction: " + fixedOps.size());
        check(fixedOps.get(0) == Opcode.NOP, "first instruction NOP");
        check(fixedOps.get(fixedOps.size() - 1) == Opcode.GOTO_W,
                "FIX_SHORT_JUMPS auto-widens to GOTO_W: " + fixedOps.get(fixedOps.size() - 1));
        CodeAttribute ca = (CodeAttribute) cf.parse(fixed).methods().get(0).code().get();
        check(ca.codeLength() == 40005, "codeLength 40000 NOPs + 5-byte GOTO_W: " + ca.codeLength());
        check(cf.verify(fixed).isEmpty(), "widened product verifies clean");

        // same input under FAIL_ON_SHORT_JUMPS: build aborted with the overflow error
        boolean threw = false;
        String msg = null;
        try {
            buildBackwardJump(cf.withOptions(ClassFile.ShortJumpsOption.FAIL_ON_SHORT_JUMPS), "SjoL2", 40000);
        } catch (IllegalArgumentException iae) {
            threw = true;
            msg = iae.getMessage();
        }
        check(threw, "FAIL_ON_SHORT_JUMPS throws IllegalArgumentException on long jump");
        check(msg != null && msg.contains("Label target offset overflow"), "overflow message: " + msg);

        // boundary still inside the 16-bit signed range passes FAIL_ON_SHORT_JUMPS unchanged
        byte[] mid = buildBackwardJump(cf.withOptions(ClassFile.ShortJumpsOption.FAIL_ON_SHORT_JUMPS), "SjoM", 30000);
        java.util.List<Opcode> midOps = ops(cf, mid);
        check(midOps.size() == 30001, "30000-nop build under FAIL_ON_SHORT_JUMPS");
        check(midOps.get(midOps.size() - 1) == Opcode.GOTO, "short-enough jump stays GOTO under FAIL_ON_SHORT_JUMPS");

        // explicit goto_w emission exists independent of the option
        byte[] explicit = cf.build(ClassDesc.of("SjoW"), cb -> cb.withMethodBody("m",
                ConstantDescs.MTD_void, 0, cob -> {
                    Label l = cob.newLabel();
                    cob.goto_w(l);
                    cob.labelBinding(l);
                    cob.return_();
                }));
        java.util.List<Opcode> explicitOps = ops(cf, explicit);
        check(explicitOps.contains(Opcode.GOTO_W), "explicit goto_w emitted: " + explicitOps);

        // transform path composes with FIX: rebuilding a long-jump class stays buildable
        ClassModel longM = cf.parse(fixed);
        byte[] tr = cf.transformClass(longM, ClassTransform.ACCEPT_ALL);
        java.util.List<Opcode> trOps = ops(cf, tr);
        check(trOps.get(trOps.size() - 1) == Opcode.GOTO_W, "transform preserves widened GOTO_W");
        check(cf.verify(tr).isEmpty(), "transformed long-jump class verifies clean");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testOptionSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

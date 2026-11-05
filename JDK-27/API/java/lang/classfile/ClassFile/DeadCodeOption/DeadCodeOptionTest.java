/*
 * @test
 * @summary java.lang.classfile.ClassFile.DeadCodeOption conformance: white-box structure + black-box behavior.
 * @run main DeadCodeOptionTest
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

public class DeadCodeOptionTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassFile$DeadCodeOption");
        check(c != null, "java.lang.classfile.ClassFile.DeadCodeOption loads");
        check(c.getName().equals("java.lang.classfile.ClassFile$DeadCodeOption"), "binary name java.lang.classfile.ClassFile$DeadCodeOption");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassFile.DeadCodeOption is public");
        check(c.isEnum(), "java.lang.classfile.ClassFile.DeadCodeOption is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.ClassFile.DeadCodeOption superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 2, "2 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("PATCH_DEAD_CODE", "KEEP_DEAD_CODE")), "exact enum constant names");
        check(declares(c, "values"), "declares values");
        check(declares(c, "valueOf"), "declares valueOf");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(ClassFile.DeadCodeOption.PATCH_DEAD_CODE != ClassFile.DeadCodeOption.KEEP_DEAD_CODE, "two constants");
        check(ClassFile.DeadCodeOption.values().length == 2, "2 constants");
        ClassFile cf = ClassFile.of(ClassFile.DeadCodeOption.KEEP_DEAD_CODE);
        check(cf != null, "usable as option");

    }

    // ---- Part 3: option semantics measured on JDK 27 ----
    static java.util.List<Opcode> ops(ClassFile cf, byte[] bytes) {
        java.util.List<Opcode> r = new java.util.ArrayList<>();
        for (CodeElement el : cf.parse(bytes).methods().get(0).code().get().elementList())
            if (el instanceof Instruction i) r.add(i.opcode());
        return r;
    }

    static boolean hasSmt(ClassFile cf, byte[] bytes) {
        return cf.parse(bytes).methods().get(0).code().get()
                .findAttribute(Attributes.stackMapTable()).isPresent();
    }

    static void testOptionSemantics() throws Exception {
        ClassFile cf = ClassFile.of();

        // enum matrix
        check(ClassFile.DeadCodeOption.values()[0] == ClassFile.DeadCodeOption.PATCH_DEAD_CODE,
                "ordinal 0 is PATCH_DEAD_CODE (the default semantic)");
        for (ClassFile.DeadCodeOption o : ClassFile.DeadCodeOption.values())
            check(ClassFile.DeadCodeOption.valueOf(o.name()) == o, "valueOf round-trip " + o);
        check(cf.withOptions(ClassFile.DeadCodeOption.KEEP_DEAD_CODE) != cf, "withOptions returns new instance");

        // direct build of two sequential returns (second is unreachable):
        // PATCH rewrites the dead tail so a stack map frame can be generated
        byte[] patched = cf.build(ClassDesc.of("DcoT1"), cb -> cb.withMethodBody("twoReturns",
                ConstantDescs.MTD_void, 0, cob -> cob.return_().return_()));
        java.util.List<Opcode> patchedOps = ops(cf, patched);
        check(patchedOps.get(0) == Opcode.RETURN, "PATCH keeps the reachable RETURN first");
        check(patchedOps.size() == 2, "PATCH keeps code length, one instruction rewritten: " + patchedOps);
        check(hasSmt(cf, patched), "PATCH build generates StackMapTable");
        check(cf.verify(patched).isEmpty(), "PATCH build verifies clean");

        // KEEP_DEAD_CODE: stack map generation for dead code fails at build time
        boolean threw = false;
        String msg = null;
        try {
            cf.withOptions(ClassFile.DeadCodeOption.KEEP_DEAD_CODE).build(ClassDesc.of("DcoT2"),
                    cb -> cb.withMethodBody("twoReturns", ConstantDescs.MTD_void, 0, cob -> cob.return_().return_()));
        } catch (IllegalArgumentException iae) {
            threw = true;
            msg = iae.getMessage();
        }
        check(threw, "KEEP_DEAD_CODE build of dead code throws IllegalArgumentException");
        check(msg != null && msg.contains("Unable to generate stack map frame for dead code at bytecode offset 1"),
                "KEEP_DEAD_CODE failure message: " + (msg == null ? "null" : msg.split("\n")[0]));

        // KEEP + DROP_STACK_MAPS: build succeeds because no frames are generated, dead RETURN preserved verbatim
        ClassFile keepDrop = ClassFile.of(ClassFile.StackMapsOption.DROP_STACK_MAPS, ClassFile.DeadCodeOption.KEEP_DEAD_CODE);
        byte[] kept = keepDrop.build(ClassDesc.of("DcoT3"), cb -> cb.withMethodBody("twoReturns",
                ConstantDescs.MTD_void, 0, cob -> cob.return_().return_()));
        java.util.List<Opcode> keptOps = ops(cf, kept);
        check(keptOps.equals(java.util.List.of(Opcode.RETURN, Opcode.RETURN)),
                "KEEP + DROP_STACK_MAPS preserves both RETURNs verbatim: " + keptOps);
        check(!hasSmt(cf, kept), "KEEP + DROP_STACK_MAPS emits no StackMapTable");
        check(!cf.verify(kept).isEmpty(), "missing frame for dead code is flagged by verify");

        // replay rebuild (StackMapsTest pattern): default PATCH rebuild succeeds and verifies clean;
        // KEEP rebuild hits the same dead-code frame failure
        ClassModel keptM = cf.parse(kept);
        byte[] replayPatch = cf.build(keptM.thisClass().asSymbol(), cb -> keptM.forEach(cb));
        check(ops(cf, replayPatch).size() == 2, "PATCH replay of dead-code class rebuilds");
        check(cf.verify(replayPatch).isEmpty(), "PATCH replay verifies clean");
        threw = false;
        try {
            cf.withOptions(ClassFile.DeadCodeOption.KEEP_DEAD_CODE)
                    .build(keptM.thisClass().asSymbol(), cb -> keptM.forEach(cb));
        } catch (IllegalArgumentException iae) {
            threw = true;
            msg = iae.getMessage();
        }
        check(threw, "KEEP replay of dead-code class throws");
        check(msg != null && msg.contains("Unable to generate stack map frame for dead code"),
                "KEEP replay failure message: " + (msg == null ? "null" : msg.split("\n")[0]));

        // chained options combine: KEEP after PATCH still fails, PATCH after KEEP succeeds
        ClassFile chainedFail = cf.withOptions(ClassFile.DeadCodeOption.PATCH_DEAD_CODE)
                                  .withOptions(ClassFile.DeadCodeOption.KEEP_DEAD_CODE);
        threw = false;
        try {
            chainedFail.build(ClassDesc.of("DcoT4"), cb -> cb.withMethodBody("twoReturns",
                    ConstantDescs.MTD_void, 0, cob -> cob.return_().return_()));
        } catch (IllegalArgumentException iae) { threw = true; }
        check(threw, "chained last-wins: KEEP after PATCH still fails");
        ClassFile chainedOk = cf.withOptions(ClassFile.DeadCodeOption.KEEP_DEAD_CODE)
                                .withOptions(ClassFile.StackMapsOption.DROP_STACK_MAPS);
        check(chainedOk.build(ClassDesc.of("DcoT5"), cb -> cb.withMethodBody("twoReturns",
                ConstantDescs.MTD_void, 0, cob -> cob.return_().return_())).length > 8,
                "KEEP combines with DROP_STACK_MAPS to allow dead code");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testOptionSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

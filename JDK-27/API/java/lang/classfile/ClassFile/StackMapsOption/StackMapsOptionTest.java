/*
 * @test
 * @summary java.lang.classfile.ClassFile.StackMapsOption conformance: white-box structure + black-box behavior.
 * @run main StackMapsOptionTest
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

public class StackMapsOptionTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassFile$StackMapsOption");
        check(c != null, "java.lang.classfile.ClassFile.StackMapsOption loads");
        check(c.getName().equals("java.lang.classfile.ClassFile$StackMapsOption"), "binary name java.lang.classfile.ClassFile$StackMapsOption");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassFile.StackMapsOption is public");
        check(c.isEnum(), "java.lang.classfile.ClassFile.StackMapsOption is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.ClassFile.StackMapsOption superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 3, "3 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("STACK_MAPS_WHEN_REQUIRED", "GENERATE_STACK_MAPS", "DROP_STACK_MAPS")), "exact enum constant names");
        check(declares(c, "values"), "declares values");
        check(declares(c, "valueOf"), "declares valueOf");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(ClassFile.StackMapsOption.values().length == 3, "3 constants");
        check(ClassFile.StackMapsOption.valueOf("GENERATE_STACK_MAPS") == ClassFile.StackMapsOption.GENERATE_STACK_MAPS, "valueOf");
        ClassFile cf = ClassFile.of(ClassFile.StackMapsOption.GENERATE_STACK_MAPS);
        check(cf != null, "usable as option");

    }

    // ---- Part 3: option semantics measured on JDK 27 ----
    static MethodTypeDesc mtdII() { return MethodTypeDesc.ofDescriptor("(I)I"); }

    // straight-line body (no branches): stack maps never required
    static byte[] buildStraight(ClassFile cf, String name) {
        return cf.build(ClassDesc.of(name), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC,
                CodeBuilder::return_));
    }

    // branching body (if_icmpge + two returns): stack maps required at v51+
    static byte[] buildBranch(ClassFile cf, String name) {
        return cf.build(ClassDesc.of(name), cb -> cb.withMethodBody("m", mtdII(), ClassFile.ACC_STATIC,
                cob -> {
                    Label l = cob.newLabel();
                    cob.iconst_0().iload(0).if_icmpge(l);
                    cob.iconst_1().ireturn();
                    cob.labelBinding(l).iconst_m1().ireturn();
                }));
    }

    static boolean hasSmt(ClassModel cm) {
        return cm.methods().get(0).code().get().findAttribute(Attributes.stackMapTable()).isPresent();
    }

    static void testOptionSemantics() throws Exception {
        ClassFile cf = ClassFile.of();

        // build matrix at latest version (71): WHEN_REQUIRED == GENERATE for both shapes; DROP omits always
        byte[] straight = buildStraight(cf, "SmEO1");
        byte[] branch = buildBranch(cf, "SmEO2");
        check(hasSmt(cf.parse(branch)), "branch build emits StackMapTable at v71");
        for (ClassFile.StackMapsOption o : ClassFile.StackMapsOption.values()) {
            ClassFile c2 = cf.withOptions(o);
            check(!hasSmt(cf.parse(buildStraight(c2, "SmS" + o.ordinal()))), o + ": straight-line build has no SMT");
            boolean branchSmt = hasSmt(cf.parse(buildBranch(c2, "SmB" + o.ordinal())));
            check(branchSmt == (o != ClassFile.StackMapsOption.DROP_STACK_MAPS),
                    o + ": branch build SMT presence " + branchSmt);
        }

        // WHEN_REQUIRED vs GENERATE distinction is observable at pre-50 class versions:
        // WHEN_REQUIRED omits SMT (not required by JVMS at v48), GENERATE emits it anyway
        byte[] v48wr = cf.withOptions(ClassFile.StackMapsOption.STACK_MAPS_WHEN_REQUIRED)
                .build(ClassDesc.of("SmEOV1"), cb -> cb.withVersion(ClassFile.JAVA_4_VERSION, 0)
                        .withMethodBody("m", mtdII(), ClassFile.ACC_STATIC,
                                cob -> {
                                    Label l = cob.newLabel();
                                    cob.iconst_0().iload(0).if_icmpge(l);
                                    cob.iconst_1().ireturn();
                                    cob.labelBinding(l).iconst_m1().ireturn();
                                }));
        ClassModel m48wr = cf.parse(v48wr);
        check(m48wr.majorVersion() == ClassFile.JAVA_4_VERSION, "v48 build honored");
        check(!hasSmt(m48wr), "STACK_MAPS_WHEN_REQUIRED omits SMT at v48");
        byte[] v48g = cf.withOptions(ClassFile.StackMapsOption.GENERATE_STACK_MAPS)
                .build(ClassDesc.of("SmEOV2"), cb -> cb.withVersion(ClassFile.JAVA_4_VERSION, 0)
                        .withMethodBody("m", mtdII(), ClassFile.ACC_STATIC,
                                cob -> {
                                    Label l = cob.newLabel();
                                    cob.iconst_0().iload(0).if_icmpge(l);
                                    cob.iconst_1().ireturn();
                                    cob.labelBinding(l).iconst_m1().ireturn();
                                }));
        check(hasSmt(cf.parse(v48g)), "GENERATE_STACK_MAPS emits SMT even at v48");

        // DROP_STACK_MAPS: build side omits; parse side keeps the model attribute; transform copies the bound attribute
        ClassFile dropSm = cf.withOptions(ClassFile.StackMapsOption.DROP_STACK_MAPS);
        byte[] noSm = buildBranch(dropSm, "SmEO3");
        check(!hasSmt(cf.parse(noSm)), "DROP_STACK_MAPS fresh build omits SMT");
        check(hasSmt(dropSm.parse(branch)), "DROP_STACK_MAPS parse keeps model SMT attribute");
        byte[] tr = dropSm.transformClass(cf.parse(branch), ClassTransform.ACCEPT_ALL);
        check(hasSmt(cf.parse(tr)), "transform ACCEPT_ALL copies bound SMT through");

        // verify three states: normal branch build clean; missing-SMT branch class flagged; straight-line clean
        check(cf.verify(branch).isEmpty(), "verify normal branch build is clean");
        java.util.List<VerifyError> errs = cf.verify(noSm);
        check(errs.size() == 1, "verify flags missing SMT on branch class: " + errs.size());
        check(errs.get(0) instanceof VerifyError, "verify error exact type");
        check(errs.get(0).getMessage() != null && errs.get(0).getMessage().contains("Expecting a stackmap frame"),
                "verify message: " + errs.get(0).getMessage());
        check(cf.verify(buildStraight(dropSm, "SmEO4")).isEmpty(),
                "verify still clean for straight-line class built with DROP_STACK_MAPS");

        // GENERATE_STACK_MAPS: built SMT re-parses back and the frame count matches WHEN_REQUIRED product
        byte[] gen = buildBranch(cf.withOptions(ClassFile.StackMapsOption.GENERATE_STACK_MAPS), "SmEO5");
        ClassModel genM = cf.parse(gen);
        check(hasSmt(genM), "GENERATE_STACK_MAPS build has SMT");
        StackMapTableAttribute gSmt = genM.methods().get(0).code().get().findAttribute(Attributes.stackMapTable()).get();
        StackMapTableAttribute wSmt = cf.parse(branch).methods().get(0).code().get().findAttribute(Attributes.stackMapTable()).get();
        check(gSmt.entries().size() == wSmt.entries().size(),
                "GENERATE and WHEN_REQUIRED produce equal SMT frame counts: " + gSmt.entries().size());
        check(!gSmt.entries().isEmpty(), "SMT entries non-empty for branch class");

        // enum matrix
        check(ClassFile.StackMapsOption.values()[0] == ClassFile.StackMapsOption.STACK_MAPS_WHEN_REQUIRED,
                "ordinal 0 is STACK_MAPS_WHEN_REQUIRED (the default semantic)");
        for (ClassFile.StackMapsOption o : ClassFile.StackMapsOption.values())
            check(ClassFile.StackMapsOption.valueOf(o.name()) == o, "valueOf round-trip " + o);
        check(cf.withOptions(ClassFile.StackMapsOption.DROP_STACK_MAPS) != cf, "withOptions returns new instance");
        // chained last-wins: GENERATE after DROP still generates on fresh build
        ClassFile chained = cf.withOptions(ClassFile.StackMapsOption.DROP_STACK_MAPS)
                              .withOptions(ClassFile.StackMapsOption.GENERATE_STACK_MAPS);
        check(hasSmt(cf.parse(buildBranch(chained, "SmEO6"))), "chained GENERATE_STACK_MAPS overrides DROP_STACK_MAPS");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testOptionSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

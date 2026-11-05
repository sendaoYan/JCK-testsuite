/*
 * @test
 * @summary java.lang.classfile.ClassFile.ClassHierarchyResolverOption conformance: white-box structure + black-box behavior.
 * @run main ClassHierarchyResolverOptionTest
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

public class ClassHierarchyResolverOptionTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassFile$ClassHierarchyResolverOption");
        check(c != null, "java.lang.classfile.ClassFile.ClassHierarchyResolverOption loads");
        check(c.getName().equals("java.lang.classfile.ClassFile$ClassHierarchyResolverOption"), "binary name java.lang.classfile.ClassFile$ClassHierarchyResolverOption");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassFile.ClassHierarchyResolverOption is public");
        check(c.isInterface(), "java.lang.classfile.ClassFile.ClassHierarchyResolverOption is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.ClassFile.ClassHierarchyResolverOption has no superclass");
        check(declares(c, "of"), "declares of");
        check(declares(c, "classHierarchyResolver"), "declares classHierarchyResolver");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ClassFile.ClassHierarchyResolverOption o = ClassFile.ClassHierarchyResolverOption.of(ClassHierarchyResolver.defaultResolver());
        check(o.classHierarchyResolver() != null, "classHierarchyResolver non-null");
        check(o instanceof ClassFile.Option, "is Option");
        ClassFile cf = ClassFile.of(o);
        check(cf != null, "ClassFile.of with resolver option");

    }

    // ---- Part 3: option semantics measured on JDK 27 ----
    static void testOptionSemantics() throws Exception {
        ClassFile cf = ClassFile.of();

        // factory/getter identity + null rejection
        ClassHierarchyResolver chain = ClassHierarchyResolver.of(java.util.List.of(), java.util.Map.of(
                ClassDesc.of("ChrMrgA"), ConstantDescs.CD_Object,
                ClassDesc.of("ChrMrgB"), ClassDesc.of("ChrMrgA")));
        ClassHierarchyResolver sib = ClassHierarchyResolver.of(java.util.List.of(), java.util.Map.of(
                ClassDesc.of("ChrMrgA"), ConstantDescs.CD_Object,
                ClassDesc.of("ChrMrgB"), ConstantDescs.CD_Object));
        ClassFile.ClassHierarchyResolverOption o = ClassFile.ClassHierarchyResolverOption.of(chain);
        check(o.classHierarchyResolver() == chain, "classHierarchyResolver() returns the same resolver instance");
        check(ClassFile.ClassHierarchyResolverOption.of(sib).classHierarchyResolver() == sib,
                "second option keeps its own resolver");
        try {
            ClassFile.ClassHierarchyResolverOption.of(null);
            check(false, "of(null) must reject null");
        } catch (NullPointerException npe) {
            check(true, "of(null) throws NPE");
        }

        // resolver map semantics: chain (MrgB extends MrgA) vs siblings vs default class-loading resolution
        check(chain.getClassInfo(ClassDesc.of("ChrMrgB")) != null, "chain resolver knows ChrMrgB");
        check(chain.getClassInfo(ClassDesc.of("NoSuchClass")) == null, "static resolver returns null for unknown class");
        check(ClassHierarchyResolver.defaultResolver().getClassInfo(ConstantDescs.CD_Object) != null,
                "defaultResolver resolves java/lang/Object");
        check(ClassHierarchyResolver.of(java.util.List.of(ClassDesc.of("ChrIface")), java.util.Map.of())
                        .getClassInfo(ClassDesc.of("ChrIface")) != null,
                "interface collection entry resolvable");
        check(ClassHierarchyResolver.ClassHierarchyInfo.ofInterface()
                        != ClassHierarchyResolver.ClassHierarchyInfo.ofClass(ConstantDescs.CD_Object),
                "ofInterface and ofClass produce distinct infos");
        check(chain.orElse(sib).getClassInfo(ClassDesc.of("ChrMrgA")) != null,
                "orElse fallback chain still resolves known entries");

        // build-side: merging two reference types at a join consults the resolver
        byte[] merged = cf.withOptions(ClassFile.ClassHierarchyResolverOption.of(chain))
                .build(ClassDesc.of("ChrT1"), cb -> cb.withMethodBody("m",
                        MethodTypeDesc.ofDescriptor("(Z)V"), ClassFile.ACC_STATIC, cob -> {
                            Label join = cob.newLabel(), l1 = cob.newLabel();
                            cob.iload(0).ifeq(l1);
                            cob.aconst_null().checkcast(ClassDesc.of("ChrMrgA")).goto_(join);
                            cob.labelBinding(l1);
                            cob.aconst_null().checkcast(ClassDesc.of("ChrMrgB"));
                            cob.labelBinding(join);
                            cob.astore(0).return_();
                        }));
        check(merged.length > 8, "hierarchy-dependent merge builds under custom resolver");
        // default resolver cannot resolve the synthetic classes -> build fails with resolution error
        boolean threw = false;
        String msg = null;
        try {
            cf.build(ClassDesc.of("ChrT2"), cb -> cb.withMethodBody("m",
                    MethodTypeDesc.ofDescriptor("(Z)V"), ClassFile.ACC_STATIC, cob -> {
                        Label join = cob.newLabel(), l1 = cob.newLabel();
                        cob.iload(0).ifeq(l1);
                        cob.aconst_null().checkcast(ClassDesc.of("ChrMrgA")).goto_(join);
                        cob.labelBinding(l1);
                        cob.aconst_null().checkcast(ClassDesc.of("ChrMrgB"));
                        cob.labelBinding(join);
                        cob.astore(0).return_();
                    }));
        } catch (IllegalArgumentException iae) {
            threw = true;
            msg = iae.getMessage();
        }
        check(threw, "same merge fails under default resolver");
        check(msg != null && msg.contains("Could not resolve class"), "default resolver failure message: " + msg);

        // verify-side: same bytes, three resolver outcomes
        check(cf.withOptions(ClassFile.ClassHierarchyResolverOption.of(chain)).verify(merged).isEmpty(),
                "verify clean under matching hierarchy resolver");
        java.util.List<VerifyError> sibErrs = cf.withOptions(ClassFile.ClassHierarchyResolverOption.of(sib)).verify(merged);
        check(sibErrs.size() == 1, "verify flags assignability under sibling resolver: " + sibErrs.size());
        check(sibErrs.get(0).getMessage().contains("not assignable from"),
                "sibling resolver message: " + sibErrs.get(0).getMessage());
        java.util.List<VerifyError> defErrs = cf.verify(merged);
        check(defErrs.size() == 1, "verify flags unresolved hierarchy under default resolver");
        check(defErrs.get(0).getMessage().contains("Could not resolve class"),
                "default resolver verify message: " + defErrs.get(0).getMessage());

        // plain class needs no hierarchy: verify clean under any resolver option
        byte[] plain = cf.build(ClassDesc.of("ChrT3"), cb -> cb.withMethodBody("m",
                ConstantDescs.MTD_void, ClassFile.ACC_STATIC, CodeBuilder::return_));
        check(cf.withOptions(ClassFile.ClassHierarchyResolverOption.of(chain)).verify(plain).isEmpty(),
                "plain class verifies clean with custom resolver");
        check(cf.withOptions(ClassFile.ClassHierarchyResolverOption.of(
                ClassHierarchyResolver.defaultResolver())).verify(plain).isEmpty(),
                "plain class verifies clean with defaultResolver option");
        check(cf.withOptions(o) != cf, "withOptions returns new instance");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testOptionSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

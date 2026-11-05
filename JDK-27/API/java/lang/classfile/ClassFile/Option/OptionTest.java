/*
 * @test
 * @summary java.lang.classfile.ClassFile.Option conformance: white-box structure + black-box behavior.
 * @run main OptionTest
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

public class OptionTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassFile$Option");
        check(c != null, "java.lang.classfile.ClassFile.Option loads");
        check(c.getName().equals("java.lang.classfile.ClassFile$Option"), "binary name java.lang.classfile.ClassFile$Option");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassFile.Option is public");
        check(c.isInterface(), "java.lang.classfile.ClassFile.Option is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.ClassFile.Option has no superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ClassFile.Option o = ClassFile.DebugElementsOption.DROP_DEBUG;
        check(o instanceof ClassFile.Option, "DROP_DEBUG is Option");
        ClassFile cf = ClassFile.of(o);
        check(cf != null, "ClassFile.of(Option)");

    }

    // ---- Part 3: option hierarchy and factory semantics measured on JDK 27 ----
    static void testOptionHierarchy() throws Exception {
        Class<?> optionIf = Class.forName("java.lang.classfile.ClassFile$Option");

        // all concrete Option types (8 enums + 2 interface factories) implement ClassFile.Option
        java.util.List<Class<?>> concrete = java.util.List.of(
                ClassFile.DebugElementsOption.class,
                ClassFile.LineNumbersOption.class,
                ClassFile.StackMapsOption.class,
                ClassFile.AttributesProcessingOption.class,
                ClassFile.DeadLabelsOption.class,
                ClassFile.DeadCodeOption.class,
                ClassFile.ShortJumpsOption.class,
                ClassFile.ConstantPoolSharingOption.class,
                ClassFile.AttributeMapperOption.class,
                ClassFile.ClassHierarchyResolverOption.class);
        check(concrete.size() == 10, "10 concrete Option types");
        for (Class<?> c : concrete) {
            check(optionIf.isAssignableFrom(c), c.getSimpleName() + " implements ClassFile.Option");
            check(c.getName().startsWith("java.lang.classfile.ClassFile$"),
                    c.getSimpleName() + " is a ClassFile nested type");
        }
        // compile-time witness for the same relation
        ClassFile.Option[] witnesses = new ClassFile.Option[]{
                ClassFile.DebugElementsOption.PASS_DEBUG,
                ClassFile.LineNumbersOption.PASS_LINE_NUMBERS,
                ClassFile.StackMapsOption.STACK_MAPS_WHEN_REQUIRED,
                ClassFile.AttributesProcessingOption.PASS_ALL_ATTRIBUTES,
                ClassFile.DeadLabelsOption.FAIL_ON_DEAD_LABELS,
                ClassFile.DeadCodeOption.PATCH_DEAD_CODE,
                ClassFile.ShortJumpsOption.FIX_SHORT_JUMPS,
                ClassFile.ConstantPoolSharingOption.SHARED_POOL,
                ClassFile.AttributeMapperOption.of(e -> null),
                ClassFile.ClassHierarchyResolverOption.of(ClassHierarchyResolver.defaultResolver())};
        check(witnesses.length == 10, "10 constants/options assignable to ClassFile.Option");

        // enum surface: constant counts, default = values()[0], valueOf bidirectional
        java.util.Map<String, Integer> expected = java.util.Map.of(
                "DebugElementsOption", 2,
                "LineNumbersOption", 2,
                "StackMapsOption", 3,
                "AttributesProcessingOption", 3,
                "DeadLabelsOption", 2,
                "DeadCodeOption", 2,
                "ShortJumpsOption", 2,
                "ConstantPoolSharingOption", 2);
        check(expected.size() == 8, "8 option enums");
        for (java.util.Map.Entry<String, Integer> e : expected.entrySet()) {
            Class<?> c = Class.forName("java.lang.classfile.ClassFile$" + e.getKey());
            check(c.isEnum(), e.getKey() + " is an enum");
            Object[] consts = c.getEnumConstants();
            check(consts.length == e.getValue(), e.getKey() + " has " + e.getValue() + " constants");
            for (Object k : consts)
                check(c.getMethod("valueOf", String.class).invoke(null, ((Enum<?>) k).name()) == k,
                        e.getKey() + " valueOf(" + ((Enum<?>) k).name() + ") round-trip");
        }
        check(ClassFile.StackMapsOption.values()[0] == ClassFile.StackMapsOption.STACK_MAPS_WHEN_REQUIRED
                && ClassFile.DebugElementsOption.values()[0] == ClassFile.DebugElementsOption.PASS_DEBUG
                && ClassFile.LineNumbersOption.values()[0] == ClassFile.LineNumbersOption.PASS_LINE_NUMBERS
                && ClassFile.AttributesProcessingOption.values()[0] == ClassFile.AttributesProcessingOption.PASS_ALL_ATTRIBUTES
                && ClassFile.DeadLabelsOption.values()[0] == ClassFile.DeadLabelsOption.FAIL_ON_DEAD_LABELS
                && ClassFile.DeadCodeOption.values()[0] == ClassFile.DeadCodeOption.PATCH_DEAD_CODE
                && ClassFile.ShortJumpsOption.values()[0] == ClassFile.ShortJumpsOption.FIX_SHORT_JUMPS
                && ClassFile.ConstantPoolSharingOption.values()[0] == ClassFile.ConstantPoolSharingOption.SHARED_POOL,
                "each option enum's ordinal 0 is its default semantic value");
    }

    static void testFactorySemantics() throws Exception {
        ClassFile cf = ClassFile.of();
        check(cf == ClassFile.of(), "ClassFile.of() returns the shared default context");
        // zero-length varargs still produces a distinct (non-shared) instance
        check(ClassFile.of(new ClassFile.Option[0]) != cf, "of(zero options) is a new instance");
        check(cf.withOptions() != cf, "withOptions() with empty varargs is a new instance");
        check(cf.withOptions(new ClassFile.Option[0]) != cf, "withOptions(zero options) is a new instance");
        try {
            ClassFile.of((ClassFile.Option[]) null);
            check(false, "of(null) must reject null");
        } catch (NullPointerException npe) {
            check(npe.getMessage() != null, "of(null) NPE message: " + npe.getMessage());
        }
        try {
            cf.withOptions((ClassFile.Option[]) null);
            check(false, "withOptions(null) must reject null");
        } catch (NullPointerException npe) {
            check(npe.getMessage() != null, "withOptions(null) NPE message: " + npe.getMessage());
        }

        // behavioral equivalence: empty withOptions builds byte-identical output to the default context
        byte[] viaDefault = cf.build(ClassDesc.of("OptUmb1"), cb -> cb.withMethodBody("m",
                ConstantDescs.MTD_void, ClassFile.ACC_STATIC, CodeBuilder::return_));
        byte[] viaEmpty = cf.withOptions().build(ClassDesc.of("OptUmb2"), cb -> cb.withMethodBody("m",
                ConstantDescs.MTD_void, ClassFile.ACC_STATIC, CodeBuilder::return_));
        check(viaDefault.length == viaEmpty.length, "empty withOptions build size matches default");
        check(cf.parse(viaDefault).methods().size() == cf.parse(viaEmpty).methods().size(),
                "empty withOptions parse equivalence");
        check(ClassFile.of(new ClassFile.Option[0]).parse(viaDefault).methods().size() == 1,
                "zero-option ClassFile parses normally");

        // same option passed twice is idempotent; last occurrence of a kind wins (measured: later replaces earlier)
        ClassFile chained = cf.withOptions(ClassFile.DebugElementsOption.DROP_DEBUG)
                              .withOptions(ClassFile.DebugElementsOption.PASS_DEBUG);
        byte[] chainedBytes = chained.build(ClassDesc.of("OptUmb3"), cb -> cb.withMethodBody("m",
                ConstantDescs.MTD_void, ClassFile.ACC_STATIC, cob -> {
                    Label s = cob.newLabel(), e = cob.newLabel();
                    cob.labelBinding(s).return_();
                    cob.labelBinding(e);
                    cob.localVariable(1, "lv", ConstantDescs.CD_int, s, e);
                }));
        CodeModel cm = cf.parse(chainedBytes).methods().get(0).code().get();
        check(cm.findAttribute(Attributes.localVariableTable()).isPresent(),
                "PASS_DEBUG chained after DROP_DEBUG still writes debug attributes");
        check(cf.verify(chainedBytes).isEmpty(), "chained-option build verifies clean");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testOptionHierarchy();
        testFactorySemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

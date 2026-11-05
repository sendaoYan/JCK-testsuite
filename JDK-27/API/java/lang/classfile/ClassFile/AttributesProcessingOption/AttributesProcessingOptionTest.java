/*
 * @test
 * @summary java.lang.classfile.ClassFile.AttributesProcessingOption conformance: white-box structure + black-box behavior.
 * @run main AttributesProcessingOptionTest
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

public class AttributesProcessingOptionTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassFile$AttributesProcessingOption");
        check(c != null, "java.lang.classfile.ClassFile.AttributesProcessingOption loads");
        check(c.getName().equals("java.lang.classfile.ClassFile$AttributesProcessingOption"), "binary name java.lang.classfile.ClassFile$AttributesProcessingOption");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassFile.AttributesProcessingOption is public");
        check(c.isEnum(), "java.lang.classfile.ClassFile.AttributesProcessingOption is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.ClassFile.AttributesProcessingOption superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 3, "3 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("PASS_ALL_ATTRIBUTES", "DROP_UNKNOWN_ATTRIBUTES", "DROP_UNSTABLE_ATTRIBUTES")), "exact enum constant names");
        check(declares(c, "values"), "declares values");
        check(declares(c, "valueOf"), "declares valueOf");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(ClassFile.AttributesProcessingOption.values().length == 3, "3 constants");
        check(ClassFile.AttributesProcessingOption.valueOf("DROP_UNKNOWN_ATTRIBUTES") == ClassFile.AttributesProcessingOption.DROP_UNKNOWN_ATTRIBUTES, "valueOf");
        ClassFile cf = ClassFile.of(ClassFile.AttributesProcessingOption.DROP_UNKNOWN_ATTRIBUTES);
        check(cf != null, "usable as option");

    }

    // ---- Part 3: option semantics measured on JDK 27 ----
    // Real UNSTABLE-stability attribute: RUNTIME-retention type annotation on a method type parameter
    // (javac emits RuntimeVisibleTypeAnnotations, whose built-in mapper stability is UNSTABLE).
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_PARAMETER)
    @interface ApTA {}
    static <@ApTA T> void gen(T t) {}

    static class ApAttribute extends CustomAttribute<ApAttribute> {
        public ApAttribute() { super(AP_MAPPER); }
    }
    static final AttributeMapper<ApAttribute> AP_MAPPER = new AttributeMapper<>() {
        public String name() { return "ApAttr"; }
        public ApAttribute readAttribute(AttributedElement encl, ClassReader cf, int pos) { return new ApAttribute(); }
        public void writeAttribute(BufWriter buf, ApAttribute attr) {
            buf.writeIndex(buf.constantPool().utf8Entry(name()));
            buf.writeInt(0);
        }
        public AttributeMapper.AttributeStability stability() { return AttributeMapper.AttributeStability.STATELESS; }
    };

    static boolean hasRtvta(ClassModel cm) {
        for (MethodModel m : cm.methods())
            if (m.methodName().stringValue().equals("gen"))
                return m.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).isPresent();
        return false;
    }

    static void testOptionSemantics() throws Exception {
        ClassFile cf = ClassFile.of();

        // enum matrix
        check(ClassFile.AttributesProcessingOption.values()[0] == ClassFile.AttributesProcessingOption.PASS_ALL_ATTRIBUTES,
                "ordinal 0 is PASS_ALL_ATTRIBUTES (the default semantic)");
        for (ClassFile.AttributesProcessingOption o : ClassFile.AttributesProcessingOption.values())
            check(ClassFile.AttributesProcessingOption.valueOf(o.name()) == o, "valueOf round-trip " + o);
        check(cf.withOptions(ClassFile.AttributesProcessingOption.DROP_UNKNOWN_ATTRIBUTES) != cf,
                "withOptions returns new instance");

        // unknown (unregistered) attribute at class level: transform filter matrix
        byte[] withAttr = cf.withOptions(ClassFile.AttributeMapperOption.of(
                        e -> e.equalsString("ApAttr") ? AP_MAPPER : null))
                .build(ClassDesc.of("ApT1"), cb -> cb.with(new ApAttribute())
                        .withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, CodeBuilder::return_));
        ClassModel unknownParsed = cf.parse(withAttr);
        check(unknownParsed.attributes().size() == 1, "unregistered parse keeps the unknown attribute");
        check(unknownParsed.attributes().get(0) instanceof UnknownAttribute, "unregistered parse yields UnknownAttribute");

        ClassModel kept = cf.parse(cf.withOptions(ClassFile.AttributesProcessingOption.PASS_ALL_ATTRIBUTES)
                .transformClass(unknownParsed, ClassTransform.ACCEPT_ALL));
        check(kept.attributes().size() == 1, "PASS_ALL_ATTRIBUTES keeps unknown attribute");
        ClassModel droppedUnknown = cf.parse(cf.withOptions(ClassFile.AttributesProcessingOption.DROP_UNKNOWN_ATTRIBUTES)
                .transformClass(unknownParsed, ClassTransform.ACCEPT_ALL));
        check(droppedUnknown.attributes().isEmpty(), "DROP_UNKNOWN_ATTRIBUTES strips unknown attribute");
        ClassModel droppedUnstable = cf.parse(cf.withOptions(ClassFile.AttributesProcessingOption.DROP_UNSTABLE_ATTRIBUTES)
                .transformClass(unknownParsed, ClassTransform.ACCEPT_ALL));
        check(droppedUnstable.attributes().isEmpty(),
                "DROP_UNSTABLE_ATTRIBUTES also strips unknown attributes (unknown stability ranks below UNSTABLE)");

        // registered custom attribute (parsed into a user object, STATELESS): never stripped by any processing option
        ClassModel regParsed = cf.withOptions(ClassFile.AttributeMapperOption.of(
                e -> e.equalsString("ApAttr") ? AP_MAPPER : null)).parse(withAttr);
        check(regParsed.attributes().get(0) instanceof ApAttribute, "registered parse materializes ApAttribute");
        for (ClassFile.AttributesProcessingOption o : ClassFile.AttributesProcessingOption.values()) {
            ClassModel out = cf.parse(cf.withOptions(o).transformClass(regParsed, ClassTransform.ACCEPT_ALL));
            check(out.attributes().size() == 1,
                    o + " keeps registered STATELESS custom attribute");
        }

        // real UNSTABLE bound attribute (RuntimeVisibleTypeAnnotations) is stripped by DROP_UNSTABLE only
        byte[] self = AttributesProcessingOptionTest.class.getResourceAsStream(
                "AttributesProcessingOptionTest.class").readAllBytes();
        ClassModel selfM = cf.parse(self);
        check(hasRtvta(selfM), "self class carries RuntimeVisibleTypeAnnotations on gen()");
        for (ClassFile.AttributesProcessingOption o : ClassFile.AttributesProcessingOption.values()) {
            // NEW_POOL forces element replay, so the per-stability filter applies
            ClassModel outNew = cf.parse(cf.withOptions(ClassFile.ConstantPoolSharingOption.NEW_POOL, o)
                    .transformClass(selfM, ClassTransform.ACCEPT_ALL));
            boolean keptRtvta = hasRtvta(outNew);
            check(keptRtvda(o, keptRtvta), o + " + NEW_POOL: UNSTABLE attribute kept=" + keptRtvta);
            // SHARED_POOL bulk-copies members verbatim, so the attribute survives every processing option
            ClassModel outShared = cf.parse(cf.withOptions(ClassFile.ConstantPoolSharingOption.SHARED_POOL, o)
                    .transformClass(selfM, ClassTransform.ACCEPT_ALL));
            check(hasRtvta(outShared), o + " + SHARED_POOL keeps member attributes (bulk copy)");
            // transforming method bodies forces rebuild, filter applies again
            ClassModel outTfm = cf.parse(cf.withOptions(o).transformClass(selfM,
                    ClassTransform.transformingMethodBodies(CodeTransform.ACCEPT_ALL)));
            check(keptRtvda(o, hasRtvta(outTfm)), o + " + transformingMethodBodies: kept=" + hasRtvta(outTfm));
        }

        // sanity: default (no option) transform behaves as PASS_ALL
        check(hasRtvta(cf.parse(cf.transformClass(selfM, ClassTransform.ACCEPT_ALL))),
                "default transform passes UNSTABLE attribute through");
    }

    static boolean keptRtvda(ClassFile.AttributesProcessingOption o, boolean kept) {
        return kept == (o != ClassFile.AttributesProcessingOption.DROP_UNSTABLE_ATTRIBUTES);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testOptionSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.lang.classfile.ClassFile.AttributeMapperOption conformance: white-box structure + black-box behavior.
 * @run main AttributeMapperOptionTest
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

public class AttributeMapperOptionTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassFile$AttributeMapperOption");
        check(c != null, "java.lang.classfile.ClassFile.AttributeMapperOption loads");
        check(c.getName().equals("java.lang.classfile.ClassFile$AttributeMapperOption"), "binary name java.lang.classfile.ClassFile$AttributeMapperOption");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassFile.AttributeMapperOption is public");
        check(c.isInterface(), "java.lang.classfile.ClassFile.AttributeMapperOption is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.ClassFile.AttributeMapperOption has no superclass");
        check(declares(c, "of"), "declares of");
        check(declares(c, "attributeMapper"), "declares attributeMapper");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ClassFile.AttributeMapperOption o = ClassFile.AttributeMapperOption.of(u -> null);
        check(o.attributeMapper() != null, "attributeMapper non-null");
        check(o instanceof ClassFile.Option, "is Option");
        ClassFile cf = ClassFile.of(o);
        check(cf != null, "ClassFile.of with AttributeMapperOption");

    }

    // ---- Part 3: option semantics measured on JDK 27 ----
    static class ProbeAttribute extends CustomAttribute<ProbeAttribute> {
        public ProbeAttribute() { super(AMO_MAPPER); }
    }
    static final AttributeMapper<ProbeAttribute> AMO_MAPPER = new AttributeMapper<>() {
        public String name() { return "AmoAttr"; }
        public ProbeAttribute readAttribute(AttributedElement encl, ClassReader cf, int pos) { return new ProbeAttribute(); }
        public void writeAttribute(BufWriter buf, ProbeAttribute attr) {
            buf.writeIndex(buf.constantPool().utf8Entry(name()));
            buf.writeInt(0);
        }
        public AttributeMapper.AttributeStability stability() { return AttributeMapper.AttributeStability.STATELESS; }
    };

    static void testOptionSemantics() throws Exception {
        ClassFile cf = ClassFile.of();

        // factory/getter identity: of(fn) hands the exact function object back via attributeMapper()
        java.util.function.Function<Utf8Entry, AttributeMapper<?>> fn1 = e -> e.equalsString("AmoAttr") ? AMO_MAPPER : null;
        java.util.function.Function<Utf8Entry, AttributeMapper<?>> fn2 = e -> null;
        ClassFile.AttributeMapperOption o1 = ClassFile.AttributeMapperOption.of(fn1);
        ClassFile.AttributeMapperOption o2 = ClassFile.AttributeMapperOption.of(fn2);
        check(o1.attributeMapper() == fn1, "attributeMapper() returns the same function instance");
        check(o2.attributeMapper() == fn2, "second option keeps its own function instance");
        check(o1.attributeMapper() != o2.attributeMapper(), "distinct options expose distinct functions");
        try {
            ClassFile.AttributeMapperOption.of(null);
            check(false, "of(null) must reject null");
        } catch (NullPointerException npe) {
            check(true, "of(null) throws NPE (no detail message)");
        }

        // function is actually consulted during parse, keyed by attribute name Utf8Entry
        int[] consulted = new int[1];
        boolean[] rightName = new boolean[1];
        java.util.function.Function<Utf8Entry, AttributeMapper<?>> counting = e -> {
            consulted[0]++;
            rightName[0] |= e.equalsString("AmoAttr");
            return e.equalsString("AmoAttr") ? AMO_MAPPER : null;
        };
        byte[] withAttr = cf.withOptions(ClassFile.AttributeMapperOption.of(counting))
                .build(ClassDesc.of("AmoT1"), cb -> cb.with(new ProbeAttribute())
                        .withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, CodeBuilder::return_));
        check(withAttr.length > 8, "custom attribute class built");
        ClassModel counted = cf.withOptions(ClassFile.AttributeMapperOption.of(counting)).parse(withAttr);
        check(counted.attributes().size() == 1, "attribute materialized through counting mapper");
        check(consulted[0] >= 1, "mapper function consulted at least once, got " + consulted[0]);
        check(rightName[0], "mapper function saw the AmoAttr name entry");

        // registered round-trip: parse yields the custom attribute type with its mapper readable
        ClassModel reg = cf.withOptions(ClassFile.AttributeMapperOption.of(fn1)).parse(withAttr);
        check(reg.attributes().size() == 1, "one attribute on registered parse");
        Attribute<?> attr = reg.attributes().get(0);
        check(attr instanceof ProbeAttribute, "registered parse materializes ProbeAttribute, got " + attr.getClass().getName());
        check(attr.attributeMapper() == AMO_MAPPER, "attributeMapper() identity from parsed attribute");
        check(attr.attributeMapper().name().equals("AmoAttr"), "mapper name readback");
        check(attr.attributeMapper().stability() == AttributeMapper.AttributeStability.STATELESS, "mapper stability readback");
        check(attr.attributeName().stringValue().equals("AmoAttr"), "attributeName readback");

        // unregistered parse: preserved as UnknownAttribute with name intact (no exception)
        ClassModel unreg = cf.parse(withAttr);
        check(unreg.attributes().size() == 1, "unregistered parse keeps one attribute");
        Attribute<?> unknown = unreg.attributes().get(0);
        check(!(unknown instanceof ProbeAttribute), "unregistered parse does not materialize ProbeAttribute");
        check(unknown.attributeName().stringValue().equals("AmoAttr"), "unknown attribute name preserved");
        check(unknown instanceof UnknownAttribute, "unregistered parse yields UnknownAttribute");

        // fn returning null everywhere: same default UnknownAttribute path
        ClassModel nullFn = cf.withOptions(o2).parse(withAttr);
        check(nullFn.attributes().get(0) instanceof UnknownAttribute, "null-returning fn falls back to UnknownAttribute");
        check(nullFn.attributes().get(0).attributeName().stringValue().equals("AmoAttr"), "fallback keeps attribute name");

        // build-side use + re-build round-trip stays stable
        byte[] again = cf.withOptions(ClassFile.AttributeMapperOption.of(fn1))
                .build(ClassDesc.of("AmoT2"), cb -> cb.with(new ProbeAttribute())
                        .withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, CodeBuilder::return_));
        check(cf.parse(again).attributes().size() == 1, "rebuild with registered mapper keeps attribute");
        check(Arrays.equals(again, withAttr) || again.length == withAttr.length, "deterministic rebuild size stable");

        // combination: mapper option composes with parse options into a new ClassFile instance
        ClassFile combo = cf.withOptions(ClassFile.AttributeMapperOption.of(fn1), ClassFile.DebugElementsOption.DROP_DEBUG);
        check(combo != cf, "withOptions returns new instance");
        check(combo.parse(withAttr).attributes().get(0) instanceof ProbeAttribute, "combo still resolves custom mapper");
        check(ClassFile.of(ClassFile.AttributeMapperOption.of(fn1)) instanceof ClassFile, "ClassFile.of(mapperOption) usable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testOptionSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

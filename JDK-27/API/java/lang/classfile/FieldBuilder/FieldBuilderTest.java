/*
 * @test
 * @summary java.lang.classfile.FieldBuilder conformance: white-box structure + black-box behavior.
 * @run main FieldBuilderTest
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

public class FieldBuilderTest {
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
        Class<?> c = Class.forName("java.lang.classfile.FieldBuilder");
        check(c != null, "java.lang.classfile.FieldBuilder loads");
        check(c.getName().equals("java.lang.classfile.FieldBuilder"), "binary name java.lang.classfile.FieldBuilder");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.FieldBuilder is public");
        check(c.isInterface(), "java.lang.classfile.FieldBuilder is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.FieldBuilder has no superclass");
        check(declares(c, "withFlags"), "declares withFlags");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withField("x", ConstantDescs.CD_int, fb -> fb.withFlags(ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC | ClassFile.ACC_FINAL)));
        ClassModel cm = ClassFile.of().parse(b);
        FieldModel f = cm.fields().get(0);
        check(f.fieldName().stringValue().equals("x"), "name");
        check(f.fieldTypeSymbol().equals(ConstantDescs.CD_int), "type");
        check(f.flags().flagsMask() == (ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC | ClassFile.ACC_FINAL), "flags");
        check(f.parent().isPresent() && f.parent().get().thisClass().asInternalName().equals("pkg/T"), "parent");

    }

    // ---- Part 3: ConstantValue 常量注入全类型矩阵（实测锁定 of 重载面） ----
    static void testConstantValueMatrix() throws Exception {
        ClassFile cf = ClassFile.of();
        // of(42) → IntegerEntry；of(2L) → LongEntry；of(3.5f) → FloatEntry；of(2.5d) → DoubleEntry；of("s") → StringEntry
        byte[] b = cf.build(ClassDesc.of("Fbt1.T"), cb -> cb
                .withField("i", ConstantDescs.CD_int, fb -> fb.withFlags(ClassFile.ACC_STATIC | ClassFile.ACC_FINAL).with(ConstantValueAttribute.of(42)))
                .withField("l", ConstantDescs.CD_long, fb -> fb.withFlags(ClassFile.ACC_STATIC | ClassFile.ACC_FINAL).with(ConstantValueAttribute.of(2L)))
                .withField("f", ConstantDescs.CD_float, fb -> fb.withFlags(ClassFile.ACC_STATIC | ClassFile.ACC_FINAL).with(ConstantValueAttribute.of(3.5f)))
                .withField("d", ConstantDescs.CD_double, fb -> fb.withFlags(ClassFile.ACC_STATIC | ClassFile.ACC_FINAL).with(ConstantValueAttribute.of(2.5d)))
                .withField("s", ConstantDescs.CD_String, fb -> fb.withFlags(ClassFile.ACC_STATIC | ClassFile.ACC_FINAL).with(ConstantValueAttribute.of("strv"))));
        ClassModel cm = cf.parse(b);
        check(cm.fields().size() == 5, "5 constant fields");
        ConstantValueAttribute cvi = cm.fields().get(0).findAttribute(Attributes.constantValue()).orElseThrow();
        check(cvi.constant() instanceof IntegerEntry, "of(int) yields IntegerEntry");
        check(((IntegerEntry) cvi.constant()).intValue() == 42, "int value 42");
        check(cvi.attributeName().stringValue().equals("ConstantValue"), "attributeName ConstantValue");
        check(cvi.attributeMapper() == Attributes.constantValue(), "attributeMapper identity");
        ConstantValueAttribute cvl = cm.fields().get(1).findAttribute(Attributes.constantValue()).orElseThrow();
        check(cvl.constant() instanceof LongEntry && ((LongEntry) cvl.constant()).longValue() == 2L, "of(long) yields LongEntry 2");
        ConstantValueAttribute cvf = cm.fields().get(2).findAttribute(Attributes.constantValue()).orElseThrow();
        check(cvf.constant() instanceof FloatEntry && ((FloatEntry) cvf.constant()).floatValue() == 3.5f, "of(float) yields FloatEntry 3.5");
        ConstantValueAttribute cvd = cm.fields().get(3).findAttribute(Attributes.constantValue()).orElseThrow();
        check(cvd.constant() instanceof DoubleEntry && ((DoubleEntry) cvd.constant()).doubleValue() == 2.5d, "of(double) yields DoubleEntry 2.5");
        ConstantValueAttribute cvs = cm.fields().get(4).findAttribute(Attributes.constantValue()).orElseThrow();
        check(cvs.constant() instanceof StringEntry && ((StringEntry) cvs.constant()).stringValue().equals("strv"), "of(String) yields StringEntry");
        check(cf.verify(cm).isEmpty(), "constant matrix verify clean");
        // of(ConstantValueEntry) 条目重载（从 cb.constantPool() 取 IntegerEntry）
        byte[] b2 = cf.build(ClassDesc.of("Fbt2.U"), cb -> {
            IntegerEntry ie = cb.constantPool().intEntry(77);
            cb.withField("ie", ConstantDescs.CD_int, fb -> fb.withFlags(ClassFile.ACC_STATIC | ClassFile.ACC_FINAL).with(ConstantValueAttribute.of(ie)));
        });
        ConstantValueAttribute cv2 = cf.parse(b2).fields().get(0).findAttribute(Attributes.constantValue()).orElseThrow();
        check(((IntegerEntry) cv2.constant()).intValue() == 77, "of(ConstantValueEntry) roundtrip");
        // 负例：of((ConstantDesc)null) → NPE（实测）；注意没有 of(boolean)/of(char) 重载（boolean/char 非 ConstantDesc）
        try {
            ConstantValueAttribute.of((java.lang.constant.ConstantDesc) null);
            check(false, "of((ConstantDesc)null) must throw");
        } catch (NullPointerException e) {
            check(e.getMessage() == null, "of(null) NPE（实测消息为 null 的裸 NPE）");
        }
    }

    // ---- Part 4: 少用 flag 位回读（VOLATILE/TRANSIENT/ENUM） ----
    static void testFlagBits() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("Fbt3.T"), cb -> cb
                .withField("vt", ConstantDescs.CD_int, ClassFile.ACC_VOLATILE | ClassFile.ACC_TRANSIENT)
                .withField("en", ConstantDescs.CD_int, ClassFile.ACC_ENUM | ClassFile.ACC_STATIC | ClassFile.ACC_FINAL)
                .withField("pb", ConstantDescs.CD_int, ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL));
        ClassModel cm = cf.parse(b);
        FieldModel vt = cm.fields().get(0), en = cm.fields().get(1), pb = cm.fields().get(2);
        check(vt.flags().flagsMask() == (ClassFile.ACC_VOLATILE | ClassFile.ACC_TRANSIENT), "VOLATILE|TRANSIENT mask 192");
        check(vt.flags().has(AccessFlag.VOLATILE) && vt.flags().has(AccessFlag.TRANSIENT), "VOLATILE/TRANSIENT bits");
        check(!vt.flags().has(AccessFlag.ENUM), "no ENUM on vt");
        check(en.flags().flagsMask() == (ClassFile.ACC_ENUM | ClassFile.ACC_STATIC | ClassFile.ACC_FINAL), "ENUM mask 16408");
        check(en.flags().has(AccessFlag.ENUM), "ENUM bit");
        check(pb.flags().flags().containsAll(List.of(AccessFlag.PUBLIC, AccessFlag.FINAL)), "PUBLIC|FINAL set");
        check(vt.flags().location() == AccessFlag.Location.FIELD, "flags location FIELD");
        check(vt.flags().toString().equals("AccessFlags[flags=192]"), "field flags toString: " + vt.flags());
        // withFlags(AccessFlag...) 重载回读
        byte[] b2 = cf.build(ClassDesc.of("Fbt4.U"), cb -> cb.withField("fl", ConstantDescs.CD_int, fb -> fb.withFlags(AccessFlag.STATIC, AccessFlag.FINAL)));
        check(cf.parse(b2).fields().get(0).flags().flagsMask() == (ClassFile.ACC_STATIC | ClassFile.ACC_FINAL), "withFlags(AccessFlag...) roundtrip");
        check(cf.verify(cm).isEmpty(), "flag bits verify clean");
    }

    // ---- Part 5: Signature/Deprecated/Synthetic 属性 + Utf8Entry 重载 + transformField ----
    static void testAttributeAndTransform() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("Fbt5.T"), cb -> cb
                .withField("g", ClassDesc.of("java.util.List"), fb -> fb
                        .withFlags(ClassFile.ACC_PRIVATE)
                        .with(SignatureAttribute.of(Signature.parseFrom("Ljava/util/List<Ljava/lang/String;>;")))
                        .with(DeprecatedAttribute.of())
                        .with(SyntheticAttribute.of())));
        FieldModel f = cf.parse(b).fields().get(0);
        check(f.findAttribute(Attributes.signature()).get().signature().stringValue().equals("Ljava/util/List<Ljava/lang/String;>;"), "field signature roundtrip");
        check(f.findAttribute(Attributes.signature()).get().attributeName().stringValue().equals("Signature"), "Signature attributeName");
        check(f.findAttribute(Attributes.deprecated()).isPresent(), "field deprecated present");
        check(f.findAttribute(Attributes.deprecated()).get().attributeName().stringValue().equals("Deprecated"), "Deprecated attributeName");
        check(f.findAttribute(Attributes.synthetic()).isPresent(), "field synthetic present");
        check(f.findAttribute(Attributes.synthetic()).get().attributeName().stringValue().equals("Synthetic"), "Synthetic attributeName");
        check(f.flags().has(AccessFlag.PRIVATE), "flags via consumer withFlags(int)");
        // withField(Utf8Entry,Utf8Entry,Consumer) 重载
        byte[] b2 = cf.build(ClassDesc.of("Fbt6.U"), cb -> {
            ConstantPoolBuilder cpb = cb.constantPool();
            cb.withField(cpb.utf8Entry("ef"), cpb.utf8Entry("D"), fb -> fb.withFlags(ClassFile.ACC_PUBLIC));
        });
        FieldModel f2 = cf.parse(b2).fields().get(0);
        check(f2.fieldName().stringValue().equals("ef") && f2.fieldType().stringValue().equals("D"), "Utf8Entry field overload roundtrip");
        // transformField(FieldModel, FieldTransform) 通道 + FieldBuilder.constantPool
        byte[] src = cf.build(ClassDesc.of("Fbt7.V"), cb -> cb
                .withField("sf", ConstantDescs.CD_int, ClassFile.ACC_PRIVATE)
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_STATIC, c -> c.iconst_1().ireturn()));
        ClassModel sm = cf.parse(src);
        final boolean[] ok = new boolean[1];
        byte[] out = cf.transformClass(sm, new ClassTransform() {
            public void accept(ClassBuilder b, ClassElement e) {
                if (e instanceof FieldModel fm) b.transformField(fm, new FieldTransform() {
                    public void accept(FieldBuilder fb, FieldElement fe) { ok[0] = fb.constantPool() != null; fb.with(fe); }
                });
                else b.with(e);
            }
        });
        check(cf.parse(out).fields().size() == 1, "transformField preserves field");
        check(ok[0], "FieldTransform accept builder constantPool non-null");
        // FieldTransform.dropping 剥属性
        byte[] out2 = cf.transformClass(cf.parse(b), new ClassTransform() {
            public void accept(ClassBuilder b, ClassElement e) {
                if (e instanceof FieldModel fm) b.transformField(fm, FieldTransform.dropping(fe -> fe instanceof SignatureAttribute));
                else b.with(e);
            }
        });
        FieldModel f3 = cf.parse(out2).fields().get(0);
        check(!f3.findAttribute(Attributes.signature()).isPresent(), "FieldTransform.dropping removes signature");
        check(f3.findAttribute(Attributes.deprecated()).isPresent(), "dropping keeps other attributes");
    }

    // ---- Part 6: defineClass 运行闭环（常量字段真读值） ----
    static int dynSeq;
    static void testDefineClassLoop() throws Exception {
        ClassFile cf = ClassFile.of();
        String name = "DynFB" + (++dynSeq);
        byte[] b = cf.build(ClassDesc.of(name), cb -> cb
                .withField("C", ConstantDescs.CD_int, fb -> fb.withFlags(ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC | ClassFile.ACC_FINAL).with(ConstantValueAttribute.of(77)))
                .withMethodBody("get", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        c -> c.getstatic(ClassDesc.of(name), "C", ConstantDescs.CD_int).ireturn()));
        ClassModel rm = cf.parse(b);
        check(rm.thisClass().asInternalName().equals(name), "dyn thisClass roundtrip");
        check(cf.verify(rm).isEmpty(), "dyn verify clean");
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getField("C").getInt(null) == 77, "DynFB constant field C==77 via reflection");
        check(dc.getMethod("get").invoke(null).equals(77), "DynFB get()==77 ConstantValue 初始化闭环");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantValueMatrix();
        testFlagBits();
        testAttributeAndTransform();
        testDefineClassLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

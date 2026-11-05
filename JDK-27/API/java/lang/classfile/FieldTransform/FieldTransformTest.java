/*
 * @test
 * @summary java.lang.classfile.FieldTransform conformance: white-box structure + black-box behavior.
 * @run main FieldTransformTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import java.nio.file.*;

public class FieldTransformTest {
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
        Class<?> c = Class.forName("java.lang.classfile.FieldTransform");
        check(c != null, "java.lang.classfile.FieldTransform loads");
        check(c.getName().equals("java.lang.classfile.FieldTransform"), "binary name java.lang.classfile.FieldTransform");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.FieldTransform is public");
        check(c.isInterface(), "java.lang.classfile.FieldTransform is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.FieldTransform has no superclass");
        check(declares(c, "ofStateful"), "declares ofStateful");
        check(declares(c, "endHandler"), "declares endHandler");
        check(declares(c, "dropping"), "declares dropping");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] src = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withField("x", ConstantDescs.CD_int, fb -> fb.withFlags(ClassFile.ACC_STATIC).with(ConstantValueAttribute.of(42))));
        ClassModel cm = ClassFile.of().parse(src);
        check(cm.fields().get(0).findAttribute(Attributes.constantValue()).isPresent(), "source has constant value");
        byte[] out = ClassFile.of().transformClass(cm, ClassTransform.transformingFields(FieldTransform.dropping(e -> e instanceof ConstantValueAttribute)));
        ClassModel cm2 = ClassFile.of().parse(out);
        check(cm2.fields().size() == 1 && cm2.fields().get(0).fieldName().stringValue().equals("x"), "field retained");
        check(cm2.fields().get(0).findAttribute(Attributes.constantValue()).isEmpty(), "constant value dropped");

    }

    // ---- Part 3: ACCEPT_ALL 存在性 + 经 transformingFields 使用（字段/属性全守恒） ----
    static void testAcceptAll() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("FTA"), cb -> cb
                .withField("x", ConstantDescs.CD_int, fb -> fb.withFlags(ClassFile.ACC_PRIVATE | ClassFile.ACC_STATIC)
                        .with(ConstantValueAttribute.of(42)).with(DeprecatedAttribute.of())));
        ClassModel src = cf.parse(b);
        ClassModel out = cf.parse(cf.transformClass(src, ClassTransform.transformingFields(FieldTransform.ACCEPT_ALL)));
        check(out.fields().size() == 1, "ACCEPT_ALL keeps field");
        FieldModel fm = out.fields().get(0);
        check(fm.fieldName().stringValue().equals("x"), "ACCEPT_ALL keeps field name");
        check(fm.fieldTypeSymbol().descriptorString().equals("I"), "ACCEPT_ALL keeps type");
        check(fm.flags().has(AccessFlag.PRIVATE) && fm.flags().has(AccessFlag.STATIC), "ACCEPT_ALL keeps flags");
        check(fm.findAttribute(Attributes.constantValue()).isPresent(), "ACCEPT_ALL keeps ConstantValue");
        check(fm.findAttribute(Attributes.deprecated()).isPresent(), "ACCEPT_ALL keeps Deprecated");
        check(FieldTransform.ACCEPT_ALL == FieldTransform.ACCEPT_ALL, "ACCEPT_ALL singleton identity");
        check(ClassFileTransform.class.isAssignableFrom(FieldTransform.class), "FieldTransform is-a ClassFileTransform");
    }

    // ---- Part 4: dropping 按属性名剥 + endHandler 注入属性 → re-parse ----
    static void testDroppingAndEndHandler() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("FTD"), cb -> cb
                .withField("x", ConstantDescs.CD_int, fb -> fb.with(ConstantValueAttribute.of(7)).with(DeprecatedAttribute.of()))
                .withField("y", ConstantDescs.CD_int, 0));
        ClassModel src = cf.parse(b);
        // dropping 剥 ConstantValue（保留字段本身与 Deprecated）
        ClassModel noCv = cf.parse(cf.transformClass(src, ClassTransform.transformingFields(FieldTransform.dropping(e -> e instanceof ConstantValueAttribute))));
        check(noCv.fields().get(0).findAttribute(Attributes.constantValue()).isEmpty(), "dropping strips ConstantValue");
        check(noCv.fields().get(0).findAttribute(Attributes.deprecated()).isPresent(), "dropping keeps other attrs");
        check(noCv.fields().get(0).fieldName().stringValue().equals("x"), "dropping keeps field itself");
        // dropping 剥全部属性元素
        ClassModel noAttrs = cf.parse(cf.transformClass(src, ClassTransform.transformingFields(FieldTransform.dropping(e -> e instanceof Attribute<?>))));
        check(noAttrs.fields().get(0).attributes().isEmpty(), "dropping(all attrs) empties field attributes");
        check(noAttrs.fields().size() == 2, "fields retained");
        // endHandler 注入 SyntheticAttribute → re-parse
        ClassModel inj = cf.parse(cf.transformClass(src, ClassTransform.transformingFields(
                FieldTransform.endHandler(fb -> fb.with(SyntheticAttribute.of())))));
        check(inj.fields().stream().allMatch(f -> f.findAttribute(Attributes.synthetic()).isPresent()), "endHandler injects Synthetic on every field");
        check(inj.fields().get(0).findAttribute(Attributes.constantValue()).isPresent(), "endHandler keeps existing attrs");
        // andThen：dropping(constantValue).andThen(endHandler(deprecated)) 双效
        ClassModel both = cf.parse(cf.transformClass(src, ClassTransform.transformingFields(
                FieldTransform.dropping(e -> e instanceof ConstantValueAttribute)
                        .andThen(FieldTransform.endHandler(fb -> fb.with(DeprecatedAttribute.of()))))));
        check(both.fields().get(0).findAttribute(Attributes.constantValue()).isEmpty(), "andThen dropping effect");
        check(both.fields().stream().allMatch(f -> f.findAttribute(Attributes.deprecated()).isPresent()), "andThen endHandler effect");
    }

    // ---- Part 5: ofStateful 每字段新实例（实测 2 字段 → 2 次 Supplier）+ null 负例 ----
    static void testOfStateful() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("FTS"), cb -> cb
                .withField("x", ConstantDescs.CD_int, 0)
                .withField("y", ConstantDescs.CD_int, 0));
        int[] supplies = {0};
        FieldTransform st = FieldTransform.ofStateful(() -> { supplies[0]++; return FieldTransform.ACCEPT_ALL; });
        cf.transformClass(cf.parse(b), ClassTransform.transformingFields(st));
        check(supplies[0] == 2, "ofStateful supplier per field: " + supplies[0]);
        try {
            FieldTransform.ofStateful(null);
            check(false, "ofStateful(null) must throw");
        } catch (NullPointerException expected) {
            check(true, "ofStateful(null) NPE");
        }
        try {
            FieldTransform.endHandler(null);
            check(false, "endHandler(null) must throw");
        } catch (NullPointerException expected) {
            check(true, "endHandler(null) NPE");
        }
        try {
            FieldTransform.dropping(null);
            check(false, "dropping(null) must throw");
        } catch (NullPointerException expected) {
            check(true, "dropping(null) NPE");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testAcceptAll();
        testDroppingAndEndHandler();
        testOfStateful();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

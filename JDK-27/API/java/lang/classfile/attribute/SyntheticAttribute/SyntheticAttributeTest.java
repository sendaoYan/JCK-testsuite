/*
 * @test
 * @summary java.lang.classfile.attribute.SyntheticAttribute conformance: white-box structure + black-box behavior.
 * @run main SyntheticAttributeTest
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

public class SyntheticAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.SyntheticAttribute");
        check(c != null, "java.lang.classfile.attribute.SyntheticAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.SyntheticAttribute"), "binary name java.lang.classfile.attribute.SyntheticAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.SyntheticAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.SyntheticAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.SyntheticAttribute has no superclass");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        SyntheticAttribute a = SyntheticAttribute.of();
        check(a.attributeName().stringValue().equals("Synthetic"), "attributeName");
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.with(SyntheticAttribute.of()));
        check(ClassFile.of().parse(b).findAttribute(Attributes.synthetic()).isPresent(), "round-trip");

    }


    // ---- real-product member（javac 用 ACC_SYNTHETIC 标志而非 Synthetic 属性——v49+）----
    enum SynE { X }

    // ---- Part 3: of() 三级注入→re-parse 存在 + verify 干净 ----
    static void testInjectRoundTrip() throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("DynSYN"), cb -> cb
                .withSuperclass(ConstantDescs.CD_Object)
                .with(SyntheticAttribute.of())
                .withField("f", ConstantDescs.CD_int, fb -> fb.with(SyntheticAttribute.of()))
                .withMethod("m", MethodTypeDesc.ofDescriptor("()V"), 0, mb -> mb
                        .with(SyntheticAttribute.of()).withCode(cob -> cob.return_())));
        ClassModel m = ClassFile.of().parse(b);
        check(m.findAttribute(Attributes.synthetic()).isPresent(), "class-level present");
        check(m.fields().get(0).findAttribute(Attributes.synthetic()).isPresent(), "field-level present");
        check(m.methods().get(0).findAttribute(Attributes.synthetic()).isPresent(), "method-level present");
        check(m.findAttribute(Attributes.synthetic()).get().attributeMapper() == Attributes.synthetic(), "parsed mapper identity");
        check(ClassFile.of().verify(m).isEmpty(), "verify clean");
    }

    // ---- Part 4: 属性面（NAME/稳定性/多重性）----
    static void testAttributeSurface() {
        SyntheticAttribute a = SyntheticAttribute.of();
        check(a.attributeName().stringValue().equals("Synthetic")
                && a.attributeName().stringValue().equals(Attributes.NAME_SYNTHETIC), "NAME_SYNTHETIC");
        check(a.attributeMapper() == Attributes.synthetic(), "mapper identity");
        check(a.attributeMapper().stability() == AttributeMapper.AttributeStability.STATELESS, "stability STATELESS");
        check(a.attributeMapper().allowMultiple(), "allowMultiple true");
        // allowMultiple=true：双注入两个都保留
        try {
            byte[] b2 = ClassFile.of().build(ClassDesc.of("DynSYN2"), cb -> cb
                    .with(SyntheticAttribute.of()).with(SyntheticAttribute.of()));
            int cnt = 0;
            for (Attribute<?> at : ClassFile.of().parse(b2).attributes()) if (at instanceof SyntheticAttribute) cnt++;
            check(cnt == 2, "double synthetic both kept: " + cnt);
        } catch (Throwable t) {
            check(false, "double synthetic build failed: " + t);
        }
    }

    // ---- Part 5: javac 真产物（ACC_SYNTHETIC 标志 vs 属性负空间）----
    static void testRealProduct() throws Exception {
        byte[] eb = SyntheticAttributeTest.class.getResourceAsStream("SyntheticAttributeTest$SynE.class").readAllBytes();
        ClassModel em = ClassFile.of().parse(eb);
        FieldModel valuesField = null;
        for (FieldModel f : em.fields()) if (f.fieldName().stringValue().equals("$VALUES")) valuesField = f;
        check(valuesField != null, "$VALUES field present");
        check(valuesField.flags().has(AccessFlag.SYNTHETIC), "$VALUES carries ACC_SYNTHETIC flag");
        check(!valuesField.findAttribute(Attributes.synthetic()).isPresent(), "but no SyntheticAttribute (v49+ uses flag)");
        for (MethodModel mm : em.methods())
            if (mm.methodName().stringValue().equals("values")) {
                check(mm.flags().flagsMask() == 0x0009, "values() PUBLIC|STATIC mask 9");
                check(!mm.findAttribute(Attributes.synthetic()).isPresent(), "values() no SyntheticAttribute");
            }
        byte[] self = SyntheticAttributeTest.class.getResourceAsStream("SyntheticAttributeTest.class").readAllBytes();
        check(ClassFile.of().parse(self).findAttribute(Attributes.synthetic()).isEmpty(), "test class itself no SyntheticAttribute");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testInjectRoundTrip();
        testAttributeSurface();
        testRealProduct();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

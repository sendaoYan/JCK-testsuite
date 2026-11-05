/*
 * @test
 * @summary java.lang.classfile.attribute.RecordAttribute conformance: white-box structure + black-box behavior.
 * @run main RecordAttributeTest
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

public class RecordAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.RecordAttribute");
        check(c != null, "java.lang.classfile.attribute.RecordAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.RecordAttribute"), "binary name java.lang.classfile.attribute.RecordAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.RecordAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.RecordAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.RecordAttribute has no superclass");
        check(declares(c, "components"), "declares components");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        RecordComponentInfo rci = RecordComponentInfo.of("x", ConstantDescs.CD_int);
        RecordAttribute a = RecordAttribute.of(rci);
        check(a.components().size() == 1, "size");
        check(a.components().get(0).name().stringValue().equals("x"), "name");
        check(a.components().get(0).descriptorSymbol().equals(ConstantDescs.CD_int), "descriptor");

    }


    // ---- real-product members ----
    record RAPoint(int x, int y) {}
    record RAGeneric<T>(T v, java.util.List<String> names) {}

    // ---- Part 3: javac 真产物（record 组件表）----
    static void testRealProduct() throws Exception {
        byte[] rb = RecordAttributeTest.class.getResourceAsStream("RecordAttributeTest$RAPoint.class").readAllBytes();
        var at = ClassFile.of().parse(rb).findAttribute(Attributes.record()).get();
        check(at.components().size() == 2, "RAPoint component count");
        check(at.components().get(0).name().stringValue().equals("x"), "comp0 name");
        check(at.components().get(0).descriptor().stringValue().equals("I"), "comp0 descriptor");
        check(at.components().get(1).name().stringValue().equals("y"), "comp1 name");
        check(at.components().get(1).descriptor().stringValue().equals("I"), "comp1 descriptor");
        check(at.components().get(0).attributes().isEmpty(), "plain component no attrs");
        check(at.attributeMapper() == Attributes.record(), "parsed mapper identity");
        // 泛型 record 组件携带 Signature
        byte[] gb = RecordAttributeTest.class.getResourceAsStream("RecordAttributeTest$RAGeneric.class").readAllBytes();
        var ga = ClassFile.of().parse(gb).findAttribute(Attributes.record()).get();
        check(ga.components().size() == 2, "RAGeneric component count");
        check(ga.components().get(0).descriptor().stringValue().equals("Ljava/lang/Object;"), "generic comp descriptor erased");
        boolean sigV = false, sigN = false;
        for (Attribute<?> a : ga.components().get(0).attributes())
            if (a instanceof SignatureAttribute s && s.signature().stringValue().equals("TT;")) sigV = true;
        for (Attribute<?> a : ga.components().get(1).attributes())
            if (a instanceof SignatureAttribute s && s.signature().stringValue().equals("Ljava/util/List<Ljava/lang/String;>;")) sigN = true;
        check(sigV, "generic comp Signature TT;");
        check(sigN, "list comp Signature");
    }

    // ---- Part 4: 工厂 of(List)/of(varargs) + null 负例 + 属性面 ----
    static void testFactoryOverloads() {
        RecordComponentInfo c1 = RecordComponentInfo.of("a", ConstantDescs.CD_int, List.of());
        RecordComponentInfo c2 = RecordComponentInfo.of("b", ConstantDescs.CD_String);
        RecordAttribute a1 = RecordAttribute.of(List.of(c1, c2));
        check(a1.components().size() == 2 && a1.components().get(0) == c1, "of(List) identity");
        RecordAttribute a2 = RecordAttribute.of(c1);
        check(a2.components().get(0) == c1, "of(varargs)");
        check(RecordAttribute.of(new RecordComponentInfo[0]).components().isEmpty(), "of(empty varargs)");
        check(RecordAttribute.of(List.of()).components().isEmpty(), "of(empty List)");
        check(a1.attributeName().stringValue().equals("Record")
                && a1.attributeName().stringValue().equals(Attributes.NAME_RECORD), "NAME_RECORD");
        check(a1.attributeMapper() == Attributes.record(), "mapper identity");
        check(a1.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!a1.attributeMapper().allowMultiple(), "allowMultiple false");
        try { RecordAttribute.of((List<RecordComponentInfo>) null); check(false, "of(null) must throw"); }
        catch (NullPointerException e) { check(true, "of(null List) NPE"); }
    }

    // ---- Part 5: 注入闭环（组件名/描述符/携带属性全回读）----
    static void testInjectRoundTrip() throws Exception {
        RecordComponentInfo rc = RecordComponentInfo.of("n", ConstantDescs.CD_Object,
                List.of(SignatureAttribute.of(Signature.parseFrom("TT;"))));
        byte[] b = ClassFile.of().build(ClassDesc.of("DynRDA"), cb -> cb.withSuperclass(ConstantDescs.CD_Object)
                .with(RecordAttribute.of(List.of(RecordComponentInfo.of("x", ConstantDescs.CD_int, List.of()), rc))));
        ClassModel m = ClassFile.of().parse(b);
        var at = m.findAttribute(Attributes.record()).get();
        check(at.components().size() == 2, "inject component count");
        check(at.components().get(0).name().stringValue().equals("x"), "inject comp0 name");
        check(at.components().get(0).descriptor().stringValue().equals("I"), "inject comp0 descriptor");
        check(at.components().get(1).attributes().size() == 1, "inject comp1 attr count");
        check(((SignatureAttribute) at.components().get(1).attributes().get(0)).signature().stringValue().equals("TT;"), "inject comp1 Signature");
        check(at.attributeMapper() == Attributes.record(), "parsed mapper identity");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRealProduct();
        testFactoryOverloads();
        testInjectRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

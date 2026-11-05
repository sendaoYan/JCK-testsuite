/*
 * @test
 * @summary java.lang.classfile.attribute.RecordComponentInfo conformance: white-box structure + black-box behavior.
 * @run main RecordComponentInfoTest
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

public class RecordComponentInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.RecordComponentInfo");
        check(c != null, "java.lang.classfile.attribute.RecordComponentInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.RecordComponentInfo"), "binary name java.lang.classfile.attribute.RecordComponentInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.RecordComponentInfo is public");
        check(c.isInterface(), "java.lang.classfile.attribute.RecordComponentInfo is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.RecordComponentInfo has no superclass");
        check(declares(c, "name"), "declares name");
        check(declares(c, "descriptor"), "declares descriptor");
        check(declares(c, "descriptorSymbol"), "declares descriptorSymbol");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        RecordComponentInfo rci = RecordComponentInfo.of("x", ConstantDescs.CD_int);
        check(rci.name().stringValue().equals("x"), "name");
        check(rci.descriptor().stringValue().equals("I"), "descriptor");
        check(rci.descriptorSymbol().equals(ConstantDescs.CD_int), "descriptorSymbol");

    }


    // ---- real-product member ----
    record RCBox<T>(T v) {}

    // ---- Part 3: 工厂 4 重载（javap 锁定）----
    static void testFactoryOverloads() {
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        Utf8Entry n = cpb.utf8Entry("f");
        Utf8Entry d = cpb.utf8Entry("I");
        RecordComponentInfo i1 = RecordComponentInfo.of(n, d, List.of());
        check(i1.name() == n && i1.descriptor() == d, "of(utf,utf,List) entry identity");
        check(i1.attributes().isEmpty(), "of(utf,utf,List) empty attrs");
        RecordComponentInfo i2 = RecordComponentInfo.of(n, d, new Attribute<?>[0]);
        check(i2.attributes().isEmpty(), "of(utf,utf,varargs)");
        SignatureAttribute sig = SignatureAttribute.of(Signature.parseFrom("I"));
        RecordComponentInfo i3 = RecordComponentInfo.of("g", ConstantDescs.CD_int, List.of(sig));
        check(i3.name().stringValue().equals("g") && i3.descriptor().stringValue().equals("I"), "of(String,ClassDesc,List)");
        check(i3.attributes().get(0) == sig, "attr carried by identity");
        RecordComponentInfo i4 = RecordComponentInfo.of("h", ConstantDescs.CD_String);
        check(i4.descriptor().stringValue().equals("Ljava/lang/String;"), "of(String,ClassDesc,empty varargs)");
        check(i4.attributes().isEmpty(), "no attrs default");
        check(i4.descriptorSymbol().descriptorString().equals("Ljava/lang/String;"), "descriptorSymbol default method");
        try { RecordComponentInfo.of(null, (ClassDesc) null, (Attribute<?>[]) null); check(false, "of(nulls) must throw"); }
        catch (NullPointerException e) { check(true, "of(nulls) NPE"); }
    }

    // ---- Part 4: javac 真产物（泛型 record 组件）----
    static void testRealProduct() throws Exception {
        byte[] bb = RecordComponentInfoTest.class.getResourceAsStream("RecordComponentInfoTest$RCBox.class").readAllBytes();
        var at = ClassFile.of().parse(bb).findAttribute(Attributes.record()).get();
        check(at.components().size() == 1, "RCBox single component");
        RecordComponentInfo c = at.components().get(0);
        check(c.name().stringValue().equals("v"), "component name");
        check(c.descriptor().stringValue().equals("Ljava/lang/Object;"), "component descriptor erased");
        check(c.descriptorSymbol().descriptorString().equals("Ljava/lang/Object;"), "descriptorSymbol");
        boolean sig = false;
        for (Attribute<?> a : c.attributes())
            if (a instanceof SignatureAttribute s && s.signature().stringValue().equals("TT;")) sig = true;
        check(sig, "component Signature == TT;");
        check(c.attributes().get(0).attributeMapper() == Attributes.signature(), "component attr mapper identity");
    }

    // ---- Part 5: 注入闭环（经 RecordAttribute 写入类再回读）----
    static void testInjectRoundTrip() throws Exception {
        RecordComponentInfo rc = RecordComponentInfo.of("p", ConstantDescs.CD_long,
                List.of(SignatureAttribute.of(Signature.parseFrom("J"))));
        byte[] b = ClassFile.of().build(ClassDesc.of("DynRCI"), cb -> cb.withSuperclass(ConstantDescs.CD_Object)
                .with(RecordAttribute.of(List.of(rc))));
        RecordComponentInfo back = ClassFile.of().parse(b).findAttribute(Attributes.record()).get().components().get(0);
        check(back.name().stringValue().equals("p"), "inject name roundtrip");
        check(back.descriptor().stringValue().equals("J"), "inject descriptor roundtrip");
        check(back.attributes().size() == 1, "inject attr roundtrip count");
        check(((SignatureAttribute) back.attributes().get(0)).signature().stringValue().equals("J"), "inject Signature roundtrip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryOverloads();
        testRealProduct();
        testInjectRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

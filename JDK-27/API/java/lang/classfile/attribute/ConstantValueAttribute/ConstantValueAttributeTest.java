/*
 * @test
 * @summary java.lang.classfile.attribute.ConstantValueAttribute conformance: white-box structure + black-box behavior.
 * @run main ConstantValueAttributeTest
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

public class ConstantValueAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.ConstantValueAttribute");
        check(c != null, "java.lang.classfile.attribute.ConstantValueAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.ConstantValueAttribute"), "binary name java.lang.classfile.attribute.ConstantValueAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.ConstantValueAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.ConstantValueAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.ConstantValueAttribute has no superclass");
        check(declares(c, "constant"), "declares constant");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ConstantValueAttribute a = ConstantValueAttribute.of(42);
        check(a.constant().constantValue().equals(42), "constant 42");
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withField("x", ConstantDescs.CD_int, fb -> fb.with(ConstantValueAttribute.of(42))));
        check(ClassFile.of().parse(b).fields().get(0).findAttribute(Attributes.constantValue()).get().constant().constantValue().equals(42), "round-trip");

    }

    // ---- Part 3: 五型工厂 of(ConstantDesc) + 池条目工厂 + null 负例 ----
    static void testFactoryAllTypes() {
        ConstantValueAttribute i = ConstantValueAttribute.of(42);
        check(i.constant() instanceof IntegerEntry && ((IntegerEntry) i.constant()).intValue() == 42, "of(int)->IntegerEntry 42");
        ConstantValueAttribute l = ConstantValueAttribute.of(1L);
        check(l.constant() instanceof LongEntry && ((LongEntry) l.constant()).longValue() == 1L, "of(long)");
        ConstantValueAttribute f = ConstantValueAttribute.of(2.5f);
        check(f.constant() instanceof FloatEntry && ((FloatEntry) f.constant()).floatValue() == 2.5f, "of(float)");
        ConstantValueAttribute d = ConstantValueAttribute.of(3.5);
        check(d.constant() instanceof DoubleEntry && ((DoubleEntry) d.constant()).doubleValue() == 3.5, "of(double)");
        ConstantValueAttribute s = ConstantValueAttribute.of("hello");
        check(s.constant() instanceof StringEntry && ((StringEntry) s.constant()).stringValue().equals("hello"), "of(String)");
        // 边界值
        check(((IntegerEntry) ConstantValueAttribute.of(Integer.MAX_VALUE).constant()).intValue() == Integer.MAX_VALUE, "int max");
        check(((LongEntry) ConstantValueAttribute.of(Long.MIN_VALUE).constant()).longValue() == Long.MIN_VALUE, "long min");
        check(Float.isNaN(((FloatEntry) ConstantValueAttribute.of(Float.NaN).constant()).floatValue()), "float NaN");
        check(Double.isInfinite(((DoubleEntry) ConstantValueAttribute.of(Double.POSITIVE_INFINITY).constant()).doubleValue()), "double +Inf");
        check(((StringEntry) ConstantValueAttribute.of("").constant()).stringValue().isEmpty(), "empty String");
        // 池条目工厂
        ConstantPoolBuilder cp = ConstantPoolBuilder.of();
        IntegerEntry ie = cp.intEntry(7);
        ConstantValueAttribute pe = ConstantValueAttribute.of(ie);
        check(pe.constant() == ie && ((IntegerEntry) pe.constant()).intValue() == 7, "of(ConstantValueEntry) identity");
        ConstantValueAttribute pe2 = ConstantValueAttribute.of(cp.longEntry(-1L));
        check(((LongEntry) pe2.constant()).longValue() == -1L, "of(LongEntry)");
        ConstantValueAttribute pe3 = ConstantValueAttribute.of(cp.stringEntry("sv"));
        check(((StringEntry) pe3.constant()).stringValue().equals("sv"), "of(StringEntry)");
        // null 负例
        try { ConstantValueAttribute.of((java.lang.constant.ConstantDesc) null); check(false, "of((ConstantDesc)null) must NPE"); }
        catch (NullPointerException e) { check(true, "of((ConstantDesc)null) NPE"); }
        try { ConstantValueAttribute.of((IntegerEntry) null); check(false, "of((ConstantValueEntry)null) must NPE"); }
        catch (NullPointerException e) { check(true, "of((ConstantValueEntry)null) NPE"); }
        // 元数据
        check(i.attributeName().stringValue().equals(Attributes.NAME_CONSTANT_VALUE), "attributeName==NAME_CONSTANT_VALUE");
        check(i.attributeName().stringValue().equals("ConstantValue"), "attributeName literal");
        check(i.attributeMapper() == Attributes.constantValue(), "attributeMapper==Attributes.constantValue()");
        check(i.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!i.attributeMapper().allowMultiple(), "allowMultiple false");
    }

    // ---- Part 4: build static final 字段注入 → re-parse 五型精确回读 ----
    static void testBuildRoundtrip() {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("CVT1"), cb -> cb
                .withField("i", ConstantDescs.CD_int, fb -> { fb.withFlags(ClassFile.ACC_STATIC | ClassFile.ACC_FINAL); fb.with(ConstantValueAttribute.of(42)); })
                .withField("l", ConstantDescs.CD_long, fb -> { fb.withFlags(ClassFile.ACC_STATIC | ClassFile.ACC_FINAL); fb.with(ConstantValueAttribute.of(1L)); })
                .withField("f", ConstantDescs.CD_float, fb -> { fb.withFlags(ClassFile.ACC_STATIC | ClassFile.ACC_FINAL); fb.with(ConstantValueAttribute.of(2.5f)); })
                .withField("d", ConstantDescs.CD_double, fb -> { fb.withFlags(ClassFile.ACC_STATIC | ClassFile.ACC_FINAL); fb.with(ConstantValueAttribute.of(3.5)); })
                .withField("s", ConstantDescs.CD_String, fb -> { fb.withFlags(ClassFile.ACC_STATIC | ClassFile.ACC_FINAL); fb.with(ConstantValueAttribute.of("hello")); })
                .withField("nf", ConstantDescs.CD_int, fb -> { fb.withFlags(ClassFile.ACC_STATIC); fb.with(ConstantValueAttribute.of(9)); }));
        ClassModel cm = cf.parse(b);
        Map<String, ConstantValueEntry> cvs = new HashMap<>();
        for (FieldModel fm : cm.fields()) {
            var o = fm.findAttribute(Attributes.constantValue());
            if (o.isPresent()) cvs.put(fm.fieldName().stringValue(), o.get().constant());
        }
        check(cvs.size() == 6, "6 fields carry ConstantValue (incl non-final injected)");
        check(((IntegerEntry) cvs.get("i")).intValue() == 42, "rt i=42");
        check(((LongEntry) cvs.get("l")).longValue() == 1L, "rt l=1");
        check(((FloatEntry) cvs.get("f")).floatValue() == 2.5f, "rt f=2.5");
        check(((DoubleEntry) cvs.get("d")).doubleValue() == 3.5, "rt d=3.5");
        check(((StringEntry) cvs.get("s")).stringValue().equals("hello"), "rt s=hello");
        check(((IntegerEntry) cvs.get("nf")).intValue() == 9, "rt nf=9");
        // 常量池 tag 表：Integer=3 Float=4 Long=5 Double=6 String=8
        check(cvs.get("i").tag() == 3 && cvs.get("l").tag() == 5 && cvs.get("f").tag() == 4
                && cvs.get("d").tag() == 6 && cvs.get("s").tag() == 8, "CP tag table 3/5/4/6/8");
        check(cvs.get("i").constantPool() != null, "entry bound to pool");
        // 非 static 字段（实例字段）也允许注入
        byte[] b2 = cf.build(ClassDesc.of("CVT2"), cb -> cb
                .withField("x", ConstantDescs.CD_int, fb -> fb.with(ConstantValueAttribute.of(1))));
        check(cf.parse(b2).fields().get(0).findAttribute(Attributes.constantValue()).isPresent(), "instance field injection tolerated");
        // 无注入且非 static final 的字段无 ConstantValue
        byte[] b3 = cf.build(ClassDesc.of("CVT3"), cb -> cb.withField("p", ConstantDescs.CD_int, ClassFile.ACC_STATIC));
        check(cf.parse(b3).fields().get(0).findAttribute(Attributes.constantValue()).isEmpty(), "no injection -> no ConstantValue");
    }

    // ---- Part 5: javac 真类产物（static final 五型）----
    static final int CONST_I = 42;
    static final long CONST_L = 1L;
    static final float CONST_F = 2.5f;
    static final double CONST_D = 3.5;
    static final String CONST_S = "hello";
    static int NON_FINAL = 7;

    static void testRealClass() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] self = ConstantValueAttributeTest.class.getResourceAsStream("ConstantValueAttributeTest.class").readAllBytes();
        ClassModel cm = cf.parse(self);
        Map<String, ConstantValueEntry> cvs = new HashMap<>();
        for (FieldModel fm : cm.fields()) {
            var o = fm.findAttribute(Attributes.constantValue());
            if (o.isPresent()) cvs.put(fm.fieldName().stringValue(), o.get().constant());
        }
        check(cvs.size() == 5, "5 static final constants carry ConstantValue");
        check(((IntegerEntry) cvs.get("CONST_I")).intValue() == 42, "real CONST_I");
        check(((LongEntry) cvs.get("CONST_L")).longValue() == 1L, "real CONST_L");
        check(((FloatEntry) cvs.get("CONST_F")).floatValue() == 2.5f, "real CONST_F");
        check(((DoubleEntry) cvs.get("CONST_D")).doubleValue() == 3.5, "real CONST_D");
        check(((StringEntry) cvs.get("CONST_S")).stringValue().equals("hello"), "real CONST_S");
        check(!cvs.containsKey("NON_FINAL"), "non-final static has no ConstantValue");
        check(cf.verify(cm).isEmpty(), "real class verifies");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryAllTypes();
        testBuildRoundtrip();
        testRealClass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

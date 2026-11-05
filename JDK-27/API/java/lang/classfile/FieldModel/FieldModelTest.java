/*
 * @test
 * @summary java.lang.classfile.FieldModel conformance: white-box structure + black-box behavior.
 * @run main FieldModelTest
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

public class FieldModelTest {
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
        Class<?> c = Class.forName("java.lang.classfile.FieldModel");
        check(c != null, "java.lang.classfile.FieldModel loads");
        check(c.getName().equals("java.lang.classfile.FieldModel"), "binary name java.lang.classfile.FieldModel");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.FieldModel is public");
        check(c.isInterface(), "java.lang.classfile.FieldModel is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.FieldModel has no superclass");
        check(declares(c, "flags"), "declares flags");
        check(declares(c, "parent"), "declares parent");
        check(declares(c, "fieldName"), "declares fieldName");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withField("count", ConstantDescs.CD_long, ClassFile.ACC_PRIVATE | ClassFile.ACC_STATIC | ClassFile.ACC_FINAL));
        ClassModel cm = ClassFile.of().parse(b);
        FieldModel f = cm.fields().get(0);
        check(f.fieldName().stringValue().equals("count"), "name");
        check(f.fieldType().stringValue().equals("J"), "descriptor");
        check(f.fieldTypeSymbol().equals(ConstantDescs.CD_long), "type symbol");
        check(f.flags().has(AccessFlag.PRIVATE) && f.flags().has(AccessFlag.STATIC) && f.flags().has(AccessFlag.FINAL), "flags");
        check(f.parent().isPresent(), "parent");
        check(f.elementList().size() >= 1, "elements");

    }


    // ---- real-class members (javac 产物属性源) ----
    static final int CONST = 42;
    static final long LONG_CONST = 0x1122334455667788L;
    static final boolean BOOL_CONST = true;
    static final String STR_CONST = "hi";
    java.util.List<String> gfield;
    @Deprecated static int depField;
    volatile int vol;

    static FieldModel find(ClassModel cm, String name) {
        for (FieldModel f : cm.fields()) if (f.fieldName().stringValue().equals(name)) return f;
        return null;
    }

    // ---- Part 3: 真类字段族：ConstantValue/Signature/Deprecated/flags ----
    static void testSelfFields() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] self = FieldModelTest.class.getResourceAsStream("FieldModelTest.class").readAllBytes();
        ClassModel cm = cf.parse(self);
        FieldModel konst = find(cm, "CONST");
        check(konst != null, "self CONST located");
        check(konst.findAttribute(Attributes.constantValue()).isPresent(), "CONST ConstantValue present");
        check(((IntegerEntry) konst.findAttribute(Attributes.constantValue()).get().constant()).intValue() == 42, "CONST value 42");
        check(konst.fieldName().stringValue().equals("CONST"), "CONST name");
        check(konst.fieldName().equalsString("CONST"), "CONST equalsString");
        check(konst.fieldType().stringValue().equals("I"), "CONST descriptor I");
        check(konst.fieldTypeSymbol().descriptorString().equals("I"), "CONST fieldTypeSymbol I");
        check(konst.flags().has(AccessFlag.STATIC) && konst.flags().has(AccessFlag.FINAL), "CONST flags STATIC+FINAL");
        check(konst.flags().flagsMask() == (ClassFile.ACC_STATIC | ClassFile.ACC_FINAL), "CONST flagsMask 0x0018");
        check(konst.flags().flags().contains(AccessFlag.STATIC) && konst.flags().flags().contains(AccessFlag.FINAL), "CONST flag set contents");
        check(konst.flags().location() == AccessFlag.Location.FIELD, "CONST flags location FIELD");
        // long 常量 → LongEntry
        FieldModel lc = find(cm, "LONG_CONST");
        check(lc != null && ((LongEntry) lc.findAttribute(Attributes.constantValue()).get().constant()).longValue() == 0x1122334455667788L, "LONG_CONST value");
        // boolean 常量 → IntegerEntry 1
        FieldModel bc = find(cm, "BOOL_CONST");
        check(bc != null && ((IntegerEntry) bc.findAttribute(Attributes.constantValue()).get().constant()).intValue() == 1, "BOOL_CONST value 1");
        // String 常量 → StringEntry
        FieldModel sc = find(cm, "STR_CONST");
        check(sc != null && sc.findAttribute(Attributes.constantValue()).get().constant() instanceof StringEntry, "STR_CONST StringEntry");
        check(sc != null && ((StringEntry) sc.findAttribute(Attributes.constantValue()).get().constant()).stringValue().equals("hi"), "STR_CONST value hi");
        // 泛型字段 Signature
        FieldModel gf = find(cm, "gfield");
        check(gf != null && gf.findAttribute(Attributes.signature()).isPresent(), "gfield Signature present");
        check(gf.findAttribute(Attributes.signature()).get().signature().stringValue().equals("Ljava/util/List<Ljava/lang/String;>;"), "gfield signature exact");
        check(gf.fieldType().stringValue().equals("Ljava/util/List;"), "gfield erasure descriptor");
        check(gf.fieldTypeSymbol().descriptorString().equals("Ljava/util/List;") && !gf.fieldTypeSymbol().isPrimitive(), "gfield fieldTypeSymbol");
        // @Deprecated 字段：Deprecated + RuntimeVisibleAnnotations
        FieldModel df = find(cm, "depField");
        check(df != null && df.findAttribute(Attributes.deprecated()).isPresent(), "depField Deprecated present");
        check(df.findAttribute(Attributes.runtimeVisibleAnnotations()).isPresent(), "depField RuntimeVisibleAnnotations");
        check(df.findAttribute(Attributes.runtimeVisibleAnnotations()).get().annotations().get(0).className().stringValue().equals("Ljava/lang/Deprecated;"), "depField annotation class");
        // volatile 字段 flags
        FieldModel vf = find(cm, "vol");
        check(vf != null && vf.flags().has(AccessFlag.VOLATILE), "vol flags VOLATILE");
        // elementList：全为 FieldElement；含 ConstantValue
        int cvCount = 0;
        for (FieldElement e : konst.elementList()) if (e instanceof ConstantValueAttribute) cvCount++;
        check(cvCount == 1, "CONST elementList ConstantValue count 1");
        check(konst.elementList().size() >= 2, "CONST elementList >= 2 (AccessFlags+ConstantValue)");
        check(konst instanceof CompoundElement, "FieldModel instanceof CompoundElement");
        check(konst instanceof ClassElement, "FieldModel instanceof ClassElement");
        check(konst.parent().isPresent() && konst.parent().get() == cm, "CONST parent identity == cm");
        check(cf.verify(cm).isEmpty(), "self verify clean");
    }

    // ---- Part 4: 构造矩阵：无属性字段 elementList + build 注入回读 ----
    static void testFieldShapes() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("pkg.FShapes"), cb -> cb
                .withField("plain", ConstantDescs.CD_int, ClassFile.ACC_PRIVATE)
                .withField("withCv", ConstantDescs.CD_int,
                        fb -> fb.withFlags(ClassFile.ACC_STATIC | ClassFile.ACC_FINAL).with(ConstantValueAttribute.of(7)))
                .withField("withSig", ConstantDescs.CD_String,
                        fb -> fb.with(SignatureAttribute.of(Signature.parseFrom("Ljava/lang/String;")))));
        ClassModel cm = cf.parse(b);
        FieldModel plain = cm.fields().get(0);
        check(plain.fieldName().stringValue().equals("plain"), "plain name");
        check(plain.elementList().size() == 1 && plain.elementList().get(0) instanceof AccessFlags, "plain elementList [AccessFlags] only");
        check(plain.findAttribute(Attributes.constantValue()).isEmpty(), "plain no ConstantValue");
        FieldModel withCv = cm.fields().get(1);
        check(((IntegerEntry) withCv.findAttribute(Attributes.constantValue()).get().constant()).intValue() == 7, "withCv value 7");
        check(withCv.flags().has(AccessFlag.STATIC) && withCv.flags().has(AccessFlag.FINAL), "withCv flags via withFlags");
        FieldModel withSig = cm.fields().get(2);
        check(withSig.findAttribute(Attributes.signature()).get().signature().stringValue().equals("Ljava/lang/String;"), "withSig signature roundtrip");
        check(cf.verify(cm).isEmpty(), "shapes verify clean");
        for (FieldModel f : cm.fields()) check(f.parent().isPresent() && f.parent().get() == cm, "shape parent identity");
    }

    // ---- Part 5: defineClass 运行闭环（getstatic 读常量） ----
    static int dynSeq;
    static void testDefineClassLoop() throws Exception {
        ClassFile cf = ClassFile.of();
        String name = "DynFieldModel" + (++dynSeq);
        byte[] b = cf.build(ClassDesc.of(name), cb -> cb
                .withField("CV", ConstantDescs.CD_int,
                        fb -> fb.withFlags(ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC | ClassFile.ACC_FINAL).with(ConstantValueAttribute.of(99)))
                .withMethodBody("get", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        c -> c.getstatic(ClassDesc.of(name), "CV", ConstantDescs.CD_int).ireturn()));
        ClassModel rm = cf.parse(b);
        FieldModel f = rm.fields().get(0);
        check(((IntegerEntry) f.findAttribute(Attributes.constantValue()).get().constant()).intValue() == 99, "dyn CV 99");
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getField("CV").getInt(null) == 99, "dyn CV reflect 99");
        check(dc.getMethod("get").invoke(null).equals(99), "dyn get()==99 运行闭环");
        check(cf.verify(rm).isEmpty(), "dyn verify clean");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSelfFields();
        testFieldShapes();
        testDefineClassLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

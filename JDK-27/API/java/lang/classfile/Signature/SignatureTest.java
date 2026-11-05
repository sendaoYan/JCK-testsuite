/*
 * @test
 * @summary java.lang.classfile.Signature conformance: white-box structure + black-box behavior.
 * @run main SignatureTest
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

public class SignatureTest {
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

    // ---- 泛型真类闭环素材：javac 产物自带 Signature 属性（方法/字段两形态） ----
    static <T extends Object> T gen(T t, java.util.List<String> ls) { return t; }
    static final java.util.List<String> GENERIC_FIELD = java.util.List.of();

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.Signature");
        check(c != null, "java.lang.classfile.Signature loads");
        check(c.getName().equals("java.lang.classfile.Signature"), "binary name java.lang.classfile.Signature");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Signature is public");
        check(c.isInterface(), "java.lang.classfile.Signature is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.Signature has no superclass");
        check(declares(c, "signatureString"), "declares signatureString");
        check(declares(c, "parseFrom"), "declares parseFrom");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        Signature s = Signature.parseFrom("Ljava/lang/String;");
        check(s.signatureString().equals("Ljava/lang/String;"), "signatureString");
        Signature si = Signature.of(ConstantDescs.CD_int);
        check(si.signatureString().equals("I"), "of(int) signature");
        Signature sar = Signature.parseFrom("[I");
        check(sar instanceof Signature.ArrayTypeSig, "array sig");

    }

    // ---- Part 3: 合法串 round-trip 幂等（基类型/类类型/数组/泛型/TypeVar/通配全形态） ----
    static void testRoundTripIdempotent() {
        String[] fixed = {
                "I", "J", "Z", "D", "B", "C", "F", "S", "V",                                   // 基类型（V 也可解析为 BaseTypeSig）
                "Ljava/lang/String;",                                                             // 类类型
                "[I", "[[D", "[Ljava/lang/String;",                                              // 数组
                "Ljava/util/List<Ljava/lang/String;>;",                                           // 泛型类类型
                "Ljava/util/Map<TK;TV;>;",                                                        // 泛型 + TypeVar 实参
                "TT;",                                                                            // TypeVar
                "Ljava/util/List<*>;", "Ljava/util/Map<**>;",                                    // 无界通配
                "Ljava/util/List<+Ljava/lang/Number;>;", "Ljava/util/List<-Ljava/lang/Number;>;", // 有界通配
                "LOuter<TT;>.Inner;",                                                             // 内部类点形式
                "[Ljava/util/List<*>;",                                                           // 数组套泛型
        };
        for (String s : fixed) {
            Signature p = Signature.parseFrom(s);
            check(p != null, "parseFrom legal non-null: " + s);
            String out = p.signatureString();
            check(out.equals(s), "round-trip '" + s + "' -> '" + out + "'");
            check(Signature.parseFrom(out).signatureString().equals(s), "re-parse idempotent: " + s);
        }
        // instanceof 分型
        check(Signature.parseFrom("I") instanceof Signature.BaseTypeSig, "I is BaseTypeSig");
        check(Signature.parseFrom("V") instanceof Signature.BaseTypeSig, "V parseable as BaseTypeSig");
        check(Signature.parseFrom("Ljava/lang/String;") instanceof Signature.ClassTypeSig, "L..; is ClassTypeSig");
        check(Signature.parseFrom("[I") instanceof Signature.ArrayTypeSig, "[I is ArrayTypeSig");
        check(Signature.parseFrom("TT;") instanceof Signature.TypeVarSig, "TT; is TypeVarSig");
        check(Signature.parseFrom("Ljava/lang/String;") instanceof Signature.RefTypeSig, "class type is RefTypeSig");
        check(Signature.parseFrom("[I") instanceof Signature.RefTypeSig, "array is RefTypeSig");
        check(!(Signature.parseFrom("I") instanceof Signature.RefTypeSig), "base type is not RefTypeSig");
        check(Signature.parseFrom("Ljava/lang/Exception;") instanceof Signature.ThrowableSig, "class type is ThrowableSig");
        check(Signature.parseFrom("TT;") instanceof Signature.ThrowableSig, "type var is ThrowableSig");
        check(!(Signature.parseFrom("[I") instanceof Signature.ThrowableSig), "array is not ThrowableSig");
        check(!(Signature.parseFrom("I") instanceof Signature.ThrowableSig), "base type is not ThrowableSig");
    }

    // ---- Part 4: of(ClassDesc) 工厂矩阵 ----
    static void testFactoryOf() {
        Signature s = Signature.of(ClassDesc.of("java.lang.String"));
        check(s.signatureString().equals("Ljava/lang/String;"), "of(String) -> Ljava/lang/String;");
        check(s instanceof Signature.ClassTypeSig, "of(String) ClassTypeSig");
        Signature i = Signature.of(ConstantDescs.CD_int);
        check(i.signatureString().equals("I"), "of(int) -> I");
        check(i instanceof Signature.BaseTypeSig && ((Signature.BaseTypeSig) i).baseType() == 'I', "of(int) BaseTypeSig baseType I");
        check(Signature.of(ConstantDescs.CD_void).signatureString().equals("V"), "of(void) -> V");
        Signature arr = Signature.of(ClassDesc.ofDescriptor("[I"));
        check(arr.signatureString().equals("[I"), "of(desc [I) -> [I");
        check(arr instanceof Signature.ArrayTypeSig, "of(desc [I) ArrayTypeSig");
        Signature arr2 = Signature.of(ClassDesc.ofDescriptor("[Ljava/lang/String;"));
        check(arr2.signatureString().equals("[Ljava/lang/String;"), "of(desc [Ljava/lang/String;)");
        check(arr2 instanceof Signature.ArrayTypeSig, "of(desc [Ljava/lang/String;) ArrayTypeSig");
        check(Signature.of(ClassDesc.of("java.lang.String")).equals(Signature.parseFrom("Ljava/lang/String;")), "of == parseFrom value equality");
        try {
            Signature.of(null);
            check(false, "of(null) must throw");
        } catch (NullPointerException e) {
            check(true, "of(null) NPE");
        }
    }

    // ---- Part 5: 畸形串精确异常表（实测锁定，全部 IAE） ----
    static void testMalformedMatrix() {
        String[][] bad = {
                {"bad(", "Unexpected character b at position 0"},
                {"I[", "Not a valid type signature"},
                {"Ljava/lang/Object", "Unexpected end of signature at position 17"},
                {"<", "Unexpected character < at position 0"},
                {"(", "Unexpected character ( at position 0"},
                {"", "Not a valid type signature"},
                {"nope", "Unexpected character n at position 0"},
                {"X", "Unexpected character X at position 0"},
                {"[", "Not a valid type signature"},
                {"TT", "expected ;"},
                {"T", "expected an identifier"},
                {"Ljava/util/List<>;", "Unexpected character > at position 16"},
                {"Ljava/util/List<*", "Not a valid type signature"},
                {"Ljava/lang/String;x", "Not a valid type signature"},
                {"*", "Unexpected character * at position 0"},
                {"+Ljava/lang/Number;", "Unexpected character + at position 0"},
                {";", "Unexpected character ; at position 0"},
        };
        for (String[] row : bad) {
            boolean threw = false;
            try {
                Signature.parseFrom(row[0]);
            } catch (IllegalArgumentException e) {
                threw = true;
                check(e.getMessage() != null && e.getMessage().contains(row[1]),
                        "IAE message for '" + row[0] + "': " + e.getMessage());
            }
            check(threw, "must throw IAE: '" + row[0] + "'");
        }
        try {
            Signature.parseFrom(null);
            check(false, "parseFrom(null) must throw");
        } catch (NullPointerException e) {
            check(true, "parseFrom(null) NPE");
        }
    }

    // ---- Part 6: equals/hashCode/toString 值语义 ----
    static void testEqualityContract() {
        Signature a = Signature.parseFrom("Ljava/lang/String;");
        Signature b = Signature.parseFrom("Ljava/lang/String;");
        check(a.equals(b), "parseFrom equal values equals");
        check(a.hashCode() == b.hashCode(), "hashCode consistent with equals");
        check(!a.equals(Signature.parseFrom("I")), "different type not equals");
        check(!a.equals(null), "not equals null");
        check(!a.equals(Signature.parseFrom("Ljava/lang/Object;")), "different class not equals");
        check(Signature.parseFrom("[I").equals(Signature.parseFrom("[I")), "array value equals");
        check(Signature.parseFrom("TT;").equals(Signature.parseFrom("TT;")), "typevar value equals");
        check(a.toString() != null && !a.toString().isEmpty(), "toString non-empty");
        check(Signature.parseFrom("I").toString() != null, "base toString non-null");
    }

    // ---- Part 7: 真类泛型闭环（javac 产物 Signature 属性 → 模型 → 幂等） ----
    static void testRealClassGenericLoop() throws Exception {
        byte[] self = SignatureTest.class.getResourceAsStream("SignatureTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        MethodModel gen = null;
        for (MethodModel m : cm.methods()) if (m.methodName().stringValue().equals("gen")) gen = m;
        check(gen != null, "gen method found in own class");
        String ms = gen.findAttribute(Attributes.signature()).orElseThrow().signature().stringValue();
        check(ms.equals("<T:Ljava/lang/Object;>(TT;Ljava/util/List<Ljava/lang/String;>;)TT;"), "gen signature exact: " + ms);
        MethodSignature mp = MethodSignature.parseFrom(ms);
        check(mp.signatureString().equals(ms), "javac sig -> model -> sig idempotent");
        check(mp.typeParameters().size() == 1, "gen typeParameters 1");
        check(mp.result() instanceof Signature.TypeVarSig, "gen result TypeVarSig");
        FieldModel gf = null;
        for (FieldModel f : cm.fields()) if (f.fieldName().stringValue().equals("GENERIC_FIELD")) gf = f;
        check(gf != null, "GENERIC_FIELD found in own class");
        String fs = gf.findAttribute(Attributes.signature()).orElseThrow().signature().stringValue();
        check(fs.equals("Ljava/util/List<Ljava/lang/String;>;"), "field signature exact: " + fs);
        Signature fp = Signature.parseFrom(fs);
        check(fp.signatureString().equals(fs), "javac field sig -> model idempotent");
        check(fp instanceof Signature.ClassTypeSig, "field sig ClassTypeSig");
        check(((Signature.ClassTypeSig) fp).className().equals("java/util/List"), "field sig className");
    }

    // ---- Part 8: build 注入 SignatureAttribute → re-parse 回读闭环 ----
    static void testBuildAttributeLoop() throws Exception {
        Signature model = Signature.parseFrom("Ljava/util/List<*>;");
        byte[] b = ClassFile.of().build(ClassDesc.of("SigAttrLoopT"), cb -> cb
                .withField("f", ClassDesc.of("java.util.List"), fb -> fb
                        .withFlags(ClassFile.ACC_PUBLIC)
                        .with(SignatureAttribute.of(model))));
        FieldModel fm = ClassFile.of().parse(b).fields().get(0);
        SignatureAttribute sa = fm.findAttribute(Attributes.signature()).orElseThrow();
        check(sa.signature().stringValue().equals("Ljava/util/List<*>;"), "attr string roundtrip");
        Signature rt = sa.asTypeSignature();
        check(rt.signatureString().equals("Ljava/util/List<*>;"), "asTypeSignature roundtrip");
        check(rt.equals(model), "asTypeSignature value equals original model");
        SignatureAttribute sa2 = SignatureAttribute.of(ConstantPoolBuilder.of().utf8Entry("[I"));
        check(sa2.signature().stringValue().equals("[I"), "of(Utf8Entry) raw string");
        check(sa2.asTypeSignature().signatureString().equals("[I"), "of(Utf8Entry) asTypeSignature");
        check(sa2.asTypeSignature() instanceof Signature.ArrayTypeSig, "of(Utf8Entry) typed as ArrayTypeSig");
        try {
            SignatureAttribute.of((Signature) null);
            check(false, "SignatureAttribute.of(null) must throw");
        } catch (NullPointerException e) {
            check(true, "SignatureAttribute.of(null) NPE");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRoundTripIdempotent();
        testFactoryOf();
        testMalformedMatrix();
        testEqualityContract();
        testRealClassGenericLoop();
        testBuildAttributeLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

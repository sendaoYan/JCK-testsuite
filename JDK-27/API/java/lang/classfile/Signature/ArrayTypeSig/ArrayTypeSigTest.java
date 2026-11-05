/*
 * @test
 * @summary java.lang.classfile.Signature.ArrayTypeSig conformance: white-box structure + black-box behavior.
 * @run main ArrayTypeSigTest
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

public class ArrayTypeSigTest {
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

    // ---- 真类闭环素材：泛型元素数组才有 Signature 属性（元素非泛型时签名==描述符，javac 省略属性） ----
    static final String[] ARRAY_FIELD = {};
    static final int[][] MATRIX_FIELD = new int[0][];
    @SuppressWarnings("unchecked")
    static final java.util.List<String>[] GENERIC_ARRAY_FIELD = new java.util.List[0];

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.Signature$ArrayTypeSig");
        check(c != null, "java.lang.classfile.Signature.ArrayTypeSig loads");
        check(c.getName().equals("java.lang.classfile.Signature$ArrayTypeSig"), "binary name java.lang.classfile.Signature$ArrayTypeSig");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Signature.ArrayTypeSig is public");
        check(c.isInterface(), "java.lang.classfile.Signature.ArrayTypeSig is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.Signature.ArrayTypeSig has no superclass");
        check(declares(c, "componentSignature"), "declares componentSignature");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        Signature.ArrayTypeSig a = Signature.ArrayTypeSig.of(Signature.BaseTypeSig.of(ConstantDescs.CD_int));
        check(a.signatureString().equals("[I"), "signatureString");
        check(a.componentSignature() instanceof Signature.BaseTypeSig, "component base");
        Signature.ArrayTypeSig a2 = Signature.ArrayTypeSig.of(2, Signature.BaseTypeSig.of(ConstantDescs.CD_int));
        check(a2.signatureString().equals("[[I"), "2-dim array");

    }

    // ---- Part 3: parseFrom 合法族 round-trip（全基类型 + 引用 + 多维 + 泛型元素 + TypeVar 元素） ----
    static void testParseFamily() {
        String[] legal = {
                "[I", "[J", "[Z", "[D", "[B", "[C", "[F", "[S",
                "[Ljava/lang/String;",
                "[[D", "[[I", "[[Ljava/lang/String;", "[[[Z",
                "[Ljava/util/List<Ljava/lang/String;>;",
                "[Ljava/util/List<*>;",
                "[TT;",
        };
        for (String s : legal) {
            Signature p = Signature.parseFrom(s);
            check(p instanceof Signature.ArrayTypeSig, "parse '" + s + "' ArrayTypeSig");
            String out = p.signatureString();
            check(out.equals(s), "round-trip '" + s + "' -> '" + out + "'");
            check(Signature.parseFrom(out).signatureString().equals(s), "re-parse idempotent: " + s);
        }
    }

    // ---- Part 4: componentSignature 逐层下钻 ----
    static void testComponentWalk() {
        Signature.ArrayTypeSig d2 = (Signature.ArrayTypeSig) Signature.parseFrom("[[D");
        check(d2.componentSignature() instanceof Signature.ArrayTypeSig, "[[D comp is ArrayTypeSig");
        check(d2.componentSignature().signatureString().equals("[D"), "[[D comp [D");
        Signature.ArrayTypeSig d1 = (Signature.ArrayTypeSig) d2.componentSignature();
        check(d1.componentSignature() instanceof Signature.BaseTypeSig, "[D comp is BaseTypeSig");
        check(((Signature.BaseTypeSig) d1.componentSignature()).baseType() == 'D', "bottom baseType D");
        Signature.ArrayTypeSig s1 = (Signature.ArrayTypeSig) Signature.parseFrom("[Ljava/lang/String;");
        check(s1.componentSignature() instanceof Signature.ClassTypeSig, "ref array comp ClassTypeSig");
        check(((Signature.ClassTypeSig) s1.componentSignature()).className().equals("java/lang/String"), "comp className");
        Signature.ArrayTypeSig g1 = (Signature.ArrayTypeSig) Signature.parseFrom("[Ljava/util/List<*>;");
        check(((Signature.ClassTypeSig) g1.componentSignature()).typeArgs().get(0) instanceof Signature.TypeArg.Unbounded,
                "generic comp wildcard arg");
        Signature.ArrayTypeSig t1 = (Signature.ArrayTypeSig) Signature.parseFrom("[TT;");
        check(t1.componentSignature() instanceof Signature.TypeVarSig, "typevar comp");
        check(((Signature.TypeVarSig) t1.componentSignature()).identifier().equals("T"), "typevar comp id T");
    }

    // ---- Part 5: of 工厂矩阵（of(Signature)/of(int,Signature) + 深度边界） ----
    static void testFactoryMatrix() {
        Signature.BaseTypeSig i = Signature.BaseTypeSig.of(ConstantDescs.CD_int);
        check(Signature.ArrayTypeSig.of(i).signatureString().equals("[I"), "of(sig) depth 1");
        Signature.ArrayTypeSig d2 = Signature.ArrayTypeSig.of(2, i);
        check(d2.signatureString().equals("[[I"), "of(2, sig) depth 2");
        check(d2.componentSignature() instanceof Signature.ArrayTypeSig, "of(2,sig) comp nested");
        check(d2.componentSignature().signatureString().equals("[I"), "of(2,sig) comp [I");
        check(Signature.ArrayTypeSig.of(3, Signature.parseFrom("Ljava/lang/String;")).signatureString()
                .equals("[[[Ljava/lang/String;"), "of(3, ref)");
        // 跨工厂值相等
        check(Signature.parseFrom("[I").equals(Signature.ArrayTypeSig.of(i)), "parseFrom == of value equality");
        check(Signature.parseFrom("[[D").equals(Signature.ArrayTypeSig.of(2, Signature.BaseTypeSig.of('D'))), "depth-2 value equality");
        // 深度边界负例（实测 IAE "illegal array depth value"）
        for (int bad : new int[]{0, -1}) {
            boolean threw = false;
            try {
                Signature.ArrayTypeSig.of(bad, i);
            } catch (IllegalArgumentException e) {
                threw = true;
                check(e.getMessage() != null && e.getMessage().contains("illegal array depth value"),
                        "depth " + bad + " IAE message: " + e.getMessage());
            }
            check(threw, "depth " + bad + " must throw IAE");
        }
        try {
            Signature.ArrayTypeSig.of(null);
            check(false, "of(null) must throw");
        } catch (NullPointerException e) {
            check(true, "of(null) NPE");
        }
    }

    // ---- Part 6: 类型层次（Signature/RefTypeSig 是，ThrowableSig 否） ----
    static void testSubtypingMatrix() {
        Signature.ArrayTypeSig a = (Signature.ArrayTypeSig) Signature.parseFrom("[I");
        Signature asSig = a; // 以根类型静态视图做跨子型 instanceof
        check(asSig instanceof Signature, "ArrayTypeSig is Signature");
        check(asSig instanceof Signature.RefTypeSig, "ArrayTypeSig is RefTypeSig");
        check(!(asSig instanceof Signature.ThrowableSig), "ArrayTypeSig not ThrowableSig");
        check(!(asSig instanceof Signature.ClassTypeSig), "ArrayTypeSig not ClassTypeSig");
        check(Signature.RefTypeSig.class.isAssignableFrom(Signature.ArrayTypeSig.class), "RefTypeSig assignable");
        // 嵌套数组元素也是 RefTypeSig（引用元素）
        Signature.ArrayTypeSig r = (Signature.ArrayTypeSig) Signature.parseFrom("[Ljava/lang/String;");
        check(r.componentSignature() instanceof Signature.RefTypeSig, "ref element is RefTypeSig");
    }

    // ---- Part 7: 真类闭环（javac 数组字段 Signature 属性 → ArrayTypeSig 模型） ----
    static void testRealClassFieldLoop() throws Exception {
        byte[] self = ArrayTypeSigTest.class.getResourceAsStream("ArrayTypeSigTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        // 实测：元素非泛型的数组字段，签名==描述符，javac 不发 Signature 属性
        for (String name : new String[]{"ARRAY_FIELD", "MATRIX_FIELD"}) {
            FieldModel fm = null;
            for (FieldModel f : cm.fields()) if (f.fieldName().stringValue().equals(name)) fm = f;
            check(fm != null, name + " found");
            check(fm.findAttribute(Attributes.signature()).isEmpty(), name + " has NO Signature attr (sig==descriptor)");
        }
        // 泛型元素数组：Signature 属性存在且以 ArrayTypeSig 建模
        FieldModel gf = null;
        for (FieldModel f : cm.fields()) if (f.fieldName().stringValue().equals("GENERIC_ARRAY_FIELD")) gf = f;
        check(gf != null, "GENERIC_ARRAY_FIELD found");
        String fs = gf.findAttribute(Attributes.signature()).orElseThrow().signature().stringValue();
        check(fs.equals("[Ljava/util/List<Ljava/lang/String;>;"), "generic array sig exact: " + fs);
        Signature.ArrayTypeSig model = (Signature.ArrayTypeSig) Signature.parseFrom(fs);
        check(model.signatureString().equals(fs), "generic array model idempotent");
        check(model.componentSignature() instanceof Signature.ClassTypeSig, "generic array comp ClassTypeSig");
        Signature.ClassTypeSig comp = (Signature.ClassTypeSig) model.componentSignature();
        check(comp.className().equals("java/util/List"), "comp className List");
        check(comp.typeArgs().get(0) instanceof Signature.TypeArg.Bounded, "comp arg Bounded");
        check(comp.typeArgs().get(0).equals(Signature.TypeArg.of(
                (Signature.RefTypeSig) Signature.parseFrom("Ljava/lang/String;"))), "comp arg value");
    }

    // ---- Part 8: build 注入 SignatureAttribute → re-parse 回读 ----
    static void testBuildAttributeLoop() throws Exception {
        Signature.ArrayTypeSig model = Signature.ArrayTypeSig.of(2, Signature.BaseTypeSig.of('J'));
        byte[] b = ClassFile.of().build(ClassDesc.of("ArrLoopT"), cb -> cb
                .withField("f", ClassDesc.ofDescriptor("[[J"), fb -> fb
                        .withFlags(ClassFile.ACC_PUBLIC)
                        .with(SignatureAttribute.of(model))));
        FieldModel fm = ClassFile.of().parse(b).fields().get(0);
        SignatureAttribute sa = fm.findAttribute(Attributes.signature()).orElseThrow();
        check(sa.signature().stringValue().equals("[[J"), "attr raw [[J");
        check(sa.asTypeSignature().equals(model), "asTypeSignature equals model");
        check(sa.asTypeSignature() instanceof Signature.ArrayTypeSig, "asTypeSignature ArrayTypeSig");
    }

    // ---- Part 9: equals/hashCode 值语义 ----
    static void testEqualityContract() {
        check(Signature.parseFrom("[I").equals(Signature.parseFrom("[I")), "value equals");
        check(Signature.parseFrom("[I").hashCode() == Signature.parseFrom("[I").hashCode(), "hashCode consistent");
        check(!Signature.parseFrom("[I").equals(Signature.parseFrom("[[I")), "depth difference not equals");
        check(!Signature.parseFrom("[I").equals(Signature.parseFrom("[J")), "element difference not equals");
        check(Signature.ArrayTypeSig.of(2, Signature.BaseTypeSig.of('J'))
                .equals(Signature.ArrayTypeSig.of(2, Signature.BaseTypeSig.of('J'))), "factory value equals");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testParseFamily();
        testComponentWalk();
        testFactoryMatrix();
        testSubtypingMatrix();
        testRealClassFieldLoop();
        testBuildAttributeLoop();
        testEqualityContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

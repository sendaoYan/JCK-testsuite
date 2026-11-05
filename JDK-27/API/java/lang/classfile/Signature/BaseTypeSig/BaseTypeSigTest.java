/*
 * @test
 * @summary java.lang.classfile.Signature.BaseTypeSig conformance: white-box structure + black-box behavior.
 * @run main BaseTypeSigTest
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

public class BaseTypeSigTest {
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

    // ---- 真类闭环素材：含基类型参数的泛型返回方法（javac Signature 属性中含 BaseTypeSig） ----
    static java.util.List<String> baseGen(int count, long total) { return null; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.Signature$BaseTypeSig");
        check(c != null, "java.lang.classfile.Signature.BaseTypeSig loads");
        check(c.getName().equals("java.lang.classfile.Signature$BaseTypeSig"), "binary name java.lang.classfile.Signature$BaseTypeSig");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Signature.BaseTypeSig is public");
        check(c.isInterface(), "java.lang.classfile.Signature.BaseTypeSig is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.Signature.BaseTypeSig has no superclass");
        check(declares(c, "baseType"), "declares baseType");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        Signature.BaseTypeSig b = Signature.BaseTypeSig.of(ConstantDescs.CD_int);
        check(b.baseType() == 'I', "baseType I");
        check(b.signatureString().equals("I"), "signatureString");
        check(Signature.BaseTypeSig.of('J').baseType() == 'J', "of(char) J");

    }

    // ---- Part 3: 9 基类型字符全表（of(char)/of(ClassDesc)/parseFrom 三通道） ----
    static void testBaseBattery() {
        char[] chars = {'I', 'J', 'Z', 'D', 'B', 'C', 'F', 'S', 'V'};
        for (char ch : chars) {
            Signature.BaseTypeSig f = Signature.BaseTypeSig.of(ch);
            check(f.baseType() == ch, "of('" + ch + "') baseType");
            check(f.signatureString().equals(String.valueOf(ch)), "of('" + ch + "') signatureString");
            Signature p = Signature.parseFrom(String.valueOf(ch));
            check(p instanceof Signature.BaseTypeSig, "parseFrom('" + ch + "') BaseTypeSig");
            check(((Signature.BaseTypeSig) p).baseType() == ch, "parseFrom('" + ch + "') baseType");
            check(p.signatureString().equals(String.valueOf(ch)), "parseFrom('" + ch + "') round-trip");
            check(f.equals(p), "of('" + ch + "') == parseFrom value equality");
        }
        // of(ClassDesc) 通道：基本类型描述符
        check(Signature.BaseTypeSig.of(ConstantDescs.CD_int).baseType() == 'I', "of(CD_int)");
        check(Signature.BaseTypeSig.of(ConstantDescs.CD_void).baseType() == 'V', "of(CD_void) V 合法");
        check(Signature.BaseTypeSig.of(ClassDesc.ofDescriptor("J")).baseType() == 'J', "of(CD J)");
        check(Signature.BaseTypeSig.of(ConstantDescs.CD_boolean).baseType() == 'Z', "of(CD_boolean)");
        check(Signature.BaseTypeSig.of(ClassDesc.ofDescriptor("D")).baseType() == 'D', "of(CD D)");
    }

    // ---- Part 4: 8 参数方法签名中的基类型逐位矩阵 ----
    static void testMethodArgMatrix() {
        MethodSignature m = MethodSignature.parseFrom("(IJZDBCFS)V");
        char[] expect = {'I', 'J', 'Z', 'D', 'B', 'C', 'F', 'S'};
        check(m.arguments().size() == 8, "8 base args");
        for (int i = 0; i < 8; i++) {
            check(m.arguments().get(i) instanceof Signature.BaseTypeSig
                            && ((Signature.BaseTypeSig) m.arguments().get(i)).baseType() == expect[i],
                    "arg " + i + " baseType " + expect[i]);
        }
        // V 只出现在 result 位（实测 Signature.parseFrom("V") 也可单独解析）
        MethodSignature v = MethodSignature.parseFrom("(I)V");
        check(v.result() instanceof Signature.BaseTypeSig, "result V BaseTypeSig");
        check(((Signature.BaseTypeSig) v.result()).baseType() == 'V', "result baseType V");
        check(Signature.parseFrom("V") instanceof Signature.BaseTypeSig, "standalone V parseable");
        check(Signature.of(ConstantDescs.CD_void).signatureString().equals("V"), "of(CD_void) string V");
    }

    // ---- Part 5: 非法字符/非基本类型负例（实测精确 IAE） ----
    static void testInvalidMatrix() {
        for (char bad : new char[]{'X', 'A', 'x', '0', (char) 0}) {
            boolean threw = false;
            try {
                Signature.BaseTypeSig.of(bad);
            } catch (IllegalArgumentException e) {
                threw = true;
                check(e.getMessage() != null && e.getMessage().contains("invalid base type signature"),
                        "of('" + (int) bad + "') IAE message: " + e.getMessage());
            }
            check(threw, "of('" + bad + "') must throw IAE");
        }
        // 非基本类型 ClassDesc
        boolean threw = false;
        try {
            Signature.BaseTypeSig.of(ClassDesc.of("java.lang.String"));
        } catch (IllegalArgumentException e) {
            threw = true;
            check(e.getMessage() != null && e.getMessage().contains("primitive class type required"),
                    "reference CD IAE message: " + e.getMessage());
        }
        check(threw, "reference CD must throw IAE");
        try {
            Signature.BaseTypeSig.of((ClassDesc) null);
            check(false, "of((ClassDesc)null) must throw");
        } catch (NullPointerException e) {
            check(true, "of((ClassDesc)null) NPE");
        }
        // 畸形串（基类型相关实测）
        String[][] badStr = {{"X", "Unexpected character X at position 0"}, {"I[", "Not a valid type signature"}};
        for (String[] row : badStr) {
            boolean t = false;
            try {
                Signature.parseFrom(row[0]);
            } catch (IllegalArgumentException e) {
                t = true;
                check(e.getMessage() != null && e.getMessage().contains(row[1]), "IAE msg '" + row[0] + "': " + e.getMessage());
            }
            check(t, "parseFrom('" + row[0] + "') must throw IAE");
        }
    }

    // ---- Part 6: 类型层次（Signature 是，RefTypeSig/ThrowableSig 否） ----
    static void testSubtypingMatrix() {
        Signature.BaseTypeSig b = Signature.BaseTypeSig.of('I');
        Signature asSig = b; // 以根类型静态视图做跨子型 instanceof
        check(asSig instanceof Signature, "BaseTypeSig is Signature");
        check(!(asSig instanceof Signature.RefTypeSig), "BaseTypeSig not RefTypeSig");
        check(!(asSig instanceof Signature.ThrowableSig), "BaseTypeSig not ThrowableSig");
        check(!Signature.RefTypeSig.class.isAssignableFrom(Signature.BaseTypeSig.class), "RefTypeSig not assignable");
        // 嵌入位置：只能出现在方法参数/返回/数组元素位
        check(Signature.ArrayTypeSig.of(b).componentSignature().equals(b), "array of base comp equals");
        check(Signature.ArrayTypeSig.of(b).signatureString().equals("[I"), "array of base string");
    }

    // ---- Part 7: 真类闭环（javac 产物 Signature 属性中的 BaseTypeSig 参数） ----
    static void testRealClassLoop() throws Exception {
        byte[] self = BaseTypeSigTest.class.getResourceAsStream("BaseTypeSigTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        MethodModel bm = null;
        for (MethodModel m : cm.methods()) if (m.methodName().stringValue().equals("baseGen")) bm = m;
        check(bm != null, "baseGen found");
        String bs = bm.findAttribute(Attributes.signature()).orElseThrow().signature().stringValue();
        check(bs.equals("(IJ)Ljava/util/List<Ljava/lang/String;>;"), "baseGen sig exact: " + bs);
        MethodSignature mm = MethodSignature.parseFrom(bs);
        check(mm.arguments().size() == 2, "baseGen 2 args");
        check(mm.arguments().get(0) instanceof Signature.BaseTypeSig
                && ((Signature.BaseTypeSig) mm.arguments().get(0)).baseType() == 'I', "baseGen arg0 I");
        check(mm.arguments().get(1) instanceof Signature.BaseTypeSig
                && ((Signature.BaseTypeSig) mm.arguments().get(1)).baseType() == 'J', "baseGen arg1 J");
        check(mm.result() instanceof Signature.ClassTypeSig, "baseGen result ClassTypeSig");
    }

    // ---- Part 8: build 注入 SignatureAttribute → re-parse 回读 ----
    static void testBuildAttributeLoop() throws Exception {
        Signature.BaseTypeSig model = Signature.BaseTypeSig.of('I');
        byte[] b = ClassFile.of().build(ClassDesc.of("BaseLoopT"), cb -> cb
                .withField("f", ConstantDescs.CD_int, fb -> fb
                        .withFlags(ClassFile.ACC_PUBLIC)
                        .with(SignatureAttribute.of(model))));
        FieldModel fm = ClassFile.of().parse(b).fields().get(0);
        SignatureAttribute sa = fm.findAttribute(Attributes.signature()).orElseThrow();
        check(sa.signature().stringValue().equals("I"), "attr raw I");
        check(sa.asTypeSignature().equals(model), "asTypeSignature equals model");
        check(sa.asTypeSignature() instanceof Signature.BaseTypeSig, "asTypeSignature BaseTypeSig");
    }

    // ---- Part 9: equals/hashCode 值语义 ----
    static void testEqualityContract() {
        check(Signature.BaseTypeSig.of('I').equals(Signature.parseFrom("I")), "of==parseFrom");
        check(Signature.BaseTypeSig.of('I').hashCode() == Signature.parseFrom("I").hashCode(), "hashCode consistent");
        check(!Signature.BaseTypeSig.of('I').equals(Signature.BaseTypeSig.of('J')), "different base not equals");
        check(!Signature.BaseTypeSig.of('I').equals(null), "not equals null");
        check(Signature.of(ConstantDescs.CD_int).equals(Signature.BaseTypeSig.of('I')), "Signature.of(int)==BaseTypeSig.of('I')");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testBaseBattery();
        testMethodArgMatrix();
        testInvalidMatrix();
        testSubtypingMatrix();
        testRealClassLoop();
        testBuildAttributeLoop();
        testEqualityContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

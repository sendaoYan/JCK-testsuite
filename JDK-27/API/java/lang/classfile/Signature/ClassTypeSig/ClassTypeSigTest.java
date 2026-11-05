/*
 * @test
 * @summary java.lang.classfile.Signature.ClassTypeSig conformance: white-box structure + black-box behavior.
 * @run main ClassTypeSigTest
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

public class ClassTypeSigTest {
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

    // ---- 真类闭环素材：泛型字段（javac 产物 Signature 属性 → ClassTypeSig） ----
    static final java.util.List<String> GENERIC_FIELD = java.util.List.of();

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.Signature$ClassTypeSig");
        check(c != null, "java.lang.classfile.Signature.ClassTypeSig loads");
        check(c.getName().equals("java.lang.classfile.Signature$ClassTypeSig"), "binary name java.lang.classfile.Signature$ClassTypeSig");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Signature.ClassTypeSig is public");
        check(c.isInterface(), "java.lang.classfile.Signature.ClassTypeSig is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.Signature.ClassTypeSig has no superclass");
        check(declares(c, "outerType"), "declares outerType");
        check(declares(c, "className"), "declares className");
        check(declares(c, "classDesc"), "declares classDesc");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        Signature.ClassTypeSig c = Signature.ClassTypeSig.of(ClassDesc.of("java.lang.String"));
        check(c.className().equals("java/lang/String"), "className");
        check(c.classDesc().equals(ClassDesc.of("java.lang.String")), "classDesc");
        check(c.typeArgs().isEmpty(), "no type args");
        check(c.outerType().isEmpty(), "no outer type");
        check(c.signatureString().equals("Ljava/lang/String;"), "signatureString");

    }

    // ---- Part 3: parseFrom 结构矩阵（plain/泛型/TypeVar 实参/内部类点形式/通配） ----
    static void testParseMatrix() {
        Signature.ClassTypeSig plain = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/lang/String;");
        check(plain.className().equals("java/lang/String"), "plain className");
        check(plain.outerType().isEmpty(), "plain no outer");
        check(plain.typeArgs().isEmpty(), "plain no args");
        check(plain.classDesc().descriptorString().equals("Ljava/lang/String;"), "plain classDesc");
        Signature.ClassTypeSig lst = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/List<Ljava/lang/String;>;");
        check(lst.className().equals("java/util/List"), "List className");
        check(lst.typeArgs().size() == 1, "List 1 arg");
        check(lst.typeArgs().get(0) instanceof Signature.TypeArg.Bounded, "List arg Bounded");
        check(((Signature.TypeArg.Bounded) lst.typeArgs().get(0)).boundType().signatureString().equals("Ljava/lang/String;"),
                "List arg bound String");
        check(((Signature.TypeArg.Bounded) lst.typeArgs().get(0)).wildcardIndicator()
                == Signature.TypeArg.Bounded.WildcardIndicator.NONE, "List arg NONE");
        Signature.ClassTypeSig map = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/Map<TK;TV;>;");
        check(map.className().equals("java/util/Map"), "Map className");
        check(map.typeArgs().size() == 2, "Map 2 args");
        check(((Signature.TypeArg.Bounded) map.typeArgs().get(0)).boundType() instanceof Signature.TypeVarSig, "Map arg0 TypeVarSig");
        check(((Signature.TypeVarSig) ((Signature.TypeArg.Bounded) map.typeArgs().get(0)).boundType()).identifier().equals("K"),
                "Map arg0 id K");
        check(((Signature.TypeVarSig) ((Signature.TypeArg.Bounded) map.typeArgs().get(1)).boundType()).identifier().equals("V"),
                "Map arg1 id V");
        // 内部类点形式：owner 带 TypeVar 实参，inner 无
        Signature.ClassTypeSig inner = (Signature.ClassTypeSig) Signature.parseFrom("LOuter<TT;>.Inner;");
        check(inner.className().equals("Inner"), "inner className Inner");
        check(inner.outerType().isPresent(), "inner outer present");
        Signature.ClassTypeSig outer = inner.outerType().get();
        check(outer.className().equals("Outer"), "outer className Outer");
        check(outer.outerType().isEmpty(), "outer no further outer");
        check(outer.typeArgs().size() == 1, "outer 1 arg");
        check(((Signature.TypeVarSig) ((Signature.TypeArg.Bounded) outer.typeArgs().get(0)).boundType()).identifier().equals("T"),
                "outer arg TypeVar T");
        check(inner.typeArgs().isEmpty(), "inner no args");
        check(inner.classDesc().descriptorString().equals("LOuter$Inner;"), "inner classDesc LOuter$Inner;");
        check(inner.classDesc().displayName().equals("Outer$Inner"), "inner classDesc displayName");
        // 通配实参形态
        Signature.ClassTypeSig un = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/List<*>;");
        check(un.typeArgs().get(0) instanceof Signature.TypeArg.Unbounded, "unbounded arg");
        // round-trip 幂等
        String[] legal = {"Ljava/lang/String;", "Ljava/util/List<Ljava/lang/String;>;", "Ljava/util/Map<TK;TV;>;",
                "LOuter<TT;>.Inner;", "Ljava/util/List<*>;", "LOuter<TT;>.Inner<Ljava/lang/String;>;"};
        for (String s : legal) {
            String out = Signature.parseFrom(s).signatureString();
            check(out.equals(s), "round-trip '" + s + "' -> '" + out + "'");
            check(Signature.parseFrom(out).signatureString().equals(s), "re-parse idempotent: " + s);
        }
    }

    // ---- Part 4: of 工厂全 4 重载（CD/String/嵌套 owner 两形态） ----
    static void testFactoryMatrix() {
        Signature.ClassTypeSig f1 = Signature.ClassTypeSig.of(ClassDesc.of("java.lang.Object"));
        check(f1.className().equals("java/lang/Object"), "of(CD) className");
        check(f1.classDesc().equals(ClassDesc.of("java.lang.Object")), "of(CD) classDesc");
        check(f1.signatureString().equals("Ljava/lang/Object;"), "of(CD) string");
        Signature.ClassTypeSig f2 = Signature.ClassTypeSig.of("java/util/List");
        check(f2.className().equals("java/util/List"), "of(String) className");
        check(f2.classDesc().descriptorString().equals("Ljava/util/List;"), "of(String) classDesc");
        Signature.ClassTypeSig f3 = Signature.ClassTypeSig.of(ClassDesc.of("java.util.List"),
                Signature.TypeArg.of((Signature.RefTypeSig) Signature.parseFrom("Ljava/lang/String;")));
        check(f3.signatureString().equals("Ljava/util/List<Ljava/lang/String;>;"), "of(CD, arg) string");
        Signature.ClassTypeSig f4 = Signature.ClassTypeSig.of("java/util/List",
                Signature.TypeArg.unbounded());
        check(f4.signatureString().equals("Ljava/util/List<*>;"), "of(String, unbounded) string");
        Signature.ClassTypeSig o = Signature.ClassTypeSig.of("pkg/Outer");
        Signature.ClassTypeSig f5 = Signature.ClassTypeSig.of(o, "Inner");
        check(f5.signatureString().equals("Lpkg/Outer.Inner;"), "of(outer, \"Inner\") string");
        check(f5.className().equals("Inner"), "of(outer, \"Inner\") className Inner");
        check(f5.outerType().get().className().equals("pkg/Outer"), "of(outer, \"Inner\") outer pkg/Outer");
        check(f5.classDesc().descriptorString().equals("Lpkg/Outer$Inner;"), "of(outer,inner) classDesc");
        Signature.ClassTypeSig ow = Signature.ClassTypeSig.of("pkg/Outer",
                Signature.TypeArg.of((Signature.RefTypeSig) Signature.parseFrom("Ljava/lang/String;")));
        Signature.ClassTypeSig f6 = Signature.ClassTypeSig.of(ow, "Inner", Signature.TypeArg.unbounded());
        check(f6.signatureString().equals("Lpkg/Outer<Ljava/lang/String;>.Inner<*>;"), "of(outer-with-arg, inner, arg) string");
        check(f6.outerType().get().typeArgs().size() == 1, "outer keeps its args");
        @SuppressWarnings({"deprecation", "removal"})
        Signature.ClassTypeSig f7 = Signature.ClassTypeSig.of(o, ClassDesc.of("Inner"));
        check(f7.signatureString().equals("Lpkg/Outer.Inner;"), "deprecated of(outer, CD) string");
        check(Signature.ClassTypeSig.of(ClassDesc.of("java.lang.String"))
                .equals(Signature.parseFrom("Ljava/lang/String;")), "of == parseFrom value equality");
    }

    // ---- Part 5: 负例（嵌套名必须是简单标识符 + null NPE 实测消息） ----
    static void testNegativeMatrix() {
        Signature.ClassTypeSig o = Signature.ClassTypeSig.of("pkg/Outer");
        boolean threw = false;
        try {
            Signature.ClassTypeSig.of(o, "pkg/Outer$Inner");
        } catch (IllegalArgumentException e) {
            threw = true;
            check(e.getMessage() != null && e.getMessage().contains("Not a valid identifier"),
                    "qualified inner name IAE: " + e.getMessage());
        }
        check(threw, "qualified inner name must throw IAE");
        threw = false;
        try {
            Signature.ClassTypeSig.of((ClassDesc) null);
        } catch (NullPointerException e) {
            threw = true;
            check(e.getMessage() != null && e.getMessage().contains("\"cd\" is null"), "of((ClassDesc)null) NPE msg: " + e.getMessage());
        }
        check(threw, "of((ClassDesc)null) NPE");
        threw = false;
        try {
            Signature.ClassTypeSig.of((String) null);
        } catch (NullPointerException e) {
            threw = true;
            check(e.getMessage() != null && e.getMessage().contains("\"st\" is null"), "of((String)null) NPE msg: " + e.getMessage());
        }
        check(threw, "of((String)null) NPE");
    }

    // ---- Part 6: 类型层次（RefTypeSig + ThrowableSig 双身份） ----
    static void testSubtypingMatrix() {
        Signature.ClassTypeSig c = Signature.ClassTypeSig.of(ClassDesc.of("java.lang.String"));
        Signature asSig = c; // 以根类型静态视图做跨子型 instanceof
        check(asSig instanceof Signature, "ClassTypeSig is Signature");
        check(asSig instanceof Signature.RefTypeSig, "ClassTypeSig is RefTypeSig");
        check(asSig instanceof Signature.ThrowableSig, "ClassTypeSig is ThrowableSig");
        check(!(asSig instanceof Signature.ArrayTypeSig), "ClassTypeSig not ArrayTypeSig");
        check(Signature.ThrowableSig.class.isAssignableFrom(Signature.ClassTypeSig.class), "ThrowableSig assignable from ClassTypeSig");
        // 可直接作为 throws 段成员
        MethodSignature ms = MethodSignature.parseFrom("()V^Ljava/lang/Exception;");
        check(ms.throwableSignatures().get(0) instanceof Signature.ClassTypeSig, "ClassTypeSig usable as throwable");
    }

    // ---- Part 7: 真类闭环（javac 泛型字段 Signature 属性 → ClassTypeSig） ----
    static void testRealClassFieldLoop() throws Exception {
        byte[] self = ClassTypeSigTest.class.getResourceAsStream("ClassTypeSigTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        FieldModel gf = null;
        for (FieldModel f : cm.fields()) if (f.fieldName().stringValue().equals("GENERIC_FIELD")) gf = f;
        check(gf != null, "GENERIC_FIELD found");
        String fs = gf.findAttribute(Attributes.signature()).orElseThrow().signature().stringValue();
        check(fs.equals("Ljava/util/List<Ljava/lang/String;>;"), "field sig exact: " + fs);
        Signature.ClassTypeSig model = (Signature.ClassTypeSig) Signature.parseFrom(fs);
        check(model.signatureString().equals(fs), "model idempotent");
        check(model.className().equals("java/util/List"), "className java/util/List");
        check(model.typeArgs().size() == 1, "1 type arg");
        check(((Signature.TypeArg.Bounded) model.typeArgs().get(0)).boundType().signatureString()
                .equals("Ljava/lang/String;"), "arg bound String");
        check(model.classDesc().descriptorString().equals("Ljava/util/List;"), "classDesc roundtrip");
    }

    // ---- Part 8: build 注入 SignatureAttribute → re-parse 回读 ----
    static void testBuildAttributeLoop() throws Exception {
        Signature.ClassTypeSig model = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/Map<TK;TV;>;");
        byte[] b = ClassFile.of().build(ClassDesc.of("CTSLoopT"), cb -> cb
                .withField("f", ClassDesc.of("java.util.Map"), fb -> fb
                        .withFlags(ClassFile.ACC_PUBLIC)
                        .with(SignatureAttribute.of(model))));
        FieldModel fm = ClassFile.of().parse(b).fields().get(0);
        SignatureAttribute sa = fm.findAttribute(Attributes.signature()).orElseThrow();
        check(sa.signature().stringValue().equals("Ljava/util/Map<TK;TV;>;"), "attr raw string");
        check(sa.asTypeSignature().equals(model), "asTypeSignature equals model");
        Signature.ClassTypeSig rt = (Signature.ClassTypeSig) sa.asTypeSignature();
        check(rt.className().equals("java/util/Map") && rt.typeArgs().size() == 2, "asTypeSignature structure");
    }

    // ---- Part 9: equals/hashCode 值语义 ----
    static void testEqualityContract() {
        check(Signature.ClassTypeSig.of(ClassDesc.of("java.lang.String")).equals(Signature.parseFrom("Ljava/lang/String;")), "of==parseFrom");
        check(Signature.parseFrom("Ljava/util/List<Ljava/lang/String;>;").hashCode()
                == Signature.parseFrom("Ljava/util/List<Ljava/lang/String;>;").hashCode(), "hashCode consistent");
        check(!Signature.ClassTypeSig.of(ClassDesc.of("a.B")).equals(Signature.ClassTypeSig.of(ClassDesc.of("a.C"))), "different class not equals");
        check(!Signature.parseFrom("Ljava/util/List<Ljava/lang/String;>;").equals(Signature.parseFrom("Ljava/util/List<Ljava/lang/Object;>;")),
                "different arg not equals");
        check(!Signature.ClassTypeSig.of(ClassDesc.of("a.B")).equals(null), "not equals null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testParseMatrix();
        testFactoryMatrix();
        testNegativeMatrix();
        testSubtypingMatrix();
        testRealClassFieldLoop();
        testBuildAttributeLoop();
        testEqualityContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

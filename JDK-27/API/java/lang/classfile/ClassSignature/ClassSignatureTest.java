/*
 * @test
 * @summary java.lang.classfile.ClassSignature conformance: white-box structure + black-box behavior.
 * @run main ClassSignatureTest
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

public class ClassSignatureTest {
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

    // ---- 泛型真类闭环素材：javac 为泛型嵌套类生成类级 Signature 属性 ----
    static class CsBox<T> { }
    static class CsPair<K extends Object, V extends java.lang.Comparable<K>> implements java.lang.Runnable {
        public void run() { }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.ClassSignature");
        check(c != null, "java.lang.classfile.ClassSignature loads");
        check(c.getName().equals("java.lang.classfile.ClassSignature"), "binary name java.lang.classfile.ClassSignature");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassSignature is public");
        check(c.isInterface(), "java.lang.classfile.ClassSignature is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.ClassSignature has no superclass");
        check(declares(c, "typeParameters"), "declares typeParameters");
        check(declares(c, "superclassSignature"), "declares superclassSignature");
        check(declares(c, "superinterfaceSignatures"), "declares superinterfaceSignatures");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ClassSignature cs = ClassSignature.parseFrom("Ljava/lang/Object;Ljava/lang/Comparable<Ljava/lang/String;>;");
        check(cs.typeParameters().isEmpty(), "no type params");
        check(cs.superclassSignature().className().equals("java/lang/Object"), "superclass Object");
        check(cs.superinterfaceSignatures().size() == 1, "one superinterface");
        check(cs.superinterfaceSignatures().get(0).className().equals("java/lang/Comparable"), "Comparable");
        check(cs.superinterfaceSignatures().get(0).typeArgs().size() == 1, "Comparable type arg");
        check(cs.signatureString().startsWith("Ljava/lang/Object;"), "signatureString");
        ClassSignature cs2 = ClassSignature.of(Signature.ClassTypeSig.of(ClassDesc.of("java.lang.Object")));
        check(cs2.superclassSignature().classDesc().equals(ClassDesc.of("java.lang.Object")), "of() superclass");

    }

    // ---- Part 3: 合法族 round-trip 幂等 ----
    static void testRoundTripFamily() {
        String[] legal = {
                "Ljava/lang/Object;",
                "<T:Ljava/lang/Object;>Ljava/lang/Object;",
                "<T::Ljava/lang/Comparable<*>;>Ljava/lang/Object;Ljava/lang/Runnable;",
                "Ljava/lang/Object;Ljava/lang/Runnable;",
                "Ljava/lang/Object;Ljava/lang/Runnable;Ljava/io/Serializable;",   // 多 superinterface
                "<T:>Ljava/lang/Object;",                                          // 无 bound（quirk：合法）
                "<E:Ljava/lang/Object;:Ljava/lang/Runnable;:Ljava/io/Serializable;>Ljava/lang/Object;", // 多 interface bound
                "<T:TT;>Ljava/lang/Object;",                                       // TypeVar 作 classBound
                "<K:Ljava/lang/Object;V::Ljava/lang/Comparable<TK;>;>Ljava/util/AbstractMap<TK;TV;>;Ljava/util/Map<TK;TV;>;",
        };
        for (String s : legal) {
            ClassSignature p = ClassSignature.parseFrom(s);
            check(p != null, "parseFrom legal non-null: " + s);
            String out = p.signatureString();
            check(out.equals(s), "round-trip '" + s + "' -> '" + out + "'");
            check(ClassSignature.parseFrom(out).signatureString().equals(s), "re-parse idempotent: " + s);
        }
    }

    // ---- Part 4: 结构矩阵（typeParameters/superclass/superinterfaces + TypeParam 逐访问器） ----
    static void testStructureMatrix() {
        ClassSignature c1 = ClassSignature.parseFrom("Ljava/lang/Object;");
        check(c1.typeParameters().isEmpty(), "c1 no tp");
        check(c1.superclassSignature().className().equals("java/lang/Object"), "c1 superclass Object");
        check(c1.superinterfaceSignatures().isEmpty(), "c1 no ifaces");
        ClassSignature c2 = ClassSignature.parseFrom("<T:Ljava/lang/Object;>Ljava/lang/Object;");
        check(c2.typeParameters().size() == 1, "c2 one tp");
        check(c2.typeParameters().get(0).identifier().equals("T"), "c2 tp id T");
        check(c2.typeParameters().get(0).classBound().isPresent(), "c2 tp classBound present");
        check(c2.typeParameters().get(0).classBound().get().signatureString().equals("Ljava/lang/Object;"), "c2 tp classBound Object");
        check(c2.typeParameters().get(0).interfaceBounds().isEmpty(), "c2 tp no ib");
        ClassSignature c3 = ClassSignature.parseFrom("<T::Ljava/lang/Comparable<*>;>Ljava/lang/Object;Ljava/lang/Runnable;");
        check(c3.typeParameters().size() == 1, "c3 one tp");
        check(!c3.typeParameters().get(0).classBound().isPresent(), "c3 tp classBound empty (::<)" );
        check(c3.typeParameters().get(0).interfaceBounds().size() == 1, "c3 tp one ib");
        check(c3.typeParameters().get(0).interfaceBounds().get(0).signatureString().equals("Ljava/lang/Comparable<*>;"), "c3 tp ib string");
        check(c3.superclassSignature().className().equals("java/lang/Object"), "c3 superclass");
        check(c3.superinterfaceSignatures().size() == 1, "c3 one iface");
        check(c3.superinterfaceSignatures().get(0).className().equals("java/lang/Runnable"), "c3 iface Runnable");
        ClassSignature c4 = ClassSignature.parseFrom("Ljava/lang/Object;Ljava/lang/Runnable;Ljava/io/Serializable;");
        check(c4.superinterfaceSignatures().size() == 2, "c4 two ifaces");
        check(c4.superinterfaceSignatures().get(0).className().equals("java/lang/Runnable"), "c4 iface0");
        check(c4.superinterfaceSignatures().get(1).className().equals("java/io/Serializable"), "c4 iface1 order");
        ClassSignature c5 = ClassSignature.parseFrom("<T:TT;>Ljava/lang/Object;");
        check(c5.typeParameters().get(0).classBound().get() instanceof Signature.TypeVarSig, "c5 classBound TypeVarSig");
        // 泛型超类 + 泛型 iface 的深结构
        ClassSignature c6 = ClassSignature.parseFrom(
                "<K:Ljava/lang/Object;V::Ljava/lang/Comparable<TK;>;>Ljava/util/AbstractMap<TK;TV;>;Ljava/util/Map<TK;TV;>;");
        check(c6.superclassSignature().className().equals("java/util/AbstractMap"), "c6 superclass AbstractMap");
        check(c6.superclassSignature().typeArgs().size() == 2, "c6 superclass 2 args");
        check(c6.superclassSignature().typeArgs().get(0) instanceof Signature.TypeArg.Bounded, "c6 superclass arg0 Bounded");
        check(c6.superinterfaceSignatures().get(0).typeArgs().size() == 2, "c6 iface 2 args");
        // superclass/superinterface 都是 ClassTypeSig
        check(c6.superclassSignature() instanceof Signature.RefTypeSig, "c6 superclass RefTypeSig");
        check(c6.superinterfaceSignatures().get(0) instanceof Signature.ThrowableSig, "c6 iface ClassTypeSig (also ThrowableSig)");
    }

    // ---- Part 5: 畸形串精确异常表（实测锁定：必须 L 开头） ----
    static void testMalformedMatrix() {
        String[][] bad = {
                {"bad(", "expected L"},
                {"I", "expected L"},
                {"(", "expected L"},
                {"", "Unexpected end of signature at position 0, expected L"},
                {"<", "expected an identifier"},
                {"<T:Ljava/lang/Object;>", "expected L"},
                {"Ljava/lang/Object", "expected . or ;"},
        };
        for (String[] row : bad) {
            boolean threw = false;
            try {
                ClassSignature.parseFrom(row[0]);
            } catch (IllegalArgumentException e) {
                threw = true;
                check(e.getMessage() != null && e.getMessage().contains(row[1]),
                        "IAE message for '" + row[0] + "': " + e.getMessage());
            }
            check(threw, "must throw IAE: '" + row[0] + "'");
        }
        try {
            ClassSignature.parseFrom(null);
            check(false, "parseFrom(null) must throw");
        } catch (NullPointerException e) {
            check(true, "parseFrom(null) NPE");
        }
    }

    // ---- Part 6: 工厂全重载 ----
    static void testFactoryMatrix() {
        ClassSignature f1 = ClassSignature.of(Signature.ClassTypeSig.of(ClassDesc.of("java.lang.Object")));
        check(f1.signatureString().equals("Ljava/lang/Object;"), "of(cts) plain");
        check(f1.typeParameters().isEmpty(), "of(cts) no tp");
        ClassSignature f2 = ClassSignature.of(
                Signature.ClassTypeSig.of(ClassDesc.of("java.lang.Object")),
                Signature.ClassTypeSig.of(ClassDesc.of("java.lang.Runnable")));
        check(f2.signatureString().equals("Ljava/lang/Object;Ljava/lang/Runnable;"), "of(cts, ifaces)");
        check(f2.superinterfaceSignatures().size() == 1, "of(cts, ifaces) 1 iface");
        ClassSignature f3 = ClassSignature.of(
                java.util.List.of(Signature.TypeParam.of("T",
                        (Signature.RefTypeSig) Signature.parseFrom("Ljava/lang/Object;"))),
                Signature.ClassTypeSig.of(ClassDesc.of("java.lang.Object")));
        check(f3.signatureString().equals("<T:Ljava/lang/Object;>Ljava/lang/Object;"), "of(tps, cts)");
        check(f3.typeParameters().get(0).identifier().equals("T"), "of(tps, cts) tp id");
        check(ClassSignature.parseFrom("<T:Ljava/lang/Object;>Ljava/lang/Object;").equals(f3), "of == parseFrom value equality");
        // 多 interface bound 工厂渲染 + 回读
        ClassSignature f4 = ClassSignature.of(
                java.util.List.of(Signature.TypeParam.of("E",
                        (Signature.RefTypeSig) Signature.parseFrom("Ljava/lang/Object;"),
                        (Signature.RefTypeSig) Signature.parseFrom("Ljava/lang/Runnable;"),
                        (Signature.RefTypeSig) Signature.parseFrom("Ljava/io/Serializable;"))),
                Signature.ClassTypeSig.of(ClassDesc.of("java.lang.Object")));
        check(f4.signatureString().equals("<E:Ljava/lang/Object;:Ljava/lang/Runnable;:Ljava/io/Serializable;>Ljava/lang/Object;"), "multi-bound render");
        check(f4.typeParameters().get(0).interfaceBounds().size() == 2, "multi-bound 2 ibs");
        try {
            ClassSignature.of(null);
            check(false, "of(null) must throw");
        } catch (NullPointerException e) {
            check(true, "of(null) NPE");
        }
    }

    // ---- Part 7: build 注入 SignatureAttribute → re-parse 全等闭环 ----
    static void testBuildAttributeLoop() throws Exception {
        ClassSignature model = ClassSignature.parseFrom("<T:Ljava/lang/Object;>Ljava/lang/Object;");
        byte[] b = ClassFile.of().build(ClassDesc.of("CSLoopT"), cb -> cb
                .withSuperclass(ClassDesc.of("java.lang.Object"))
                .with(SignatureAttribute.of(model)));
        ClassModel cm = ClassFile.of().parse(b);
        SignatureAttribute sa = cm.findAttribute(Attributes.signature()).orElseThrow();
        check(sa.signature().stringValue().equals("<T:Ljava/lang/Object;>Ljava/lang/Object;"), "class attr raw string");
        ClassSignature rt = sa.asClassSignature();
        check(rt.signatureString().equals(model.signatureString()), "asClassSignature string equal");
        check(rt.equals(model), "asClassSignature value equals model");
        check(rt.typeParameters().size() == 1, "asClassSignature tp 1");
    }

    // ---- Part 8: 真类泛型闭环（javac 嵌套泛型类 → 类级 Signature 属性 → 模型 → 幂等） ----
    static void testRealClassGenericLoop() throws Exception {
        byte[] box = ClassSignatureTest.class.getResourceAsStream("ClassSignatureTest$CsBox.class").readAllBytes();
        ClassModel bm = ClassFile.of().parse(box);
        check(bm.thisClass().asInternalName().endsWith("ClassSignatureTest$CsBox"), "CsBox class bytes parsed");
        String bs = bm.findAttribute(Attributes.signature()).orElseThrow().signature().stringValue();
        check(bs.equals("<T:Ljava/lang/Object;>Ljava/lang/Object;"), "CsBox sig exact: " + bs);
        ClassSignature bmodel = ClassSignature.parseFrom(bs);
        check(bmodel.signatureString().equals(bs), "CsBox sig -> model idempotent");
        check(bmodel.typeParameters().size() == 1 && bmodel.superinterfaceSignatures().isEmpty(), "CsBox model structure");
        byte[] pair = ClassSignatureTest.class.getResourceAsStream("ClassSignatureTest$CsPair.class").readAllBytes();
        ClassModel pm = ClassFile.of().parse(pair);
        String ps = pm.findAttribute(Attributes.signature()).orElseThrow().signature().stringValue();
        check(ps.equals("<K:Ljava/lang/Object;V::Ljava/lang/Comparable<TK;>;>Ljava/lang/Object;Ljava/lang/Runnable;"), "CsPair sig exact: " + ps);
        ClassSignature pmodel = ClassSignature.parseFrom(ps);
        check(pmodel.signatureString().equals(ps), "CsPair sig -> model idempotent");
        check(pmodel.typeParameters().size() == 2, "CsPair 2 tps");
        check(pmodel.typeParameters().get(0).identifier().equals("K"), "CsPair tp0 K");
        check(!pmodel.typeParameters().get(1).classBound().isPresent(), "CsPair tp1 classBound empty");
        check(pmodel.typeParameters().get(1).interfaceBounds().get(0).signatureString().equals("Ljava/lang/Comparable<TK;>;"), "CsPair tp1 ib");
        check(pmodel.superinterfaceSignatures().size() == 1, "CsPair 1 iface");
    }

    // ---- Part 9: equals/hashCode 值语义 ----
    static void testEqualityContract() {
        check(ClassSignature.parseFrom("Ljava/lang/Object;").equals(ClassSignature.parseFrom("Ljava/lang/Object;")), "value equals");
        check(ClassSignature.parseFrom("Ljava/lang/Object;").hashCode() == ClassSignature.parseFrom("Ljava/lang/Object;").hashCode(), "hashCode consistent");
        check(!ClassSignature.parseFrom("Ljava/lang/Object;").equals(ClassSignature.parseFrom("<T:Ljava/lang/Object;>Ljava/lang/Object;")), "tp difference not equals");
        check(!ClassSignature.parseFrom("Ljava/lang/Object;").equals(null), "not equals null");
        check(ClassSignature.of(Signature.ClassTypeSig.of(ClassDesc.of("java.lang.Object")))
                .equals(ClassSignature.parseFrom("Ljava/lang/Object;")), "of == parseFrom value equality");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRoundTripFamily();
        testStructureMatrix();
        testMalformedMatrix();
        testFactoryMatrix();
        testBuildAttributeLoop();
        testRealClassGenericLoop();
        testEqualityContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.lang.classfile.MethodSignature conformance: white-box structure + black-box behavior.
 * @run main MethodSignatureTest
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

public class MethodSignatureTest {
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

    // ---- 泛型真类闭环素材：javac 产物自带 Signature 属性（含 throws 段 TypeVar） ----
    static <T extends Object> T gen(T t, java.util.List<String> ls) { return t; }
    static <T extends Exception> void genThrows(T t) throws T { }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.MethodSignature");
        check(c != null, "java.lang.classfile.MethodSignature loads");
        check(c.getName().equals("java.lang.classfile.MethodSignature"), "binary name java.lang.classfile.MethodSignature");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.MethodSignature is public");
        check(c.isInterface(), "java.lang.classfile.MethodSignature is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.MethodSignature has no superclass");
        check(declares(c, "typeParameters"), "declares typeParameters");
        check(declares(c, "arguments"), "declares arguments");
        check(declares(c, "result"), "declares result");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        MethodSignature ms = MethodSignature.parseFrom("(Ljava/lang/String;)I");
        check(ms.typeParameters().isEmpty(), "no type params");
        check(ms.arguments().size() == 1, "one argument");
        check(ms.arguments().get(0).signatureString().equals("Ljava/lang/String;"), "arg String");
        check(ms.result().signatureString().equals("I"), "result int");
        check(ms.throwableSignatures().isEmpty(), "no throws");
        MethodSignature ms2 = MethodSignature.of(MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_String));
        check(ms2.arguments().size() == 1 && ms2.result().signatureString().equals("I"), "of(MethodTypeDesc)");

    }

    // ---- Part 3: 合法族 round-trip 幂等 ----
    static void testRoundTripFamily() {
        String[] legal = {
                "(I)V",
                "()V",
                "(Ljava/lang/String;I[[J)Ljava/util/List<Ljava/lang/String;>;",
                "<T:Ljava/lang/Object;>(TT;)TT;",
                "<T:Ljava/lang/Object;>(TT;)V^Ljava/lang/Exception;",
                "<T:Ljava/lang/Object;>(TT;)V^TT;",
                "(TT;)TT;",
                "<T:>(I)V",                                                       // 无 bound 的 type param（quirk：合法）
                "(IJZDBCFS)V",                                                    // 8 基类型参数
                "(ILjava/util/List<Ljava/lang/String;>;)V",
                "<K:Ljava/lang/Object;V::Ljava/lang/Comparable<TK;>;>(TK;TV;)V^TK;",
        };
        for (String s : legal) {
            MethodSignature p = MethodSignature.parseFrom(s);
            check(p != null, "parseFrom legal non-null: " + s);
            String out = p.signatureString();
            check(out.equals(s), "round-trip '" + s + "' -> '" + out + "'");
            check(MethodSignature.parseFrom(out).signatureString().equals(s), "re-parse idempotent: " + s);
        }
    }

    // ---- Part 4: 结构矩阵（typeParameters/arguments/result/throwableSignatures 逐访问器） ----
    static void testStructureMatrix() {
        MethodSignature m1 = MethodSignature.parseFrom("(I)V");
        check(m1.typeParameters().isEmpty(), "(I)V no type params");
        check(m1.arguments().size() == 1, "(I)V one argument");
        check(m1.arguments().get(0) instanceof Signature.BaseTypeSig, "(I)V arg0 BaseTypeSig");
        check(m1.result().signatureString().equals("V"), "(I)V result V");
        check(m1.result() instanceof Signature.BaseTypeSig, "(I)V result BaseTypeSig");
        check(((Signature.BaseTypeSig) m1.result()).baseType() == 'V', "(I)V result baseType V");
        check(m1.throwableSignatures().isEmpty(), "(I)V no throws");
        MethodSignature m2 = MethodSignature.parseFrom("(Ljava/lang/String;I[[J)Ljava/util/List<Ljava/lang/String;>;");
        check(m2.arguments().size() == 3, "m2 3 args");
        check(m2.arguments().get(0).signatureString().equals("Ljava/lang/String;"), "m2 arg0 String");
        check(m2.arguments().get(0) instanceof Signature.ClassTypeSig, "m2 arg0 ClassTypeSig");
        check(m2.arguments().get(1).signatureString().equals("I"), "m2 arg1 int");
        check(m2.arguments().get(2).signatureString().equals("[[J"), "m2 arg2 [[J");
        check(m2.arguments().get(2) instanceof Signature.ArrayTypeSig, "m2 arg2 ArrayTypeSig");
        check(m2.result() instanceof Signature.ClassTypeSig, "m2 result ClassTypeSig");
        Signature.ClassTypeSig r2 = (Signature.ClassTypeSig) m2.result();
        check(r2.className().equals("java/util/List"), "m2 result className");
        check(r2.typeArgs().size() == 1, "m2 result 1 type arg");
        MethodSignature m3 = MethodSignature.parseFrom("<T:Ljava/lang/Object;>(TT;)TT;");
        check(m3.typeParameters().size() == 1, "m3 one type param");
        check(m3.typeParameters().get(0).identifier().equals("T"), "m3 tp identifier T");
        check(m3.typeParameters().get(0).classBound().isPresent(), "m3 tp classBound present");
        check(m3.typeParameters().get(0).classBound().get().signatureString().equals("Ljava/lang/Object;"), "m3 tp classBound Object");
        check(m3.typeParameters().get(0).interfaceBounds().isEmpty(), "m3 tp no interface bounds");
        check(m3.arguments().get(0) instanceof Signature.TypeVarSig, "m3 arg0 TypeVarSig");
        check(m3.result() instanceof Signature.TypeVarSig, "m3 result TypeVarSig");
        check(((Signature.TypeVarSig) m3.result()).identifier().equals("T"), "m3 result identifier T");
        // throws 段：类类型 / TypeVar / 多段有序
        MethodSignature m4 = MethodSignature.parseFrom("<T:Ljava/lang/Object;>(TT;)V^Ljava/lang/Exception;");
        check(m4.throwableSignatures().size() == 1, "m4 one throw");
        check(m4.throwableSignatures().get(0) instanceof Signature.ClassTypeSig, "m4 throw ClassTypeSig");
        check(m4.throwableSignatures().get(0).signatureString().equals("Ljava/lang/Exception;"), "m4 throw string");
        MethodSignature m5 = MethodSignature.parseFrom("<T:Ljava/lang/Object;>(TT;)V^TT;");
        check(m5.throwableSignatures().get(0) instanceof Signature.TypeVarSig, "m5 throw TypeVarSig");
        check(m5.throwableSignatures().get(0).signatureString().equals("TT;"), "m5 throw TT;");
        MethodSignature m6 = MethodSignature.parseFrom("<T:Ljava/lang/Object;>(TT;)V^Ljava/lang/Exception;^Ljava/io/IOException;");
        check(m6.throwableSignatures().size() == 2, "m6 two throws");
        check(m6.throwableSignatures().get(0).signatureString().equals("Ljava/lang/Exception;"), "m6 throw0 order");
        check(m6.throwableSignatures().get(1).signatureString().equals("Ljava/io/IOException;"), "m6 throw1 order");
        check(m6.throwableSignatures().get(1) instanceof Signature.ThrowableSig, "m6 throws are ThrowableSig");
        // result 三分型总览
        check(MethodSignature.parseFrom("(I)I").result() instanceof Signature.BaseTypeSig, "result base");
        check(MethodSignature.parseFrom("()Ljava/lang/Object;").result() instanceof Signature.ClassTypeSig, "result class");
        check(MethodSignature.parseFrom("(TT;)TT;").result() instanceof Signature.TypeVarSig, "result typevar");
        // 8 基类型参数逐位核对
        MethodSignature m7 = MethodSignature.parseFrom("(IJZDBCFS)V");
        char[] expect = {'I', 'J', 'Z', 'D', 'B', 'C', 'F', 'S'};
        for (int i = 0; i < 8; i++) {
            check(m7.arguments().get(i) instanceof Signature.BaseTypeSig
                    && ((Signature.BaseTypeSig) m7.arguments().get(i)).baseType() == expect[i],
                    "8-base arg " + i + " is " + expect[i]);
        }
    }

    // ---- Part 5: 畸形串精确异常表（实测锁定） ----
    static void testMalformedMatrix() {
        String[][] bad = {
                {"nope", "expected ("},
                {"I", "expected ("},
                {"bad(", "expected ("},
                {"V", "expected ("},
                {"(", "Not a valid method signature"},
                {"()", "Not a valid method signature"},
                {"(I)", "Not a valid method signature"},
                {"(I)X", "Unexpected character X at position 3"},
                {"(I)TT", "Unexpected end of signature at position 5"},
        };
        for (String[] row : bad) {
            boolean threw = false;
            try {
                MethodSignature.parseFrom(row[0]);
            } catch (IllegalArgumentException e) {
                threw = true;
                check(e.getMessage() != null && e.getMessage().contains(row[1]),
                        "IAE message for '" + row[0] + "': " + e.getMessage());
            }
            check(threw, "must throw IAE: '" + row[0] + "'");
        }
        try {
            MethodSignature.parseFrom(null);
            check(false, "parseFrom(null) must throw");
        } catch (NullPointerException e) {
            check(true, "parseFrom(null) NPE");
        }
    }

    // ---- Part 6: 工厂全重载 of(MethodTypeDesc)/of(Signature,...)/of(tps,throws,result,...) ----
    static void testFactoryMatrix() {
        MethodSignature f1 = MethodSignature.of(MethodTypeDesc.ofDescriptor("(Ljava/lang/String;I)V"));
        check(f1.signatureString().equals("(Ljava/lang/String;I)V"), "of(MTD) string");
        check(f1.typeParameters().isEmpty() && f1.throwableSignatures().isEmpty(), "of(MTD) no tp/throws");
        check(f1.arguments().size() == 2, "of(MTD) 2 args");
        MethodSignature f2 = MethodSignature.of(MethodTypeDesc.ofDescriptor("(I[[J)V"));
        check(f2.arguments().get(1).signatureString().equals("[[J"), "of(MTD) array param");
        MethodSignature f3 = MethodSignature.of(Signature.parseFrom("I"), Signature.parseFrom("Ljava/lang/String;"));
        check(f3.signatureString().equals("(Ljava/lang/String;)I"), "of(result, args) string");
        MethodSignature f4 = MethodSignature.of(
                java.util.List.of(Signature.TypeParam.of("T", java.util.Optional.empty())),
                java.util.List.<Signature.ThrowableSig>of(),
                Signature.parseFrom("V"),
                Signature.parseFrom("I"));
        check(f4.signatureString().equals("<T:>(I)V"), "of(tps, throws, result, args) renders <T:>(I)V");
        check(MethodSignature.parseFrom(f4.signatureString()).equals(f4), "<T:>(I)V re-parse equals model");
        // 多 interface bound 的 type param 渲染（实测 <E::Ljava/lang/Runnable;>()V）
        MethodSignature f5 = MethodSignature.of(
                java.util.List.of(Signature.TypeParam.of("E", java.util.Optional.empty(),
                        (Signature.RefTypeSig) Signature.parseFrom("Ljava/lang/Runnable;"))),
                java.util.List.<Signature.ThrowableSig>of(),
                Signature.parseFrom("V"));
        check(f5.signatureString().equals("<E::Ljava/lang/Runnable;>()V"), "multi-bound render");
        check(f5.typeParameters().get(0).interfaceBounds().size() == 1, "multi-bound parsed back");
        try {
            MethodSignature.of((MethodTypeDesc) null);
            check(false, "of((MethodTypeDesc)null) must throw");
        } catch (NullPointerException e) {
            check(true, "of((MethodTypeDesc)null) NPE");
        }
        try {
            MethodSignature.of(null, new Signature[0]);
            check(false, "of(null result) must throw");
        } catch (NullPointerException e) {
            check(true, "of(null result) NPE");
        }
        try {
            MethodSignature.of(java.util.List.of(), java.util.List.of(), null);
            check(false, "of(..., null result) must throw");
        } catch (NullPointerException e) {
            check(true, "of(..., null result) NPE");
        }
    }

    // ---- Part 7: build 注入 SignatureAttribute → re-parse 全等闭环 ----
    static void testBuildAttributeLoop() throws Exception {
        MethodSignature model = MethodSignature.parseFrom("<T:Ljava/lang/Object;>(TT;)V");
        byte[] b = ClassFile.of().build(ClassDesc.of("MSLoopT"), cb -> cb
                .withMethod("m", MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;)V"), ClassFile.ACC_PUBLIC,
                        mb -> mb.with(SignatureAttribute.of(model))));
        MethodModel mm = ClassFile.of().parse(b).methods().get(0);
        SignatureAttribute sa = mm.findAttribute(Attributes.signature()).orElseThrow();
        check(sa.signature().stringValue().equals("<T:Ljava/lang/Object;>(TT;)V"), "attr raw string roundtrip");
        MethodSignature rt = sa.asMethodSignature();
        check(rt.signatureString().equals(model.signatureString()), "asMethodSignature string equal");
        check(rt.equals(model), "asMethodSignature value equals model");
        check(rt.typeParameters().size() == 1 && rt.throwableSignatures().isEmpty(), "asMethodSignature structure");
    }

    // ---- Part 8: 真类泛型闭环（javac 产物 Signature 属性 → 模型 → 幂等） ----
    static void testRealClassGenericLoop() throws Exception {
        byte[] self = MethodSignatureTest.class.getResourceAsStream("MethodSignatureTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        MethodModel gen = null, genThrows = null;
        for (MethodModel m : cm.methods()) {
            if (m.methodName().stringValue().equals("gen")) gen = m;
            if (m.methodName().stringValue().equals("genThrows")) genThrows = m;
        }
        check(gen != null && genThrows != null, "gen/genThrows found");
        String gs = gen.findAttribute(Attributes.signature()).orElseThrow().signature().stringValue();
        check(gs.equals("<T:Ljava/lang/Object;>(TT;Ljava/util/List<Ljava/lang/String;>;)TT;"), "gen sig exact: " + gs);
        MethodSignature gm = MethodSignature.parseFrom(gs);
        check(gm.signatureString().equals(gs), "javac sig -> model idempotent");
        check(gm.typeParameters().size() == 1, "gen tp 1");
        check(gm.arguments().size() == 2, "gen args 2");
        check(gm.result() instanceof Signature.TypeVarSig, "gen result TypeVarSig");
        String ts = genThrows.findAttribute(Attributes.signature()).orElseThrow().signature().stringValue();
        check(ts.equals("<T:Ljava/lang/Exception;>(TT;)V^TT;"), "genThrows sig exact: " + ts);
        MethodSignature tm = MethodSignature.parseFrom(ts);
        check(tm.throwableSignatures().size() == 1, "genThrows 1 throw");
        check(tm.throwableSignatures().get(0) instanceof Signature.TypeVarSig, "genThrows throw TypeVarSig");
        check(tm.typeParameters().get(0).classBound().get().signatureString().equals("Ljava/lang/Exception;"), "genThrows tp bound Exception");
    }

    // ---- Part 9: equals/hashCode 值语义 ----
    static void testEqualityContract() {
        check(MethodSignature.parseFrom("(I)V").equals(MethodSignature.parseFrom("(I)V")), "value equals");
        check(MethodSignature.parseFrom("(I)V").hashCode() == MethodSignature.parseFrom("(I)V").hashCode(), "hashCode consistent");
        check(!MethodSignature.parseFrom("(I)V").equals(MethodSignature.parseFrom("(I)I")), "different result not equals");
        check(!MethodSignature.parseFrom("(I)V").equals(MethodSignature.parseFrom("()V")), "different args not equals");
        check(!MethodSignature.parseFrom("(I)V").equals(null), "not equals null");
        check(MethodSignature.of(MethodTypeDesc.ofDescriptor("(I)V")).equals(MethodSignature.parseFrom("(I)V")), "of(MTD)==parseFrom value equality");
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

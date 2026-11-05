/*
 * @test
 * @summary java.lang.classfile.Signature.TypeParam conformance: white-box structure + black-box behavior.
 * @run main TypeParamTest
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

public class TypeParamTest {
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

    // ---- 真类闭环素材：javac 为多类型参数方法生成 <T:...;U::...;> Signature 属性 ----
    static <T extends Object, U extends java.lang.Comparable<?>> void genTP(T t, U u) { }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.Signature$TypeParam");
        check(c != null, "java.lang.classfile.Signature.TypeParam loads");
        check(c.getName().equals("java.lang.classfile.Signature$TypeParam"), "binary name java.lang.classfile.Signature$TypeParam");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Signature.TypeParam is public");
        check(c.isInterface(), "java.lang.classfile.Signature.TypeParam is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.Signature.TypeParam has no superclass");
        check(declares(c, "identifier"), "declares identifier");
        check(declares(c, "classBound"), "declares classBound");
        check(declares(c, "interfaceBounds"), "declares interfaceBounds");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        Signature.TypeParam tp = Signature.TypeParam.of("T", Signature.ClassTypeSig.of(ClassDesc.of("java.lang.Object")), Signature.ClassTypeSig.of(ClassDesc.of("java.io.Serializable")));
        check(tp.identifier().equals("T"), "identifier");
        check(tp.classBound().get().signatureString().equals("Ljava/lang/Object;"), "classBound");
        check(tp.interfaceBounds().size() == 1, "one interface bound");
        check(tp.interfaceBounds().get(0).signatureString().equals("Ljava/io/Serializable;"), "interface bound");

    }

    // ---- Part 3: 解析矩阵（classBound 存在/缺失 × interfaceBounds 数量 × 多参数顺序） ----
    static void testParseMatrix() {
        // <T:Ljava/lang/Object;> — classBound 存在、无 interface bound
        ClassSignature c1 = ClassSignature.parseFrom("<T:Ljava/lang/Object;>Ljava/lang/Object;");
        Signature.TypeParam tp1 = c1.typeParameters().get(0);
        check(tp1.identifier().equals("T"), "tp1 id T");
        check(tp1.classBound().isPresent(), "tp1 classBound present");
        check(tp1.classBound().get().signatureString().equals("Ljava/lang/Object;"), "tp1 classBound Object");
        check(tp1.interfaceBounds().isEmpty(), "tp1 no ib");
        // <T::Ljava/lang/Comparable<*>;> — classBound 缺失、1 个 interface bound
        ClassSignature c2 = ClassSignature.parseFrom("<T::Ljava/lang/Comparable<*>;>Ljava/lang/Object;Ljava/lang/Runnable;");
        Signature.TypeParam tp2 = c2.typeParameters().get(0);
        check(!tp2.classBound().isPresent(), "tp2 classBound empty (::)");
        check(tp2.interfaceBounds().size() == 1, "tp2 1 ib");
        check(tp2.interfaceBounds().get(0).signatureString().equals("Ljava/lang/Comparable<*>;"), "tp2 ib string");
        check(tp2.interfaceBounds().get(0) instanceof Signature.RefTypeSig, "tp2 ib RefTypeSig");
        // <T:TT;> — classBound 是 TypeVarSig
        ClassSignature c3 = ClassSignature.parseFrom("<T:TT;>Ljava/lang/Object;");
        check(c3.typeParameters().get(0).classBound().get() instanceof Signature.TypeVarSig, "typevar classBound");
        // 多类型参数：<T:...;U::...;> 顺序保持
        ClassSignature c4 = ClassSignature.parseFrom("<T:Ljava/lang/Object;U::Ljava/lang/Comparable<*>;>Ljava/lang/Object;");
        check(c4.typeParameters().size() == 2, "2 tps");
        check(c4.typeParameters().get(0).identifier().equals("T"), "tp order T first");
        check(c4.typeParameters().get(1).identifier().equals("U"), "tp order U second");
        check(c4.typeParameters().get(0).classBound().isPresent(), "T has classBound");
        check(!c4.typeParameters().get(1).classBound().isPresent(), "U classBound empty");
        check(c4.typeParameters().get(1).interfaceBounds().size() == 1, "U 1 ib");
        // 多 interface bound：<E:...:...:...>
        ClassSignature c5 = ClassSignature.parseFrom(
                "<E:Ljava/lang/Object;:Ljava/lang/Runnable;:Ljava/io/Serializable;>Ljava/lang/Object;");
        Signature.TypeParam tp5 = c5.typeParameters().get(0);
        check(tp5.classBound().isPresent(), "tp5 classBound present");
        check(tp5.interfaceBounds().size() == 2, "tp5 2 ibs");
        check(tp5.interfaceBounds().get(0).signatureString().equals("Ljava/lang/Runnable;"), "tp5 ib0");
        check(tp5.interfaceBounds().get(1).signatureString().equals("Ljava/io/Serializable;"), "tp5 ib1 order");
        // 无 bound 形态 <T:>（实测合法 quirk）
        ClassSignature c6 = ClassSignature.parseFrom("<T:>Ljava/lang/Object;");
        Signature.TypeParam tp6 = c6.typeParameters().get(0);
        check(tp6.identifier().equals("T"), "tp6 id T");
        check(!tp6.classBound().isPresent(), "tp6 classBound empty");
        check(tp6.interfaceBounds().isEmpty(), "tp6 no ib");
        // MethodSignature 侧同一 TypeParam 语义
        MethodSignature m1 = MethodSignature.parseFrom("<T:Ljava/lang/Object;>(TT;)TT;");
        check(m1.typeParameters().get(0).classBound().get().signatureString().equals("Ljava/lang/Object;"), "method tp classBound");
    }

    // ---- Part 4: 工厂全重载（of(String,RefTypeSig,RefTypeSig...)/of(String,Optional,...)） ----
    static void testFactoryMatrix() {
        Signature.RefTypeSig obj = (Signature.RefTypeSig) Signature.parseFrom("Ljava/lang/Object;");
        Signature.TypeParam f1 = Signature.TypeParam.of("T", obj);
        check(f1.identifier().equals("T"), "f1 id");
        check(f1.classBound().isPresent() && f1.classBound().get().equals(obj), "f1 classBound");
        check(f1.interfaceBounds().isEmpty(), "f1 no ib");
        Signature.TypeParam f2 = Signature.TypeParam.of("T", java.util.Optional.empty());
        check(!f2.classBound().isPresent(), "f2 classBound empty");
        Signature.TypeParam f3 = Signature.TypeParam.of("T", obj,
                (Signature.RefTypeSig) Signature.parseFrom("Ljava/lang/Runnable;"),
                (Signature.RefTypeSig) Signature.parseFrom("Ljava/io/Serializable;"));
        check(f3.interfaceBounds().size() == 2, "f3 2 ibs");
        check(f3.interfaceBounds().get(0).signatureString().equals("Ljava/lang/Runnable;"), "f3 ib0");
        // null classBound → Optional.empty（实测：of("T", (RefTypeSig)null) 合法）
        Signature.TypeParam f4 = Signature.TypeParam.of("T", (Signature.RefTypeSig) null);
        check(!f4.classBound().isPresent(), "f4 null bound -> empty Optional");
        // 工厂 → 渲染串精确（实测 <E:...:...>）+ 回读等值
        Signature.TypeParam e = Signature.TypeParam.of("E", obj,
                (Signature.RefTypeSig) Signature.parseFrom("Ljava/lang/Runnable;"),
                (Signature.RefTypeSig) Signature.parseFrom("Ljava/io/Serializable;"));
        ClassSignature cs = ClassSignature.of(java.util.List.of(e), Signature.ClassTypeSig.of(ClassDesc.of("java.lang.Object")));
        check(cs.signatureString().equals("<E:Ljava/lang/Object;:Ljava/lang/Runnable;:Ljava/io/Serializable;>Ljava/lang/Object;"),
                "factory render exact: " + cs.signatureString());
        check(ClassSignature.parseFrom(cs.signatureString()).equals(cs), "rendered re-parse equals model");
        check(ClassSignature.parseFrom(cs.signatureString()).typeParameters().get(0).interfaceBounds().size() == 2,
                "re-parsed ibs 2");
    }

    // ---- Part 5: null 负例 + 不可变列表负例 ----
    static void testNegativeMatrix() {
        boolean threw = false;
        try {
            Signature.TypeParam.of(null, java.util.Optional.empty());
        } catch (NullPointerException e) {
            threw = true;
            check(e.getMessage() != null && e.getMessage().contains("\"st\" is null"), "of(null id) NPE msg: " + e.getMessage());
        }
        check(threw, "of(null id) NPE");
        // interfaceBounds 返回不可变列表（实测 add → UnsupportedOperationException）
        Signature.TypeParam tp = ClassSignature.parseFrom("<T::Ljava/lang/Runnable;>Ljava/lang/Object;").typeParameters().get(0);
        threw = false;
        try {
            tp.interfaceBounds().add((Signature.RefTypeSig) Signature.parseFrom("Ljava/lang/Object;"));
        } catch (UnsupportedOperationException e) {
            threw = true;
        }
        check(threw, "interfaceBounds immutable (add UOE)");
    }

    // ---- Part 6: equals/hashCode 值语义 ----
    static void testEqualityContract() {
        check(Signature.TypeParam.of("T", java.util.Optional.empty())
                .equals(Signature.TypeParam.of("T", java.util.Optional.empty())), "value equals");
        check(Signature.TypeParam.of("T", java.util.Optional.empty()).hashCode()
                == Signature.TypeParam.of("T", java.util.Optional.empty()).hashCode(), "hashCode consistent");
        check(!Signature.TypeParam.of("T", java.util.Optional.empty())
                .equals(Signature.TypeParam.of("U", java.util.Optional.empty())), "different id not equals");
        check(!Signature.TypeParam.of("T", java.util.Optional.empty()).equals(null), "not equals null");
        Signature.RefTypeSig obj = (Signature.RefTypeSig) Signature.parseFrom("Ljava/lang/Object;");
        check(Signature.TypeParam.of("T", obj).equals(
                ClassSignature.parseFrom("<T:Ljava/lang/Object;>Ljava/lang/Object;").typeParameters().get(0)),
                "factory == parsed value equality");
    }

    // ---- Part 7: 真类闭环（javac <T:...;U::...;>(...)V → TypeParam 模型） ----
    static void testRealClassGenericLoop() throws Exception {
        byte[] self = TypeParamTest.class.getResourceAsStream("TypeParamTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        MethodModel gen = null;
        for (MethodModel m : cm.methods()) if (m.methodName().stringValue().equals("genTP")) gen = m;
        check(gen != null, "genTP found");
        String gs = gen.findAttribute(Attributes.signature()).orElseThrow().signature().stringValue();
        check(gs.equals("<T:Ljava/lang/Object;U::Ljava/lang/Comparable<*>;>(TT;TU;)V"), "genTP sig exact: " + gs);
        MethodSignature mm = MethodSignature.parseFrom(gs);
        check(mm.signatureString().equals(gs), "javac sig -> model idempotent");
        check(mm.typeParameters().size() == 2, "genTP 2 tps");
        Signature.TypeParam t = mm.typeParameters().get(0);
        Signature.TypeParam u = mm.typeParameters().get(1);
        check(t.identifier().equals("T") && t.classBound().isPresent()
                && t.classBound().get().signatureString().equals("Ljava/lang/Object;") && t.interfaceBounds().isEmpty(),
                "T: classBound Object, no ib");
        check(u.identifier().equals("U") && !u.classBound().isPresent() && u.interfaceBounds().size() == 1,
                "U: no classBound, 1 ib");
        check(u.interfaceBounds().get(0).signatureString().equals("Ljava/lang/Comparable<*>;"), "U ib string");
        check(((Signature.ClassTypeSig) u.interfaceBounds().get(0)).typeArgs().get(0)
                instanceof Signature.TypeArg.Unbounded, "U ib arg Unbounded");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testParseMatrix();
        testFactoryMatrix();
        testNegativeMatrix();
        testEqualityContract();
        testRealClassGenericLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.lang.classfile.Signature.TypeVarSig conformance: white-box structure + black-box behavior.
 * @run main TypeVarSigTest
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

public class TypeVarSigTest {
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

    // ---- 真类闭环素材：泛型方法（javac 产物 Signature 属性中 TT; 为 TypeVarSig） ----
    static <T extends Object> T gen(T t, java.util.List<String> ls) { return t; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.Signature$TypeVarSig");
        check(c != null, "java.lang.classfile.Signature.TypeVarSig loads");
        check(c.getName().equals("java.lang.classfile.Signature$TypeVarSig"), "binary name java.lang.classfile.Signature$TypeVarSig");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Signature.TypeVarSig is public");
        check(c.isInterface(), "java.lang.classfile.Signature.TypeVarSig is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.Signature.TypeVarSig has no superclass");
        check(declares(c, "identifier"), "declares identifier");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        Signature.TypeVarSig v = Signature.TypeVarSig.of("T");
        check(v.identifier().equals("T"), "identifier");
        check(v.signatureString().equals("TT;"), "signatureString");

    }

    // ---- Part 3: 五个语法位置全矩阵（参数/返回/throws/类型实参/type param bound） ----
    static void testParsePositions() {
        Signature.TypeVarSig t = (Signature.TypeVarSig) Signature.parseFrom("TT;");
        check(t.identifier().equals("T"), "identifier T");
        check(t.signatureString().equals("TT;"), "signatureString TT;");
        // 参数位 + 返回位
        MethodSignature m1 = MethodSignature.parseFrom("(TT;)TT;");
        check(m1.arguments().get(0) instanceof Signature.TypeVarSig, "param position");
        check(((Signature.TypeVarSig) m1.arguments().get(0)).identifier().equals("T"), "param id T");
        check(m1.result() instanceof Signature.TypeVarSig, "result position");
        check(((Signature.TypeVarSig) m1.result()).identifier().equals("T"), "result id T");
        // throws 段位
        MethodSignature m2 = MethodSignature.parseFrom("()V^TT;");
        check(m2.throwableSignatures().get(0) instanceof Signature.TypeVarSig, "throws position");
        // 类型实参位（Map<TK;TV;> 两个不同标识符）
        Signature.ClassTypeSig map = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/Map<TK;TV;>;");
        check(((Signature.TypeArg.Bounded) map.typeArgs().get(0)).boundType() instanceof Signature.TypeVarSig, "typearg0 position");
        check(((Signature.TypeVarSig) ((Signature.TypeArg.Bounded) map.typeArgs().get(0)).boundType()).identifier().equals("K"), "typearg0 id K");
        check(((Signature.TypeVarSig) ((Signature.TypeArg.Bounded) map.typeArgs().get(1)).boundType()).identifier().equals("V"), "typearg1 id V");
        // type param bound 位（<T:TT;>）
        ClassSignature cs = ClassSignature.parseFrom("<T:TT;>Ljava/lang/Object;");
        check(cs.typeParameters().get(0).classBound().get() instanceof Signature.TypeVarSig, "classBound position");
        check(((Signature.TypeVarSig) cs.typeParameters().get(0).classBound().get()).identifier().equals("T"), "classBound id T");
        // 数组元素位
        check(Signature.parseFrom("[TT;") instanceof Signature.ArrayTypeSig, "array element position");
        check(((Signature.ArrayTypeSig) Signature.parseFrom("[TT;")).componentSignature()
                instanceof Signature.TypeVarSig, "array comp TypeVarSig");
        // 有界通配实参位
        Signature.ClassTypeSig ext = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/List<+TT;>;");
        check(((Signature.TypeArg.Bounded) ext.typeArgs().get(0)).boundType() instanceof Signature.TypeVarSig, "wildcard bound position");
    }

    // ---- Part 4: of 工厂矩阵（合法标识符 + 空串 IAE + null NPE） ----
    static void testFactoryMatrix() {
        check(Signature.TypeVarSig.of("X").identifier().equals("X"), "of(X) id");
        check(Signature.TypeVarSig.of("X").signatureString().equals("TX;"), "of(X) string");
        check(Signature.TypeVarSig.of("Element").signatureString().equals("TElement;"), "of(Element) string");
        check(Signature.TypeVarSig.of("T").equals(Signature.parseFrom("TT;")), "of == parseFrom value equality");
        boolean threw = false;
        try {
            Signature.TypeVarSig.of("");
        } catch (IllegalArgumentException e) {
            threw = true;
            check(e.getMessage() != null && e.getMessage().contains("Not a valid identifier"), "of(\"\") IAE msg: " + e.getMessage());
        }
        check(threw, "of(\"\") IAE");
        threw = false;
        try {
            Signature.TypeVarSig.of(null);
        } catch (NullPointerException e) {
            threw = true;
            check(e.getMessage() != null && e.getMessage().contains("\"st\" is null"), "of(null) NPE msg: " + e.getMessage());
        }
        check(threw, "of(null) NPE");
    }

    // ---- Part 5: 畸形串负例（实测 IAE 消息） ----
    static void testMalformedMatrix() {
        String[][] bad = {
                {"T;", "Unexpected character ; at position 1, expected an identifier"},
                {"TT", "Unexpected end of signature at position 2, expected ;"},
                {"T", "Unexpected end of signature at position 1, expected an identifier"},
        };
        for (String[] row : bad) {
            boolean threw = false;
            try {
                Signature.parseFrom(row[0]);
            } catch (IllegalArgumentException e) {
                threw = true;
                check(e.getMessage() != null && e.getMessage().contains(row[1]),
                        "IAE msg for '" + row[0] + "': " + e.getMessage());
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

    // ---- Part 6: 类型层次（RefTypeSig + ThrowableSig 双身份） ----
    static void testSubtypingMatrix() {
        Signature.TypeVarSig t = Signature.TypeVarSig.of("T");
        Signature asSig = t; // 以根类型静态视图做跨子型 instanceof
        check(asSig instanceof Signature, "TypeVarSig is Signature");
        check(asSig instanceof Signature.RefTypeSig, "TypeVarSig is RefTypeSig");
        check(asSig instanceof Signature.ThrowableSig, "TypeVarSig is ThrowableSig");
        check(!(asSig instanceof Signature.ClassTypeSig), "TypeVarSig not ClassTypeSig");
        check(Signature.ThrowableSig.class.isAssignableFrom(Signature.TypeVarSig.class), "ThrowableSig assignable from TypeVarSig");
        // round-trip 幂等族
        String[] legal = {"TT;", "TK;", "TV;", "[TT;", "Ljava/util/List<TT;>;", "Ljava/util/Map<TK;TV;>;"};
        for (String s : legal) {
            String out = Signature.parseFrom(s).signatureString();
            check(out.equals(s), "round-trip '" + s + "' -> '" + out + "'");
            check(Signature.parseFrom(out).signatureString().equals(s), "re-parse idempotent: " + s);
        }
    }

    // ---- Part 7: equals/hashCode 值语义 ----
    static void testEqualityContract() {
        check(Signature.TypeVarSig.of("T").equals(Signature.TypeVarSig.of("T")), "value equals");
        check(Signature.TypeVarSig.of("T").hashCode() == Signature.TypeVarSig.of("T").hashCode(), "hashCode consistent");
        check(!Signature.TypeVarSig.of("T").equals(Signature.TypeVarSig.of("U")), "different id not equals");
        check(!Signature.TypeVarSig.of("T").equals(null), "not equals null");
        check(!Signature.TypeVarSig.of("T").equals(Signature.parseFrom("Ljava/lang/String;")), "typevar vs class not equals");
    }

    // ---- Part 8: 真类泛型闭环（javac 产物 TT; 参数/返回 → TypeVarSig） ----
    static void testRealClassGenericLoop() throws Exception {
        byte[] self = TypeVarSigTest.class.getResourceAsStream("TypeVarSigTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        MethodModel gen = null;
        for (MethodModel m : cm.methods()) if (m.methodName().stringValue().equals("gen")) gen = m;
        check(gen != null, "gen found");
        String gs = gen.findAttribute(Attributes.signature()).orElseThrow().signature().stringValue();
        check(gs.equals("<T:Ljava/lang/Object;>(TT;Ljava/util/List<Ljava/lang/String;>;)TT;"), "gen sig exact: " + gs);
        MethodSignature mm = MethodSignature.parseFrom(gs);
        check(mm.arguments().get(0) instanceof Signature.TypeVarSig, "javac TT; param TypeVarSig");
        check(((Signature.TypeVarSig) mm.arguments().get(0)).identifier().equals("T"), "param identifier T");
        check(mm.result() instanceof Signature.TypeVarSig, "javac TT; result TypeVarSig");
        check(((Signature.TypeVarSig) mm.result()).identifier().equals("T"), "result identifier T");
        check(mm.typeParameters().get(0).identifier().equals("T"), "tp identifier T (declaration site)");
        check(MethodSignature.parseFrom(gs).signatureString().equals(gs), "whole-sig idempotent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testParsePositions();
        testFactoryMatrix();
        testMalformedMatrix();
        testSubtypingMatrix();
        testEqualityContract();
        testRealClassGenericLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

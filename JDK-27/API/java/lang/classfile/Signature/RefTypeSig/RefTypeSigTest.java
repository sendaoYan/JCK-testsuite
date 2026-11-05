/*
 * @test
 * @summary java.lang.classfile.Signature.RefTypeSig conformance: white-box structure + black-box behavior.
 * @run main RefTypeSigTest
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

public class RefTypeSigTest {
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

    // ---- 真类闭环素材：泛型方法（javac 产物 Signature 属性中 TT; 为 RefTypeSig） ----
    static <T extends Object> T gen(T t, java.util.List<String> ls) { return t; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.Signature$RefTypeSig");
        check(c != null, "java.lang.classfile.Signature.RefTypeSig loads");
        check(c.getName().equals("java.lang.classfile.Signature$RefTypeSig"), "binary name java.lang.classfile.Signature$RefTypeSig");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Signature.RefTypeSig is public");
        check(c.isInterface(), "java.lang.classfile.Signature.RefTypeSig is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.Signature.RefTypeSig has no superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        Signature.RefTypeSig r = Signature.ClassTypeSig.of(ClassDesc.of("java.lang.String"));
        check(r.signatureString().equals("Ljava/lang/String;"), "class type sig");
        Signature.RefTypeSig r2 = Signature.TypeVarSig.of("E");
        check(r2.signatureString().equals("TE;"), "type var sig");

    }

    // ---- Part 3: 标记接口白盒（无自有抽象方法，extends Signature） ----
    static void testMarkerInterfaceShape() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.Signature$RefTypeSig");
        check(c.getDeclaredMethods().length == 0, "RefTypeSig declares no methods of its own");
        boolean extendsSignature = false;
        for (Class<?> i : c.getInterfaces()) if (i == Signature.class) extendsSignature = true;
        check(extendsSignature, "RefTypeSig extends Signature directly");
        check(Signature.RefTypeSig.class.isAssignableFrom(Signature.ClassTypeSig.class), "assignable from ClassTypeSig");
        check(Signature.RefTypeSig.class.isAssignableFrom(Signature.ArrayTypeSig.class), "assignable from ArrayTypeSig");
        check(Signature.RefTypeSig.class.isAssignableFrom(Signature.TypeVarSig.class), "assignable from TypeVarSig");
        check(!Signature.RefTypeSig.class.isAssignableFrom(Signature.BaseTypeSig.class), "NOT assignable from BaseTypeSig");
    }

    // ---- Part 4: 三实现多态矩阵（parseFrom 与工厂双通道） ----
    static void testPolymorphismMatrix() {
        Signature.RefTypeSig[] viaParse = {
                (Signature.RefTypeSig) Signature.parseFrom("Ljava/lang/String;"),
                (Signature.RefTypeSig) Signature.parseFrom("[I"),
                (Signature.RefTypeSig) Signature.parseFrom("TT;"),
        };
        String[] expect = {"Ljava/lang/String;", "[I", "TT;"};
        Class<?>[] kinds = {Signature.ClassTypeSig.class, Signature.ArrayTypeSig.class, Signature.TypeVarSig.class};
        for (int i = 0; i < 3; i++) {
            check(viaParse[i] instanceof Signature, "instance " + i + " is Signature");
            check(kinds[i].isInstance(viaParse[i]), "instance " + i + " exact kind");
            check(viaParse[i].signatureString().equals(expect[i]), "poly dispatch " + i + " -> " + expect[i]);
        }
        Signature.RefTypeSig cls = Signature.ClassTypeSig.of(ClassDesc.of("java.lang.String"));
        Signature.RefTypeSig arr = Signature.ArrayTypeSig.of(Signature.BaseTypeSig.of('I'));
        Signature.RefTypeSig tv = Signature.TypeVarSig.of("T");
        check(cls.signatureString().equals("Ljava/lang/String;"), "factory class dispatch");
        check(arr.signatureString().equals("[I"), "factory array dispatch");
        check(tv.signatureString().equals("TT;"), "factory typevar dispatch");
        check(cls instanceof Signature.RefTypeSig && arr instanceof Signature.RefTypeSig && tv instanceof Signature.RefTypeSig,
                "all factory products RefTypeSig");
        Signature base = Signature.BaseTypeSig.of('I');
        check(!(base instanceof Signature.RefTypeSig), "BaseTypeSig excluded from RefTypeSig");
    }

    // ---- Part 5: RefTypeSig 的四类消费位（type arg bound/type param bound/array component/throws 段） ----
    static void testUsagePositions() {
        Signature.RefTypeSig str = (Signature.RefTypeSig) Signature.parseFrom("Ljava/lang/String;");
        check(Signature.TypeArg.of(str).boundType().equals(str), "usable as TypeArg bound");
        Signature.TypeParam tp = Signature.TypeParam.of("T", str);
        check(tp.classBound().get().equals(str), "usable as TypeParam classBound");
        check(Signature.ArrayTypeSig.of(str).signatureString().equals("[Ljava/lang/String;"), "usable as array component");
        check(Signature.ArrayTypeSig.of(str) instanceof Signature.RefTypeSig, "array of ref is still RefTypeSig");
        // throws 段只接受 ClassTypeSig/TypeVarSig（都是 RefTypeSig 的子型）
        MethodSignature ms = MethodSignature.parseFrom("()V^Ljava/lang/Exception;^TT;");
        check(ms.throwableSignatures().get(0) instanceof Signature.RefTypeSig, "ClassTypeSig throw is RefTypeSig");
        check(ms.throwableSignatures().get(1) instanceof Signature.RefTypeSig, "TypeVarSig throw is RefTypeSig");
    }

    // ---- Part 6: 泛型签名深结构中 RefTypeSig 递归出现 ----
    static void testDeepStructure() {
        Signature.ClassTypeSig map = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/Map<Ljava/lang/String;[I>;");
        check(map.typeArgs().size() == 2, "Map 2 args");
        check(((Signature.TypeArg.Bounded) map.typeArgs().get(0)).boundType() instanceof Signature.RefTypeSig, "arg0 bound RefTypeSig");
        check(((Signature.TypeArg.Bounded) map.typeArgs().get(1)).boundType() instanceof Signature.ArrayTypeSig, "arg1 bound ArrayTypeSig");
        Signature.ClassTypeSig inner = (Signature.ClassTypeSig) Signature.parseFrom("LOuter<TT;>.Inner;");
        check(inner.outerType().get() instanceof Signature.RefTypeSig, "outerType is RefTypeSig");
        check(inner instanceof Signature.RefTypeSig, "inner itself RefTypeSig");
        // 跨类型值不等
        check(!Signature.parseFrom("Ljava/lang/String;").equals(Signature.parseFrom("[Ljava/lang/String;")), "class vs array not equals");
        check(!Signature.parseFrom("TT;").equals(Signature.parseFrom("Ljava/lang/String;")), "typevar vs class not equals");
    }

    // ---- Part 7: 真类泛型闭环（javac 产物 TT; 实参/返回 → RefTypeSig） ----
    static void testRealClassGenericLoop() throws Exception {
        byte[] self = RefTypeSigTest.class.getResourceAsStream("RefTypeSigTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        MethodModel gen = null;
        for (MethodModel m : cm.methods()) if (m.methodName().stringValue().equals("gen")) gen = m;
        check(gen != null, "gen found");
        String gs = gen.findAttribute(Attributes.signature()).orElseThrow().signature().stringValue();
        check(gs.equals("<T:Ljava/lang/Object;>(TT;Ljava/util/List<Ljava/lang/String;>;)TT;"), "gen sig exact: " + gs);
        MethodSignature mm = MethodSignature.parseFrom(gs);
        check(mm.arguments().get(0) instanceof Signature.RefTypeSig, "javac TT; arg is RefTypeSig");
        check(mm.arguments().get(0) instanceof Signature.TypeVarSig, "and exactly TypeVarSig");
        check(mm.result() instanceof Signature.RefTypeSig, "javac TT; result is RefTypeSig");
        check(((Signature.ClassTypeSig) mm.arguments().get(1)).typeArgs().get(0)
                instanceof Signature.TypeArg.Bounded, "List<String> arg Bounded");
        check(((Signature.TypeArg.Bounded) ((Signature.ClassTypeSig) mm.arguments().get(1)).typeArgs().get(0)).boundType()
                instanceof Signature.RefTypeSig, "bound String is RefTypeSig");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMarkerInterfaceShape();
        testPolymorphismMatrix();
        testUsagePositions();
        testDeepStructure();
        testRealClassGenericLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

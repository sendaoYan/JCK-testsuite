/*
 * @test
 * @summary java.lang.classfile.Signature.ThrowableSig conformance: white-box structure + black-box behavior.
 * @run main ThrowableSigTest
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

public class ThrowableSigTest {
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

    // ---- 真类闭环素材：泛型 throws 子句（javac 产物 Signature 属性 ^TT; 段） ----
    static <T extends Exception> void genThr(T t) throws T { }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.Signature$ThrowableSig");
        check(c != null, "java.lang.classfile.Signature.ThrowableSig loads");
        check(c.getName().equals("java.lang.classfile.Signature$ThrowableSig"), "binary name java.lang.classfile.Signature$ThrowableSig");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Signature.ThrowableSig is public");
        check(c.isInterface(), "java.lang.classfile.Signature.ThrowableSig is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.Signature.ThrowableSig has no superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        Signature.ThrowableSig t = Signature.ClassTypeSig.of(ClassDesc.of("java.lang.Exception"));
        check(t.signatureString().equals("Ljava/lang/Exception;"), "class throwable");
        Signature.ThrowableSig t2 = Signature.TypeVarSig.of("E");
        check(t2.signatureString().equals("TE;"), "type var throwable");

    }

    // ---- Part 3: 标记接口白盒（独立接口，extends Signature；实现者 = ClassTypeSig + TypeVarSig） ----
    static void testMarkerInterfaceShape() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.Signature$ThrowableSig");
        check(c.getDeclaredMethods().length == 0, "ThrowableSig declares no methods of its own");
        boolean extendsSignature = false;
        for (Class<?> i : c.getInterfaces()) if (i == Signature.class) extendsSignature = true;
        check(extendsSignature, "ThrowableSig extends Signature directly");
        check(Signature.ThrowableSig.class.isAssignableFrom(Signature.ClassTypeSig.class), "assignable from ClassTypeSig");
        check(Signature.ThrowableSig.class.isAssignableFrom(Signature.TypeVarSig.class), "assignable from TypeVarSig");
        check(!Signature.ThrowableSig.class.isAssignableFrom(Signature.ArrayTypeSig.class), "NOT assignable from ArrayTypeSig");
        check(!Signature.ThrowableSig.class.isAssignableFrom(Signature.BaseTypeSig.class), "NOT assignable from BaseTypeSig");
        check(!Signature.ThrowableSig.class.isAssignableFrom(Signature.RefTypeSig.class), "NOT assignable from abstract RefTypeSig");
    }

    // ---- Part 4: 实现者矩阵（实测：只有 ClassTypeSig 与 TypeVarSig 承载 ThrowableSig） ----
    static void testImplementationMatrix() {
        check(Signature.parseFrom("Ljava/lang/Exception;") instanceof Signature.ThrowableSig, "class sig is ThrowableSig");
        check(Signature.parseFrom("TT;") instanceof Signature.ThrowableSig, "typevar sig is ThrowableSig");
        check(!(Signature.parseFrom("[Ljava/lang/Exception;") instanceof Signature.ThrowableSig), "array NOT ThrowableSig");
        check(!(Signature.parseFrom("I") instanceof Signature.ThrowableSig), "base NOT ThrowableSig");
        check(Signature.ClassTypeSig.of(ClassDesc.of("java.io.IOException")) instanceof Signature.ThrowableSig, "factory class ThrowableSig");
        check(Signature.TypeVarSig.of("X") instanceof Signature.ThrowableSig, "factory typevar ThrowableSig");
    }

    // ---- Part 5: 方法签名 throws 段全形态（类类型/TypeVar/多段有序/泛型类） ----
    static void testThrowsSegmentMatrix() {
        MethodSignature m1 = MethodSignature.parseFrom("()V^Ljava/lang/Exception;");
        check(m1.throwableSignatures().size() == 1, "single class throw");
        check(m1.throwableSignatures().get(0) instanceof Signature.ClassTypeSig, "single throw ClassTypeSig");
        check(m1.throwableSignatures().get(0).signatureString().equals("Ljava/lang/Exception;"), "single throw string");
        MethodSignature m2 = MethodSignature.parseFrom("()V^TT;");
        check(m2.throwableSignatures().get(0) instanceof Signature.TypeVarSig, "typevar throw");
        check(m2.throwableSignatures().get(0).signatureString().equals("TT;"), "typevar throw string");
        MethodSignature m3 = MethodSignature.parseFrom("()V^Ljava/lang/Exception;^Ljava/io/IOException;");
        check(m3.throwableSignatures().size() == 2, "two throws");
        check(m3.throwableSignatures().get(0).signatureString().equals("Ljava/lang/Exception;"), "throws order 0");
        check(m3.throwableSignatures().get(1).signatureString().equals("Ljava/io/IOException;"), "throws order 1");
        check(m3.throwableSignatures().get(1) instanceof Signature.ThrowableSig, "throws entry ThrowableSig");
        MethodSignature m4 = MethodSignature.parseFrom("()V^Ljava/util/Map<TK;TV;>;");
        check(((Signature.ClassTypeSig) m4.throwableSignatures().get(0)).typeArgs().size() == 2, "generic throwable keeps args");
        // throws 段 round-trip 幂等
        String[] legal = {"()V^Ljava/lang/Exception;", "()V^TT;", "<T:Ljava/lang/Object;>(TT;)V^Ljava/lang/Exception;",
                "<T:Ljava/lang/Object;>(TT;)V^TT;", "<T:Ljava/lang/Object;>(TT;)V^Ljava/lang/Exception;^TT;"};
        for (String s : legal) {
            String out = MethodSignature.parseFrom(s).signatureString();
            check(out.equals(s), "throws round-trip '" + s + "' -> '" + out + "'");
            check(MethodSignature.parseFrom(out).signatureString().equals(s), "throws re-parse idempotent: " + s);
        }
    }

    // ---- Part 6: build 注入 → re-parse throws 段闭环 ----
    static void testBuildAttributeLoop() throws Exception {
        MethodSignature model = MethodSignature.parseFrom("<T:Ljava/lang/Object;>(TT;)V^Ljava/lang/Exception;");
        byte[] b = ClassFile.of().build(ClassDesc.of("ThrLoopT"), cb -> cb
                .withMethod("m", MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;)V"), ClassFile.ACC_PUBLIC,
                        mb -> mb.with(SignatureAttribute.of(model))));
        MethodModel mm = ClassFile.of().parse(b).methods().get(0);
        SignatureAttribute sa = mm.findAttribute(Attributes.signature()).orElseThrow();
        check(sa.signature().stringValue().equals(model.signatureString()), "attr raw string");
        MethodSignature rt = sa.asMethodSignature();
        check(rt.throwableSignatures().size() == 1, "asMethodSignature throws 1");
        check(rt.throwableSignatures().get(0) instanceof Signature.ThrowableSig, "asMethodSignature throw ThrowableSig");
        check(rt.throwableSignatures().get(0).signatureString().equals("Ljava/lang/Exception;"), "asMethodSignature throw string");
        check(rt.equals(model), "asMethodSignature equals model");
    }

    // ---- Part 7: 真类泛型闭环（javac <T extends Exception> ... throws T → ^TT;） ----
    static void testRealClassGenericLoop() throws Exception {
        byte[] self = ThrowableSigTest.class.getResourceAsStream("ThrowableSigTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        MethodModel gen = null;
        for (MethodModel m : cm.methods()) if (m.methodName().stringValue().equals("genThr")) gen = m;
        check(gen != null, "genThr found");
        String gs = gen.findAttribute(Attributes.signature()).orElseThrow().signature().stringValue();
        check(gs.equals("<T:Ljava/lang/Exception;>(TT;)V^TT;"), "genThr sig exact: " + gs);
        MethodSignature mm = MethodSignature.parseFrom(gs);
        check(mm.throwableSignatures().size() == 1, "genThr 1 throw");
        check(mm.throwableSignatures().get(0) instanceof Signature.TypeVarSig, "genThr throw TypeVarSig");
        check(mm.throwableSignatures().get(0) instanceof Signature.ThrowableSig, "genThr throw ThrowableSig");
        check(mm.typeParameters().get(0).classBound().get().signatureString().equals("Ljava/lang/Exception;"), "genThr tp bound Exception");
        check(mm.throwableSignatures().get(0).signatureString().equals("TT;"), "genThr throw TT;");
    }

    // ---- Part 8: equals/hashCode 值语义 ----
    static void testEqualityContract() {
        check(Signature.parseFrom("Ljava/lang/Exception;").equals(Signature.parseFrom("Ljava/lang/Exception;")), "class throw equals");
        check(Signature.TypeVarSig.of("T").equals(Signature.parseFrom("TT;")), "typevar throw equals");
        check(!Signature.parseFrom("Ljava/lang/Exception;").equals(Signature.parseFrom("Ljava/io/IOException;")), "different throw not equals");
        check(Signature.parseFrom("Ljava/lang/Exception;").hashCode() == Signature.parseFrom("Ljava/lang/Exception;").hashCode(),
                "hashCode consistent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMarkerInterfaceShape();
        testImplementationMatrix();
        testThrowsSegmentMatrix();
        testBuildAttributeLoop();
        testRealClassGenericLoop();
        testEqualityContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

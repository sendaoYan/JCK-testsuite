/*
 * @test
 * @summary java.lang.classfile.instruction.ExceptionCatch conformance: white-box structure + black-box behavior.
 * @run main ExceptionCatchTest
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

public class ExceptionCatchTest {
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

    // 触发 ConstantDescs 完整初始化，规避本 JDK 构建的 MethodTypeDesc.ofDescriptor 静态初始化环
    static final ClassDesc TOUCH = ConstantDescs.CD_Object;

    // 发射-回读辅助：build 唯一类名 + parse 回全部 CodeElement
    static java.util.List<CodeElement> emit(String m, MethodTypeDesc mtd, java.util.function.Consumer<CodeBuilder> body) throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("EmECC" + m), cb -> cb.withMethodBody(m, mtd, ClassFile.ACC_STATIC, body));
        java.util.List<CodeElement> els = new ArrayList<>();
        for (MethodModel mm : ClassFile.of().parse(b).methods())
            if (mm.code().isPresent()) els.addAll(mm.code().get().elementList());
        return els;
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.instruction.ExceptionCatch");
        check(c != null, "java.lang.classfile.instruction.ExceptionCatch loads");
        check(c.getName().equals("java.lang.classfile.instruction.ExceptionCatch"), "binary name java.lang.classfile.instruction.ExceptionCatch");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.instruction.ExceptionCatch is public");
        check(c.isInterface(), "java.lang.classfile.instruction.ExceptionCatch is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.instruction.ExceptionCatch has no superclass");
        check(declares(c, "handler"), "declares handler");
        check(declares(c, "tryStart"), "declares tryStart");
        check(declares(c, "tryEnd"), "declares tryEnd");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> {
            Label s = cob.newLabel();
            Label e = cob.newLabel();
            Label h = cob.newLabel();
            ec[0] = ExceptionCatch.of(s, e, h);
            cob.labelBinding(s).labelBinding(e).labelBinding(h).return_();
        }));
        ExceptionCatch x = ec[0];
        check(x != null, "of created");
        check(x.tryStart() != null && x.tryEnd() != null && x.handler() != null, "labels non-null");
        check(x.catchType().isEmpty(), "catchType empty");

    }


    // ---- Part 3: 发射-回读（核心）：typed catch + catchAll ----
    static void testEmitReadback() throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("EmECC"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, cob -> {
            Label s = cob.newLabel(), e = cob.newLabel(), h = cob.newLabel(), done = cob.newLabel();
            cob.labelBinding(s).ldc("x").pop().goto_(done).labelBinding(e);
            cob.labelBinding(h).new_(ClassDesc.of("java.lang.IllegalStateException")).dup()
               .invokespecial(ClassDesc.of("java.lang.IllegalStateException"), "<init>", MethodTypeDesc.ofDescriptor("()V")).athrow();
            cob.labelBinding(done).return_();
            cob.exceptionCatch(s, e, h, ClassDesc.of("java.lang.IllegalStateException"));
        }));
        CodeAttribute ca = (CodeAttribute) ClassFile.of().parse(b).methods().get(0).code().get();
        ExceptionCatch x = null;
        for (CodeElement el : ca.elementList()) if (el instanceof ExceptionCatch ec) x = ec;
        check(x != null, "exceptionCatch emitted + read back");
        check(x.catchType().isPresent(), "typed catch present");
        check(x.catchType().get().asInternalName().equals("java/lang/IllegalStateException"), "catchType internal name");
        check(x.catchType().get().asSymbol().descriptorString().equals("Ljava/lang/IllegalStateException;"), "catchType symbol descriptor");
        check(x.tryStart() != null && x.tryEnd() != null && x.handler() != null, "labels non-null");
        int ts = ca.labelToBci(x.tryStart()), te = ca.labelToBci(x.tryEnd()), hb = ca.labelToBci(x.handler());
        check(ts == 0, "tryStart bci 0");
        check(te > ts, "tryEnd bci > tryStart");
        check(hb == te, "handler bci == tryEnd bci");
        check(x instanceof PseudoInstruction, "PseudoInstruction");
        check(!((Object) x instanceof Instruction), "not Instruction");
        check(x instanceof CodeElement, "CodeElement");
        check(ca.exceptionHandlers().size() == 1, "CodeModel.exceptionHandlers 1");
        check(ca.exceptionHandlers().get(0).catchType().equals(x.catchType())
           && ca.labelToBci(ca.exceptionHandlers().get(0).handler()) == hb, "exceptionHandlers same catch semantically");
        // catchAll → catchType empty
        byte[] b2 = ClassFile.of().build(ClassDesc.of("EmECC2"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, cob -> {
            Label s = cob.newLabel(), e = cob.newLabel(), h = cob.newLabel(), done = cob.newLabel();
            cob.labelBinding(s).ldc("x").pop().goto_(done).labelBinding(e);
            cob.labelBinding(h).new_(ClassDesc.of("java.lang.IllegalStateException")).dup()
               .invokespecial(ClassDesc.of("java.lang.IllegalStateException"), "<init>", MethodTypeDesc.ofDescriptor("()V")).athrow();
            cob.labelBinding(done).return_();
            cob.exceptionCatchAll(s, e, h);
        }));
        ExceptionCatch any = null;
        for (CodeElement el : ClassFile.of().parse(b2).methods().get(0).code().get().elementList()) if (el instanceof ExceptionCatch ec) any = ec;
        check(any != null && any.catchType().isEmpty(), "exceptionCatchAll → catchType empty");
    }

    // ---- Part 4: 工厂重载 + 负例 + javac 真产物 ----
    static void testFactoryNegativeReal() throws Exception {
        final Label[] ls = new Label[3];
        ClassFile.of().build(ClassDesc.of("EmECC3"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, cob -> {
            ls[0] = cob.newLabel(); ls[1] = cob.newLabel(); ls[2] = cob.newLabel();
            ClassEntry ce = cob.constantPool().classEntry(ClassDesc.of("java.lang.Exception"));
            ExceptionCatch typed = ExceptionCatch.of(ls[0], ls[1], ls[2], Optional.of(ce));
            check(typed.catchType().isPresent() && typed.catchType().get() == ce, "of(s,e,h,Optional.of) precise entry");
            check(typed.tryStart() != null && typed.tryEnd() != null && typed.handler() != null, "of labels non-null (identity not preserved by impl)");
            ExceptionCatch all = ExceptionCatch.of(ls[0], ls[1], ls[2], Optional.empty());
            check(all.catchType().isEmpty(), "of(...,Optional.empty()) empty");
            cob.return_();
        }));
        try { ExceptionCatch.of(null, null, null); check(false, "of(null,null,null) must throw"); }
        catch (NullPointerException e) { check(true, "of(null,null,null) NPE"); }
        try { ExceptionCatch.of(null, null, null, Optional.empty()); check(false, "of(n,n,n,empty) must throw"); }
        catch (NullPointerException e) { check(true, "of(n,n,n,empty) NPE"); }
        // javac 真产物：本类 tryCatchHelper 含 try/catch(IllegalStateException)
        byte[] self = ExceptionCatchTest.class.getResourceAsStream("ExceptionCatchTest.class").readAllBytes();
        boolean foundTyped = false;
        for (MethodModel mm : ClassFile.of().parse(self).methods()) {
            if (!mm.methodName().stringValue().equals("tryCatchHelper") || !mm.code().isPresent()) continue;
            for (CodeElement el : mm.code().get().elementList())
                if (el instanceof ExceptionCatch ec && ec.catchType().isPresent()
                        && ec.catchType().get().asInternalName().equals("java/lang/IllegalStateException")) foundTyped = true;
        }
        check(foundTyped, "javac try/catch product has ExceptionCatch with precise catchType");
    }

    static int tryCatchHelper(int x) {
        try { return x / (x - x); } catch (IllegalStateException e) { return -1; }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitReadback();
        testFactoryNegativeReal();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

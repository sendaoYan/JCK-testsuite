/*
 * @test
 * @summary java.lang.classfile.CodeBuilder.CatchBuilder conformance: white-box structure + black-box behavior.
 * @run main CatchBuilderTest
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

public class CatchBuilderTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.CodeBuilder$CatchBuilder");
        check(c != null, "java.lang.classfile.CodeBuilder.CatchBuilder loads");
        check(c.getName().equals("java.lang.classfile.CodeBuilder$CatchBuilder"), "binary name java.lang.classfile.CodeBuilder$CatchBuilder");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.CodeBuilder.CatchBuilder is public");
        check(c.isInterface(), "java.lang.classfile.CodeBuilder.CatchBuilder is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.CodeBuilder.CatchBuilder has no superclass");
        check(declares(c, "catching"), "declares catching");
        check(declares(c, "catchingMulti"), "declares catchingMulti");
        check(declares(c, "catchingAll"), "declares catchingAll");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> cob.trying(blk -> blk.return_(), cat -> cat.catching(ClassDesc.of("java.lang.Exception"), hb -> hb.athrow())).return_()));
        ClassModel cm = ClassFile.of().parse(b);
        CodeModel code = cm.methods().get(0).code().get();
        check(code.exceptionHandlers().size() == 1, "one exception handler");
        check(code.exceptionHandlers().get(0).catchType().get().asSymbol().equals(ClassDesc.of("java.lang.Exception")), "handler catch type");

    }

    // ---- Part 3: catch 表精确矩阵（catching + catchingMulti + catchingAll 顺序/类型表，实测锁定） ----
    static void testCatchTableMatrix() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("CatT"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC, c -> c
                .trying(t -> t.aconst_null().athrow(),
                        cat -> cat.catching(ClassDesc.of("java.io.IOException"), h -> h.pop())
                                .catchingMulti(List.of(ClassDesc.of("java.lang.IllegalStateException"), ClassDesc.of("java.lang.UnsupportedOperationException")), h -> h.pop())
                                .catchingAll(h -> h.pop()))
                .return_()));
        ClassModel cm = cf.parse(b);
        check(cf.verify(cm).isEmpty(), "catch table verify clean");
        List<ExceptionCatch> eh = cm.methods().get(0).code().get().exceptionHandlers();
        check(eh.size() == 4, "catching+multi(2)+all → 4 handlers: " + eh.size());
        // 顺序与类型表精确断言（注册顺序）
        check(eh.get(0).catchType().get().asInternalName().equals("java/io/IOException"), "handler 0 IOException");
        check(eh.get(1).catchType().get().asInternalName().equals("java/lang/IllegalStateException"), "handler 1 multi[0] ISE");
        check(eh.get(2).catchType().get().asInternalName().equals("java/lang/UnsupportedOperationException"), "handler 2 multi[1] UOE");
        check(eh.get(3).catchType().isEmpty(), "handler 3 catchAll empty Optional");
        // catchingMulti 展开的多条目共享同一 handler 与同一 try 区间（实测 ==）
        check(eh.get(1).handler() == eh.get(2).handler(), "multi-catch shared handler label");
        check(eh.get(1).tryStart() == eh.get(2).tryStart() && eh.get(1).tryEnd() == eh.get(2).tryEnd(), "multi-catch shared try range");
        // 每条 try 区间非 null
        for (ExceptionCatch e : eh) check(e.handler() != null && e.tryStart() != null && e.tryEnd() != null, "labels non-null");
        // 多个 catching 链式 → 顺序保持
        byte[] b2 = cf.build(ClassDesc.of("CatU"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC, c -> c
                .trying(t -> t.aconst_null().athrow(),
                        cat -> cat.catching(ClassDesc.of("java.lang.CloneNotSupportedException"), h -> h.pop())
                                .catching(ClassDesc.of("java.lang.InterruptedException"), h -> h.pop())
                                .catchingAll(h -> h.pop()))
                .return_()));
        List<ExceptionCatch> eh2 = cf.parse(b2).methods().get(0).code().get().exceptionHandlers();
        check(eh2.size() == 3, "chain catching x2 + all → 3");
        check(eh2.get(0).catchType().get().asInternalName().equals("java/lang/CloneNotSupportedException"), "chain order 0");
        check(eh2.get(1).catchType().get().asInternalName().equals("java/lang/InterruptedException"), "chain order 1");
        check(eh2.get(2).catchType().isEmpty(), "chain all last");
        check(eh2.get(0).handler() != eh2.get(1).handler(), "distinct catching → distinct handlers");
    }

    // ---- Part 4: catching/catchingAll 运行闭环（defineClass 真捕获） ----
    static void testCatchRuntime() throws Exception {
        ClassFile cf = ClassFile.of();
        // ArithmeticException 真捕获 → 42
        byte[] b = cf.build(ClassDesc.of("CatV"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c
                .trying(t -> t.iconst_1().iconst_0().idiv().pop().iconst_0().ireturn(),
                        cat -> cat.catching(ClassDesc.of("java.lang.ArithmeticException"), h -> h.pop().bipush(42).ireturn()))
                .iconst_0().ireturn()));
        check(cf.verify(cf.parse(b)).isEmpty(), "catching verify clean");
        check(MethodHandles.lookup().defineClass(b).getMethod("m").invoke(null).equals(42), "catching(AE) runtime 42");
        // catchingAll 捕获 NPE → 7
        byte[] b2 = cf.build(ClassDesc.of("CatW"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c
                .trying(t -> t.aconst_null().athrow(),
                        cat -> cat.catchingAll(h -> h.pop().bipush(7).ireturn()))
                .iconst_0().ireturn()));
        check(cf.verify(cf.parse(b2)).isEmpty(), "catchingAll verify clean");
        check(MethodHandles.lookup().defineClass(b2).getMethod("m").invoke(null).equals(7), "catchingAll runtime 7");
        // 异常穿透：未 catch 的类型不在表内 → 传播（异常表精确性）
        byte[] b3 = cf.build(ClassDesc.of("CatX"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c
                .trying(t -> t.new_(ClassDesc.of("java.lang.IllegalStateException")).dup()
                                .invokespecial(ClassDesc.of("java.lang.IllegalStateException"), "<init>", MethodTypeDesc.ofDescriptor("()V")).athrow(),
                        cat -> cat.catching(ClassDesc.of("java.lang.ArithmeticException"), h -> h.pop().iconst_1().ireturn()))
                .iconst_0().ireturn()));
        try {
            MethodHandles.lookup().defineClass(b3).getMethod("m").invoke(null);
            check(false, "unmatched exception must propagate");
        } catch (java.lang.reflect.InvocationTargetException e) {
            check(e.getCause() instanceof IllegalStateException, "ISE not caught by AE handler");
        }
    }

    // ---- Part 5: catching 后 re-try（try 块内 block 协作）+ catch 块内再抛 ----
    static void testCatchRethrow() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("CatY"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c
                .trying(t -> t.aconst_null().athrow(),
                        cat -> cat.catching(ClassDesc.of("java.lang.NullPointerException"), h -> h.athrow()))
                .return_()));
        check(cf.verify(cf.parse(b)).isEmpty(), "rethrow verify clean");
        try {
            MethodHandles.lookup().defineClass(b).getMethod("m").invoke(null);
            check(false, "rethrown NPE must propagate");
        } catch (java.lang.reflect.InvocationTargetException e) {
            check(e.getCause() instanceof NullPointerException, "catch body athrow rethrows");
        }
        // catch handler 收到的异常对象在栈顶（pop 后可读 getMessage）
        byte[] b2 = cf.build(ClassDesc.of("CatZ"), cb -> cb.withMethodBody("msg", MethodTypeDesc.ofDescriptor("()Ljava/lang/String;"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c
                .trying(t -> t.aconst_null().athrow(),
                        cat -> cat.catchingAll(h -> h.invokevirtual(ClassDesc.of("java.lang.Object"), "toString", MethodTypeDesc.ofDescriptor("()Ljava/lang/String;")).areturn()))
                .ldc("none").areturn()));
        check(cf.verify(cf.parse(b2)).isEmpty(), "handler receiver verify clean");
        String msgv = (String) MethodHandles.lookup().defineClass(b2).getMethod("msg").invoke(null);
        check(msgv.startsWith("java.lang.NullPointerException"), "handler top-of-stack is exception object: " + msgv);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCatchTableMatrix();
        testCatchRuntime();
        testCatchRethrow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

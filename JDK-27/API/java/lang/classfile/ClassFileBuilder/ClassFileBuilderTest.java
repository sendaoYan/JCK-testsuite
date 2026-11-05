/*
 * @test
 * @summary java.lang.classfile.ClassFileBuilder conformance: white-box structure + black-box behavior.
 * @run main ClassFileBuilderTest
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

public class ClassFileBuilderTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassFileBuilder");
        check(c != null, "java.lang.classfile.ClassFileBuilder loads");
        check(c.getName().equals("java.lang.classfile.ClassFileBuilder"), "binary name java.lang.classfile.ClassFileBuilder");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassFileBuilder is public");
        check(c.isInterface(), "java.lang.classfile.ClassFileBuilder is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.ClassFileBuilder has no superclass");
        check(declares(c, "accept"), "declares accept");
        check(declares(c, "with"), "declares with");
        check(declares(c, "constantPool"), "declares constantPool");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        final ClassFileBuilder<?,?>[] cap = new ClassFileBuilder<?,?>[1];
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> { cap[0] = cb; cb.withSuperclass(ClassDesc.of("java.lang.Object")).withVersion(52, 0); });
        check(cap[0] != null, "builder captured");
        check(cap[0].constantPool() != null, "constantPool non-null");
        ClassModel cm = ClassFile.of().parse(b);
        check(cm.majorVersion() == 52, "withVersion via builder applied");
        check(cm.superclass().get().asInternalName().equals("java/lang/Object"), "withSuperclass applied");

    }

    // ---- Part 3: constantPool() 通道 —— build 回调内取池条目并引用进指令（实测） ----
    static void testConstantPoolChannel() throws Exception {
        ClassFile cf = ClassFile.of();
        final IntegerEntry[] shared = new IntegerEntry[1];
        // CodeTransform.accept 的 builder 上 cb.constantPool().intEntry(...) → ldc 引用 → re-parse 回读
        byte[] src = cf.build(ClassDesc.of("Cfbt1.T"), cb -> cb
                .withField("f0", ConstantDescs.CD_int, ClassFile.ACC_PRIVATE)
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_STATIC, c -> c.nop().iconst_1().ireturn()));
        ClassModel sm = cf.parse(src);
        byte[] out = cf.transformClass(sm, ClassTransform.transformingMethodBodies(new CodeTransform() {
            public void accept(CodeBuilder cb, CodeElement e) {
                if (e instanceof Instruction i && i.opcode() == Opcode.NOP) {
                    IntegerEntry ie = cb.constantPool().intEntry(12345);
                    shared[0] = ie;
                    cb.ldc(ie).pop();
                } else cb.with(e);
            }
        }));
        boolean hasLdc = false;
        for (CodeElement e : cf.parse(out).methods().get(0).code().get().elementList())
            if (e instanceof ConstantInstruction.LoadConstantInstruction li && Integer.valueOf(12345).equals(li.constantValue())) hasLdc = true;
        check(shared[0] != null, "CodeBuilder.constantPool() usable in transform");
        check(hasLdc, "pool entry referenced into LDC instruction and reparsed");
        // build 回调内 cb.constantPool() 是 ConstantPoolBuilder：utf8Entry 去重（同值同实例）
        final boolean[] dedup = new boolean[1];
        final int[] idx = new int[1];
        cf.build(ClassDesc.of("Cfbt2.U"), cb -> {
            ConstantPoolBuilder cpb = cb.constantPool();
            Utf8Entry u1 = cpb.utf8Entry("dupu");
            Utf8Entry u2 = cpb.utf8Entry("dupu");
            dedup[0] = u1 == u2 && u1.index() == u2.index();
            idx[0] = u1.index();
            check(u1.equalsString("dupu"), "utf8Entry equalsString");
            cb.withField("fz", ConstantDescs.CD_int, 0);
        });
        check(dedup[0], "constantPool().utf8Entry dedup == same entry");
        // 池条目索引可回查（ConstantPool.entryByIndex）
        byte[] b = cf.build(ClassDesc.of("Cfbt3.V"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_STATIC, c -> c.iconst_1().ireturn()));
        ClassModel cm = cf.parse(b);
        ConstantPool pool = cm.constantPool();
        PoolEntry any = null;
        for (PoolEntry pe : pool) { any = pe; break; }
        check(any != null, "pool iterable non-empty");
        check(pool.entryByIndex(any.index()) == any, "entryByIndex(idx) identity");
        try {
            pool.entryByIndex(pool.size() + 5);
            check(false, "entryByIndex out of range must throw");
        } catch (ConstantPoolException e) {
            check(e.getMessage() != null, "ConstantPoolException msg non-null");
        }
        // MethodBuilder/FieldBuilder 侧 constantPool() 同为 ConstantPoolBuilder（多态面）
        final boolean[] mbCp = new boolean[1], fbCp = new boolean[1];
        cf.transformClass(sm, new ClassTransform() {
            public void accept(ClassBuilder b, ClassElement e) {
                if (e instanceof MethodModel mm) b.transformMethod(mm, new MethodTransform() {
                    public void accept(MethodBuilder mb, MethodElement me) { mbCp[0] = mb.constantPool() instanceof ConstantPoolBuilder; mb.with(me); }
                });
                else if (e instanceof FieldModel fm) b.transformField(fm, new FieldTransform() {
                    public void accept(FieldBuilder fb, FieldElement fe) { fbCp[0] = fb.constantPool() instanceof ConstantPoolBuilder; fb.with(fe); }
                });
                else b.with(e);
            }
        });
        check(mbCp[0], "MethodBuilder.constantPool() is ConstantPoolBuilder");
        check(fbCp[0], "FieldBuilder.constantPool() is ConstantPoolBuilder");
    }

    // ---- Part 4: with/accept Consumer 面 + transform(CompoundElement, ClassFileTransform) 默认方法 ----
    static void testWithAcceptTransform() throws Exception {
        ClassFile cf = ClassFile.of();
        // ClassFileBuilder extends Consumer<E>：accept(e) 与 with(e) 等效（实测注入成员成功）
        byte[] src = cf.build(ClassDesc.of("Cfbt4.T"), cb -> cb
                .withInterfaceSymbols(ClassDesc.of("java.io.Serializable"))
                .withField("f", ConstantDescs.CD_int, ClassFile.ACC_PRIVATE)
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_STATIC, c -> c.iconst_1().ireturn()));
        ClassModel sm = cf.parse(src);
        Interfaces ie = sm.elementList().stream().filter(e -> e instanceof Interfaces).map(e -> (Interfaces) e).findFirst().get();
        byte[] b = cf.build(ClassDesc.of("Cfbt5.U"), cb -> cb.accept(ie)); // Consumer.accept 通道
        check(cf.parse(b).interfaces().size() == 1, "accept(ClassElement) injects like with()");
        // with 返回自身可链式
        byte[] b2 = cf.build(ClassDesc.of("Cfbt6.V"), cb -> cb.with(ie).with(ClassFileVersion.of(52, 0)));
        ClassModel cm2 = cf.parse(b2);
        check(cm2.interfaces().size() == 1 && cm2.majorVersion() == 52, "with() chainable returns same builder");
        // transform(CompoundElement<E>, ClassFileTransform<?,E,B>) 默认方法：整模型拷贝
        byte[] b3 = cf.build(ClassDesc.of("Cfbt7.W"), cb -> cb.transform(sm, ClassTransform.ACCEPT_ALL));
        ClassModel cm3 = cf.parse(b3);
        check(cm3.fields().size() == 1 && cm3.methods().size() == 1 && cm3.interfaces().size() == 1, "builder.transform copies all members");
        check(cf.verify(cm3).isEmpty(), "copied model verify clean");
        // CodeBuilder.transforming(CodeTransform, Consumer<CodeBuilder>)：transform 只作用于 handler 内发射的元素
        // （实测：前置 NOP 保留，handler 内 NOP 被剥）—— 语义锁定
        byte[] b4 = cf.build(ClassDesc.of("Cfbt8.X"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_STATIC,
                c -> c.nop().transforming(new CodeTransform() {
                    public void accept(CodeBuilder cb, CodeElement e) {
                        if (!(e instanceof Instruction i) || i.opcode() != Opcode.NOP) cb.with(e);
                    }
                }, inner -> inner.nop().bipush(9).ireturn())));
        List<String> ops = new ArrayList<>();
        for (CodeElement e : cf.parse(b4).methods().get(0).code().get().elementList())
            if (e instanceof Instruction i) ops.add(i.opcode().name());
        check(ops.equals(List.of("NOP", "BIPUSH", "IRETURN")), "transforming filters handler-emitted only: " + ops);
    }

    // ---- Part 5: 泛型参数面（E/B 自引用约束）+ defineClass 闭环 ----
    static int dynSeq;
    static void testTypeFaceAndDefineClass() throws Exception {
        ClassFile cf = ClassFile.of();
        // ClassFileBuilder<E,B> 的 B 自引用：with/transform 返回类型即具体 builder，可链式再调
        String name = "DynCFB" + (++dynSeq);
        byte[] b = cf.build(ClassDesc.of(name), cb -> ((ClassBuilder) cb)
                .withSuperclass(ConstantDescs.CD_Object)
                .withMethodBody("go", MethodTypeDesc.ofDescriptor("()Ljava/lang/String;"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        c -> c.ldc("cfb").areturn()));
        ClassModel rm = cf.parse(b);
        check(rm.thisClass().asInternalName().equals(name), "dyn thisClass");
        check(cf.verify(rm).isEmpty(), "dyn verify clean");
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getMethod("go").invoke(null).equals("cfb"), "DynCFB go()=='cfb'");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantPoolChannel();
        testWithAcceptTransform();
        testTypeFaceAndDefineClass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.lang.classfile.ClassFile conformance: white-box structure + black-box behavior.
 * @run main ClassFileTest
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

public class ClassFileTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassFile");
        check(c != null, "java.lang.classfile.ClassFile loads");
        check(c.getName().equals("java.lang.classfile.ClassFile"), "binary name java.lang.classfile.ClassFile");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassFile is public");
        check(c.isInterface(), "java.lang.classfile.ClassFile is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.ClassFile has no superclass");
        check(declares(c, "of"), "declares of");
        check(declares(c, "withOptions"), "declares withOptions");
        check(declares(c, "parse"), "declares parse");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ClassFile cf = ClassFile.of();
        check(ClassFile.MAGIC_NUMBER == 0xCAFEBABE, "MAGIC_NUMBER");
        check(ClassFile.ACC_PUBLIC == 0x0001 && ClassFile.ACC_PRIVATE == 0x0002 && ClassFile.ACC_STATIC == 0x0008 && ClassFile.ACC_FINAL == 0x0010 && ClassFile.ACC_INTERFACE == 0x0200, "ACC_* constants");
        check(ClassFile.JAVA_1_VERSION == 45, "JAVA_1_VERSION 45");
        check(ClassFile.JAVA_27_VERSION == 71, "JAVA_27_VERSION 71");
        check(ClassFile.PREVIEW_MINOR_VERSION == 0xFFFF, "PREVIEW_MINOR 0xFFFF");
        check(ClassFile.latestMajorVersion() == 71, "latestMajorVersion");
        check(ClassFile.latestMinorVersion() == 0, "latestMinorVersion");
        byte[] b = cf.build(ClassDesc.of("pkg.T"), cb -> cb.withSuperclass(ClassDesc.of("java.lang.Object")).withField("x", ConstantDescs.CD_int, ClassFile.ACC_PUBLIC).withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_int), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> cob.iconst_1().ireturn()));
        check(b.length > 8, "build produces bytes");
        int magic = ((b[0] & 0xff) << 24) | ((b[1] & 0xff) << 16) | ((b[2] & 0xff) << 8) | (b[3] & 0xff);
        check(magic == 0xCAFEBABE, "magic in bytes");
        ClassModel cm = cf.parse(b);
        check("pkg/T".equals(cm.thisClass().asInternalName()), "parse thisClass");
        check(cm.superclass().get().asInternalName().equals("java/lang/Object"), "parse superclass");
        check(cm.fields().size() == 1 && cm.methods().size() == 1, "field+method count");
        check(cf.verify(cm).isEmpty(), "verify clean");
        ClassFile cf2 = cf.withOptions(ClassFile.DebugElementsOption.DROP_DEBUG);
        check(cf2 != cf, "withOptions new instance");
        byte[] b2 = cf2.build(ClassDesc.of("pkg.U"), cb -> cb.withSuperclass(ClassDesc.of("java.lang.Object")));
        check(cf2.parse(b2).majorVersion() == 71, "cf2 still parses");

    }

    // ---- Part 3: parse 负例精确异常矩阵（实测锁定） ----
    static void testParseNegative() {
        ClassFile cf = ClassFile.of();
        try {
            cf.parse((byte[]) null);
            check(false, "parse(null) must throw");
        } catch (NullPointerException e) {
            check(e.getMessage() != null && e.getMessage().contains("classfileBytes"), "parse(null) NPE message mentions classfileBytes");
        }
        for (byte[][] bad : new byte[][][] {
                {new byte[0]},
                {new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}},
                {new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA}},                         // 魔数截断
                {new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBF, 0, 0, 0, 71}}, // 魔数错 1 位
        }) {
            try {
                cf.parse(bad[0]);
                check(false, "bad bytes must throw: " + Arrays.toString(bad[0]));
            } catch (IllegalArgumentException e) {
                check("Bad magic number".equals(e.getMessage()), "bad magic IAE message: " + e.getMessage());
            }
        }
        // 正确 magic 但 length=8 截断
        try {
            cf.parse(new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE, 0, 0, 0, 71});
            check(false, "truncated header must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null, "truncated IAE message non-null");
        }
        // build 栈形状负例：invokestatic 缺参数
        try {
            cf.build(ClassDesc.of("pkg.Bad"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC,
                    cob -> cob.invokestatic(ClassDesc.of("java.lang.Math"), "abs", MethodTypeDesc.ofDescriptor("(I)I"))));
            check(false, "stack underflow build must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("underflow"), "underflow IAE message: " + e.getMessage());
        }
    }

    // ---- Part 4: 版本数字精确表 ----
    static void testVersionTable() {
        int[] expected = {0, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71};
        try {
            java.lang.reflect.Field[] fs = ClassFile.class.getFields();
            List<Integer> got = new ArrayList<>();
            for (java.lang.reflect.Field f : fs)
                if (f.getName().startsWith("JAVA_") && f.getName().endsWith("_VERSION")) got.add(f.getInt(null));
            Collections.sort(got);
            List<Integer> exp = new ArrayList<>();
            for (int i = 1; i < expected.length; i++) exp.add(expected[i]);
            check(got.equals(exp), "JAVA_x_VERSION set " + got);
        } catch (Exception e) {
            check(false, "reflective version read: " + e);
        }
        check(ClassFileVersion.of(71, 0).majorVersion() == 71 && ClassFileVersion.of(71, 0).minorVersion() == 0, "ClassFileVersion.of 71");
        check(ClassFileVersion.of(45, 3).majorVersion() == 45 && ClassFileVersion.of(45, 3).minorVersion() == 3, "ClassFileVersion.of 45.3");
        // withVersion 元素注入 → 回读版本（ClassFileVersion 是 ClassElement）
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.Old"), cb -> cb
                .with(ClassFileVersion.of(52, 0)).withSuperclass(ConstantDescs.CD_Object));
        ClassModel m = ClassFile.of().parse(b);
        check(m.majorVersion() == 52 && m.minorVersion() == 0, "build with version 52 roundtrip");
        // 版本元素在 elementList 中可见
        boolean verElem = false;
        for (ClassElement e : m.elementList()) if (e instanceof ClassFileVersion cv && cv.majorVersion() == 52) verElem = true;
        check(verElem, "ClassFileVersion element present in elementList");
    }

    // ---- Part 5: defineClass 运行闭环（生成类真运行） ----
    static int dynSeq;
    static void testDefineClassLoop() throws Exception {
        ClassFile cf = ClassFile.of();
        String name = "DynCFT" + (++dynSeq);
        byte[] b = cf.build(ClassDesc.of(name), cb -> cb
                .withMethod("go", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        mb -> mb.withCode(c -> c.iconst_1().ireturn()))
                .withMethodBody("add", MethodTypeDesc.ofDescriptor("(II)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        c -> c.iload(0).iload(1).iadd().ireturn()));
        check(b.length > 8, "dyn bytes non-empty");
        ClassModel rm = cf.parse(b);
        check(rm.thisClass().asInternalName().equals(name), "dyn thisClass roundtrip");
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getName().equals(name), "defined class name");
        check(dc.getMethod("go").invoke(null).equals(1), "dyn go()==1 运行闭环");
        check(dc.getMethod("add", int.class, int.class).invoke(null, 20, 22).equals(42), "dyn add(20,22)==42");
        // verify 生成类干净
        check(cf.verify(rm).isEmpty(), "dyn verify clean");
        // 第二个动态类（唯一后缀防冲突）
        String name2 = "DynCFT" + (++dynSeq);
        byte[] b2 = cf.build(ClassDesc.of(name2), cb -> cb
                .withField("V", ConstantDescs.CD_int, ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC)
                .withMethodBody("get", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        c -> c.getstatic(ClassDesc.of(name2), "V", ConstantDescs.CD_int).ireturn()));
        ClassModel rm2 = cf.parse(b2);
        check(rm2.fields().size() == 1 && rm2.fields().get(0).fieldName().stringValue().equals("V"), "dyn2 field");
        Class<?> dc2 = MethodHandles.lookup().defineClass(b2);
        check(dc2.getMethod("get").invoke(null).equals(0), "dyn2 get()==0 默认字段值");
    }

    // ---- Part 6: 选项语义实测 ----
    static void testOptionSemantics() throws Exception {
        ClassFile cf = ClassFile.of();
        check(cf == ClassFile.of(), "ClassFile.of() singleton ==");
        byte[] self = ClassFileTest.class.getResourceAsStream("ClassFileTest.class").readAllBytes();
        ClassModel full = cf.parse(self);
        MethodModel withCode = null;
        for (MethodModel m : full.methods())
            if (m.methodName().stringValue().equals("testOptionSemantics") && m.code().isPresent()) withCode = m;
        check(withCode != null, "self method found");
        // 选项语义实测：发射 localVariable/lineNumber 伪指令后按选项解析，逐类型计数矩阵
        byte[] dbg = cf.build(ClassDesc.of("OptDbg"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, cob -> {
            Label s = cob.newLabel(), e = cob.newLabel();
            cob.labelBinding(s).return_();
            cob.labelBinding(e);
            cob.localVariable(1, "lv", ConstantDescs.CD_int, s, e);
            cob.lineNumber(9);
        }));
        ClassModel dbgFull = cf.parse(dbg);
        CodeModel dbgCode = dbgFull.methods().get(0).code().get();
        check(dbgCode.findAttribute(Attributes.localVariableTable()).isPresent(), "emitted LVT attr present");
        check(dbgCode.findAttribute(Attributes.lineNumberTable()).isPresent(), "emitted LNT attr present");
        int lvFull = 0, lnFull = 0;
        for (CodeElement el : dbgCode.elementList()) { if (el instanceof LocalVariable) lvFull++; if (el instanceof LineNumber) lnFull++; }
        check(lvFull == 1 && lnFull == 1, "emitted pseudos lv=" + lvFull + " ln=" + lnFull);
        CodeModel dbgDrop = cf.withOptions(ClassFile.DebugElementsOption.DROP_DEBUG).parse(dbg).methods().get(0).code().get();
        int lvDrop = 0, lnDrop = 0;
        for (CodeElement el : dbgDrop.elementList()) { if (el instanceof LocalVariable) lvDrop++; if (el instanceof LineNumber) lnDrop++; }
        check(lvDrop == 0, "DROP_DEBUG strips LocalVariable pseudo");
        check(lnDrop == 1, "DROP_DEBUG keeps LineNumber pseudo");
        CodeModel lnDropM = cf.withOptions(ClassFile.LineNumbersOption.DROP_LINE_NUMBERS).parse(dbg).methods().get(0).code().get();
        int lv2 = 0, ln2 = 0;
        for (CodeElement el : lnDropM.elementList()) { if (el instanceof LocalVariable) lv2++; if (el instanceof LineNumber) ln2++; }
        check(ln2 == 0, "DROP_LINE_NUMBERS strips LineNumber pseudo");
        check(lv2 == 1, "DROP_LINE_NUMBERS keeps LocalVariable pseudo");
        check(dbgDrop.findAttribute(Attributes.localVariableTable()).isPresent(), "options keep LVT attribute");
        // DROP_STACK_MAPS：build 侧不生成（fresh build 无 SMT）；parse 侧模型保留属性；transform 拷贝保留
        byte[] plain = cf.build(ClassDesc.of("OptT"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC,
                cob -> { Label l = cob.newLabel(); cob.iconst_0().iload(0).if_icmpge(l); cob.iconst_1().ireturn(); cob.labelBinding(l).iconst_m1().ireturn(); }));
        check(cf.parse(plain).methods().get(0).code().get().findAttribute(Attributes.stackMapTable()).isPresent(), "branch build emits StackMapTable");
        ClassFile dropSm = cf.withOptions(ClassFile.StackMapsOption.DROP_STACK_MAPS);
        byte[] noSmB = dropSm.build(ClassDesc.of("OptT2"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_STATIC,
                cob -> { Label l = cob.newLabel(); cob.iconst_0().iload(0).if_icmpge(l); cob.iconst_1().ireturn(); cob.labelBinding(l).iconst_m1().ireturn(); }));
        check(!cf.parse(noSmB).methods().get(0).code().get().findAttribute(Attributes.stackMapTable()).isPresent(), "DROP_STACK_MAPS build omits attribute");
        check(dropSm.parse(plain).methods().get(0).code().get().findAttribute(Attributes.stackMapTable()).isPresent(), "DROP_STACK_MAPS parse keeps model attribute");
        byte[] tr = dropSm.transformClass(cf.parse(plain), ClassTransform.ACCEPT_ALL);
        check(cf.parse(tr).methods().get(0).code().get().findAttribute(Attributes.stackMapTable()).isPresent(), "transform ACCEPT_ALL copies bound SMT");
        var verrs = cf.verify(noSmB);
        check(!verrs.isEmpty(), "verify flags missing stack maps at v71");
        check(verrs.get(0) instanceof VerifyError, "verify error type");
        check(verrs.get(0).getMessage() != null, "verify error message non-null");
        // LineNumbersOption / DeadLabelsOption / AttributesProcessingOption 枚举完备
        check(ClassFile.LineNumbersOption.values().length == 2, "LineNumbersOption 2 values");
        check(ClassFile.DeadLabelsOption.values().length == 2, "DeadLabelsOption 2 values");
        check(ClassFile.AttributesProcessingOption.values().length == 3, "AttributesProcessingOption 3 values");
        check(ClassFile.StackMapsOption.values().length == 3, "StackMapsOption 3 values");
        check(ClassFile.DebugElementsOption.values().length == 2, "DebugElementsOption 2 values");
        // withOptions 幂等链
        ClassFile chained = cf.withOptions(ClassFile.DebugElementsOption.DROP_DEBUG).withOptions(ClassFile.StackMapsOption.GENERATE_STACK_MAPS);
        check(chained != cf, "chained withOptions new instance");
    }

    static long count(MethodModel m, Class<? extends CodeElement> kind) {
        return m.code().get().elementList().stream().filter(kind::isInstance).count();
    }

    // ---- Part 7: transform 三段闭环 ----
    static void testTransformPipeline() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("TrT"), cb -> cb
                .withField("f1", ConstantDescs.CD_int, ClassFile.ACC_PRIVATE)
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_STATIC, c -> c.bipush(7).ireturn()));
        ClassModel model = cf.parse(b);
        // ACCEPT_ALL 语义等价回读
        byte[] t1 = cf.transformClass(model, ClassTransform.ACCEPT_ALL);
        ClassModel m1 = cf.parse(t1);
        check(m1.elementList().size() == model.elementList().size(), "ACCEPT_ALL element count preserved");
        check(m1.fields().size() == 1 && m1.methods().size() == 1, "ACCEPT_ALL members preserved");
        // dropping(e->true)：全剥
        byte[] t2 = cf.transformClass(model, ClassTransform.dropping(e -> true));
        ClassModel m2 = cf.parse(t2);
        check(m2.fields().isEmpty() && m2.methods().isEmpty(), "drop-all empties members");
        check(m2.superclass().isPresent(), "drop-all keeps superclass");
        // endHandler 注入字段副作用
        byte[] t3 = cf.transformClass(model, ClassTransform.endHandler(cb -> cb.withField("injected", ConstantDescs.CD_int, ClassFile.ACC_STATIC)));
        ClassModel m3 = cf.parse(t3);
        check(m3.fields().size() == 2, "endHandler injected field: " + m3.fields().size());
        boolean foundInjected = false;
        for (FieldModel f : m3.fields()) foundInjected |= f.fieldName().stringValue().equals("injected");
        check(foundInjected, "injected field name");
        // andThen 组合
        ClassTransform combo = ClassTransform.dropping(e -> e instanceof MethodModel).andThen(ClassTransform.endHandler(cb -> cb.withField("x2", ConstantDescs.CD_int, 0)));
        ClassModel m4 = cf.parse(cf.transformClass(model, combo));
        check(m4.methods().isEmpty() && m4.fields().size() == 2, "andThen composed semantics");
        // transformingMethodBodies 自定义 CodeTransform 剥 NOP
        byte[] nb = cf.build(ClassDesc.of("NopT"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC,
                c -> { c.nop(); c.nop(); c.return_(); }));
        ClassModel nm = cf.parse(nb);
        CodeTransform dropNop = new CodeTransform() {
            public void accept(CodeBuilder cb, CodeElement e) {
                if (!(e instanceof Instruction i) || i.opcode() != Opcode.NOP) cb.with(e);
            }
        };
        ClassModel after = cf.parse(cf.transformClass(nm, ClassTransform.transformingMethodBodies(dropNop)));
        List<Opcode> ops = new ArrayList<>();
        for (CodeElement e : after.methods().get(0).code().get().elementList())
            if (e instanceof Instruction i) ops.add(i.opcode());
        check(ops.equals(List.of(Opcode.RETURN)), "dropped NOPs: " + ops);
    }

    // ---- Part 8: parse(Path)/buildTo/buildModule/verify 全家族 ----
    static void testPathModuleVerify() throws Exception {
        ClassFile cf = ClassFile.of();
        Path dir = Path.of(System.getProperty("java.io.tmpdir"), "cft-" + System.nanoTime());
        Files.createDirectories(dir);
        try {
            Path out = dir.resolve("PathT.class");
            cf.buildTo(out, ClassDesc.of("PathT"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, c -> c.return_()));
            check(Files.size(out) > 8, "buildTo wrote file");
            ClassModel pm = cf.parse(out);
            check(pm.thisClass().asInternalName().equals("PathT"), "parse(Path) thisClass");
            check(cf.verify(out).isEmpty(), "verify(Path) clean");
            // buildModule 闭环
            ModuleAttribute ma = ModuleAttribute.of(ModuleDesc.of("m1"), mb -> mb.moduleFlags(ClassFile.ACC_OPEN).moduleVersion("1.0"));
            byte[] mb1 = cf.buildModule(ma);
            ClassModel mm = cf.parse(mb1);
            check(mm.isModuleInfo(), "buildModule isModuleInfo");
            check((mm.flags().flagsMask() & ClassFile.ACC_MODULE) != 0, "module ACC_MODULE flag");
            check(mm.findAttribute(Attributes.module()).get().moduleName().asSymbol().name().equals("m1"), "module name roundtrip");
            check(mm.findAttribute(Attributes.module()).get().moduleVersion().get().stringValue().equals("1.0"), "module version roundtrip");
            check(cf.verify(mm).isEmpty(), "module verify clean");
        } finally {
            try (var s = Files.list(dir)) { for (Path p : s.toList()) Files.deleteIfExists(p); }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testParseNegative();
        testVersionTable();
        testDefineClassLoop();
        testOptionSemantics();
        testTransformPipeline();
        testPathModuleVerify();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

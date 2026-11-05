/*
 * @test
 * @summary java.lang.classfile.CodeBuilder conformance: white-box structure + black-box behavior.
 * @run main CodeBuilderTest
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

public class CodeBuilderTest {
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
        Class<?> c = Class.forName("java.lang.classfile.CodeBuilder");
        check(c != null, "java.lang.classfile.CodeBuilder loads");
        check(c.getName().equals("java.lang.classfile.CodeBuilder"), "binary name java.lang.classfile.CodeBuilder");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.CodeBuilder is public");
        check(c.isInterface(), "java.lang.classfile.CodeBuilder is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.CodeBuilder has no superclass");
        check(declares(c, "newLabel"), "declares newLabel");
        check(declares(c, "startLabel"), "declares startLabel");
        check(declares(c, "endLabel"), "declares endLabel");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_int), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> {
            cob.iconst_0().istore(1).iload(1).iconst_1().iadd().istore(1).iload(1).ireturn();
        }));
        ClassModel cm = ClassFile.of().parse(b);
        CodeModel code = cm.methods().get(0).code().get();
        int n = 0;
        for (CodeElement ce : code) { if (ce instanceof Instruction i && i.opcode() == Opcode.ISTORE_1) n++; }
        check(n == 2, "istore emitted");
        check(code.elementList().stream().anyMatch(e -> e instanceof Instruction i && i.opcode() == Opcode.IRETURN), "ireturn");

    }

    static List<String> ops(CodeModel code) {
        List<String> r = new ArrayList<>();
        for (CodeElement e : code.elementList()) if (e instanceof Instruction i) r.add(i.opcode().name());
        return r;
    }
    static CodeModel codeOf(ClassModel m, String name) {
        return m.methods().stream().filter(x -> x.methodName().stringValue().equals(name)).findFirst().orElseThrow().code().orElseThrow();
    }

    // ---- Part 3: 常数族发射-回读矩阵（opcode/操作数/sizeInBytes 精确表，实测锁定） ----
    static void testConstantFamily() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("Cbt1.T"), cb -> cb
                .withMethodBody("consts", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_STATIC, c -> c
                        .iconst_m1().pop().iconst_0().pop().iconst_1().pop().iconst_2().pop()
                        .iconst_3().pop().iconst_4().pop().iconst_5().pop()
                        .bipush(7).pop().sipush(1000).pop()
                        .aconst_null().pop().iconst_1().ireturn())
                .withMethodBody("lds", MethodTypeDesc.ofDescriptor("()Ljava/lang/String;"), ClassFile.ACC_STATIC, c -> c.ldc("hello").areturn())
                .withMethodBody("ldl", MethodTypeDesc.ofDescriptor("()J"), ClassFile.ACC_STATIC, c -> c.ldc(100000L).lreturn())
                .withMethodBody("ldi", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_STATIC, c -> c.ldc(200000).ireturn()));
        ClassModel cm = cf.parse(b);
        List<String> ops = ops(codeOf(cm, "consts"));
        check(ops.equals(List.of("ICONST_M1", "POP", "ICONST_0", "POP", "ICONST_1", "POP", "ICONST_2", "POP",
                "ICONST_3", "POP", "ICONST_4", "POP", "ICONST_5", "POP",
                "BIPUSH", "POP", "SIPUSH", "POP", "ACONST_NULL", "POP", "ICONST_1", "IRETURN")), "const family opcodes: " + ops);
        for (CodeElement e : codeOf(cm, "consts").elementList()) {
            if (e instanceof Instruction i) {
                if (i.opcode() == Opcode.ICONST_2) check(i.sizeInBytes() == 1, "iconst_2 size 1");
                if (i.opcode() == Opcode.BIPUSH) { check(i.sizeInBytes() == 2, "bipush size 2"); check(((ConstantInstruction) i).constantValue().equals(7), "bipush operand 7"); }
                if (i.opcode() == Opcode.SIPUSH) { check(i.sizeInBytes() == 3, "sipush size 3"); check(((ConstantInstruction) i).constantValue().equals(1000), "sipush operand 1000"); }
            }
        }
        // ldc String → LDC size 2 + constantValue/constantEntry/typeKind
        for (CodeElement e : codeOf(cm, "lds").elementList())
            if (e instanceof ConstantInstruction.LoadConstantInstruction li) {
                check(li.opcode() == Opcode.LDC, "ldc(String) → LDC");
                check(li.sizeInBytes() == 2, "LDC size 2");
                check(li.constantValue().equals("hello"), "LDC value hello");
                check(li.constantEntry() instanceof StringEntry, "LDC entry StringEntry");
                check(li.typeKind() == TypeKind.REFERENCE, "String typeKind REFERENCE");
            }
        // ldc long → LDC2_W size 3
        for (CodeElement e : codeOf(cm, "ldl").elementList())
            if (e instanceof ConstantInstruction.LoadConstantInstruction li) {
                check(li.opcode() == Opcode.LDC2_W, "ldc(long) → LDC2_W");
                check(li.sizeInBytes() == 3, "LDC2_W size 3");
                check(((Long) li.constantValue()) == 100000L, "LDC2_W value");
                check(li.typeKind() == TypeKind.LONG, "long typeKind LONG");
            }
        // ldc int 装箱 → LDC size 2
        for (CodeElement e : codeOf(cm, "ldi").elementList())
            if (e instanceof ConstantInstruction.LoadConstantInstruction li) {
                check(li.opcode() == Opcode.LDC && li.sizeInBytes() == 2, "ldc(int) → LDC size 2");
                check(li.constantEntry() instanceof IntegerEntry && li.typeKind() == TypeKind.INT, "int entry/typeKind");
            }
        check(cf.verify(cm).isEmpty(), "const family verify clean");
        // loadConstant 族默认方法（int/long/ConstantDesc 分派）
        byte[] b2 = cf.build(ClassDesc.of("Cbt2.U"), cb -> cb.withMethodBody("lc", MethodTypeDesc.ofDescriptor("()J"), ClassFile.ACC_STATIC,
                c -> c.loadConstant(3).i2l().loadConstant(java.lang.constant.ConstantDescs.CD_long == null ? 1L : 5L).ladd().lreturn()));
        boolean sawLoad = false;
        for (CodeElement e : codeOf(cf.parse(b2), "lc").elementList())
            if (e instanceof ConstantInstruction.LoadConstantInstruction li && ((Long) li.constantValue()) == 5L) sawLoad = true;
        check(sawLoad, "loadConstant(long) emitted");
        check(cf.verify(cf.parse(b2)).isEmpty(), "loadConstant verify clean");
    }

    // ---- Part 4: 算术/转换族 defineClass 运行闭环 ----
    static void testArithmeticConversion() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("ArtT"), cb -> cb
                .withMethodBody("imul", MethodTypeDesc.ofDescriptor("(II)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c.iload(0).iload(1).imul().ireturn())
                .withMethodBody("idiv", MethodTypeDesc.ofDescriptor("(II)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c.iload(0).iload(1).idiv().ireturn())
                .withMethodBody("lsub", MethodTypeDesc.ofDescriptor("(JJ)J"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c.lload(0).lload(2).lsub().lreturn())
                .withMethodBody("fdiv", MethodTypeDesc.ofDescriptor("(F)F"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c.fconst_2().fload(0).fdiv().freturn())
                .withMethodBody("i2l", MethodTypeDesc.ofDescriptor("(I)J"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c.iload(0).i2l().lreturn())
                .withMethodBody("l2i", MethodTypeDesc.ofDescriptor("(J)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c.lload(0).l2i().ireturn())
                .withMethodBody("i2b", MethodTypeDesc.ofDescriptor("(I)B"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c.iload(0).i2b().ireturn())
                .withMethodBody("d2i", MethodTypeDesc.ofDescriptor("(D)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c.dload(0).d2i().ireturn()));
        ClassModel cm = cf.parse(b);
        check(cf.verify(cm).isEmpty(), "arith/conv verify clean");
        // opcode 回读
        check(ops(codeOf(cm, "i2l")).equals(List.of("ILOAD_0", "I2L", "LRETURN")), "i2l ops: " + ops(codeOf(cm, "i2l")));
        check(ops(codeOf(cm, "l2i")).equals(List.of("LLOAD_0", "L2I", "IRETURN")), "l2i ops: " + ops(codeOf(cm, "l2i")));
        for (CodeElement e : codeOf(cm, "lsub").elementList())
            if (e instanceof Instruction i && i.opcode() == Opcode.LSUB) check(i.sizeInBytes() == 1, "lsub size 1");
        // kind 分类
        for (CodeElement e : codeOf(cm, "i2l").elementList())
            if (e instanceof Instruction i && i.opcode() == Opcode.I2L) check(i.opcode().kind() == Opcode.Kind.CONVERT, "I2L kind CONVERT");
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getMethod("imul", int.class, int.class).invoke(null, 6, 7).equals(42), "imul(6,7)==42");
        check(dc.getMethod("idiv", int.class, int.class).invoke(null, 84, 2).equals(42), "idiv(84,2)==42");
        check(dc.getMethod("lsub", long.class, long.class).invoke(null, 10L, 4L).equals(6L), "lsub(10,4)==6");
        check(dc.getMethod("fdiv", float.class).invoke(null, 8f).equals(0.25f), "fdiv(2/8)==0.25");
        check(dc.getMethod("i2l", int.class).invoke(null, 3).equals(3L), "i2l(3)==3L");
        check(dc.getMethod("l2i", long.class).invoke(null, 0x100000005L).equals(5), "l2i truncation==5");
        check(dc.getMethod("i2b", int.class).invoke(null, 300).equals((byte) 300), "i2b(300)==44");
        check(dc.getMethod("d2i", double.class).invoke(null, 2.7d).equals(2), "d2i(2.7)==2");
    }

    // ---- Part 5: 比较跳转 + goto_ + LabelTarget 回访 ----
    static void testBranchFamily() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("BrtT"), cb -> cb.withMethodBody("cmp", MethodTypeDesc.ofDescriptor("(II)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> {
            Label ge = c.newLabel();
            c.iload(0).iload(1).if_icmpge(ge);      // a>=b → 跳
            c.iconst_m1().ireturn();
            c.labelBinding(ge).iconst_1().ireturn();
        }));
        ClassModel cm = cf.parse(b);
        CodeModel code = codeOf(cm, "cmp");
        check(cf.verify(cm).isEmpty(), "branch verify clean");
        BranchInstruction bi = null;
        int labelTargets = 0;
        for (CodeElement e : code.elementList()) {
            if (e instanceof BranchInstruction x) bi = x;
            if (e instanceof LabelTarget) labelTargets++;
        }
        check(bi != null && bi.opcode() == Opcode.IF_ICMPGE, "if_icmpge emitted");
        check(bi.sizeInBytes() == 3, "if_icmpge size 3");
        check(bi.target() != null, "branch target label");
        check(labelTargets == 1, "LabelTarget pseudo present: " + labelTargets);
        check(bi.opcode().kind() == Opcode.Kind.BRANCH, "IF_ICMPGE kind BRANCH");
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getMethod("cmp", int.class, int.class).invoke(null, 5, 3).equals(1), "cmp(5,3)==1 (branch taken)");
        check(dc.getMethod("cmp", int.class, int.class).invoke(null, 2, 9).equals(-1), "cmp(2,9)==-1 (fallthrough)");
        // ifeq + goto_ 循环
        byte[] b2 = cf.build(ClassDesc.of("BrtU"), cb -> cb.withMethodBody("until", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> {
            Label loop = c.newLabel(), done = c.newLabel();
            c.labelBinding(loop);
            c.iload(0).ifeq(done);
            c.iload(0).iconst_1().isub().istore(0);
            c.goto_(loop);
            c.labelBinding(done).iconst_0().ireturn();
        }));
        ClassModel cm2 = cf.parse(b2);
        check(cf.verify(cm2).isEmpty(), "loop verify clean");
        int gotos = 0, ifeqs = 0;
        for (CodeElement e : codeOf(cm2, "until").elementList()) {
            if (e instanceof Instruction i && i.opcode() == Opcode.GOTO) { gotos++; check(i.sizeInBytes() == 3, "goto size 3"); }
            if (e instanceof Instruction i && i.opcode() == Opcode.IFEQ) ifeqs++;
        }
        check(gotos == 1 && ifeqs == 1, "goto_/ifeq each once");
        Class<?> dc2 = MethodHandles.lookup().defineClass(b2);
        check(dc2.getMethod("until", int.class).invoke(null, 5).equals(0), "until(5) loop terminates");
    }

    // ---- Part 6: 数组族（newarray/anewarray/multianewarray/iaload/iastore/aaload/aastore） ----
    static void testArrayFamily() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("AryT"), cb -> cb
                .withMethodBody("sum", MethodTypeDesc.ofDescriptor("([I)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> {
                    c.iconst_0().istore(1).iconst_0().istore(2);
                    Label loop = c.newLabel(), end = c.newLabel();
                    c.labelBinding(loop);
                    c.iload(2).aload(0).arraylength().if_icmpge(end);
                    c.iload(1).aload(0).iload(2).iaload().iadd().istore(1);
                    c.iinc(2, 1).goto_(loop);
                    c.labelBinding(end).iload(1).ireturn();
                })
                .withMethodBody("mkstr", MethodTypeDesc.ofDescriptor("(I)[Ljava/lang/String;"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c
                        .iload(0).anewarray(ClassDesc.of("java.lang.String")).astore(1)
                        .aload(1).iconst_0().ldc("z").aastore()
                        .aload(1).areturn())
                .withMethodBody("mm", MethodTypeDesc.ofDescriptor("()[[I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c
                        .iconst_2().iconst_3().multianewarray(ClassDesc.ofDescriptor("[[I"), 2).astore(0)
                        .aload(0).iconst_1().aaload().iconst_2().bipush(42).iastore()
                        .aload(0).areturn())
                .withMethodBody("na", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c
                        .iconst_3().newarray(TypeKind.BOOLEAN).astore(0)
                        .iconst_2().newarray(TypeKind.INT).astore(1)
                        .iconst_1().newarray(TypeKind.LONG).astore(2)
                        .aload(1).iconst_0().bipush(77).iastore()
                        .aload(1).iconst_0().iaload().ireturn()));
        ClassModel cm = cf.parse(b);
        check(cf.verify(cm).isEmpty(), "array family verify clean");
        // newarray 回读：typeKind + newarrayCode 精确表
        List<TypeKind> nks = new ArrayList<>();
        for (CodeElement e : codeOf(cm, "na").elementList()) if (e instanceof NewPrimitiveArrayInstruction np) nks.add(np.typeKind());
        check(nks.equals(List.of(TypeKind.BOOLEAN, TypeKind.INT, TypeKind.LONG)), "newarray typeKinds: " + nks);
        check(TypeKind.BOOLEAN.newarrayCode() == 4 && TypeKind.INT.newarrayCode() == 10 && TypeKind.LONG.newarrayCode() == 11, "newarrayCode 4/10/11");
        // anewarray/multianewarray 回读
        for (CodeElement e : codeOf(cm, "mkstr").elementList())
            if (e instanceof NewReferenceArrayInstruction nr) check(nr.componentType().asInternalName().equals("java/lang/String"), "anewarray component");
        for (CodeElement e : codeOf(cm, "mm").elementList())
            if (e instanceof NewMultiArrayInstruction na) {
                check(na.arrayType().asInternalName().equals("[[I"), "multianewarray [[I");
                check(na.dimensions() == 2, "dimensions 2");
                check(na.opcode().kind() == Opcode.Kind.NEW_MULTI_ARRAY, "NEW_MULTI_ARRAY kind");
            }
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getMethod("sum", int[].class).invoke(null, (Object) new int[]{5, 10, 15}).equals(30), "sum([5,10,15])==30");
        String[] sa = (String[]) dc.getMethod("mkstr", int.class).invoke(null, 1);
        check(sa.length == 1 && "z".equals(sa[0]), "anewarray+aastore roundtrip");
        int[][] ma = (int[][]) dc.getMethod("mm").invoke(null);
        check(ma.length == 2 && ma[1].length == 3 && ma[1][2] == 42, "multianewarray dims + aaload/iastore");
        check(dc.getMethod("na").invoke(null).equals(77), "newarray+iastore/iaload==77");
    }

    // ---- Part 7: 字段访问族（getstatic/putstatic/getfield/putfield 三元组回读+运行） ----
    static void testFieldAccessFamily() throws Exception {
        ClassFile cf = ClassFile.of();
        String kn = "DynFA1";
        byte[] b = cf.build(ClassDesc.of(kn), cb -> cb
                .withField("C", ConstantDescs.CD_int, ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC)
                .withMethodBody("set", MethodTypeDesc.ofDescriptor("(I)V"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        c -> c.iload(0).putstatic(ClassDesc.of(kn), "C", ConstantDescs.CD_int).return_())
                .withMethodBody("get", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        c -> c.getstatic(ClassDesc.of(kn), "C", ConstantDescs.CD_int).ireturn())
                .withMethodBody("getX", MethodTypeDesc.ofDescriptor("(Ljava/awt/Point;)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        c -> c.aload(0).getfield(ClassDesc.of("java.awt.Point"), "x", ConstantDescs.CD_int).ireturn())
                .withMethodBody("setX", MethodTypeDesc.ofDescriptor("(Ljava/awt/Point;I)V"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        c -> c.aload(0).iload(1).putfield(ClassDesc.of("java.awt.Point"), "x", ConstantDescs.CD_int).return_()));
        ClassModel cm = cf.parse(b);
        check(cf.verify(cm).isEmpty(), "field family verify clean");
        // 三元组回读：owner/name/type descriptor 精确表
        List<String> triples = new ArrayList<>();
        for (CodeElement e : codeOf(cm, "set").elementList())
            if (e instanceof FieldInstruction fi) triples.add(fi.opcode() + ":" + fi.owner().asInternalName() + "." + fi.name().stringValue() + ":" + fi.typeSymbol().descriptorString());
        check(triples.equals(List.of("PUTSTATIC:DynFA1.C:I")), "putstatic triple: " + triples);
        triples.clear();
        for (CodeElement e : codeOf(cm, "getX").elementList())
            if (e instanceof FieldInstruction fi) triples.add(fi.opcode() + ":" + fi.owner().asInternalName() + "." + fi.name().stringValue() + ":" + fi.type().stringValue());
        check(triples.equals(List.of("GETFIELD:java/awt/Point.x:I")), "getfield triple: " + triples);
        for (CodeElement e : codeOf(cm, "get").elementList())
            if (e instanceof FieldInstruction fi) check(fi.field() instanceof FieldRefEntry, "field() FieldRefEntry");
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        dc.getMethod("set", int.class).invoke(null, 3);
        check(dc.getMethod("get").invoke(null).equals(3), "putstatic/getstatic==3");
        java.awt.Point pt = new java.awt.Point(3, 4);
        check(dc.getMethod("getX", java.awt.Point.class).invoke(null, pt).equals(3), "getfield Point.x==3");
        dc.getMethod("setX", java.awt.Point.class, int.class).invoke(null, pt, 9);
        check(pt.x == 9, "putfield Point.x=9 visible");
    }

    // ---- Part 8: 调用族（invokevirtual/static/interface/special 回读+运行） ----
    static void testInvokeFamily() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("InvT"), cb -> cb
                .withMethodBody("cat", MethodTypeDesc.ofDescriptor("(I)Ljava/lang/String;"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c
                        .ldc("v").iload(0).invokestatic(ClassDesc.of("java.lang.String"), "valueOf", MethodTypeDesc.ofDescriptor("(I)Ljava/lang/String;"))
                        .invokevirtual(ClassDesc.of("java.lang.String"), "concat", MethodTypeDesc.ofDescriptor("(Ljava/lang/String;)Ljava/lang/String;")).areturn())
                .withMethodBody("size", MethodTypeDesc.ofDescriptor("(Ljava/util/List;)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c
                        .aload(0).invokeinterface(ClassDesc.of("java.util.List"), "size", MethodTypeDesc.ofDescriptor("()I")).ireturn())
                .withMethodBody("abs", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c
                        .iload(0).invokestatic(ClassDesc.of("java.lang.Math"), "abs", MethodTypeDesc.ofDescriptor("(I)I")).ireturn())
                .withMethodBody("newObj", MethodTypeDesc.ofDescriptor("()Ljava/lang/Object;"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c
                        .new_(ClassDesc.of("java.lang.Object")).dup().invokespecial(ClassDesc.of("java.lang.Object"), "<init>", MethodTypeDesc.ofDescriptor("()V")).areturn()));
        ClassModel cm = cf.parse(b);
        check(cf.verify(cm).isEmpty(), "invoke family verify clean");
        // invokeinterface 回读：isInterface/count/owner
        for (CodeElement e : codeOf(cm, "size").elementList())
            if (e instanceof InvokeInstruction ii) {
                check(ii.opcode() == Opcode.INVOKEINTERFACE, "invokeinterface opcode");
                check(ii.isInterface(), "isInterface true");
                check(ii.count() == 1, "count 1 (receiver only)");
                check(ii.owner().asInternalName().equals("java/util/List"), "owner List");
                check(ii.method() instanceof InterfaceMethodRefEntry, "method() InterfaceMethodRefEntry");
                check(ii.typeSymbol().parameterCount() == 0, "typeSymbol parameterCount");
            }
        // invokevirtual 回读：isInterface false
        for (CodeElement e : codeOf(cm, "cat").elementList())
            if (e instanceof InvokeInstruction ii && ii.opcode() == Opcode.INVOKEVIRTUAL) {
                check(!ii.isInterface(), "invokevirtual isInterface false");
                check(ii.name().stringValue().equals("concat"), "name concat");
                check(ii.typeSymbol().descriptorString().equals("(Ljava/lang/String;)Ljava/lang/String;"), "descriptor");
            }
        // invokespecial（构造器）名字实测为 invokespecial
        boolean sawSpecial = false;
        for (CodeElement e : codeOf(cm, "newObj").elementList())
            if (e instanceof InvokeInstruction ii && ii.opcode() == Opcode.INVOKESPECIAL) {
                sawSpecial = true;
                check(ii.name().stringValue().equals("<init>"), "invokespecial <init>");
                check(ii.method() instanceof MethodRefEntry, "method() MethodRefEntry");
            }
        check(sawSpecial, "invokespecial emitted");
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getMethod("cat", int.class).invoke(null, 9).equals("v9"), "concat(v,9)=='v9'");
        check(dc.getMethod("size", java.util.List.class).invoke(null, List.of(1, 2, 3)).equals(3), "List.size()==3");
        check(dc.getMethod("abs", int.class).invoke(null, -8).equals(8), "Math.abs(-8)==8");
        check(dc.getMethod("newObj").invoke(null) instanceof Object, "new+invokespecial <init> runs");
    }

    // ---- Part 9: 返回族 + 栈操作族 + monitor/throw/typecheck ----
    static void testReturnStackMonitorFamily() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("RetT"), cb -> cb
                .withMethodBody("lr", MethodTypeDesc.ofDescriptor("()J"), ClassFile.ACC_STATIC, c -> c.lconst_1().lreturn())
                .withMethodBody("dr", MethodTypeDesc.ofDescriptor("()D"), ClassFile.ACC_STATIC, c -> c.dconst_1().dreturn())
                .withMethodBody("fr", MethodTypeDesc.ofDescriptor("()F"), ClassFile.ACC_STATIC, c -> c.fconst_2().freturn())
                .withMethodBody("ar", MethodTypeDesc.ofDescriptor("()Ljava/lang/Object;"), ClassFile.ACC_STATIC, c -> c.aconst_null().areturn())
                .withMethodBody("vr", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC, c -> c.return_())
                .withMethodBody("swp", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c.iconst_1().iconst_2().swap().isub().ireturn())
                .withMethodBody("dpp", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c.iconst_3().dup().pop().ireturn())
                .withMethodBody("mon", MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;)V"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        c -> c.aload(0).dup().monitorenter().monitorexit().return_())
                .withMethodBody("ck", MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;)Z"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        c -> c.new_(ClassDesc.of("java.lang.Object")).dup().invokespecial(ClassDesc.of("java.lang.Object"), "<init>", MethodTypeDesc.ofDescriptor("()V"))
                                .checkcast(ClassDesc.of("java.lang.Object")).instanceOf(ClassDesc.of("java.lang.String")).ireturn()));
        ClassModel cm = cf.parse(b);
        check(cf.verify(cm).isEmpty(), "return/stack/monitor verify clean");
        for (String m : List.of("lr", "dr", "fr", "ar", "vr")) {
            boolean ok = false;
            for (CodeElement e : codeOf(cm, m).elementList())
                if (e instanceof Instruction i && i.opcode().kind() == Opcode.Kind.RETURN) { ok = i.sizeInBytes() == 1; }
            check(ok, m + " RETURN kind size 1");
        }
        check(ops(codeOf(cm, "swp")).equals(List.of("ICONST_1", "ICONST_2", "SWAP", "ISUB", "IRETURN")), "swap ops: " + ops(codeOf(cm, "swp")));
        check(ops(codeOf(cm, "dpp")).equals(List.of("ICONST_3", "DUP", "POP", "IRETURN")), "dup/pop ops");
        check(ops(codeOf(cm, "mon")).equals(List.of("ALOAD_0", "DUP", "MONITORENTER", "MONITOREXIT", "RETURN")), "monitor ops");
        check(ops(codeOf(cm, "ck")).equals(List.of("NEW", "DUP", "INVOKESPECIAL", "CHECKCAST", "INSTANCEOF", "IRETURN")), "typecheck ops");
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getMethod("swp").invoke(null).equals(1), "swap: 2-1==1");
        check(dc.getMethod("dpp").invoke(null).equals(3), "dup+pop==3");
        dc.getMethod("mon", Object.class).invoke(null, new Object());
        check(dc.getMethod("ck", Object.class).invoke(null, new Object()).equals(false), "instanceOf Object→String false");
        // monitorenter 缺 receiver → 栈下溢 IAE（实测）
        try {
            cf.build(ClassDesc.of("Ret2.U"), cb -> cb.withMethodBody("bad", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC, c -> c.monitorenter()));
            check(false, "monitorenter underflow must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("underflow"), "underflow IAE: " + e.getMessage());
        }
        // athrow 真抛出闭环
        byte[] b2 = cf.build(ClassDesc.of("RetV"), cb -> cb.withMethodBody("thr", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                c -> c.new_(ClassDesc.of("java.lang.IllegalStateException")).dup().invokespecial(ClassDesc.of("java.lang.IllegalStateException"), "<init>", MethodTypeDesc.ofDescriptor("()V")).athrow()));
        Class<?> dc2 = MethodHandles.lookup().defineClass(b2);
        try {
            dc2.getMethod("thr").invoke(null);
            check(false, "athrow must propagate");
        } catch (java.lang.reflect.InvocationTargetException e) {
            check(e.getCause() instanceof IllegalStateException, "athrow propagated ISE");
        }
    }

    // ---- Part 10: switch 族（tableswitch/lookupswitch 回读） ----
    static void testSwitchFamily() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("SwT"), cb -> cb
                .withMethodBody("tbl", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> {
                    Label def = c.newLabel(), c1 = c.newLabel(), c2 = c.newLabel();
                    c.iload(0).tableswitch(0, 1, def, List.of(SwitchCase.of(0, c1), SwitchCase.of(1, c2)));
                    c.labelBinding(c1).bipush(10).ireturn();
                    c.labelBinding(c2).bipush(20).ireturn();
                    c.labelBinding(def).iconst_m1().ireturn();
                })
                .withMethodBody("lku", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> {
                    Label def = c.newLabel(), c1 = c.newLabel(), c5 = c.newLabel();
                    c.iload(0).lookupswitch(def, List.of(SwitchCase.of(1, c1), SwitchCase.of(5, c5)));
                    c.labelBinding(c1).iconst_1().ireturn();
                    c.labelBinding(c5).iconst_5().ireturn();
                    c.labelBinding(def).iconst_0().ireturn();
                }));
        ClassModel cm = cf.parse(b);
        check(cf.verify(cm).isEmpty(), "switch verify clean");
        for (CodeElement e : codeOf(cm, "tbl").elementList())
            if (e instanceof TableSwitchInstruction t) {
                check(t.lowValue() == 0 && t.highValue() == 1, "tableswitch low/high");
                check(t.cases().size() == 2, "tableswitch 2 cases");
                check(t.cases().get(0).caseValue() == 0 && t.cases().get(1).caseValue() == 1, "case values order");
                check(t.defaultTarget() != null, "default target non-null");
                check(t.sizeInBytes() == 23, "tableswitch sizeInBytes 23 (bci1 对齐) 实测: " + t.sizeInBytes());
                check(t.opcode().kind() == Opcode.Kind.TABLE_SWITCH, "TABLE_SWITCH kind");
            }
        for (CodeElement e : codeOf(cm, "lku").elementList())
            if (e instanceof LookupSwitchInstruction l) {
                check(l.cases().size() == 2, "lookupswitch 2 cases");
                check(l.cases().get(0).caseValue() == 1 && l.cases().get(1).caseValue() == 5, "lookup cases ascending");
                check(l.defaultTarget() != null, "lookup default target");
            }
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getMethod("tbl", int.class).invoke(null, 0).equals(10), "tbl(0)==10");
        check(dc.getMethod("tbl", int.class).invoke(null, 1).equals(20), "tbl(1)==20");
        check(dc.getMethod("tbl", int.class).invoke(null, 9).equals(-1), "tbl(9) default==-1");
        check(dc.getMethod("lku", int.class).invoke(null, 5).equals(5), "lku(5)==5");
        check(dc.getMethod("lku", int.class).invoke(null, 2).equals(0), "lku(2) default==0");
    }

    // ---- Part 11: 异常表族（exceptionCatch/exceptionCatchAll + trying block） ----
    static void testExceptionFamily() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("Exc1.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC, c -> {
            Label s = c.newLabel(), e = c.newLabel(), h = c.newLabel();
            c.labelBinding(s).aconst_null().athrow().labelBinding(e);
            c.exceptionCatch(s, e, h, ClassDesc.of("java.lang.NullPointerException"));
            c.labelBinding(h).pop().return_();
        }));
        ClassModel cm = cf.parse(b);
        List<ExceptionCatch> eh = codeOf(cm, "m").exceptionHandlers();
        check(eh.size() == 1, "1 handler via exceptionCatch");
        check(eh.get(0).catchType().get().asInternalName().equals("java/lang/NullPointerException"), "catchType NPE");
        check(eh.get(0).handler() != null && eh.get(0).tryStart() != null && eh.get(0).tryEnd() != null, "handler/tryStart/tryEnd labels");
        check(eh.get(0) instanceof PseudoInstruction && eh.get(0) instanceof CodeElement, "ExceptionCatch pseudo+element");
        check(cf.verify(cm).isEmpty(), "exceptionCatch verify clean");
        // exceptionCatchAll → catchType Optional.empty
        byte[] b2 = cf.build(ClassDesc.of("Exc2.U"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC, c -> {
            Label s = c.newLabel(), e = c.newLabel(), h = c.newLabel();
            c.labelBinding(s).aconst_null().athrow().labelBinding(e);
            c.exceptionCatchAll(s, e, h);
            c.labelBinding(h).pop().return_();
        }));
        List<ExceptionCatch> eh2 = codeOf(cf.parse(b2), "m").exceptionHandlers();
        check(eh2.size() == 1 && eh2.get(0).catchType().isEmpty(), "exceptionCatchAll catchType empty Optional");
        check(cf.verify(cf.parse(b2)).isEmpty(), "catchAll verify clean");
        // exceptionCatch(Optional) 重载
        byte[] b3 = cf.build(ClassDesc.of("Exc3.V"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC, c -> {
            Label s = c.newLabel(), e = c.newLabel(), h = c.newLabel();
            c.labelBinding(s).aconst_null().athrow().labelBinding(e);
            c.exceptionCatch(s, e, h, java.util.Optional.empty());
            c.labelBinding(h).pop().return_();
        }));
        check(codeOf(cf.parse(b3), "m").exceptionHandlers().get(0).catchType().isEmpty(), "exceptionCatch(Optional.empty) = any");
        // trying(...) 运行闭环：1/0 → ArithmeticException → catch → 42
        byte[] b4 = cf.build(ClassDesc.of("ExcW"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c
                .trying(t -> t.iconst_1().iconst_0().idiv().pop().iconst_0().ireturn(),
                        cat -> cat.catching(ClassDesc.of("java.lang.ArithmeticException"), h -> h.pop().bipush(42).ireturn()))
                .iconst_0().ireturn()));
        check(cf.verify(cf.parse(b4)).isEmpty(), "trying verify clean");
        check(codeOf(cf.parse(b4), "m").exceptionHandlers().size() == 1, "trying creates 1 handler");
        check(MethodHandles.lookup().defineClass(b4).getMethod("m").invoke(null).equals(42), "trying/catching runtime 42");
    }

    // ---- Part 12: wide 自动指令（iinc(256,32767)/lload(300)）+ localVariable/lineNumber 伪指令 + slot 分配 ----
    static void testWideAndPseudo() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("WidT"), cb -> cb
                .withMethodBody("w2", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c
                        .iconst_0().istore(256).iinc(256, 32767).iload(256).ireturn())
                .withMethodBody("w1", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c
                        .lconst_1().lstore(300).lload(300).l2i().ireturn()));
        ClassModel cm = cf.parse(b);
        check(cf.verify(cm).isEmpty(), "wide verify clean");
        Map<String, Integer> wideSizes = new LinkedHashMap<>();
        for (MethodModel m : cm.methods())
            for (CodeElement e : m.code().get().elementList())
                if (e instanceof Instruction i && i.opcode().isWide())
                    wideSizes.put(m.methodName().stringValue() + ":" + i.opcode(), i.sizeInBytes());
        check(wideSizes.get("w2:ISTORE_W") == 4, "istore(256)→ISTORE_W size 4");
        check(wideSizes.get("w2:IINC_W") == 6, "iinc(256,32767)→IINC_W size 6");
        check(wideSizes.get("w2:ILOAD_W") == 4, "iload(256)→ILOAD_W size 4");
        check(wideSizes.get("w1:LSTORE_W") == 4, "lstore(300)→LSTORE_W size 4");
        check(wideSizes.get("w1:LLOAD_W") == 4, "lload(300)→LLOAD_W size 4");
        check(MethodHandles.lookup().defineClass(b).getMethod("w2").invoke(null).equals(32767), "wide iinc runtime 32767");
        // 实测锁定：startLabel()/endLabel() 由 builder 预绑定，显式 labelBinding 再绑抛 IAE
        try {
            cf.build(ClassDesc.of("Wid2.N"), cb -> cb.withMethodBody("lv", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_STATIC,
                    c -> c.labelBinding(c.startLabel())));
            check(false, "rebinding startLabel must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("already-set label"), "rebind IAE msg: " + e.getMessage());
        }
        // localVariable/lineNumber 伪指令发射 + 属性（startLabel/endLabel 直接作作用域端点，无需手绑）
        byte[] b2 = cf.build(ClassDesc.of("Wid2.U"), cb -> cb.withMethodBody("lv", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_STATIC, c -> {
            c.lineNumber(42);
            c.iconst_0().istore(1);
            c.localVariable(1, "acc", ConstantDescs.CD_int, c.startLabel(), c.endLabel());
            c.iload(1).ireturn();
        }));
        CodeModel lv = codeOf(cf.parse(b2), "lv");
        check(lv.findAttribute(Attributes.localVariableTable()).isPresent(), "LVT attr present");
        check(lv.findAttribute(Attributes.lineNumberTable()).isPresent(), "LNT attr present");
        LocalVariableInfo lvi = lv.findAttribute(Attributes.localVariableTable()).get().localVariables().get(0);
        check(lvi.slot() == 1 && lvi.name().stringValue().equals("acc") && lvi.type().stringValue().equals("I"), "LVT row slot/name/type");
        for (CodeElement el : lv.elementList()) {
            if (el instanceof LocalVariable l2) {
                check(l2.slot() == 1 && l2.name().stringValue().equals("acc") && l2.typeSymbol().equals(ConstantDescs.CD_int), "LocalVariable pseudo slot/name/typeSymbol");
                check(l2.startScope() != null && l2.endScope() != null, "LocalVariable scopes non-null");
                check(l2 instanceof PseudoInstruction, "LocalVariable is PseudoInstruction (编译期即知非 Instruction——不可转换)");
            }
            if (el instanceof LineNumber ln) { check(ln.line() == 42, "LineNumber 42"); check(ln instanceof PseudoInstruction, "LineNumber is PseudoInstruction"); }
        }
        // startLabel/endLabel/newLabel/newBoundLabel
        byte[] b3 = cf.build(ClassDesc.of("Wid3.V"), cb -> cb.withMethodBody("sl", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_STATIC, c -> {
            Label l1 = c.newLabel(), l2 = c.newLabel();
            check(l1 != l2, "newLabel distinct");
            Label nb = c.newBoundLabel();
            check(nb != null, "newBoundLabel non-null");
            check(c.startLabel() != null && c.endLabel() != null && c.startLabel() != c.endLabel(), "start/end labels distinct");
            c.labelBinding(l1).nop().labelBinding(l2);
            c.iconst_1().ireturn();
        }));
        check(cf.verify(cf.parse(b3)).isEmpty(), "label verify clean");
        // receiverSlot/parameterSlot/allocateLocal（(IJLjava/lang/String;)V 实例方法：this=0,I=1,J=2-3,ref=4,分配=5）
        final int[] slots = new int[4];
        cf.build(ClassDesc.of("Wid4.W"), cb -> cb.withMethod("inst", MethodTypeDesc.ofDescriptor("(IJLjava/lang/String;)V"), ClassFile.ACC_PUBLIC, mb -> mb.withCode(c -> {
            slots[0] = c.receiverSlot();
            slots[1] = c.parameterSlot(0);
            slots[2] = c.parameterSlot(2);
            slots[3] = c.allocateLocal(TypeKind.INT);
            c.return_();
        })));
        check(slots[0] == 0, "receiverSlot 0");
        check(slots[1] == 1, "parameterSlot(0 int)==1");
        check(slots[2] == 4, "parameterSlot(2 ref)==4 (long 占 2-3)");
        check(slots[3] == 5, "allocateLocal after params==5");
    }

    // ---- Part 13: defineClass 重头闭环 —— fib 循环（比较/跳转/算术/局部变量全协作） ----
    static int dynSeq;
    static void testDefineClassFib() throws Exception {
        ClassFile cf = ClassFile.of();
        String name = "DynFib" + (++dynSeq);
        byte[] b = cf.build(ClassDesc.of(name), cb -> cb.withMethodBody("fib", MethodTypeDesc.ofDescriptor("(I)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> {
            c.iconst_0().istore(1).iconst_1().istore(2).iconst_0().istore(3);   // a=0,b=1,i=0
            Label loop = c.newLabel(), end = c.newLabel();
            c.labelBinding(loop);
            c.iload(3).iload(0).if_icmpge(end);                                   // i>=n → end
            c.iload(1).iload(2).iadd().istore(4);                                 // t=a+b
            c.iload(2).istore(1).iload(4).istore(2);                              // a=b;b=t
            c.iinc(3, 1);
            c.goto_(loop);
            c.labelBinding(end).iload(1).ireturn();
        }));
        ClassModel rm = cf.parse(b);
        check(rm.thisClass().asInternalName().equals(name), "fib thisClass");
        check(cf.verify(rm).isEmpty(), "fib verify clean");
        CodeAttribute ca = (CodeAttribute) rm.methods().get(0).code().get();
        check(ca.maxLocals() == 5, "fib maxLocals 5");
        check(ca.codeLength() == 29, "fib codeLength 29 实测: " + ca.codeLength());
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getMethod("fib", int.class).invoke(null, 0).equals(0), "fib(0)==0");
        check(dc.getMethod("fib", int.class).invoke(null, 1).equals(1), "fib(1)==1");
        check(dc.getMethod("fib", int.class).invoke(null, 10).equals(55), "fib(10)==55 生成代码真运行");
        check(dc.getMethod("fib", int.class).invoke(null, 20).equals(6765), "fib(20)==6765");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantFamily();
        testArithmeticConversion();
        testBranchFamily();
        testArrayFamily();
        testFieldAccessFamily();
        testInvokeFamily();
        testReturnStackMonitorFamily();
        testSwitchFamily();
        testExceptionFamily();
        testWideAndPseudo();
        testDefineClassFib();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.lang.classfile.Opcode conformance: white-box structure + black-box behavior.
 * @run main OpcodeTest
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

public class OpcodeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.Opcode");
        check(c != null, "java.lang.classfile.Opcode loads");
        check(c.getName().equals("java.lang.classfile.Opcode"), "binary name java.lang.classfile.Opcode");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Opcode is public");
        check(c.isEnum(), "java.lang.classfile.Opcode is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.Opcode superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 213, "213 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("NOP", "ACONST_NULL", "ICONST_M1", "ICONST_0", "ICONST_1", "ICONST_2", "ICONST_3", "ICONST_4", "ICONST_5", "LCONST_0", "LCONST_1", "FCONST_0", "FCONST_1", "FCONST_2", "DCONST_0", "DCONST_1", "BIPUSH", "SIPUSH", "LDC", "LDC_W", "LDC2_W", "ILOAD", "LLOAD", "FLOAD", "DLOAD", "ALOAD", "ILOAD_0", "ILOAD_1", "ILOAD_2", "ILOAD_3", "LLOAD_0", "LLOAD_1", "LLOAD_2", "LLOAD_3", "FLOAD_0", "FLOAD_1", "FLOAD_2", "FLOAD_3", "DLOAD_0", "DLOAD_1", "DLOAD_2", "DLOAD_3", "ALOAD_0", "ALOAD_1", "ALOAD_2", "ALOAD_3", "IALOAD", "LALOAD", "FALOAD", "DALOAD", "AALOAD", "BALOAD", "CALOAD", "SALOAD", "ISTORE", "LSTORE", "FSTORE", "DSTORE", "ASTORE", "ISTORE_0", "ISTORE_1", "ISTORE_2", "ISTORE_3", "LSTORE_0", "LSTORE_1", "LSTORE_2", "LSTORE_3", "FSTORE_0", "FSTORE_1", "FSTORE_2", "FSTORE_3", "DSTORE_0", "DSTORE_1", "DSTORE_2", "DSTORE_3", "ASTORE_0", "ASTORE_1", "ASTORE_2", "ASTORE_3", "IASTORE", "LASTORE", "FASTORE", "DASTORE", "AASTORE", "BASTORE", "CASTORE", "SASTORE", "POP", "POP2", "DUP", "DUP_X1", "DUP_X2", "DUP2", "DUP2_X1", "DUP2_X2", "SWAP", "IADD", "LADD", "FADD", "DADD", "ISUB", "LSUB", "FSUB", "DSUB", "IMUL", "LMUL", "FMUL", "DMUL", "IDIV", "LDIV", "FDIV", "DDIV", "IREM", "LREM", "FREM", "DREM", "INEG", "LNEG", "FNEG", "DNEG", "ISHL", "LSHL", "ISHR", "LSHR", "IUSHR", "LUSHR", "IAND", "LAND", "IOR", "LOR", "IXOR", "LXOR", "IINC", "I2L", "I2F", "I2D", "L2I", "L2F", "L2D", "F2I", "F2L", "F2D", "D2I", "D2L", "D2F", "I2B", "I2C", "I2S", "LCMP", "FCMPL", "FCMPG", "DCMPL", "DCMPG", "IFEQ", "IFNE", "IFLT", "IFGE", "IFGT", "IFLE", "IF_ICMPEQ", "IF_ICMPNE", "IF_ICMPLT", "IF_ICMPGE", "IF_ICMPGT", "IF_ICMPLE", "IF_ACMPEQ", "IF_ACMPNE", "GOTO", "JSR", "RET", "TABLESWITCH", "LOOKUPSWITCH", "IRETURN", "LRETURN", "FRETURN", "DRETURN", "ARETURN", "RETURN", "GETSTATIC", "PUTSTATIC", "GETFIELD", "PUTFIELD", "INVOKEVIRTUAL", "INVOKESPECIAL", "INVOKESTATIC", "INVOKEINTERFACE", "INVOKEDYNAMIC", "NEW", "NEWARRAY", "ANEWARRAY", "ARRAYLENGTH", "ATHROW", "CHECKCAST", "INSTANCEOF", "MONITORENTER", "MONITOREXIT", "MULTIANEWARRAY", "IFNULL", "IFNONNULL", "GOTO_W", "JSR_W", "ILOAD_W", "LLOAD_W", "FLOAD_W", "DLOAD_W", "ALOAD_W", "ISTORE_W", "LSTORE_W", "FSTORE_W", "DSTORE_W", "ASTORE_W", "RET_W", "IINC_W")), "exact enum constant names");
        check(declares(c, "values"), "declares values");
        check(declares(c, "valueOf"), "declares valueOf");
        check(declares(c, "bytecode"), "declares bytecode");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(Opcode.NOP.bytecode() == 0x00, "NOP bytecode 0");
        check(Opcode.ICONST_0.bytecode() == 0x03, "ICONST_0 bytecode 3");
        check(Opcode.RETURN.bytecode() == 0xb1, "RETURN bytecode");
        check(Opcode.IRETURN.bytecode() == 0xac, "IRETURN bytecode");
        check(Opcode.ILOAD_W.isWide(), "ILOAD_W is wide");
        check(!Opcode.ILOAD.isWide(), "ILOAD not wide");
        check(Opcode.GOTO.sizeIfFixed() == 3, "GOTO size 3");
        check(Opcode.NOP.sizeIfFixed() == 1, "NOP size 1");
        check(Opcode.IADD.kind() == Opcode.Kind.OPERATOR, "IADD kind OPERATOR");
        check(Opcode.IINC.kind() == Opcode.Kind.INCREMENT, "IINC kind INCREMENT");
        check(Opcode.LOOKUPSWITCH.kind() == Opcode.Kind.LOOKUP_SWITCH, "LOOKUPSWITCH kind");
        check(Opcode.valueOf("NOP") == Opcode.NOP, "valueOf round trip");
        check(Opcode.values().length == 213, "213 opcodes");

    }

    // ---- Part 3: bytecode() 精确表（JVMS 操作码映射，JDK-27 实测锁定） ----
    static void testBytecodeTable() {
        check(Opcode.NOP.bytecode() == 0x00, "NOP=0x00");
        check(Opcode.ACONST_NULL.bytecode() == 0x01, "ACONST_NULL=0x01");
        check(Opcode.ICONST_M1.bytecode() == 0x02, "ICONST_M1=0x02");
        check(Opcode.ICONST_0.bytecode() == 0x03, "ICONST_0=0x03");
        check(Opcode.ICONST_5.bytecode() == 0x08, "ICONST_5=0x08");
        check(Opcode.BIPUSH.bytecode() == 0x10, "BIPUSH=0x10");
        check(Opcode.SIPUSH.bytecode() == 0x11, "SIPUSH=0x11");
        check(Opcode.LDC.bytecode() == 0x12, "LDC=0x12");
        check(Opcode.LDC_W.bytecode() == 0x13, "LDC_W=0x13");
        check(Opcode.LDC2_W.bytecode() == 0x14, "LDC2_W=0x14");
        check(Opcode.ILOAD.bytecode() == 0x15, "ILOAD=0x15");
        check(Opcode.ILOAD_0.bytecode() == 0x1a, "ILOAD_0=0x1a");
        check(Opcode.LLOAD_0.bytecode() == 0x1e, "LLOAD_0=0x1e");
        check(Opcode.ALOAD_0.bytecode() == 0x2a, "ALOAD_0=0x2a");
        check(Opcode.ISTORE.bytecode() == 0x36, "ISTORE=0x36");
        check(Opcode.ISTORE_0.bytecode() == 0x3b, "ISTORE_0=0x3b");
        check(Opcode.IASTORE.bytecode() == 0x4f, "IASTORE=0x4f");
        check(Opcode.POP.bytecode() == 0x57, "POP=0x57");
        check(Opcode.DUP.bytecode() == 0x59, "DUP=0x59");
        check(Opcode.IADD.bytecode() == 0x60, "IADD=0x60");
        check(Opcode.IMUL.bytecode() == 0x68, "IMUL=0x68");
        check(Opcode.IINC.bytecode() == 0x84, "IINC=0x84");
        check(Opcode.I2L.bytecode() == 0x85, "I2L=0x85");
        check(Opcode.LCMP.bytecode() == 0x94, "LCMP=0x94");
        check(Opcode.IFEQ.bytecode() == 0x99, "IFEQ=0x99");
        check(Opcode.IF_ICMPGE.bytecode() == 0xA2, "IF_ICMPGE=0xA2");
        check(Opcode.GOTO.bytecode() == 0xA7, "GOTO=0xA7");
        check(Opcode.JSR.bytecode() == 0xA8, "JSR=0xA8");
        check(Opcode.RET.bytecode() == 0xA9, "RET=0xA9");
        check(Opcode.TABLESWITCH.bytecode() == 0xAA, "TABLESWITCH=0xAA");
        check(Opcode.LOOKUPSWITCH.bytecode() == 0xAB, "LOOKUPSWITCH=0xAB");
        check(Opcode.IRETURN.bytecode() == 0xAC, "IRETURN=0xAC");
        check(Opcode.LRETURN.bytecode() == 0xAD, "LRETURN=0xAD");
        check(Opcode.RETURN.bytecode() == 0xB1, "RETURN=0xB1");
        check(Opcode.GETSTATIC.bytecode() == 0xB2, "GETSTATIC=0xB2");
        check(Opcode.PUTSTATIC.bytecode() == 0xB3, "PUTSTATIC=0xB3");
        check(Opcode.PUTFIELD.bytecode() == 0xB5, "PUTFIELD=0xB5");
        check(Opcode.INVOKEVIRTUAL.bytecode() == 0xB6, "INVOKEVIRTUAL=0xB6");
        check(Opcode.INVOKESPECIAL.bytecode() == 0xB7, "INVOKESPECIAL=0xB7");
        check(Opcode.INVOKESTATIC.bytecode() == 0xB8, "INVOKESTATIC=0xB8");
        check(Opcode.INVOKEINTERFACE.bytecode() == 0xB9, "INVOKEINTERFACE=0xB9");
        check(Opcode.INVOKEDYNAMIC.bytecode() == 0xBA, "INVOKEDYNAMIC=0xBA");
        check(Opcode.NEW.bytecode() == 0xBB, "NEW=0xBB");
        check(Opcode.NEWARRAY.bytecode() == 0xBC, "NEWARRAY=0xBC");
        check(Opcode.ANEWARRAY.bytecode() == 0xBD, "ANEWARRAY=0xBD");
        check(Opcode.ATHROW.bytecode() == 0xBF, "ATHROW=0xBF");
        check(Opcode.CHECKCAST.bytecode() == 0xC0, "CHECKCAST=0xC0");
        check(Opcode.INSTANCEOF.bytecode() == 0xC1, "INSTANCEOF=0xC1");
        check(Opcode.MONITORENTER.bytecode() == 0xC2, "MONITORENTER=0xC2");
        check(Opcode.MONITOREXIT.bytecode() == 0xC3, "MONITOREXIT=0xC3");
        check(Opcode.MULTIANEWARRAY.bytecode() == 0xC5, "MULTIANEWARRAY=0xC5");
        check(Opcode.IFNULL.bytecode() == 0xC6, "IFNULL=0xC6");
        check(Opcode.IFNONNULL.bytecode() == 0xC7, "IFNONNULL=0xC7");
        check(Opcode.GOTO_W.bytecode() == 0xC8, "GOTO_W=0xC8");
        check(Opcode.JSR_W.bytecode() == 0xC9, "JSR_W=0xC9");
    }

    // ---- Part 4: bytecode() 全互异 + 宽型 0xC4 编码（JDK-27 实测锁定） ----
    static void testBytecodeBijection() {
        Opcode[] ops = Opcode.values();
        Set<Integer> bcs = new HashSet<>();
        for (Opcode o : ops) bcs.add(o.bytecode());
        check(bcs.size() == 213, "213 distinct bytecode values");
        // 每个操作码的原始值：非宽型为单字节 [0,0xC9]，宽型为 0xC4<<8|base
        boolean shapeOk = true;
        for (Opcode o : ops) {
            int bc = o.bytecode();
            if (o.isWide()) {
                if ((bc >> 8) != 0xC4) shapeOk = false;
            } else {
                if (bc < 0 || bc > 0xC9) shapeOk = false;
            }
        }
        check(shapeOk, "bytecode raw value shape (plain byte or 0xC4<<8|base)");
        // 宽型编码样本：ILOAD_W = 0xC415（WIDE 前缀 0xC4 + ILOAD 0x15）
        check(Opcode.ILOAD_W.bytecode() == 0xC415, "ILOAD_W=0xC415 (WIDE-prefixed)");
        check(Opcode.ALOAD_W.bytecode() == 0xC419, "ALOAD_W=0xC419");
        check(Opcode.ASTORE_W.bytecode() == 0xC43A, "ASTORE_W=0xC43a");
        check(Opcode.RET_W.bytecode() == 0xC4A9, "RET_W=0xC4a9");
        check(Opcode.IINC_W.bytecode() == 0xC484, "IINC_W=0xC484");
        check(Opcode.ILOAD_W.bytecode() == ((0xC4 << 8) | Opcode.ILOAD.bytecode()), "ILOAD_W = WIDE<<8 | ILOAD");
        check(Opcode.IINC_W.bytecode() == ((0xC4 << 8) | Opcode.IINC.bytecode()), "IINC_W = WIDE<<8 | IINC");
        // 12 个宽型全部满足 X_W == (0xC4<<8)|X（基名去掉 _W 后缀即非宽型常量名）
        String[] wides = {"ILOAD_W", "LLOAD_W", "FLOAD_W", "DLOAD_W", "ALOAD_W",
                "ISTORE_W", "LSTORE_W", "FSTORE_W", "DSTORE_W", "ASTORE_W", "RET_W", "IINC_W"};
        boolean allWide = true;
        for (String w : wides) {
            Opcode wide = Opcode.valueOf(w);
            Opcode base = Opcode.valueOf(w.substring(0, w.length() - 2));
            if (!wide.isWide() || base.isWide()) allWide = false;
            if (wide.bytecode() != ((0xC4 << 8) | base.bytecode())) allWide = false;
        }
        check(allWide, "all 12 _W opcodes: isWide + WIDE<<8|base encoding");
        // 枚举中没有独立 WIDE 常量（宽型语义内嵌于 _W 值）
        boolean hasWide = false;
        for (Opcode o : ops) if (o.name().equals("WIDE")) hasWide = true;
        check(!hasWide, "no standalone WIDE enum constant");
    }

    // ---- Part 5: kind() 30 精确分类 + 26 类计数表（JDK-27 实测锁定） ----
    static void testKindTable() {
        check(Opcode.IADD.kind() == Opcode.Kind.OPERATOR, "IADD OPERATOR");
        check(Opcode.LADD.kind() == Opcode.Kind.OPERATOR, "LADD OPERATOR");
        check(Opcode.IRETURN.kind() == Opcode.Kind.RETURN, "IRETURN RETURN");
        check(Opcode.GOTO.kind() == Opcode.Kind.BRANCH, "GOTO BRANCH");
        check(Opcode.I2L.kind() == Opcode.Kind.CONVERT, "I2L CONVERT");
        check(Opcode.ILOAD.kind() == Opcode.Kind.LOAD, "ILOAD LOAD");
        check(Opcode.ALOAD_0.kind() == Opcode.Kind.LOAD, "ALOAD_0 LOAD");
        check(Opcode.ASTORE.kind() == Opcode.Kind.STORE, "ASTORE STORE");
        check(Opcode.ISTORE_1.kind() == Opcode.Kind.STORE, "ISTORE_1 STORE");
        check(Opcode.IINC.kind() == Opcode.Kind.INCREMENT, "IINC INCREMENT");
        check(Opcode.NEW.kind() == Opcode.Kind.NEW_OBJECT, "NEW NEW_OBJECT");
        check(Opcode.NEWARRAY.kind() == Opcode.Kind.NEW_PRIMITIVE_ARRAY, "NEWARRAY NEW_PRIMITIVE_ARRAY");
        check(Opcode.ANEWARRAY.kind() == Opcode.Kind.NEW_REF_ARRAY, "ANEWARRAY NEW_REF_ARRAY");
        check(Opcode.MULTIANEWARRAY.kind() == Opcode.Kind.NEW_MULTI_ARRAY, "MULTIANEWARRAY NEW_MULTI_ARRAY");
        check(Opcode.MONITORENTER.kind() == Opcode.Kind.MONITOR, "MONITORENTER MONITOR");
        check(Opcode.MONITOREXIT.kind() == Opcode.Kind.MONITOR, "MONITOREXIT MONITOR");
        check(Opcode.ATHROW.kind() == Opcode.Kind.THROW_EXCEPTION, "ATHROW THROW_EXCEPTION");
        check(Opcode.CHECKCAST.kind() == Opcode.Kind.TYPE_CHECK, "CHECKCAST TYPE_CHECK");
        check(Opcode.INSTANCEOF.kind() == Opcode.Kind.TYPE_CHECK, "INSTANCEOF TYPE_CHECK");
        check(Opcode.GETSTATIC.kind() == Opcode.Kind.FIELD_ACCESS, "GETSTATIC FIELD_ACCESS");
        check(Opcode.PUTFIELD.kind() == Opcode.Kind.FIELD_ACCESS, "PUTFIELD FIELD_ACCESS");
        check(Opcode.INVOKEVIRTUAL.kind() == Opcode.Kind.INVOKE, "INVOKEVIRTUAL INVOKE");
        check(Opcode.INVOKESTATIC.kind() == Opcode.Kind.INVOKE, "INVOKESTATIC INVOKE");
        check(Opcode.INVOKEDYNAMIC.kind() == Opcode.Kind.INVOKE_DYNAMIC, "INVOKEDYNAMIC INVOKE_DYNAMIC");
        check(Opcode.NOP.kind() == Opcode.Kind.NOP, "NOP NOP");
        check(Opcode.RET.kind() == Opcode.Kind.DISCONTINUED_RET, "RET DISCONTINUED_RET");
        check(Opcode.JSR.kind() == Opcode.Kind.DISCONTINUED_JSR, "JSR DISCONTINUED_JSR");
        check(Opcode.JSR_W.kind() == Opcode.Kind.DISCONTINUED_JSR, "JSR_W DISCONTINUED_JSR");
        check(Opcode.TABLESWITCH.kind() == Opcode.Kind.TABLE_SWITCH, "TABLESWITCH TABLE_SWITCH");
        check(Opcode.LOOKUPSWITCH.kind() == Opcode.Kind.LOOKUP_SWITCH, "LOOKUPSWITCH LOOKUP_SWITCH");
        check(Opcode.IALOAD.kind() == Opcode.Kind.ARRAY_LOAD, "IALOAD ARRAY_LOAD");
        check(Opcode.IASTORE.kind() == Opcode.Kind.ARRAY_STORE, "IASTORE ARRAY_STORE");
        check(Opcode.DUP.kind() == Opcode.Kind.STACK, "DUP STACK");
        check(Opcode.SWAP.kind() == Opcode.Kind.STACK, "SWAP STACK");
        check(Opcode.LDC.kind() == Opcode.Kind.CONSTANT, "LDC CONSTANT");
        check(Opcode.ICONST_2.kind() == Opcode.Kind.CONSTANT, "ICONST_2 CONSTANT");
        // 遍历：kind() 全部非 null 且落在 26 值集合内
        Set<Opcode.Kind> kinds = new HashSet<>(Arrays.asList(Opcode.Kind.values()));
        boolean allNonNull = true;
        for (Opcode o : Opcode.values())
            if (o.kind() == null || !kinds.contains(o.kind())) allNonNull = false;
        check(allNonNull, "kind() non-null and in 26-value set for all 213");
        // 每 kind 的 opcode 计数表（实测锁定）
        Map<Opcode.Kind, Integer> counts = new EnumMap<>(Opcode.Kind.class);
        for (Opcode o : Opcode.values()) counts.merge(o.kind(), 1, Integer::sum);
        check(counts.size() == 26, "all 26 kinds have >=1 opcode");
        check(counts.get(Opcode.Kind.LOAD) == 30, "LOAD count 30");
        check(counts.get(Opcode.Kind.STORE) == 30, "STORE count 30");
        check(counts.get(Opcode.Kind.OPERATOR) == 42, "OPERATOR count 42");
        check(counts.get(Opcode.Kind.CONSTANT) == 20, "CONSTANT count 20");
        check(counts.get(Opcode.Kind.BRANCH) == 18, "BRANCH count 18");
        check(counts.get(Opcode.Kind.CONVERT) == 15, "CONVERT count 15");
        check(counts.get(Opcode.Kind.STACK) == 9, "STACK count 9");
        check(counts.get(Opcode.Kind.ARRAY_LOAD) == 8, "ARRAY_LOAD count 8");
        check(counts.get(Opcode.Kind.ARRAY_STORE) == 8, "ARRAY_STORE count 8");
        check(counts.get(Opcode.Kind.RETURN) == 6, "RETURN count 6");
        check(counts.get(Opcode.Kind.FIELD_ACCESS) == 4, "FIELD_ACCESS count 4");
        check(counts.get(Opcode.Kind.INVOKE) == 4, "INVOKE count 4");
        check(counts.get(Opcode.Kind.INCREMENT) == 2, "INCREMENT count 2");
        check(counts.get(Opcode.Kind.TYPE_CHECK) == 2, "TYPE_CHECK count 2");
        check(counts.get(Opcode.Kind.MONITOR) == 2, "MONITOR count 2");
        check(counts.get(Opcode.Kind.DISCONTINUED_JSR) == 2, "DISCONTINUED_JSR count 2");
        check(counts.get(Opcode.Kind.DISCONTINUED_RET) == 2, "DISCONTINUED_RET count 2");
        check(counts.get(Opcode.Kind.NOP) == 1 && counts.get(Opcode.Kind.THROW_EXCEPTION) == 1
                && counts.get(Opcode.Kind.INVOKE_DYNAMIC) == 1 && counts.get(Opcode.Kind.NEW_OBJECT) == 1
                && counts.get(Opcode.Kind.NEW_PRIMITIVE_ARRAY) == 1 && counts.get(Opcode.Kind.NEW_REF_ARRAY) == 1
                && counts.get(Opcode.Kind.NEW_MULTI_ARRAY) == 1 && counts.get(Opcode.Kind.LOOKUP_SWITCH) == 1
                && counts.get(Opcode.Kind.TABLE_SWITCH) == 1, "nine singleton kinds each 1");
        int sum = 0;
        for (int v : counts.values()) sum += v;
        check(sum == 213, "kind counts sum 213");
    }

    // ---- Part 6: sizeIfFixed 精确表 + 全域分布（-1 为变长哨兵，实测锁定） ----
    static void testSizeTable() {
        check(Opcode.NOP.sizeIfFixed() == 1, "NOP size 1");
        check(Opcode.IADD.sizeIfFixed() == 1, "IADD size 1");
        check(Opcode.RETURN.sizeIfFixed() == 1, "RETURN size 1");
        check(Opcode.BIPUSH.sizeIfFixed() == 2, "BIPUSH size 2");
        check(Opcode.LDC.sizeIfFixed() == 2, "LDC size 2");
        check(Opcode.NEWARRAY.sizeIfFixed() == 2, "NEWARRAY size 2");
        check(Opcode.ILOAD.sizeIfFixed() == 2, "ILOAD size 2");
        check(Opcode.RET.sizeIfFixed() == 2, "RET size 2");
        check(Opcode.SIPUSH.sizeIfFixed() == 3, "SIPUSH size 3");
        check(Opcode.LDC_W.sizeIfFixed() == 3, "LDC_W size 3");
        check(Opcode.LDC2_W.sizeIfFixed() == 3, "LDC2_W size 3");
        check(Opcode.IINC.sizeIfFixed() == 3, "IINC size 3");
        check(Opcode.GOTO.sizeIfFixed() == 3, "GOTO size 3");
        check(Opcode.IF_ICMPGE.sizeIfFixed() == 3, "IF_ICMPGE size 3");
        check(Opcode.GETSTATIC.sizeIfFixed() == 3, "GETSTATIC size 3");
        check(Opcode.PUTFIELD.sizeIfFixed() == 3, "PUTFIELD size 3");
        check(Opcode.INVOKEVIRTUAL.sizeIfFixed() == 3, "INVOKEVIRTUAL size 3");
        check(Opcode.INVOKESTATIC.sizeIfFixed() == 3, "INVOKESTATIC size 3");
        check(Opcode.NEW.sizeIfFixed() == 3, "NEW size 3");
        check(Opcode.ANEWARRAY.sizeIfFixed() == 3, "ANEWARRAY size 3");
        check(Opcode.CHECKCAST.sizeIfFixed() == 3, "CHECKCAST size 3");
        check(Opcode.MULTIANEWARRAY.sizeIfFixed() == 4, "MULTIANEWARRAY size 4");
        check(Opcode.INVOKEINTERFACE.sizeIfFixed() == 5, "INVOKEINTERFACE size 5");
        check(Opcode.INVOKEDYNAMIC.sizeIfFixed() == 5, "INVOKEDYNAMIC size 5");
        check(Opcode.GOTO_W.sizeIfFixed() == 5, "GOTO_W size 5");
        check(Opcode.JSR_W.sizeIfFixed() == 5, "JSR_W size 5");
        check(Opcode.ILOAD_W.sizeIfFixed() == 4, "ILOAD_W size 4");
        check(Opcode.IINC_W.sizeIfFixed() == 6, "IINC_W size 6");
        // 变长指令哨兵为 -1（仅 TABLESWITCH/LOOKUPSWITCH）
        check(Opcode.TABLESWITCH.sizeIfFixed() == -1, "TABLESWITCH variable -1");
        check(Opcode.LOOKUPSWITCH.sizeIfFixed() == -1, "LOOKUPSWITCH variable -1");
        int variableCount = 0;
        for (Opcode o : Opcode.values()) if (o.sizeIfFixed() == -1) variableCount++;
        check(variableCount == 2, "exactly 2 variable-size opcodes");
        // 遍历：sizeIfFixed() >= -1
        boolean allGe = true;
        for (Opcode o : Opcode.values()) if (o.sizeIfFixed() < -1) allGe = false;
        check(allGe, "sizeIfFixed >= -1 for all 213");
        // 全域分布（实测锁定）：{-1:2, 1:147, 2:14, 3:33, 4:12, 5:4, 6:1}
        Map<Integer, Integer> dist = new TreeMap<>();
        for (Opcode o : Opcode.values()) dist.merge(o.sizeIfFixed(), 1, Integer::sum);
        check(dist.equals(Map.of(-1, 2, 1, 147, 2, 14, 3, 33, 4, 12, 5, 4, 6, 1)),
                "sizeIfFixed distribution {-1:2,1:147,2:14,3:33,4:12,5:4,6:1}: " + dist);
    }

    // ---- Part 7: isWide 族（12 宽型 true，基础型 false） ----
    static void testWideFamily() {
        check(!Opcode.IINC.isWide(), "IINC not wide");
        check(!Opcode.ILOAD.isWide(), "ILOAD not wide");
        check(!Opcode.GOTO.isWide(), "GOTO not wide");
        check(Opcode.IINC_W.isWide(), "IINC_W wide");
        check(Opcode.ILOAD_W.isWide(), "ILOAD_W wide");
        check(Opcode.LLOAD_W.isWide(), "LLOAD_W wide");
        check(Opcode.FLOAD_W.isWide(), "FLOAD_W wide");
        check(Opcode.DLOAD_W.isWide(), "DLOAD_W wide");
        check(Opcode.ALOAD_W.isWide(), "ALOAD_W wide");
        check(Opcode.ISTORE_W.isWide(), "ISTORE_W wide");
        check(Opcode.LSTORE_W.isWide(), "LSTORE_W wide");
        check(Opcode.FSTORE_W.isWide(), "FSTORE_W wide");
        check(Opcode.DSTORE_W.isWide(), "DSTORE_W wide");
        check(Opcode.ASTORE_W.isWide(), "ASTORE_W wide");
        check(Opcode.RET_W.isWide(), "RET_W wide");
        // GOTO_W/JSR_W 是独立操作码（0xC8/0xC9），并非 WIDE 前缀形式——isWide()==false（实测锁定）
        check(!Opcode.GOTO_W.isWide(), "GOTO_W is NOT wide (standalone opcode 0xC8)");
        check(!Opcode.JSR_W.isWide(), "JSR_W is NOT wide (standalone opcode 0xC9)");
        // 注意 LDC_W/LDC2_W 名字带 _W 但不是 wide 形式（无 WIDE 前缀语义）
        check(!Opcode.LDC_W.isWide(), "LDC_W is NOT a wide-form opcode");
        check(!Opcode.LDC2_W.isWide(), "LDC2_W is NOT a wide-form opcode");
        // 计数：恰 12 个 isWide
        int wideCount = 0;
        for (Opcode o : Opcode.values()) if (o.isWide()) wideCount++;
        check(wideCount == 12, "exactly 12 wide opcodes");
    }

    // ---- Part 8: 枚举语义（valueOf 负例 + values 防御拷贝 + ordinal 头部） ----
    static void testEnumSemantics() {
        check(Opcode.valueOf("IADD") == Opcode.IADD, "valueOf(IADD) round trip");
        check(Opcode.valueOf("MONITOREXIT") == Opcode.MONITOREXIT, "valueOf(MONITOREXIT) round trip");
        try {
            Opcode.valueOf("NOT_AN_OPCODE");
            check(false, "valueOf unknown must throw");
        } catch (IllegalArgumentException e) {
            check("No enum constant java.lang.classfile.Opcode.NOT_AN_OPCODE".equals(e.getMessage()),
                    "valueOf unknown IAE message: " + e.getMessage());
        }
        try {
            Opcode.valueOf(null);
            check(false, "valueOf(null) must throw");
        } catch (NullPointerException e) {
            check("Name is null".equals(e.getMessage()), "valueOf(null) NPE message");
        }
        // values() 每次返回防御拷贝
        Opcode[] a1 = Opcode.values();
        Opcode[] a2 = Opcode.values();
        check(a1 != a2, "values() defensive copy");
        a1[0] = null;
        check(Opcode.values()[0] == Opcode.NOP, "mutating returned array does not affect enum");
        // ordinal 头部（声明顺序即 JVMS 常规顺序）
        check(Opcode.NOP.ordinal() == 0, "NOP ordinal 0");
        check(Opcode.ACONST_NULL.ordinal() == 1, "ACONST_NULL ordinal 1");
        check(Opcode.ICONST_M1.ordinal() == 2, "ICONST_M1 ordinal 2");
        check(Opcode.IADD.name().equals("IADD") && Opcode.IADD.toString().equals("IADD"), "name/toString");
    }

    // ---- Part 9: 真实发射-回读交叉验证（opcode 与 sizeInBytes 一致） ----
    static void testEmissionCrossCheck() throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.OpX1"), cb -> cb
                .withSuperclass(ClassDesc.of("java.lang.Object"))
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> cob
                        .iconst_2().ldc("hello").pop().iconst_3().imul()
                        .getstatic(ClassDesc.of("java.lang.System"), "out", ClassDesc.of("java.io.PrintStream")).pop()
                        .ireturn()));
        ClassModel cm = ClassFile.of().parse(b);
        List<Opcode> seen = new ArrayList<>();
        CodeAttribute ca = (CodeAttribute) cm.methods().get(0).code().get();
        for (CodeElement e : ca) if (e instanceof Instruction i) seen.add(i.opcode());
        check(seen.equals(Arrays.asList(Opcode.ICONST_2, Opcode.LDC, Opcode.POP, Opcode.ICONST_3,
                Opcode.IMUL, Opcode.GETSTATIC, Opcode.POP, Opcode.IRETURN)),
                "emission readback opcode sequence: " + seen);
        // sizeInBytes 与 sizeIfFixed 一致（8 个交叉点）
        List<Integer> sizes = new ArrayList<>();
        for (CodeElement e : ca) if (e instanceof Instruction i) sizes.add(i.sizeInBytes());
        check(sizes.equals(Arrays.asList(1, 2, 1, 1, 1, 3, 1, 1)), "readback sizeInBytes sequence: " + sizes);
        check(Opcode.LDC.sizeIfFixed() == 2 && Opcode.GETSTATIC.sizeIfFixed() == 3
                && Opcode.IRETURN.sizeIfFixed() == 1, "sizes match sizeIfFixed");
        // ldc(long) 自动选 LDC2_W
        byte[] b2 = ClassFile.of().build(ClassDesc.of("pkg.OpX2"), cb -> cb
                .withSuperclass(ClassDesc.of("java.lang.Object"))
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()J"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        cob -> cob.ldc(100000L).lreturn()));
        boolean sawLdc2w = false;
        for (CodeElement e : (CodeAttribute) ClassFile.of().parse(b2).methods().get(0).code().get())
            if (e instanceof Instruction i && i.opcode() == Opcode.LDC2_W) sawLdc2w = true;
        check(sawLdc2w, "ldc(long) emits LDC2_W");
        // iinc(256,1) 超出单字节索引自动升级 IINC_W（实测 wide 自动选择）
        byte[] b3 = ClassFile.of().build(ClassDesc.of("pkg.OpX3"), cb -> cb
                .withSuperclass(ClassDesc.of("java.lang.Object"))
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        cob -> cob.iinc(256, 1).iconst_0().ireturn()));
        boolean sawIincW = false, sawIinc = false;
        for (CodeElement e : (CodeAttribute) ClassFile.of().parse(b3).methods().get(0).code().get()) {
            if (e instanceof Instruction i) {
                if (i.opcode() == Opcode.IINC_W) sawIincW = true;
                if (i.opcode() == Opcode.IINC) sawIinc = true;
            }
        }
        check(sawIincW && !sawIinc, "iinc(256,1) auto-widens to IINC_W");
        // iload(256) 自动 ILOAD_W
        byte[] b4 = ClassFile.of().build(ClassDesc.of("pkg.OpX4"), cb -> cb
                .withSuperclass(ClassDesc.of("java.lang.Object"))
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        cob -> cob.iload(256).ireturn()));
        boolean sawIloadW = false;
        for (CodeElement e : (CodeAttribute) ClassFile.of().parse(b4).methods().get(0).code().get())
            if (e instanceof Instruction i && i.opcode() == Opcode.ILOAD_W) sawIloadW = true;
        check(sawIloadW, "iload(256) auto-widens to ILOAD_W");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testBytecodeTable();
        testBytecodeBijection();
        testKindTable();
        testSizeTable();
        testWideFamily();
        testEnumSemantics();
        testEmissionCrossCheck();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

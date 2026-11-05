/*
 * @test
 * @summary java.lang.classfile.TypeKind conformance: white-box structure + black-box behavior.
 * @run main TypeKindTest
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

public class TypeKindTest {
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
        Class<?> c = Class.forName("java.lang.classfile.TypeKind");
        check(c != null, "java.lang.classfile.TypeKind loads");
        check(c.getName().equals("java.lang.classfile.TypeKind"), "binary name java.lang.classfile.TypeKind");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeKind is public");
        check(c.isEnum(), "java.lang.classfile.TypeKind is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.TypeKind superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 10, "10 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("BOOLEAN", "BYTE", "CHAR", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE", "REFERENCE", "VOID")), "exact enum constant names");
        check(declares(c, "values"), "declares values");
        check(declares(c, "valueOf"), "declares valueOf");
        check(declares(c, "upperBound"), "declares upperBound");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(TypeKind.INT.slotSize() == 1, "INT slots 1");
        check(TypeKind.LONG.slotSize() == 2, "LONG slots 2");
        check(TypeKind.DOUBLE.slotSize() == 2, "DOUBLE slots 2");
        check(TypeKind.VOID.slotSize() == 0, "VOID slots 0");
        check(TypeKind.REFERENCE.slotSize() == 1, "REFERENCE slots 1");
        check(TypeKind.INT.newarrayCode() == 10, "INT newarray code");
        check(TypeKind.BOOLEAN.newarrayCode() == 4, "BOOLEAN newarray code");
        check(TypeKind.fromNewarrayCode(10) == TypeKind.INT, "fromNewarrayCode INT");
        check(TypeKind.fromDescriptor("J") == TypeKind.LONG, "fromDescriptor J");
        check(TypeKind.from(ConstantDescs.CD_int) == TypeKind.INT, "from ClassDesc int");
        check(TypeKind.INT.asLoadable() == TypeKind.INT, "INT asLoadable");
        check(TypeKind.VOID.asLoadable() == TypeKind.VOID, "VOID asLoadable");
        check(TypeKind.INT.upperBound().equals(ConstantDescs.CD_int), "upperBound int");

    }

    // ---- Part 3: 全量精确表（slotSize/newarrayCode/asLoadable/upperBound，JDK-27 实测锁定） ----
    static void testFullTables() {
        // slotSize：INT 族 1 槽、LONG/DOUBLE 2 槽、VOID 0 槽
        check(TypeKind.BOOLEAN.slotSize() == 1, "BOOLEAN slot 1");
        check(TypeKind.BYTE.slotSize() == 1, "BYTE slot 1");
        check(TypeKind.CHAR.slotSize() == 1, "CHAR slot 1");
        check(TypeKind.SHORT.slotSize() == 1, "SHORT slot 1");
        check(TypeKind.FLOAT.slotSize() == 1, "FLOAT slot 1");
        // INT=1/LONG=2/DOUBLE=2/REFERENCE=1/VOID=0 已在 testBlackBox 断言
        // newarrayCode 与 JVM newarray atype 精确对应（BOOLEAN=4..LONG=11）
        check(TypeKind.BOOLEAN.newarrayCode() == 4, "BOOLEAN code 4");
        check(TypeKind.CHAR.newarrayCode() == 5, "CHAR code 5");
        check(TypeKind.FLOAT.newarrayCode() == 6, "FLOAT code 6");
        check(TypeKind.DOUBLE.newarrayCode() == 7, "DOUBLE code 7");
        check(TypeKind.BYTE.newarrayCode() == 8, "BYTE code 8");
        check(TypeKind.SHORT.newarrayCode() == 9, "SHORT code 9");
        // REFERENCE/VOID 不能用 newarray：-1 哨兵（实测锁定）
        check(TypeKind.REFERENCE.newarrayCode() == -1, "REFERENCE code -1");
        check(TypeKind.VOID.newarrayCode() == -1, "VOID code -1");
        // asLoadable：子字（Z/B/C/S）归并为 INT，其余自映射
        check(TypeKind.BOOLEAN.asLoadable() == TypeKind.INT, "BOOLEAN asLoadable INT");
        check(TypeKind.BYTE.asLoadable() == TypeKind.INT, "BYTE asLoadable INT");
        check(TypeKind.CHAR.asLoadable() == TypeKind.INT, "CHAR asLoadable INT");
        check(TypeKind.SHORT.asLoadable() == TypeKind.INT, "SHORT asLoadable INT");
        check(TypeKind.LONG.asLoadable() == TypeKind.LONG, "LONG asLoadable LONG");
        check(TypeKind.FLOAT.asLoadable() == TypeKind.FLOAT, "FLOAT asLoadable FLOAT");
        check(TypeKind.DOUBLE.asLoadable() == TypeKind.DOUBLE, "DOUBLE asLoadable DOUBLE");
        check(TypeKind.REFERENCE.asLoadable() == TypeKind.REFERENCE, "REFERENCE asLoadable REFERENCE");
        // upperBound：原始类型为自身描述符，REFERENCE 上界 Object，VOID 为 V
        check(TypeKind.BOOLEAN.upperBound().equals(ConstantDescs.CD_boolean), "BOOLEAN upperBound Z");
        check(TypeKind.BYTE.upperBound().equals(ConstantDescs.CD_byte), "BYTE upperBound B");
        check(TypeKind.CHAR.upperBound().equals(ConstantDescs.CD_char), "CHAR upperBound C");
        check(TypeKind.SHORT.upperBound().equals(ConstantDescs.CD_short), "SHORT upperBound S");
        check(TypeKind.LONG.upperBound().equals(ConstantDescs.CD_long), "LONG upperBound J");
        check(TypeKind.FLOAT.upperBound().equals(ConstantDescs.CD_float), "FLOAT upperBound F");
        check(TypeKind.DOUBLE.upperBound().equals(ConstantDescs.CD_double), "DOUBLE upperBound D");
        check(TypeKind.REFERENCE.upperBound().equals(ConstantDescs.CD_Object), "REFERENCE upperBound Object");
        check(TypeKind.VOID.upperBound().equals(ConstantDescs.CD_void), "VOID upperBound V");
    }

    // ---- Part 4: fromDescriptor 精确表 + 负例（实测锁定） ----
    static void testFromDescriptor() {
        check(TypeKind.fromDescriptor("I") == TypeKind.INT, "I->INT");
        check(TypeKind.fromDescriptor("J") == TypeKind.LONG, "J->LONG");
        check(TypeKind.fromDescriptor("D") == TypeKind.DOUBLE, "D->DOUBLE");
        check(TypeKind.fromDescriptor("Z") == TypeKind.BOOLEAN, "Z->BOOLEAN");
        check(TypeKind.fromDescriptor("C") == TypeKind.CHAR, "C->CHAR");
        check(TypeKind.fromDescriptor("S") == TypeKind.SHORT, "S->SHORT");
        check(TypeKind.fromDescriptor("B") == TypeKind.BYTE, "B->BYTE");
        check(TypeKind.fromDescriptor("F") == TypeKind.FLOAT, "F->FLOAT");
        check(TypeKind.fromDescriptor("Ljava/lang/String;") == TypeKind.REFERENCE, "L->REFERENCE");
        check(TypeKind.fromDescriptor("V") == TypeKind.VOID, "V->VOID");
        // 数组描述符 → REFERENCE
        check(TypeKind.fromDescriptor("[I") == TypeKind.REFERENCE, "[I->REFERENCE");
        check(TypeKind.fromDescriptor("[Ljava/lang/String;") == TypeKind.REFERENCE, "[L->REFERENCE");
        check(TypeKind.fromDescriptor("[[J") == TypeKind.REFERENCE, "[[J->REFERENCE");
        // 宽松点：非原始描述符只看首字符（"Ljava/lang/String" 缺分号也判 REFERENCE；"JJ" 判 LONG）
        check(TypeKind.fromDescriptor("Ljava/lang/String") == TypeKind.REFERENCE, "missing-semicolon -> REFERENCE");
        check(TypeKind.fromDescriptor("JJ") == TypeKind.LONG, "JJ -> LONG (first char wins)");
        // 负例：未知字符 → IAE "Bad type: X"（实测锁定，非 ArrayIndexOutOfBounds）
        try {
            TypeKind.fromDescriptor("X");
            check(false, "fromDescriptor(X) must throw");
        } catch (IllegalArgumentException e) {
            check("Bad type: X".equals(e.getMessage()), "fromDescriptor(X) IAE message: " + e.getMessage());
        }
        try {
            TypeKind.fromDescriptor("");
            check(false, "fromDescriptor(empty) must throw");
        } catch (IllegalArgumentException e) {
            check("Empty descriptor".equals(e.getMessage()), "fromDescriptor(empty) IAE message");
        }
        try {
            TypeKind.fromDescriptor("(");
            check(false, "fromDescriptor('(') must throw");
        } catch (IllegalArgumentException e) {
            check("Bad type: (".equals(e.getMessage()), "fromDescriptor('(') IAE message");
        }
        // null → NPE（在 CharSequence.isEmpty() 上触发，消息含 s）
        try {
            TypeKind.fromDescriptor(null);
            check(false, "fromDescriptor(null) must throw");
        } catch (NullPointerException e) {
            check(e.getMessage() != null && e.getMessage().contains("\"s\" is null"),
                    "fromDescriptor(null) NPE message: " + e.getMessage());
        }
    }

    // ---- Part 5: newarrayCode/fromNewarrayCode 双射（4..11 全表 + 非法码负例） ----
    static void testNewarrayBijection() {
        TypeKind[] byCode = {null, null, null, null, TypeKind.BOOLEAN, TypeKind.CHAR, TypeKind.FLOAT,
                TypeKind.DOUBLE, TypeKind.BYTE, TypeKind.SHORT, TypeKind.INT, TypeKind.LONG};
        boolean bijection = true;
        for (int code = 4; code <= 11; code++)
            if (TypeKind.fromNewarrayCode(code) != byCode[code]
                    || TypeKind.fromNewarrayCode(code).newarrayCode() != code) bijection = false;
        check(bijection, "fromNewarrayCode x newarrayCode bijection over codes 4..11");
        for (int bad : new int[]{-1, 0, 1, 2, 3, 12, 13, Integer.MAX_VALUE}) {
            try {
                TypeKind.fromNewarrayCode(bad);
                check(false, "fromNewarrayCode(" + bad + ") must throw");
            } catch (IllegalArgumentException e) {
                check(("Bad newarray code: " + bad).equals(e.getMessage()),
                        "fromNewarrayCode(" + bad + ") IAE message");
            }
        }
    }

    // ---- Part 6: from(OfField) 全表 + null 负例 ----
    static void testFromDescriptorOfField() {
        check(TypeKind.from(ConstantDescs.CD_boolean) == TypeKind.BOOLEAN, "from(Z)");
        check(TypeKind.from(ConstantDescs.CD_byte) == TypeKind.BYTE, "from(B)");
        check(TypeKind.from(ConstantDescs.CD_char) == TypeKind.CHAR, "from(C)");
        check(TypeKind.from(ConstantDescs.CD_short) == TypeKind.SHORT, "from(S)");
        check(TypeKind.from(ConstantDescs.CD_int) == TypeKind.INT, "from(I)");
        check(TypeKind.from(ConstantDescs.CD_long) == TypeKind.LONG, "from(J)");
        check(TypeKind.from(ConstantDescs.CD_float) == TypeKind.FLOAT, "from(F)");
        check(TypeKind.from(ConstantDescs.CD_double) == TypeKind.DOUBLE, "from(D)");
        check(TypeKind.from(ConstantDescs.CD_String) == TypeKind.REFERENCE, "from(String)");
        check(TypeKind.from(ConstantDescs.CD_Object) == TypeKind.REFERENCE, "from(Object)");
        check(TypeKind.from(ConstantDescs.CD_void) == TypeKind.VOID, "from(void)");
        check(TypeKind.from(ClassDesc.ofDescriptor("[I")) == TypeKind.REFERENCE, "from([I)");
        check(TypeKind.from(ClassDesc.of("java.util.List")) == TypeKind.REFERENCE, "from(List)");
        // from(Class) 重载也接受 Class 实例（OfField<?>）
        check(TypeKind.from(Integer.class) == TypeKind.REFERENCE, "from(Integer.class)");
        check(TypeKind.from(int.class) == TypeKind.INT, "from(int.class)");
        check(TypeKind.from(long.class) == TypeKind.LONG, "from(long.class)");
        check(TypeKind.from(void.class) == TypeKind.VOID, "from(void.class)");
        try {
            TypeKind.from((java.lang.constant.ClassDesc) null);
            check(false, "from(null) must throw");
        } catch (NullPointerException e) {
            check(e.getMessage() != null && e.getMessage().contains("\"descriptor\" is null"),
                    "from(null) NPE message: " + e.getMessage());
        }
    }

    // ---- Part 7: 发射 newarray 回读 NewPrimitiveArrayInstruction（真闭环） ----
    static void testNewarrayEmission() throws Exception {
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.TkNA"), cb -> cb
                .withSuperclass(ClassDesc.of("java.lang.Object"))
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()[I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        cob -> cob.iconst_5().newarray(TypeKind.INT).areturn()));
        boolean found = false;
        for (CodeElement e : (CodeAttribute) ClassFile.of().parse(b).methods().get(0).code().get())
            if (e instanceof NewPrimitiveArrayInstruction np) {
                found = true;
                check(np.typeKind() == TypeKind.INT, "readback typeKind INT");
                check(np.opcode() == Opcode.NEWARRAY, "readback opcode NEWARRAY");
                check(np instanceof Instruction, "NewPrimitiveArrayInstruction is Instruction");
            }
        check(found, "newarray(TypeKind.INT) emission readback found");
        // 双精度数组同型验证
        byte[] b2 = ClassFile.of().build(ClassDesc.of("pkg.TkND"), cb -> cb
                .withSuperclass(ClassDesc.of("java.lang.Object"))
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()[D"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        cob -> cob.iconst_2().newarray(TypeKind.DOUBLE).areturn()));
        boolean found2 = false;
        for (CodeElement e : (CodeAttribute) ClassFile.of().parse(b2).methods().get(0).code().get())
            if (e instanceof NewPrimitiveArrayInstruction np) {
                found2 = true;
                check(np.typeKind() == TypeKind.DOUBLE, "readback typeKind DOUBLE");
            }
        check(found2, "newarray(TypeKind.DOUBLE) emission readback found");
    }

    // ---- Part 8: slotSize 与真实局部变量双槽交叉验证（maxLocals） ----
    static void testSlotSizeEmission() throws Exception {
        // static (JI)J：long 占 slot 0-1，int 占 slot 2 → maxLocals=3（若 long 占 1 槽则为 2）
        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.TkL1"), cb -> cb
                .withSuperclass(ClassDesc.of("java.lang.Object"))
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("(JI)J"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        cob -> cob.lload(0).lreturn()));
        CodeAttribute ca = (CodeAttribute) ClassFile.of().parse(b).methods().get(0).code().get();
        check(ca.maxLocals() == 3, "maxLocals(J,I)=3 (long spans 2 slots)");
        // 对照组 static (II)I → maxLocals=2
        byte[] b2 = ClassFile.of().build(ClassDesc.of("pkg.TkL2"), cb -> cb
                .withSuperclass(ClassDesc.of("java.lang.Object"))
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("(II)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        cob -> cob.iload(0).iload(1).iadd().ireturn()));
        check(((CodeAttribute) ClassFile.of().parse(b2).methods().get(0).code().get()).maxLocals() == 2,
                "maxLocals(I,I)=2 control group");
        // (DD)D：两个 double 各 2 槽 → maxLocals=4
        byte[] b3 = ClassFile.of().build(ClassDesc.of("pkg.TkL3"), cb -> cb
                .withSuperclass(ClassDesc.of("java.lang.Object"))
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("(DD)D"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        cob -> cob.dload(0).dreturn()));
        check(((CodeAttribute) ClassFile.of().parse(b3).methods().get(0).code().get()).maxLocals() == 4,
                "maxLocals(D,D)=4 (double spans 2 slots)");
        // slotSize 求和与 maxLocals 一致的通用交叉：所有 TypeKind 按描述符发射统计
        int sum = 0;
        for (TypeKind t : TypeKind.values()) sum += t.slotSize();
        check(sum == 11, "slotSize sum over all 10 kinds == 11 (1+1+1+1+1+2+1+2+1+0)");
    }

    // ---- Part 9: 枚举语义（valueOf 负例 + 防御拷贝） ----
    static void testEnumSemantics() {
        check(TypeKind.valueOf("REFERENCE") == TypeKind.REFERENCE, "valueOf REFERENCE");
        check(TypeKind.valueOf("VOID") == TypeKind.VOID, "valueOf VOID");
        try {
            TypeKind.valueOf("NOT_A_TYPE");
            check(false, "TypeKind.valueOf unknown must throw");
        } catch (IllegalArgumentException e) {
            check("No enum constant java.lang.classfile.TypeKind.NOT_A_TYPE".equals(e.getMessage()),
                    "TypeKind.valueOf unknown IAE message");
        }
        check(TypeKind.values() != TypeKind.values(), "values() defensive copy");
        // ordinal 顺序 = 声明顺序 BOOLEAN..VOID
        check(TypeKind.BOOLEAN.ordinal() == 0 && TypeKind.VOID.ordinal() == 9, "ordinal ends");
        boolean ordered = true;
        TypeKind[] vs = TypeKind.values();
        for (int i = 1; i < vs.length; i++) if (vs[i - 1].compareTo(vs[i]) >= 0) ordered = false;
        check(ordered, "ordinals strictly ascending");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFullTables();
        testFromDescriptor();
        testNewarrayBijection();
        testFromDescriptorOfField();
        testNewarrayEmission();
        testSlotSizeEmission();
        testEnumSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

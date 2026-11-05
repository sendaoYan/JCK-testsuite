# java.lang.classfile.Opcode — 设计案

## 1. API 功能
`java.lang.classfile.Opcode`（enum）。工厂/静态方法：values, valueOf。查询/实例方法：bytecode, isWide, sizeIfFixed, kind。枚举常量：NOP, ACONST_NULL, ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5, LCONST_0, LCONST_1, FCONST_0, FCONST_1, FCONST_2, DCONST_0, DCONST_1, BIPUSH, SIPUSH, LDC, LDC_W, LDC2_W, ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, ILOAD_0, ILOAD_1, ILOAD_2, ILOAD_3, LLOAD_0, LLOAD_1, LLOAD_2, LLOAD_3, FLOAD_0, FLOAD_1, FLOAD_2, FLOAD_3, DLOAD_0, DLOAD_1, DLOAD_2, DLOAD_3, ALOAD_0, ALOAD_1, ALOAD_2, ALOAD_3, IALOAD, LALOAD, FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD, ISTORE, LSTORE, FSTORE, DSTORE, ASTORE, ISTORE_0, ISTORE_1, ISTORE_2, ISTORE_3, LSTORE_0, LSTORE_1, LSTORE_2, LSTORE_3, FSTORE_0, FSTORE_1, FSTORE_2, FSTORE_3, DSTORE_0, DSTORE_1, DSTORE_2, DSTORE_3, ASTORE_0, ASTORE_1, ASTORE_2, ASTORE_3, IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE, SASTORE, POP, POP2, DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2, SWAP, IADD, LADD, FADD, DADD, ISUB, LSUB, FSUB, DSUB, IMUL, LMUL, FMUL, DMUL, IDIV, LDIV, FDIV, DDIV, IREM, LREM, FREM, DREM, INEG, LNEG, FNEG, DNEG, ISHL, LSHL, ISHR, LSHR, IUSHR, LUSHR, IAND, LAND, IOR, LOR, IXOR, LXOR, IINC, I2L, I2F, I2D, L2I, L2F, L2D, F2I, F2L, F2D, D2I, D2L, D2F, I2B, I2C, I2S, LCMP, FCMPL, FCMPG, DCMPL, DCMPG, IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE, GOTO, JSR, RET, TABLESWITCH, LOOKUPSWITCH, IRETURN, LRETURN, FRETURN, DRETURN, ARETURN, RETURN, GETSTATIC, PUTSTATIC, GETFIELD, PUTFIELD, INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC, INVOKEINTERFACE, INVOKEDYNAMIC, NEW, NEWARRAY, ANEWARRAY, ARRAYLENGTH, ATHROW, CHECKCAST, INSTANCEOF, MONITORENTER, MONITOREXIT, MULTIANEWARRAY, IFNULL, IFNONNULL, GOTO_W, JSR_W, ILOAD_W, LLOAD_W, FLOAD_W, DLOAD_W, ALOAD_W, ISTORE_W, LSTORE_W, FSTORE_W, DSTORE_W, ASTORE_W, RET_W, IINC_W

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；枚举通过 `valueOf`/`values` 与常量精确断言。 本测试在 `OpcodeTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.Opcode` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`OpcodeTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.Opcode")` + public/enum/继承 + 关键成员。黑盒（`OpcodeTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 深化条目（2026-11-05 契约深化，23 → 215 断言）
- `testBytecodeTable`：57 项 bytecode() 精确表（NOP=0x00 … GOTO_W=0xC8/GOTO_W/JSR_W，全按 JVMS+JDK-27 实测）。
- `testBytecodeBijection`：213 个 bytecode() 全互异（Set 大小 213）；**宽型编码实测锁定：`ILOAD_W.bytecode()==0xC415` 即 `(0xC4<<8)|base`**（枚举无独立 WIDE 常量），12 个 `_W` 全族断言 X_W==(WIDE<<8)|X；非宽型值域 [0,0xC9]。
- `testKindTable`：36 个知名 opcode 的 kind() 精确分类（facts §4 的 30 项 + LADD/ASTORE/IALOAD/DUP/SWAP/ICONST_2 等扩展）+ 遍历 213 个 kind() 非 null + **26 类完整计数表**（LOAD=30/STORE=30/OPERATOR=42/CONSTANT=20/BRANCH=18/CONVERT=15/STACK=9/ARRAY_LOAD=8/ARRAY_STORE=8/RETURN=6/FIELD_ACCESS=4/INVOKE=4/INCREMENT=2/TYPE_CHECK=2/MONITOR=2/DISCONTINUED_*=2×2/九个单例=1）+ 计数和==213。
- `testSizeTable`：sizeIfFixed 精确表（单字节=1、BIPUSH/LDC/ILOAD/RET=2、SIPUSH/LDC_W/LDC2_W/IINC/GOTO/IF比较/字段/调用/NEW=3、MULTIANEWARRAY=4、INVOKEINTERFACE/INVOKEDYNAMIC/GOTO_W/JSR_W=5、IINC_W=6、TABLESWITCH/LOOKUPSWITCH=-1 变长哨兵）+ **全域分布 {-1:2, 1:147, 2:14, 3:33, 4:12, 5:4, 6:1}** + 遍历 >=-1。
- `testWideFamily`：**GOTO_W/JSR_W 是独立操作码（0xC8/0xC9），isWide()==false**（易错！与 facts 预期相反）；恰 12 个 isWide==true（loads/stores×10+RET_W+IINC_W）；LDC_W/LDC2_W 名带 _W 但非 wide。
- `testEnumSemantics`：valueOf 不存在名 → IAE 精确消息 `No enum constant java.lang.classfile.Opcode.NOT_AN_OPCODE`；valueOf(null) → NPE "Name is null"；values() 防御拷贝；ordinal 头部 0/1/2。
- `testEmissionCrossCheck`：真实发射-回读 8 指令序列（ICONST_2,LDC,POP,ICONST_3,IMUL,GETSTATIC,POP,IRETURN 的 opcode + sizeInBytes 双一致）；ldc(long) 自动 LDC2_W；iinc(256,1)/iload(256) 自动升级 IINC_W/ILOAD_W。

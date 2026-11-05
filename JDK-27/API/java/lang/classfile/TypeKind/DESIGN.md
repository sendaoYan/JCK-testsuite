# java.lang.classfile.TypeKind — 设计案

## 1. API 功能
`java.lang.classfile.TypeKind`（enum）。工厂/静态方法：values, valueOf, fromNewarrayCode, fromDescriptor, from。查询/实例方法：upperBound, newarrayCode, slotSize, asLoadable。枚举常量：BOOLEAN, BYTE, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE, REFERENCE, VOID

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；枚举通过 `valueOf`/`values` 与常量精确断言。 本测试在 `TypeKindTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.TypeKind` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`TypeKindTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.TypeKind")` + public/enum/继承 + 关键成员。黑盒（`TypeKindTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 深化条目（2026-11-05 契约深化，23 → 115 断言）
- `testFullTables`：slotSize 全表（BOOLEAN/BYTE/CHAR/SHORT/FLOAT=1, LONG/DOUBLE=2, VOID=0）；newarrayCode 全表（BOOLEAN=4,CHAR=5,FLOAT=6,DOUBLE=7,BYTE=8,SHORT=9,INT=10,LONG=11, **REFERENCE/VOID=-1 哨兵**）；asLoadable 全表（**子字 Z/B/C/S→INT 归并**，LONG/FLOAT/DOUBLE/REFERENCE/VOID 自映射）；upperBound 全表（原始类型=自身描述符，REFERENCE→Ljava/lang/Object;，VOID→V）。
- `testFromDescriptor`：10 基本描述符精确表 + 数组 `[I`/`[Ljava/lang/String;`/`[[J`→REFERENCE + **宽松点实测**（"Ljava/lang/String" 缺分号仍 REFERENCE、"JJ" 只看首字符判 LONG）；负例精确锁定：`"X"`/`"("` → IAE `Bad type: X`、`""` → IAE `Empty descriptor`、null → NPE（消息含 `"s" is null`）。
- `testNewarrayBijection`：fromNewarrayCode×newarrayCode 在 4..11 上双射遍历；非法码 {-1,0,1,2,3,12,13,MAX_VALUE} 全部 IAE `Bad newarray code: N`。
- `testFromDescriptorOfField`：from(ClassDesc) 13 项全表 + from(Class) 重载（int.class→INT、void.class→VOID、Integer.class→REFERENCE）+ from(null) → NPE（消息含 `"descriptor" is null`）。
- `testNewarrayEmission`：发射 `newarray(TypeKind.INT/DOUBLE)` 回读 NewPrimitiveArrayInstruction **typeKind()**（访问器名实测：不是 componentType）+ opcode==NEWARRAY。
- `testSlotSizeEmission`：长局部变量双槽交叉验证——static `(JI)J` maxLocals==3 vs `(II)I`==2 vs `(DD)D`==4（CodeAttribute 上）；slotSize 十值总和==11。
- `testEnumSemantics`：valueOf 负例 IAE 精确消息、防御拷贝、ordinal 递增（BOOLEAN=0..VOID=9）。

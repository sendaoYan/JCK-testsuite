# java.lang.classfile.instruction.StoreInstruction — 设计案

## 1. API 功能
`java.lang.classfile.instruction.StoreInstruction`（interface）。工厂/静态方法：of。查询/实例方法：slot, typeKind。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `StoreInstructionTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.instruction.StoreInstruction` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`StoreInstructionTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.instruction.StoreInstruction")` + public/interface/继承 + 关键成员。黑盒（`StoreInstructionTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（发射-回读矩阵批）
- 8 存储表：istore(0..3)→ISTORE_0..3 size 1、lstore/fstore/dstore(1)→*_1、astore(4)→ASTORE size 2（无 ASTORE_4 实测）；slot()/typeKind() 逐条精确；wide 自动 istore(300)/lstore(300)→ISTORE_W/LSTORE_W size 4；storeLocal(TypeKind,slot) 泛型通道。
- 工厂：of(TypeKind,slot) 全表 + of(SHORT,1) 窄化 ISTORE_1 + of(LONG,300)→LSTORE_W + of(VOID,0) IAE + of(INT,-1) IAE + of(ISTORE,300) IAE（Opcode 重载不自动 wide）+ null NPE。断言 11→73（实测运行计数）。

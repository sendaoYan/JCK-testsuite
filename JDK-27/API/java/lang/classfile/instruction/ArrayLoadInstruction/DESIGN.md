# java.lang.classfile.instruction.ArrayLoadInstruction — 设计案

## 1. API 功能
`java.lang.classfile.instruction.ArrayLoadInstruction`（interface）。工厂/静态方法：of。查询/实例方法：typeKind。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `ArrayLoadInstructionTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.instruction.ArrayLoadInstruction` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`ArrayLoadInstructionTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.instruction.ArrayLoadInstruction")` + public/interface/继承 + 关键成员。黑盒（`ArrayLoadInstructionTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（发射-回读矩阵批）
- testEmitReadback：8 opcode 全族发射（iaload/laload/faload/daload/aaload/baload/caload/saload，CodeBuilder 快捷方法）→ parse 回读 instanceof ArrayLoadInstruction，逐条断言 opcode/sizeInBytes(1)/kind==ARRAY_LOAD/typeKind 精确对应；arrayLoad(TypeKind) 泛型通道同表断言。
- 工厂矩阵：ArrayLoadInstruction.of(Opcode) 8 个正例 + of(null) NPE + of(NOP/GETSTATIC) IAE（错误 kind）。
- 分类断言：instanceof Instruction/CodeElement，非 PseudoInstruction。断言 10→70（实测运行计数）。

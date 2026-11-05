# java.lang.classfile.instruction.ArrayStoreInstruction — 设计案

## 1. API 功能
`java.lang.classfile.instruction.ArrayStoreInstruction`（interface）。工厂/静态方法：of。查询/实例方法：typeKind。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `ArrayStoreInstructionTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.instruction.ArrayStoreInstruction` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`ArrayStoreInstructionTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.instruction.ArrayStoreInstruction")` + public/interface/继承 + 关键成员。黑盒（`ArrayStoreInstructionTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（发射-回读矩阵批）
- testEmitReadback：8 opcode 全族（iastore..sastore）发射回读，opcode/sizeInBytes(1)/kind==ARRAY_STORE/typeKind 精确；aastore 引用族 + arrayStore(TypeKind) 泛型通道；javac 自产物（本测试类 int[]/Object[] 辅助方法）回读对照。
- 工厂矩阵：of(Opcode) 8 正例 + of(null) NPE + 非 ARRAY_STORE kind IAE。断言 9→62（实测运行计数）。

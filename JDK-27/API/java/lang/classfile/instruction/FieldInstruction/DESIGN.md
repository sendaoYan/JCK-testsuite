# java.lang.classfile.instruction.FieldInstruction — 设计案

## 1. API 功能
`java.lang.classfile.instruction.FieldInstruction`（interface）。工厂/静态方法：of。查询/实例方法：field, owner, name, type, typeSymbol。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `FieldInstructionTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.instruction.FieldInstruction` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`FieldInstructionTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.instruction.FieldInstruction")` + public/interface/继承 + 关键成员。黑盒（`FieldInstructionTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（发射-回读矩阵批）
- 4 opcode 发射回读：getstatic/getfield/putstatic/putfield（自建类双通道：get 用本类静态字段 + 实例字段）；opcode/sizeInBytes(3)/kind==FIELD_ACCESS/owner/name/type 全等；fieldAccess(TypeKind) 泛型通道。
- 工厂 3 重载（opcode+ClassDesc+name+MethodTypeDesc / opcode+MemberRefEntry / 直接 Field_ref）身份断言；of(null,..) NPE、非 FIELD_ACCESS kind IAE（消息含 FIELD_ACCESS）。断言 13→48（实测运行计数）。

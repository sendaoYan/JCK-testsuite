# java.lang.classfile.instruction.TableSwitchInstruction — 设计案

## 1. API 功能
`java.lang.classfile.instruction.TableSwitchInstruction`（interface）。工厂/静态方法：of。查询/实例方法：lowValue, highValue, defaultTarget, cases。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `TableSwitchInstructionTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.instruction.TableSwitchInstruction` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`TableSwitchInstructionTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.instruction.TableSwitchInstruction")` + public/interface/继承 + 关键成员。黑盒（`TableSwitchInstructionTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（发射-回读矩阵批）
- 访问器实名 lowValue()/highValue()（任务提示）；发射-回读：tableswitch(0,2,def,3 cases) → low 0/high 2/cases 键 [0,1,2]（high-low+1 完整表）/sizeInBytes 27（bci1 起 padding 规则）/default bci>case0 bci；负 low(-1,0) 变体；自动推导重载 tableswitch(Label,List) → low/high 由 cases 推导（5-6 实测）。
- javac dense switch 自产物（case 0/1/2 → TABLESWITCH 非 LOOKUPSWITCH）；of() 工厂身份 + of(2,0,..) low>high IAE + of(0,1,null,null) NPE。断言 13→36（实测运行计数）。

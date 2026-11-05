# java.lang.classfile.ClassFile.DeadLabelsOption — 设计案

## 1. API 功能
`java.lang.classfile.ClassFile.DeadLabelsOption`（enum）。工厂/静态方法：values, valueOf。查询/实例方法：(none)。枚举常量：FAIL_ON_DEAD_LABELS, DROP_DEAD_LABELS

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；枚举通过 `valueOf`/`values` 与常量精确断言。 本测试在 `DeadLabelsOptionTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.ClassFile.DeadLabelsOption` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`DeadLabelsOptionTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.ClassFile$DeadLabelsOption")` + public/enum/继承 + 关键成员。黑盒（`DeadLabelsOptionTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 深化条目（2026-11-05：死标签两态实测，JDK-8361908 片段族）
新增 `testOptionSemantics()`（46 断言，12→46）：
- **FAIL_ON_DEAD_LABELS（默认）**：8 个死标签片段（exceptionCatchAll 三位置死 label ×3、localVariable 死 label ×2、localVariableType 死 label ×2、characterRange 死 label ×1，对齐 FilterDeadLabelsTest.deadLabelFragments）逐一 build → 全部 IAE；exception handler 死标签消息精确 **"Unbound label in exception handler"**。
- **DROP_DEAD_LABELS**：同一批片段合并进一个 build 成功；产物：exceptionHandlers 空；LVT/LVTy/CRT **属性仍写但行清空**；verify 干净。
- **混合场景**：live handler（bound label）+ dead handler 共存 → 只留 live（handlers==1，catchType=Optional.empty() 即 any）。
- **不可救场景（实测边界）**：分支跳到从未绑定的 label 在**两种选项下都失败**（IAE "Detected branch target out of bytecode range"）——DROP 只能丢调试伪指令里的死标签，救不了悬空跳转目标。
- 枚举 ordinal 0 = FAIL_ON_DEAD_LABELS（默认）；valueOf 双向。

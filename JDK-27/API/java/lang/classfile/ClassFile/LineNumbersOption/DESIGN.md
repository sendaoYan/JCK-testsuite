# java.lang.classfile.ClassFile.LineNumbersOption — 设计案

## 1. API 功能
`java.lang.classfile.ClassFile.LineNumbersOption`（enum）。工厂/静态方法：values, valueOf。查询/实例方法：(none)。枚举常量：PASS_LINE_NUMBERS, DROP_LINE_NUMBERS

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；枚举通过 `valueOf`/`values` 与常量精确断言。 本测试在 `LineNumbersOptionTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.ClassFile.LineNumbersOption` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`LineNumbersOptionTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.ClassFile$LineNumbersOption")` + public/enum/继承 + 关键成员。黑盒（`LineNumbersOptionTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 深化条目（2026-11-05：DROP_LINE_NUMBERS 语义实测矩阵）
新增 `testOptionSemantics()`（33 断言，12→33）：
- **parse 侧 DROP_LINE_NUMBERS**：剥 LineNumber 伪指令（1→0），**保留 LocalVariable**（归 DebugElementsOption 管）；LNT/LVT **属性保留**；指令流完好。
- **build 侧不生效**（与 DebugElementsOption 的关键差异）：`withOptions(DROP_LINE_NUMBERS).build(...)` 产物里 **LNT 属性照样写入**、LineNumber 照样回读（line 9）——实测此选项只在解析路径消费（ClassFileImpl.passLineNumbers() 仅 CodeImpl.inflateMetadata 调用）。
- 正交组合：DROP_LINE_NUMBERS + DROP_DEBUG 把两类伪指令全剥。
- 枚举：ordinal 0 = PASS_LINE_NUMBERS；valueOf 双向；withOptions 返回新实例。
- 易错点：不能照搬 DROP_DEBUG 的 build 侧行为——两选项 build 侧生效性不同（实测）。

# java.lang.classfile.ClassFile.DebugElementsOption — 设计案

## 1. API 功能
`java.lang.classfile.ClassFile.DebugElementsOption`（enum）。工厂/静态方法：values, valueOf。查询/实例方法：(none)。枚举常量：PASS_DEBUG, DROP_DEBUG

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；枚举通过 `valueOf`/`values` 与常量精确断言。 本测试在 `DebugElementsOptionTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.ClassFile.DebugElementsOption` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`DebugElementsOptionTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.ClassFile$DebugElementsOption")` + public/enum/继承 + 关键成员。黑盒（`DebugElementsOptionTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 深化条目（2026-11-05：DROP_DEBUG 语义实测矩阵）
新增 `testOptionSemantics()`（44 断言，13→44）。发射 localVariable+localVariableType+characterRange+lineNumber 四种伪指令后按选项解析，实测锁定：
- **parse 侧 DROP_DEBUG**：剥 LocalVariable/LocalVariableType/CharacterRange 伪指令（1→0），**保留 LineNumber 伪指令**（归 LineNumbersOption 管）；LVT/LVTy/CRT/LNT 四个**属性全部保留**（选项只影响元素模型不动原始属性）。
- **build 侧 DROP_DEBUG 也生效**：重新 build 时 LVT/LVTy/CRT 属性**根本不写**（findAttribute 为 empty），但 **LNT 属性仍写**；伪指令不再发射。
- 属性内容精确回读：LVT 行 slot=1/name=lv/type=I；LNT 行 lineNumber=9。
- PASS_DEBUG 与默认解析计数完全一致；链式 `DROP_DEBUG→PASS_DEBUG` last-wins 实测覆盖。
- 易错点：误以为 DROP_DEBUG 连 LineNumber 一起剥——实测不剥；误以为属性被剥——parse 侧属性保留，只有 build 侧才不写。

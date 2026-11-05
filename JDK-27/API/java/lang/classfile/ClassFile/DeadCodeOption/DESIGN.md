# java.lang.classfile.ClassFile.DeadCodeOption — 设计案

## 1. API 功能
`java.lang.classfile.ClassFile.DeadCodeOption`（enum）。工厂/静态方法：values, valueOf。查询/实例方法：(none)。枚举常量：PATCH_DEAD_CODE, KEEP_DEAD_CODE

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；枚举通过 `valueOf`/`values` 与常量精确断言。 本测试在 `DeadCodeOptionTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.ClassFile.DeadCodeOption` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`DeadCodeOptionTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.ClassFile$DeadCodeOption")` + public/enum/继承 + 关键成员。黑盒（`DeadCodeOptionTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 深化条目（2026-11-05：PATCH vs KEEP 死代码两态实测）
新增 `testOptionSemantics()`（31 断言，12→31）。死代码样本 = `return_().return_()`（第二个 RETURN 不可达）：
- **PATCH_DEAD_CODE（默认）**：直接 build 成功，死区被改写——实测 ops=[RETURN, ATHROW]（死 RETURN 被 patch 成 ATHROW 以便生成栈帧）；SMT 生成、verify 干净。
- **KEEP_DEAD_CODE**：直接 build 抛 IAE，消息精确含 **"Unable to generate stack map frame for dead code at bytecode offset 1 of method twoReturns()"** + 反汇编 dump（对齐 JDK StackMapsTest.testDeadCodePatternFail）。
- **KEEP + DROP_STACK_MAPS 组合**：build 成功（无需生成帧），两 RETURN **原样保留**（ops=[RETURN,RETURN]）、无 SMT；verify 报缺帧非空。
- **重放重建（replay）**：parse(KEEP+DROP 产物)→`build(desc, cb -> model.forEach(cb))`——默认 PATCH 重放成功且 verify 干净；KEEP 重放抛同款 IAE（对齐 StackMapsTest.testTransformedStackMaps 模式）。
- 链式 last-wins：PATCH→KEEP 仍失败；KEEP→DROP_STACK_MAPS 成功。
- 易错点：KEEP 并不「静默保留并成功」——v71 需要栈帧时直接失败；必须配 DROP_STACK_MAPS 才能保留死码通过 build。

# java.lang.classfile.attribute.CharacterRangeInfo — 设计案

## 1. API 功能
`java.lang.classfile.attribute.CharacterRangeInfo`（interface）。工厂/静态方法：of。查询/实例方法：startPc, endPc, characterRangeStart, characterRangeEnd, flags。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `CharacterRangeInfoTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.attribute.CharacterRangeInfo` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`CharacterRangeInfoTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.attribute.CharacterRangeInfo")` + public/interface/继承 + 关键成员。黑盒（`CharacterRangeInfoTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 深化条目（2026-11-05 契约深化）
新增维度：of(startPc,endPc,characterRangeStart,characterRangeEnd,flags) 参数序锁定 + 全零/65535 合法边界 + startPc/endPc/flags 三参数 u2 校验 IAE（消息含 "start pc"/"end pc"/"flags"）+ characterRangeStart/End 不校验（u4 语义，可载任意 int 含负值）+ CodeBuilder.characterRange 发射 → CRT 属性回读。
易错点（实测）：JDK-27 的 CharacterRangeInfo 没有 FLAG_* 常量（旧预览有）；CRT endPc 是「最后一条被覆盖指令的偏移」（含端点），单 return_ 体为 0-0。

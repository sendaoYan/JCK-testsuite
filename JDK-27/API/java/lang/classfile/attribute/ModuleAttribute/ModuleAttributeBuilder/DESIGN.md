# java.lang.classfile.attribute.ModuleAttribute.ModuleAttributeBuilder — 设计案

## 1. API 功能
`java.lang.classfile.attribute.ModuleAttribute.ModuleAttributeBuilder`（interface）。工厂/静态方法：(none)。查询/实例方法：moduleName, moduleFlags, moduleVersion, requires, exports, opens, uses, provides。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `ModuleAttributeBuilderTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.attribute.ModuleAttribute.ModuleAttributeBuilder` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`ModuleAttributeBuilderTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.attribute.ModuleAttribute$ModuleAttributeBuilder")` + public/interface/继承 + 关键成员。黑盒（`ModuleAttributeBuilderTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 深化条目（2026-11-05 契约深化）
新增维度：8 个链式方法全部返回 self（moduleName/moduleFlags/moduleVersion/requires/exports/opens/uses/provides，二段回调内 == 捕获断言）+ moduleName 后写覆盖 + requires/exports/opens/uses/provides 多值累积 + 每方法三重载（int 版 / AccessFlag 集合版 / Info 对象版）等价断言 + uses 双通道（ClassDesc/ClassEntry）+ provides 双通道（varargs/ModuleProvideInfo）+ 空 builder → 最小 module-info + buildModule→parse 端到端全表回读。

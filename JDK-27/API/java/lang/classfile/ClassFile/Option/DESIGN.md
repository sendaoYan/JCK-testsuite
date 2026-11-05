# java.lang.classfile.ClassFile.Option — 设计案

## 1. API 功能
`java.lang.classfile.ClassFile.Option`（interface）。工厂/静态方法：(none)。查询/实例方法：(none)。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `OptionTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.ClassFile.Option` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`OptionTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.ClassFile$Option")` + public/interface/继承 + 关键成员。黑盒（`OptionTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 深化条目（2026-11-05：选项体系与工厂语义实测）
新增 `testOptionHierarchy()`/`testFactorySemantics()`（76 断言，7→76）：
- **层次断言**：`ClassFile$Option` 的具体实现共 **10 个**（8 枚举 DebugElementsOption/LineNumbersOption/StackMapsOption/AttributesProcessingOption/DeadLabelsOption/DeadCodeOption/ShortJumpsOption/ConstantPoolSharingOption + 2 接口 AttributeMapperOption/ClassHierarchyResolverOption），逐一 `isAssignableFrom` + 编译期 `ClassFile.Option[]` 见证数组断言。
- **枚举面**：8 枚举 values() 计数 {2,2,3,3,2,2,2,2}；全部常量 valueOf(name) 双向；**每个枚举 ordinal 0 即其默认语义值**（PASS_DEBUG/PASS_LINE_NUMBERS/STACK_MAPS_WHEN_REQUIRED/PASS_ALL_ATTRIBUTES/FAIL_ON_DEAD_LABELS/PATCH_DEAD_CODE/FIX_SHORT_JUMPS/SHARED_POOL）——反射 `values()[0]` 逐名核对。
- **工厂语义（实测）**：`ClassFile.of()` 单例（==）；`of(zero-varargs)`/`withOptions()`（空可变参）均返回**新实例**（≠ 单例）；`of((Option[])null)`/`withOptions((Option[])null)` → NPE（消息 "Cannot read the array length because ..."）。空 withOptions 与默认上下文行为等价（build 字节数相同、parse 等价）。
- **链式 last-wins**：`withOptions(DROP_DEBUG).withOptions(PASS_DEBUG)` 实测后者覆盖前者（LVT 属性仍写出）。
- 易错点：不要断言 `withOptions()` 返回同一实例——实测是新实例；`ClassFile.of()` 才是单例。

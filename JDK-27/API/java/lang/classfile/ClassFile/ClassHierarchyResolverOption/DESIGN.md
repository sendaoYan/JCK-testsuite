# java.lang.classfile.ClassFile.ClassHierarchyResolverOption — 设计案

## 1. API 功能
`java.lang.classfile.ClassFile.ClassHierarchyResolverOption`（interface）。工厂/静态方法：of。查询/实例方法：classHierarchyResolver。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `ClassHierarchyResolverOptionTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.ClassFile.ClassHierarchyResolverOption` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`ClassHierarchyResolverOptionTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.ClassFile$ClassHierarchyResolverOption")` + public/interface/继承 + 关键成员。黑盒（`ClassHierarchyResolverOptionTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 深化条目（2026-11-05：resolver 真闭环实测——build 与 verify 双通道）
新增 `testOptionSemantics()`（30 断言，10→30）：
- **of(chr) getter 身份**：`classHierarchyResolver() == chr`（同实例回读）；`of(null)` → NPE。
- **resolver 本体语义**：`ClassHierarchyResolver.of(interfaces, classToSuperClass)` 静态解析器：已知类返回 ClassHierarchyInfoImpl、未知类返回 **null**（不抛）；自动内置 java/lang/Object 条目；defaultResolver 经系统类加载器解析（Object 非 null）；orElse 链回退可用；ClassHierarchyInfo.ofInterface() 与 ofClass(Object) 产出不同实例。
- **build 通道（核心实测）**：构造「checkcast ChrMrgA / checkcast ChrMrgB 在 join 点合并」的方法（合并需计算公共祖先→必须查 resolver）：自定义 chain resolver（MrgB→MrgA→Object）build **成功**；默认 resolver 下同样代码 build **抛 IAE "Could not resolve class ChrMrgA"**。
- **verify 通道**：同一份产物 bytes，三个 resolver 结论不同——chain：0 错；sibling（两者都→Object）：1 个 VerifyError（消息含 "not assignable from"）；default：1 个 VerifyError（消息含 "Could not resolve class"，IAE 被包进 VerifyError 消息）。
- 普通类（无层级依赖代码）任何 resolver 下 verify 均 0 错。
- 易错点：观测 resolver 必须用「两个不同引用类型在分支 join 处合并」的代码（用 checkcast 产生已初始化引用，**不能用 new_ 不调 <init>**——未初始化引用合并永远失败，与 resolver 无关）。

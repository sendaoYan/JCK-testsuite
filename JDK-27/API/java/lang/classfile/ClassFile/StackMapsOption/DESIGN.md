# java.lang.classfile.ClassFile.StackMapsOption — 设计案

## 1. API 功能
`java.lang.classfile.ClassFile.StackMapsOption`（enum）。工厂/静态方法：values, valueOf。查询/实例方法：(none)。枚举常量：STACK_MAPS_WHEN_REQUIRED, GENERATE_STACK_MAPS, DROP_STACK_MAPS

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；枚举通过 `valueOf`/`values` 与常量精确断言。 本测试在 `StackMapsOptionTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.ClassFile.StackMapsOption` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`StackMapsOptionTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.ClassFile$StackMapsOption")` + public/enum/继承 + 关键成员。黑盒（`StackMapsOptionTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 深化条目（2026-11-05：三值语义实测，含 v48 版本差）
新增 `testOptionSemantics()`（39 断言，12→39）：
- **v71 build 矩阵**：直线方法（无分支）三种选项都**无 SMT**；分支方法 WHEN_REQUIRED/GENERATE 有 SMT、DROP_STACK_MAPS 无。
- **WHEN_REQUIRED vs GENERATE 的真实区别在 ≤49 版本**：`withVersion(JAVA_4_VERSION)` build 分支方法，WHEN_REQUIRED 无 SMT（JVMS 不要求）、**GENERATE_STACK_MAPS 照发 SMT**——这是唯一能区分两值的场景（实测锁定）。
- **DROP_STACK_MAPS 三段**：fresh build 省 SMT；parse 侧模型保留属性；transform(ACCEPT_ALL) 拷贝 bound 属性保留。
- **verify 三态**：正常分支产物 0 错；缺 SMT 的分支类 1 个 VerifyError（消息含 "Expecting a stackmap frame at branch target"）；缺 SMT 的直线类仍 0 错（不缺省失败）。
- GENERATE 与 WHEN_REQUIRED 产物帧数相同（entries().size() 相等断言）。
- 易错点：在 v71 上断言 WHEN_REQUIRED≠GENERATE 是错的——两值在最新版产物相同；需降到 v48 才可观测。

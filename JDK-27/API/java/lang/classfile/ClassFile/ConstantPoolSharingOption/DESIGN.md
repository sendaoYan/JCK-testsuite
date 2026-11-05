# java.lang.classfile.ClassFile.ConstantPoolSharingOption — 设计案

## 1. API 功能
`java.lang.classfile.ClassFile.ConstantPoolSharingOption`（enum）。工厂/静态方法：values, valueOf。查询/实例方法：(none)。枚举常量：SHARED_POOL, NEW_POOL

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；枚举通过 `valueOf`/`values` 与常量精确断言。 本测试在 `ConstantPoolSharingOptionTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.ClassFile.ConstantPoolSharingOption` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`ConstantPoolSharingOptionTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.ClassFile$ConstantPoolSharingOption")` + public/enum/继承 + 关键成员。黑盒（`ConstantPoolSharingOptionTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 深化条目（2026-11-05：SHARED_POOL vs NEW_POOL 可观测差实测）
新增 `testOptionSemantics()`（32 断言，12→32）：
- **中性 transform（ACCEPT_ALL）**：两选项产物等价（元素数/成员数/池大小全同）。
- **可观测差**：transform 用 `dropping(FieldModel)` 删字段后——**SHARED_POOL 常量池大小不变**（未用条目保留，canWriteDirect 整池直拷）；**NEW_POOL 池变小**（按需重建，未用条目被丢，实测 10→8，断言 new < shared）。
- 两态产物 thisClass/方法体保持；fresh build 不受选项影响（无源池可共享），verify 均干净。
- 枚举 ordinal 0 = SHARED_POOL（默认）；valueOf 双向。
- 易错点：只有「transform 时删成员」才能看出池大小差；ACCEPT_ALL 下两选项产物不可区分。
- 关联：NEW_POOL 还会强制属性元素重放，是 AttributesProcessingOption 过滤的触发条件（见其 DESIGN §5）。

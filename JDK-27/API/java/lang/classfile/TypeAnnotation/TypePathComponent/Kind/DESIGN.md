# java.lang.classfile.TypeAnnotation.TypePathComponent.Kind — 设计案

## 1. API 功能
`java.lang.classfile.TypeAnnotation.TypePathComponent.Kind`（enum）。工厂/静态方法：values, valueOf。查询/实例方法：tag。枚举常量：ARRAY, INNER_TYPE, WILDCARD, TYPE_ARGUMENT

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；枚举通过 `valueOf`/`values` 与常量精确断言。 本测试在 `KindTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.TypeAnnotation.TypePathComponent.Kind` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`KindTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.TypeAnnotation$TypePathComponent$Kind")` + public/enum/继承 + 关键成员。黑盒（`KindTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（第 2 轮，17 → 55 断言）
- `testEnumMatrix()`：4 值全表——声明顺序 ARRAY/INNER_TYPE/WILDCARD/TYPE_ARGUMENT，ordinal 0..3，`tag()` 依次 0/1/2/3（**4 个 tag 互不相同**，与 JVMS 4.7.20.2 type_path_kind 编码一致）；`valueOf` 双向 + `valueOf("NOPE")` IAE（消息 "No enum constant ..."）+ `valueOf(null)` NPE；Enum 契约（compareTo 顺序、equals/hashCode、getDeclaringClass、toString 含 name）；`values()` 每次新数组且内容等价。
- 互通断言：`Kind` 喂给 `TypePathComponent.of(Kind,int)` 后 `typePathKind()` 精确回读（WILDCARD 验证）。


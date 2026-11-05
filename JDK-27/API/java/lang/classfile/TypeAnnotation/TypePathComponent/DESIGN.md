# java.lang.classfile.TypeAnnotation.TypePathComponent — 设计案

## 1. API 功能
`java.lang.classfile.TypeAnnotation.TypePathComponent`（interface）。工厂/静态方法：of。查询/实例方法：typePathKind, typeArgumentIndex。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `TypePathComponentTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.TypeAnnotation.TypePathComponent` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`TypePathComponentTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.TypeAnnotation$TypePathComponent")` + public/interface/继承 + 关键成员。黑盒（`TypePathComponentTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（第 2 轮，14 → 38 断言）
- `testFactoryMatrix()`：常量身份实测——`of(ARRAY,0)==ARRAY`、`of(INNER_TYPE,0)==INNER_TYPE`、`of(WILDCARD,0)==WILDCARD`（同一常量复用，非新实例）；非 TYPE_ARGUMENT kind 时 index **归零**（`of(WILDCARD,3).typeArgumentIndex()==0`）；`of(null,0)` NPE 消息含 "typePathKind"；TYPE_ARGUMENT 任意 index（0/1/255）回读。
- `testRealProduct()`：真 javac 产物 path 全 kind 矩阵——`List<@A String>`→[TYPE_ARGUMENT 0]；`List<? extends @A Number>`→[TYPE_ARGUMENT 0, WILDCARD]（wildcard 步前必有 TYPE_ARGUMENT 步）；`String @A [] @A []`→[]+[ARRAY]（外/内维度各一条）；`List<@A String[]>`→[TYPE_ARGUMENT 0, ARRAY]；方法返回值外层 []/内层 [TYPE_ARGUMENT 0] 两条；代码级泛型强转与泛型局部声明均 [TYPE_ARGUMENT 0]。
- `testInnerTypeInjection()`：**javac 源码无法产生 INNER_TYPE path**（限定名非末位组件的类型注解是编译错误，实测 `@A Outer.Inner` 被拒），改用 build 注入 `of(ofField(), [INNER_TYPE, TYPE_ARGUMENT 2, ARRAY], ann)` → re-parse 经 `checkPath` 逐步精确回读；`targetPath()` add → UOE。访问器实名：`typePathKind()`（非 kind()）+ `typeArgumentIndex()`。


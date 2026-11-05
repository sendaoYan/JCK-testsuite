# java.lang.classfile.ClassTransform — 设计案

## 1. API 功能
`java.lang.classfile.ClassTransform`（interface）。工厂/静态方法：ofStateful, endHandler, dropping, transformingMethods, transformingMethodBodies, transformingFields。查询/实例方法：andThen。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `ClassTransformTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.ClassTransform` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`ClassTransformTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.ClassTransform")` + public/interface/继承 + 关键成员。黑盒（`ClassTransformTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，10→53 断言）：变换/组合/生命周期全契约
新增 testAcceptAllConservation（元素守恒：elementList/成员/属性逐项、常量池大小守恒（facts §5 拷贝语义）、verify 干净、幂等）；testDroppingSelective（剥字段/剥方法/剥属性三类谓词 + 恒 false 守恒 + 恒 true 等价 DROP_ALL 且保留 superclass）；testEndHandlerAndThenOrder（两个 endHandler 都生效、eh1 先于 eh2、注入字段按序追加 [f,f1,f2]）；testOfStateful（Supplier 每 transformClass 调用一次（实测 2 次）、每实例状态隔离、null NPE）；testBridges（transformingMethods/transformingMethodBodies/transformingFields 三桥 + 带谓词重载 false 跳过 + 桥 andThen 组合）；testLifecycleHooks（自造匿名 ClassTransform：atStart→accept×N→atEnd 次序、atStart/atEnd 注入元素落盘、默认 hook 为 no-op、ClassFileTransform 层次白盒）。易错点：ClassTransform 无 DROP_ALL（用 dropping(e->true)）；ofStateful Supplier 是每次 transform 调用求值而非构造时。

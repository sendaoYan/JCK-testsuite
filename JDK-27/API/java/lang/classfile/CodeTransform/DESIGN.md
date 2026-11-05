# java.lang.classfile.CodeTransform — 设计案

## 1. API 功能
`java.lang.classfile.CodeTransform`（interface）。工厂/静态方法：ofStateful, endHandler。查询/实例方法：andThen。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `CodeTransformTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.CodeTransform` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`CodeTransformTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.CodeTransform")` + public/interface/继承 + 关键成员。黑盒（`CodeTransformTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（2026-11-05，9→28 断言）：指令级变换全契约
新增 testAcceptAllConservation（opcode 序列 [ICONST_2,ICONST_3,IADD,IRETURN] 守恒）；testAnonymousNopFilter（自造匿名 CodeTransform 剥 NOP→[RETURN]+verify 干净，facts 模板）；testDoubling（自造匿名 CodeTransform 加倍非 RETURN 指令→[ICONST_1,ICONST_1,POP,POP,RETURN] 实测且栈平衡 verify 干净）；testAndThen（dropNop.andThen(dbl) 先剥后加倍次序语义、反序组合、ACCEPT_ALL.andThen(x)==x）；testOfStateful（Supplier 每方法体调用一次（实测 2 方法→2 次）、null NPE）；testEndHandler（endHandler 在 RETURN 后追加（实测死代码归一化仍 verify 干净））。易错点：加倍变换必须保持栈平衡（push/pop 成对加倍）；CodeTransform 无 dropping 工厂——过滤须自造匿名类。

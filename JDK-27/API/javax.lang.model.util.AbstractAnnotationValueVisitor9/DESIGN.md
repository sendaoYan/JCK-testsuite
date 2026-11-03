# javax.lang.model.util.AbstractAnnotationValueVisitor9 — 设计方案

## 1. API 功能
访问器（visitor）基类：按元素/类型/注解值种类派发到对应 visit* 方法；默认实现经 defaultAction 或 visitUnknown 返回默认值/抛异常。版本 6/7/8/9/14/Preview 逐代新增 module/record 等派发。

## 2. Java 用户如何使用
匿名子类覆写关心的 visit* 方法，调用 visit(e, p)（等价 e.accept(v, p)）实现遍历/收集；未覆写方法走默认动作返回 DEFAULT_VALUE。

## 3. 开源代码实际用法调研
JDK `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/lang/model/util/AbstractAnnotationValueVisitor9.java`；`test/jdk/langtools/tools/javac/` 大量用访问器遍历编译树/元素（如 `JavacProcessingEnvironment`、`TreeDiffer`）。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 反射断言类型形态（接口/枚举/抽象类/继承链/关键成员/枚举常量）。
黑盒：白盒断言抽象类/继承链/构造器；黑盒用 `ToolProvider.getSystemJavaCompiler()` 解析固定源码，取真实元素/类型后调用 visit 断言派发目标与默认动作，版本差异断言（module/record 的 defaultAction vs visitUnknown）。 对应 `AbstractAnnotationValueVisitor9Test.java` 的 `testBlackBox()`。

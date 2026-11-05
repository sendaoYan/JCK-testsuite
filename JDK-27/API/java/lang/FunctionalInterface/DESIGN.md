# FunctionalInterface — 设计方案

## 1. API 功能
`java.lang.FunctionalInterface`（注解）标记函数式接口（恰好一个抽象方法）：`@Retention(RUNTIME)`、`@Target(TYPE)`，供 lambda/方法引用作为目标类型。

## 2. Java 用户如何使用
- 声明函数式接口并加 `@FunctionalInterface`，编译器校验抽象方法数量；然后用作 lambda 目标。
```java
@FunctionalInterface
interface FI { int apply(int x); }
FI f = x -> x * 2;
```

## 3. 开源代码实际用法调研
- `java.util.function` 全部接口标注 `@FunctionalInterface`；`test/jdk/java/lang/FunctionalInterface` 验证 RUNTIME 保留与 lambda 可用性。

## 4. 测试用例设计 + 自动化实现
白盒：验证为注解类型、`@Retention(RUNTIME)`、`@Target(TYPE)`。
黑盒：自定义 `@FunctionalInterface` 接口用作 lambda 目标并正确求值；反射确认接口上的注解存在。
对应 `<Simple>Test.java` 白盒 + 黑盒部分。

## 契约深化（2026-11）

### API 功能（新增覆盖维度）
@Target 恰 {TYPE}、@Documented、无成员代理契约、四种实现形态（lambda/静态方法引用/实例方法引用/匿名类）、SAM 计数易错（default 方法与 equals(Object) 声明不破坏）、泛型函数式接口继承、真实 JDK 接口标注存在性。

### 参考来源
JLS 9.8（函数式接口：恰一个抽象方法，Object 公有方法签名不计入）；javap 双侧同构。

### 测试用例设计 + 易错规范细节
- **default 方法不破坏 SAM**；**接口中重声明 equals(Object) 也不破坏 SAM**（JLS 9.8 特例）且 lambda 实例仍用 Object.equals 身份语义。
- 真实 API：Runnable/Callable/Function/Predicate 均 @FunctionalInterface；**Comparable 故意未标注**（多用途接口）；Serializable 无抽象方法不可标。
- 代理 toString "@java.lang.FunctionalInterface()"、hashCode 0。
- FunctionalInterfaceTest.java 现有 PASS 断言：43。

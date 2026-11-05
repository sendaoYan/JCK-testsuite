# Override — 设计方案

## 1. API 功能
`java.lang.Override`（注解）标记方法覆写：`@Retention(SOURCE)`、`@Target(METHOD)`，仅编译期校验（方法确实覆写超类/接口方法），运行期不保留。

## 2. Java 用户如何使用
- 覆写方法上加 `@Override` 让编译器检查签名匹配，避免拼写错误。
```java
@Override public String toString() { return "x"; }
```

## 3. 开源代码实际用法调研
- JDK 及各类框架广泛使用 `@Override`；`test/jdk/java/lang/annotation` 验证 SOURCE 保留（运行期不可见）。

## 4. 测试用例设计 + 自动化实现
白盒：验证为注解类型、`@Retention(SOURCE)`、`@Target(METHOD)`。
黑盒：子类覆写方法标注 `@Override`，反射确认运行期该注解不存在（SOURCE 保留）。
对应 `<Simple>Test.java` 白盒 + 黑盒部分。

## 契约深化（2026-11）

### API 功能（新增覆盖维度）
@Target 恰 {METHOD} 单值、SOURCE 保留导致的全场景运行时不可见（超类覆写/接口实现/抽象方法实现/toString 覆写四处）、无成员白盒、元注解恰 Retention+Target 两个、覆写语义行为对照。

### 参考来源
JLS 9.6.4.2（@Override 仅编译期检查）；javap -public java.lang.Override（8/27 同构）。

### 测试用例设计 + 易错规范细节
- **getAnnotation(Override.class) == null 在所有 @Override 标注处成立**（SOURCE retention 实测锁定，本批招牌）。
- getAnnotations() 在标注方法上为空数组。
- @Documented 不在 Override 上（与 Deprecated/SafeVarargs/FunctionalInterface 相对）。
- 覆写 dispatch 行为正常（toString 覆写生效），确认"注解不可见≠覆写失效"。
- OverrideTest.java 现有 PASS 断言：31。

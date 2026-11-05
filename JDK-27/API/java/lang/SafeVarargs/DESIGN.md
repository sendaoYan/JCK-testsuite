# SafeVarargs — 设计方案

## 1. API 功能
`java.lang.SafeVarargs`（注解）标注不会对可变参数数组执行不安全操作的 `final`/`static` 方法或构造器：`@Retention(RUNTIME)`、`@Target({CONSTRUCTOR, METHOD})`，用于抑制泛型 varargs 的 unchecked 警告。

## 2. Java 用户如何使用
- 在 `final`/`static` 泛型 varargs 方法上加 `@SafeVarargs`。
```java
@SafeVarargs
static <T> List<T> of(T... a) { return Arrays.asList(a); }
```

## 3. 开源代码实际用法调研
- `java.util.List.of`、`Collections` 内部工具方法使用 `@SafeVarargs`；`test/jdk/java/lang/SafeVarargs` 验证保留与目标。

## 4. 测试用例设计 + 自动化实现
白盒：验证为注解类型、`@Retention(RUNTIME)`、`@Target` 含 METHOD 与 CONSTRUCTOR。
黑盒：自定义 `@SafeVarargs static <T> List<T> asList(T...)` 正常调用返回正确大小。
对应 `<Simple>Test.java` 白盒 + 黑盒部分。

## 契约深化（2026-11）

### API 功能（新增覆盖维度）
@Target 恰 {CONSTRUCTOR, METHOD}（顺序锁定）、@Documented、静态 final/私有 final/构造器三处合法标注回读、注解代理契约（hashCode 0/toString "@java.lang.SafeVarargs()"）、varargs 行为矩阵（空调用/null 元素/构造器元数）。

### 参考来源
JLS 9.6.4.7（@SafeVarargs 仅可标 static/final/private 方法与构造器）；javap 双侧同构。

### 测试用例设计 + 易错规范细节
- **反射调用 varargs 方法必须传已打包数组**：`m.invoke(t, (Object) new Object[]{...})`；直接散传（invoke(t,"a","b","c")）两侧都抛 IllegalArgumentException（8 侧 "wrong number of arguments"、27 侧 "...: 3 expected: 1"）——本批踩坑记录。
- @SafeVarargs 标注实例非 final 方法是编译错误，无法在运行时断言（结构层用 Target 集合近似）。
- SafeVarargsTest.java 现有 PASS 断言：36。

# javax.management.BadStringOperationException 测试设计

## 1. API 功能
`BadStringOperationException` 继承 `Exception`，表示非法字符串操作。
构造器：`(String op)`；可观察方法：toString()。

## 2. Java 用户如何使用
```java
throw new BadStringOperationException("plus");
// toString() -> "BadStringOperationException: plus"
```

## 3. 开源代码实际用法调研
来源：JDK 源码 `~/git/jdk25u-dev/src/java.management/share/classes/javax/management/BadStringOperationException.java` 及 Query 表达式求值路径；
JDK 自带 `test/jdk/javax/management` 的 Query 用例在构造非法查询表达式时触发这些异常。

## 4. 测试用例设计
- 白盒：public/非接口/非抽象、继承 `Exception`、可序列化、构造器与方法形态。
- 黑盒：构造后断言 `toString()` 精确内容、`getMessage()` 为 null（无 detail message）、`instanceof Exception`；。
- 确定性：固定值字符串/对象。

## 深化（2026-11，批次 e）
新增维度：op 派生 toString、真实触发路径、序列化与 serialVersionUID。
- toString："BadStringOperationException: minus"；null → "...: null"；getMessage() null、无 cause、Exception+Serializable。
- 真实触发：Query.minus("a","b") / Query.div("a","b") 的 apply(null)（字符串上的非 plus 算术）。
- 序列化 round-trip 保 op；serialVersionUID 锁定 **7802201238441662100**（两侧一致）。
### 版本差异
- 无。

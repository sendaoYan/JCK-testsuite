# java.util.StringJoiner 测试设计

## 1. API 功能
- 构造器：`StringJoiner(CharSequence delimiter)`、`(delimiter, prefix, suffix)`。
- 追加：`add(CharSequence)`（链式）、`merge(StringJoiner)`。
- 空值：`setEmptyValue(CharSequence)`。
- 查询：`length()`、`toString()`。

## 2. Java 用户如何使用
- 拼接：`new StringJoiner(",").add("a").add("b")`。
- 带括号：`new StringJoiner(",", "[", "]")`。
- 合并：`joiner.merge(other)`。
- 空集合默认值：`setEmptyValue("none")`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/io/FilePermission.java`：`StringJoiner` 拼接权限 actions。
- JDK 源码 `java/io/ObjectOutputStream.java`：拼接字段描述。
- JDK 源码 `java/net/SocketPermission.java`：`StringJoiner` 拼接 actions。
- 广泛用法：`String.join` 内部委托 `StringJoiner`。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言 final 类、关键方法存在。
- 黑盒：分隔符拼接、prefix/suffix、length、setEmptyValue（含带前后缀空串忽略空值）、
  merge（追加元素、返回接收者、空 joiner 保留 emptyValue）。

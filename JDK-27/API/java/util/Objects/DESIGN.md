# java.util.Objects 测试设计

## 1. API 功能
- 判空：`requireNonNull`（无/有 message/Supplier）、`requireNonNullElse`、`requireNonNullElseGet`。
- 相等：`equals`、`deepEquals`、`isNull`、`nonNull`。
- 哈希/字符串：`hashCode`、`hash`、`toString`（有/无默认）、`toIdentityString`。
- 比较：`compare(T,T,Comparator)`。
- 索引校验：`checkIndex`/`checkFromToIndex`/`checkFromIndexSize`（int/long）。

## 2. Java 用户如何使用
- 参数校验：`Objects.requireNonNull(arg, "arg")`。
- 空值比较：`Objects.equals(a, b)`、`Objects.deepEquals(arr1, arr2)`。
- 哈希：`Objects.hash(a, b, c)`。
- 索引越界检查：`Objects.checkIndex(i, len)`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/io/Writer.java`：`Objects.requireNonNull` 校验参数。
- JDK 源码 `java/io/ObjectInputFilter.java`：`Objects.requireNonNull`。
- JDK 源码 `java/io/Reader.java`：`Objects.requireNonNull`。
- 广泛用法（Guava `Preconditions` 的替代）：`requireNonNullElse`、`checkIndex`。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言 final 类、关键方法存在。
- 黑盒：
  - requireNonNull 返回对象、message/supplier、NPE。
  - equals/deepEquals 边界、hashCode/hash、toString/toIdentityString、compare、isNull/nonNull。
  - requireNonNullElse/ElseGet（supplier 未被调用的路径）。
  - checkIndex/checkFromToIndex/checkFromIndexSize（int 与 long）范围与越界。

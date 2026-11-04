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

## 第 2 轮深化（2026-11，契约覆盖，63→215 断言）

新增方法：testJdk9NullContracts、testToIdentityString、testIndexCheckMessages、testHashFormulas、
testEqualsDeepMore、testToStringComparePredicates、testCompareTransitivityMatrix、
testHashOnContainers、testEqualsIdentitySemantics。

### 本轮锁定的易错规范细节（先探测后断言，jdk-27.0.1 实测）

- **`requireNonNull(null)` 的 message 为 null**——JDK-27 的 helpful NPE 机制**不**作用于该 API（显式 new NPE()）；双侧一致。
- **JDK-9+ null 契约矩阵**：`requireNonNullElse(null,null)` NPE、`requireNonNullElseGet(null, ()->null)` NPE、`requireNonNullElseGet(null, (Supplier)null)` NPE。
- **checkIndex 精确消息**：`"Index 5 out of bounds for length 5"`、`"Index -1 out of bounds for length 5"`；
  checkFromToIndex：`"Range [-1, 2) out of bounds for length 5"`、`"Range [0, 6) out of bounds for length 5"`；long 重载消息与 int 一致。
- **checkFromIndexSize(from>length, size=0)** 仍 IOOBE（from 越界即违规，即使 size 0）；`(from==length, size=0)` 合法。
- **toIdentityString**：格式 `Class@Integer.toHexString(identityHashCode)`、无视 toString 覆写、`(null)` NPE。
- **hash 公式**：4066/4096/128/31（与 JDK-8 相同公式）；`hash(list)=31+list.hashCode()`；数组元素非 deep。
- **equals 身份语义**：数组不同引用 false、同引用 true；deepEquals 两层嵌套 + null 叶子。

## 第 2 轮健壮性

全部固定输入；无 Locale/TimeZone/墙钟依赖；headless 无 `@key`。

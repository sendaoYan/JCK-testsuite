# java.util.Arrays 测试方案

对应测试：`ArraysTest.java`（`testWhiteBox()` 结构断言 + 后续 `test*` 黑盒行为断言）。

## 1. API 功能（按方法分组）

- 排序：`sort`（基本类型/对象/Comparator/区间）、`parallelSort`（同签名族）
- 查找：`binarySearch`（自然序/Comparator/区间，要求有序）
- 拷贝：`copyOf`、`copyOfRange`（截断 / 补零或 null）
- 填充/生成：`fill`、`setAll`、`parallelSetAll`
- 比较：`equals`、`deepEquals`、`compare`、`compareUnsigned`、`mismatch`
- 字符串/哈希：`toString`、`deepToString`、`hashCode`、`deepHashCode`
- 视图/流：`asList`（固定大小视图）、`stream`（对象/基本类型/区间）

## 2. Java 用户如何使用

```java
int[] a = {5, 2, 4, 1, 3};
Arrays.sort(a);                          // 原地排序
int i = Arrays.binarySearch(a, 3);       // 有序前提下查找
int[] b = Arrays.copyOf(a, 10);          // 扩容补 0
Arrays.fill(b, 0, 5, 0);                 // 区间填充
boolean eq = Arrays.equals(a, b);        // 内容比较
List<Integer> list = Arrays.asList(1, 2, 3); // 固定大小视图
int sum = Arrays.stream(a).sum();        // 流聚合
```

注意：`sort(Object[])` 要求元素可比较，否则抛 `ClassCastException`；`asList` 返回的 List 不能 `add/remove`。

## 3. 开源代码实际用法调研

来源：`~/git/jdk25u-dev`（JDK 源码）；网络检索未执行，以下为 JDK 自用、逐条标注。

- `java/io/CharArrayWriter.java:82` — `buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));`（动态扩容）
- `java/io/ByteArrayInputStream.java:195` — `byte[] result = Arrays.copyOfRange(buf, pos, count);`（区间拷贝）
- `java/io/ObjectInputStream.java:3946` — `Arrays.fill(status, 0, size, (byte) 0);`（区间填充）
- `java/io/SequenceInputStream.java:83` — `this(Collections.enumeration(Arrays.asList(s1, s2)));`（asList 构造）
- `java/net/URLPermission.java:228` — `Collections.sort(l);`（配合排序）

结论：Arrays 是数组基础设施，`copyOf/copyOfRange/fill/asList/sort` 在 IO/序列化代码中被高频使用。

## 4. 测试用例设计 + 自动化实现

- 正常：基本类型/对象/Comparator/区间/稳定排序；binarySearch 命中与插入点；copyOf/copyOfRange 截断补位；
  equals/deepEquals/compare/compareUnsigned/mismatch；setAll/parallelSetAll；stream；asList 视图联动。
- 组合链路：`sort → binarySearch → copyOf → fill → compare/mismatch → setAll → stream → asList`。
- 边界/异常：`sort(Object[])` 不可比较 → `ClassCastException`；`copyOf` 负长度 →
  `NegativeArraySizeException`；`copyOfRange` from<0 → `IndexOutOfBoundsException`、from>to →
  `IllegalArgumentException`、to 超界补 null；`asList` 的 add/remove/clear →
  `UnsupportedOperationException`。

健壮性：固定输入、无墙钟/随机；对象排序稳定性按规范断言（JDK 保证稳定）；`compare/compareUnsigned`
按确定符号断言；无全局副作用；headless，无 `@key`。

## 第 2 轮深化（2026-11，契约覆盖，94→213 断言）

新增方法：testSortAllPrimitiveTypes、testSortStability、testBinarySearchAllTypes、
testStreamRangeViolations、testNullArrayContracts、testCopyFillAllTypes、
testEqualsFloatingSemantics、testCompareMismatchJDK9、testAsListDeepContract、
testSpliteratorCharacteristics。

### 本轮锁定的易错规范细节（先探测后断言，jdk-27.0.1 实测）

- **null 数组契约矩阵**：`equals(null,null)=true`、`equals(null,arr)=false`、`hashCode(null)=0`、
  `toString(null)="null"`。
- **equals 浮点语义**：NaN==NaN true、+0.0!=-0.0。
- **JDK-9+ compare/mismatch 家族**：`compare` 字典序 + 长度差（短前缀为小）；`mismatch` 首个不同下标、无不同返回 -1；ranged `equals(a,1,3,b,1,3)`；`compare(a,from,to,cmp)` / `mismatch(a,f,t,cmp)` 对象版；`compareUnsigned`（byte/short/int/long：0xFF 系统性>0x7F 系）；带 Comparator 的 null → NPE。
- **`compare(double[])` 字典序用原始比较**：+0.0>-0.0、NaN>+Inf（与 equals 的 Double.compare 语义同为全序但方向锁定）。
- **asList 深契约**：iterator `remove` 未 next 先调抛 **UnsupportedOperationException（JDK-27）**——与 JDK-8 相反（8 抛 ISE），版本差异双侧锁定；`set` 联动、add/remove UOE。
- **spliterator 特征**：`0x4450`（ORDERED|SIZED|IMMUTABLE|SUBSIZED，无 NONNULL）。
- **stream range 违规**：from>to IAE、负 from/超长 to AIOOBE。

## 第 2 轮健壮性

- 全部固定输入；无随机、无墙钟。

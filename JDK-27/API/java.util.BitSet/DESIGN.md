# java.util.BitSet 测试设计

## 1. API 功能
- 构造器：`BitSet()`、`BitSet(int)`；工厂 `valueOf(long[]/byte[]/LongBuffer/ByteBuffer)`。
- 位操作：`set`/`clear`/`flip`（单点与范围）、`get`（单点与范围）。
- 查询：`cardinality`、`length`、`isEmpty`、`size`、`next/previousSetBit`、`next/previousClearBit`。
- 位运算：`and`/`or`/`xor`/`andNot`、`intersects`。
- 转换：`toByteArray`、`toLongArray`、`stream`、`clone`。

## 2. Java 用户如何使用
- 布隆过滤/位图：`bits.set(id)` / `bits.get(id)`。
- 交集并集：`a.and(b)` / `a.or(b)`。
- 序列化：`bits.toByteArray()` → 存储 → `BitSet.valueOf(bytes)`。
- 遍历置位：`for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i+1))`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/net/URLEncoder.java`：`new BitSet(256)` 作字符分类位图。
- JDK 源码 `java/lang/invoke/MethodHandles.java`：`BitSet` 位图标记。
- JDK 源码 `java/util/zip/ZipFile.java`：位图跟踪条目。
- 广泛用法（Lucene/Guava）：位图索引、布隆过滤器。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言具体类、实现 Cloneable/Serializable、关键方法存在。
- 黑盒：
  - set/get/cardinality/length/nextSetBit/nextClearBit/size。
  - clear/flip、and/or/xor/andNot/intersects。
  - 范围 set/get/clear、previousSetBit/previousClearBit。
  - valueOf/toByteArray/toLongArray 往返与位序、clone/equals。

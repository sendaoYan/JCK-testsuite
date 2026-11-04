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

## 5. 契约深化（第二轮追加，映射到新增方法）

| 测试点 | 用例方法 |
|---|---|
| 精确异常消息（与 JDK-8 逐字一致）：bitIndex/fromIndex 系列四类 | testExceptionMessages |
| and/or/xor/andNot/intersects(null) → NPE | testNullNpe |
| size() 字粒度矩阵（64/0/128） | testSize |
| toByteArray/toLongArray 小端精确 + 尾零丢弃 | testByteArrayExact |
| valueOf(ByteBuffer 切片/LongBuffer/空 buffer) | testByteBufferValueOf |
| toString 精确格式 | testToStringExact |
| equals/hashCode 深矩阵（空 1234、{0,3,5}=1275、容量无关、清空复原） | testEqualsHashCodeDeep |
| from==to 空区间四方法 + 超长 get + clear 不含 toIndex | testRangeEdgeCases |
| 跨 64 位字边界 + 512 位 cardinality==stream().count() | testCrossWordBoundaries |
| 逻辑运算参数不变 + and 收缩 length + clear() 全清 | testLogicalOperandUnmodified |
| 超长探针哨兵（nextClearBit/previousClearBit/nextSetBit(length)） | testBeyondLengthProbes |
| 序列化深往返（稀疏高位、hash 保持、独立可变） | testSerializationDeep |

### 易错细节

- JDK-27 与 JDK-8 在 BitSet 公开 API 与行为上实测**完全一致**（javap 逐方法相同；异常消息/哈希值/小端序相同）。
- 空集 hashCode()==1234；`BitSet(0).size()==0`；`previousSetBit(-2)` 消息下界为 -1。

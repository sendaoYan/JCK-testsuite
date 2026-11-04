# java.util.BitSet 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。纯内存位图，无全局副作用。

## 1. API 功能

- **构造器**：`BitSet()`、`BitSet(int nbits)`、`valueOf(long[]/byte[]/LongBuffer/ByteBuffer)`。
- **读写**：`set/get/clear/flip`（单点 + 范围 + boolean 变体）、`clear()`。
- **搜索**：`nextSetBit/nextClearBit/previousSetBit/previousClearBit`。
- **度量**：`length()/isEmpty()/intersects()/cardinality()/size()`。
- **逻辑**：`and/or/xor/andNot`。
- **转换**：`toByteArray()/toLongArray()`。
- **对象**：`hashCode()/equals()/clone()/toString()/stream()`；`Serializable`。

## 2. 使用方式

`new BitSet()` → `set(i)/get(i)/clear(i)/flip(i)`；`b.and/or/xor/andNot(other)`；`b.toLongArray()`。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/BitSet`。
- JDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/BitSet.java`：
  nbits<0 → NegativeArraySizeException；负索引/from>to → IndexOutOfBoundsException；
  next/previous 系列负索引 → IndexOutOfBoundsException；valueOf(null) → NPE。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 基础 set/get/cardinality/length/nextSetBit | testBasics |
| P0 | clear/flip | testClearFlip |
| P0 | or/and | testBitwise |
| P0 | nbits<0 NegativeArraySizeException | testConstructorNegative |
| P0 | 负索引/from>to IOBE 矩阵 | testIndexOutOfBounds |
| P0 | set/flip/clear 范围变体 | testRangeOperations |
| P0 | previousSetBit/-1 哨兵 | testPreviousAndSentinel |
| P0 | valueOf null NPE + toByteArray/toLongArray 往返 | testValueOf |
| P1 | intersects + xor/andNot | testIntersectsAndLogical |
| P1 | clone 独立 + equals/hashCode/toString/size | testCloneEqualsHashCodeToString |
| P1 | stream 升序位索引 | testStream |
| P1 | Serializable round-trip | testSerialization |

## 关键规范细节

- `BitSet(-1)` → `NegativeArraySizeException`；`set/get/clear/flip(-1)` → `IndexOutOfBoundsException`。
- `nextSetBit/nextClearBit/previousSetBit/previousClearBit(-1)` → IOBE；找不到时返回 `-1`。
- `toLongArray`/`toByteArray` 丢弃尾随零字；`clone` 浅拷贝但结构独立（新数组）。

## 注意事项

纯内存；无全局副作用；headless-safe 不加 `@key`。

## 5. 契约深化（第二轮追加，映射到新增方法）

| 测试点 | 用例方法 |
|---|---|
| 精确异常消息：set(-1)="bitIndex < 0: -1"、set(5,2)/flip(5,2)="fromIndex: 5 > toIndex: 2"、nextSetBit(-1)="fromIndex < 0: -1"、previousSetBit(-2)="fromIndex < -1: -2" | testExceptionMessages |
| and/or/xor/andNot/intersects(null) → NPE | testNullNpe |
| size()：默认 64、BitSet(0)→0、BitSet(63)→64、BitSet(65)→128、%64==0 且 ≥length | testSize |
| toByteArray 小端精确：{0,3,8}→[0x09,0x01]；bit9→第二字节 0x02；toLongArray {0,64}→[1,1] | testByteArrayExact |
| valueOf(ByteBuffer.wrap(data,1,2)) 只读切片小端；valueOf(LongBuffer)==valueOf(long[])；空 buffer→空集 | testByteBufferValueOf |
| toString 精确："{0, 3, 5}"/"{7}"/"{1000}"/"{}" | testToStringExact |
| equals/hashCode：空集 hash==1234（文档值）、{0,3,5} hash==1275、不同容量同内容等、清空回 1234 | testEqualsHashCodeDeep |
| 范围边界：from==to 四方法均合法空区间；get 超长全 0；clear 区间不含 toIndex | testRangeEdgeCases |
| 跨字边界：flip(63,65)、bits 63/64/127、previous/next 跨字、512 位基数 74=stream count、length==512 | testCrossWordBoundaries |
| 逻辑运算不修改参数；and 收缩 length（超参数长的位被清）；clear() 清所有远位 | testLogicalOperandUnmodified |
| 超长探针：empty nextClearBit(100)==100、previousClearBit(100)==100、nextSetBit(length)==-1 | testBeyondLengthProbes |
| 序列化深往返：稀疏高位（0,63,64,511）equals+hash 保持、可继续修改 | testSerializationDeep |

### 易错细节

- **空集 hashCode() == 1234**（javadoc 文档值）；{0,3,5} 稳定为 1275。
- `BitSet(0).size() == 0`（零容量合法退化）；`BitSet(63).size() == 64`。
- 小端序：bit 0 → 字节最低位；bit 64 → 第二个 long 的 bit 0。
- `previousSetBit(-2)` 的消息是 `"fromIndex < -1: -2"`（下界是 -1 而非 0）。
- `and` 后 length 收缩到参数长度（超出参数 words 的位被清 0）。

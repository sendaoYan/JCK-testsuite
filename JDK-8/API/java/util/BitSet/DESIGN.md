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

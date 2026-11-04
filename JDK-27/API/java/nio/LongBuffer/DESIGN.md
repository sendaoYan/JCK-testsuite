# java.nio.LongBuffer 测试方案

## 1. API 功能
- 工厂: `allocate(int)`, `wrap(long[])`, `wrap(long[],int,int)`
- 读写: 相对/绝对 `get()/get(int)/put(long)/put(int,long)`, 批量 `get(long[],..)/put(long[],..)`
- 视图/复制: `duplicate()`, `slice()`, `slice(int,int)`, `compact()`, `asReadOnlyBuffer()`
- 属性/比较: `hasArray()`, `array()`, `arrayOffset()`, `order()`, `equals`, `compareTo`, `mismatch`

## 2. Java 用户如何使用
- 构造: `LongBuffer.wrap(long[])` 或 `LongBuffer.allocate(n)`.
- 组合链路: `wrap -> put(绝对) -> get(相对) -> flip() -> 读取; duplicate() 共享存储`.
- `compact()` 压缩未读元素.

## 3. 开源代码实际用法调研
- `test/jdk/java/foreign/TestSegmentAllocators.java`: `segment.asByteBuffer()...asLongBuffer()` (同类视图模式).
- `test/jdk/java/nio/channels/DatagramChannel/EmptyBuffer.java`: `bb.putInt(1).putLong(1)` — ByteBuffer 上类型化 long 写, 与 LongBuffer 同语义.
- JDK 源码 `java.nio.LongBuffer#wrap(long[],int,int)`: 共享底层 long[].

## 4. 测试用例设计 + 自动化实现
推导: 相对/绝对/批量读写、wrap 语义、duplicate/slice 共享存储与独立游标、compact、
equals/compareTo/mismatch、只读、越界异常。实现于 `LongBufferTest.java` 的 `testBlackBox()`;
结构断言在 `testWhiteBox()`.

健壮性: 固定 long 输入; 无墙钟/随机/哈希顺序/全局状态/文件/网络/线程依赖; 不依赖平台字节序.


### 4.A2 批次深化（追加；JDK-8 + JDK-27 双侧同构）

新增 test 方法（只追加，不动既有内容；断言数见 PASS 行）：
- `testStateMachineTable`：表驱动状态机转移表——allocate → put×k → position/limit → mark/reset
  → flip/rewind/clear，每步断言五值（position/limit/capacity/remaining/hasRemaining，`checkState`
  助手）；limit<position 时 position 钳制到新 limit；position()>limit、负 position/limit、limit>capacity
  → IAE；toString 含 "pos=.. lim=.. cap=.."。
- `testMarkDiscardRules`：mark 生命周期——position/limit 移到 mark 之下、flip/rewind/clear 丢弃 mark
  → reset 抛 InvalidMarkException；mark 后 position 前移 reset 成功且可重复；新 buffer 与 slice 无 mark
  （reset 抛），duplicate 继承 mark（对照）。
- `testAccessSemantics`：allocate 逐元素零填充断言；相对 put/get 前进 position、绝对版不动；put 链式
  返回 this；相对 put 存入旧 position 处。
- `testBoundsExceptionMatrix`：相对 put 满 → BufferOverflowException；相对 get 空 →
  BufferUnderflowException；绝对索引 ==limit / ==capacity / -1 → IndexOutOfBoundsException；
  wrap(off<0、len<0、off+len>length) → IndexOOB（边界 off=len 合法）；allocate(-1) → IAE；
  失败操作后 position 不变。
- `testBulkTransferMatrix`：bulk get/put(off,len) 参数矩阵——off<0、len<0、off+len>数组长度 →
  IndexOOB，且越界错误优先于 under/overflow（实测双 JDK 一致）；len>remaining →
  BufferUnderflow/BufferOverflowException；窗口只写 [off,off+len)；len=0 合法 no-op；
  put(XBuffer)——拷贝 src 剩余区、双游标前进、put(this) → IAE、剩余不足 → Overflow、只读 → ROE。
- `testWrapContract`：wrap 数组同一性（array()==arr）、arrayOffset=0、数组写 ↔ 缓冲写双向可见；
  ranged wrap 游标从 off 起、绝对索引 0 仍映射 array[0]；只读 hasArray=false、array() → ROE。
- `testViewBuffers`：duplicate 复制 pos/limit/mark 且游标独立、内容共享；slice 全新游标覆盖
  [pos,limit)、索引 0 ↔ 源 position；asReadOnlyBuffer 复制状态、put/put(arr)/put(XBuffer)/compact
  → ROE、源变更在只读视图可见。
- `testEqualsCompareHash`：equals/compareTo/hashCode 只比较剩余区（前后缀不影响）；首个不同元素定
  符号且方向对称；前缀关系短者小；equals(null)/异类型 false；等剩余内容等哈希。整型语义：==/primitive 比较，无 NaN/±0 特例。
- `testCompactContract`：[pos,limit) 搬到 0、pos=remaining、limit=capacity、mark 丢弃；
  全未消费/全消费两极场景。
- `testByteBufferView`：视图容量 = byte 缓冲 remaining/元素字节、创建不动 byte position、
  order 继承创建时字节序（BIG/LITTLE 两版）、put 后通过 byte 层 get 逐字节对拍（0x0102030405060708L → BIG 01..08 / LITTLE 08..01）、
  视图 put 回写 byte 缓冲、byte 改动视图可见、只读 byte 缓冲 → 只读视图。
- JDK-27 专属 `testJdk13PlusApi`：slice(index,length)（状态/内容共享/越界矩阵/边界 length=0）、
  get(index,arr,off,len) 与 put(index,arr,off,len)（窗口拷贝、游标不动、index+len>limit → IndexOOB
  而非 over/underflow、只读 → ROE）、put(index,XBuffer,off,len)（双游标不动、越界 IndexOOB）、
  mismatch（相等 -1、首差异相对索引、真前缀=较短者剩余、位置无关、空/非空 0、双空 -1）。
  JDK-8 侧 `testJdk13PlusApiAbsent` 以反射断言上述方法不存在（getMethod → NoSuchMethodException），
  并在注释记录加入版本（mismatch=JDK 11，slice(int,int)/绝对 bulk=JDK 13）。

实测版本差异：两侧共享行为探针全部一致（状态机/越界矩阵/比较语义/字节序）；差异仅在 API 面
（JDK-27 多 JDK-11 mismatch 与 JDK-13 slice(int,int)、绝对 bulk get/put、put(int,XBuffer,int,int)）。

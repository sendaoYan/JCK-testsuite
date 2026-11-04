# java.io.DataOutput 测试方案 (DESIGN)

## 1. API 功能
- 与机器无关的原始类型写出契约接口：`write`(3 重载)、`writeBoolean/Byte/Short/Char/Int/Long/Float/Double/Bytes/Chars/UTF`。
- 全部大端序；`write(int)` 只写低 8 位；`writeUTF` 用 modified UTF-8 与 2 字节长度前缀。

## 2. Java 用户如何使用
- 通过 `DataOutputStream` 或 `RandomAccessFile` 等实现，以 DataOutput 引用多态写出二进制帧。

## 3. 开源代码实际用法调研
- 规范实现 — `jdk8u-dev/jdk/src/share/classes/java/io/DataOutputStream.java`、`RandomAccessFile.java`。

## 4. 测试用例设计 + 自动化实现（DataOutputTest.java，纯黑盒）
黑盒场景:
1. DataOutputStream 与 RandomAccessFile 两个实现写出同一值集，字节级完全一致。
2. 手工大端序逐字节点检（boolean/byte+short/char/int/long/float/double/UTF 前缀与正文/writeBytes/writeChars/write(int)）。
3. 非 ASCII 下 writeBytes（截断低 8 位）/writeChars（2 字节）/writeUTF（modified UTF-8）编码差异。
4. write(null) NPE、越界 IndexOutOfBoundsException。
5. DataOutput 写出可被 DataInput 读回（端到端）。
- 健壮性: 确定性字节帧；无反射白盒。

## 深化追加（契约级，批次 ioC）

新增维度清单（boundaryThrough / testCrossImplBoundaryImages / testTruncationParity / testWriteUTFBoundaryParity / testNullBoundsParity / testWriteIntLowBitsParity / testStructure + checkThrows/checkThrowsMsg）：

1. **跨实现边界镜像逐字节相等**（核心）：同一 17 项写序列（MIN/MAX/'\u0000'/'￿'/NaN/-0.0 float+double/writeUTF("\u0000中")/writeBytes("中")/writeChars("中")）分别经 DataOutputStream 与 RAF(rw) 写出，68 字节镜像 Arrays.equals 全等 + RAF 长度相等；镜像本身逐点断言（writeFloat NaN → 7F C0 00 00、writeUTF NUL → C0 80、writeBytes 截断 0x2D、writeChars 全 16 位 4E 2D）；RAF 镜像再用 DataInput 逐值读回（端到端帧证明）。
2. **writeXxx(int) 截断 parity**：双实现均 34 / 56 78 / 56 78。
3. **writeUTF 边界 parity**：65535 ASCII 前缀 FFFF；65536 双实现 → UTFDataFormatException；writeUTF("") 双实现 → 00 00。
4. **null/bounds parity**：writeUTF/writeBytes/writeChars(null) 双实现 NPE；RAF close 后 writeInt/read → IOException "Stream Closed"。
5. **write(int) 低 8 位 parity**：write(0x1FF) 双实现 0xFF 且恰 1 字节。
6. **接口形态**（反射）：恰 14 个方法、全 abstract、无 static/default；DataOutputStream 与 RAF 均实现之。

负例矩阵（双侧实测）：

| 操作 | 异常 | 消息子串 |
|---|---|---|
| writeUTF(>65535)（双实现） | UTFDataFormatException | too long: N bytes |
| writeUTF/writeBytes/writeChars(null)（双实现） | NullPointerException | — |
| write(b,off,len) 越界 | IndexOutOfBoundsException | — |
| RAF close 后 writeInt/read | IOException | Stream Closed |

版本差异：无行为差异（消息一致）；javap 双侧同为 14 个 abstract 方法。

产出：`DataOutputTest.java` 22 → 67 断言（jtreg Passed）。

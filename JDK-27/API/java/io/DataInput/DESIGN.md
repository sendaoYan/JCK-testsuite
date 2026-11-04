# java.io.DataInput 测试方案 (DESIGN)

## 1. API 功能
- 机器无关原语输入契约：readFully×2、skipBytes、readBoolean/Byte/UnsignedByte/Short/UnsignedShort/Char/Int/Long/Float/Double、readLine、readUTF。

## 2. Java 用户如何使用
- 用接口类型统一处理 DataInputStream 与 RandomAccessFile 的读侧；大端字节序假设。

## 3. 开源代码实际用法调研
- 契约 — `jdk25u-dev/src/java.base/share/classes/java/io/DataInput.java`。
- 实现 — `DataInputStream.java` 与 `RandomAccessFile.java`（同语义）。
- 官方测试 — `jdk25u-dev/test/jdk/java/io/DataInput/`（含大端与 readLine 规则）。

## 4. 测试用例设计 + 自动化实现（DataInputTest.java）
- 白盒: 接口、全部方法抽象、重载、两个 JDK 实现类。
- 黑盒场景:
  1. 规范字节图经 DataInputStream 与 RandomAccessFile 双实现逐位一致（canonical exercise）。
  2. 大端逐字节断言（int/long/float/double 位型）。
  3. readUnsignedByte/Short 正域拓宽。
  4. readFully 精确填满、前缀填充后抛 EOF。
  5. skipBytes 返回实际跳过数、钳制、EOF 0。
  6. readLine 的 \r\n、\r、\n 终止符与 EOF null。
  7. readUTF 修改版 UTF-8。
- 健壮性: 手工字节图；临时文件 finally 删除。

产出：`DataInputTest.java`（白盒 20 + 黑盒 53 断言）。

## 深化追加（契约级，批次 ioC）

新增维度清单（与 JDK-8 侧完全镜像，方法名一致；JDK-27 本接口无特有 API——javap-27 确认仍为 15 个 abstract 方法、无 static/default）：

1. **跨实现边界值矩阵**：boundaryWire 帧经 DIS/RAF 双实现，全部 readXxx 按绝对值断言（MIN/MAX/'\u0000'/'￿'/±0.0 位级/NaN/±Inf/readUTF 混合串）；RAF 指针恰耗尽。
2. **EOF/短读 readFully 前缀保留/skipBytes 负与钳制** 双实现 parity（含 RAF getFilePointer 断言）。
3. **非法/合法修改版 UTF-8 parity**（{00 01 80} 双侧 UTFDataFormatException；{C3 A9} 双侧 é）。
4. **readLine 行界 parity on RAF**。
5. **接口形态**：15 方法全 abstract、无 static/default、两个实现类关系。

负例矩阵与消息：同 JDK-8（本接口无版本差异）。

产出：`DataInputTest.java` 36 → 146 断言（jtreg Passed）。

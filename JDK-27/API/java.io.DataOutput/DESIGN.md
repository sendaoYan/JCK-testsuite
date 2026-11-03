# java.io.DataOutput 测试方案 (DESIGN)

## 1. API 功能
- 机器无关原语输出契约：write×3、writeBoolean/Byte/Short/Char/Int/Long/Float/Double、writeBytes（每字符低 8 位）、writeChars（每字符 2 字节）、writeUTF（修改版 UTF-8 + 2 字节长度前缀）。

## 2. Java 用户如何使用
- DataOutputStream / RandomAccessFile / ObjectOutputStream 的共同写侧契约；跨平台二进制文件格式。

## 3. 开源代码实际用法调研
- 契约 — `jdk25u-dev/src/java.base/share/classes/java/io/DataOutput.java`。
- 官方测试 — `jdk25u-dev/test/jdk/java/io/DataOutputStream/`（writeUTF/writeBytes/writeChars 编码差异）。

## 4. 测试用例设计 + 自动化实现（DataOutputTest.java）
- 白盒: 接口、全部方法抽象、重载、两个 JDK 实现类。
- 黑盒场景:
  1. 规范值集经 DataOutputStream 与 RandomAccessFile 双实现产出逐位相同字节图。
  2. 大端布局逐字节断言（int/long/float/double 位型、UTF 长度前缀、writeBytes/writeChars 差异）。
  3. 'é' 的 writeBytes 截断 / writeChars 2 字节 / writeUTF 修改版 UTF-8 三态对比。
  4. write(int) 只保留低 8 位。
  5. write(null) NPE、越界 IOOBE。
  6. 与 DataInput 端到端读回。
- 健壮性: 全内存 + 临时文件 finally 删除。

产出：`DataOutputTest.java`（白盒 18 + 黑盒 34 断言）。

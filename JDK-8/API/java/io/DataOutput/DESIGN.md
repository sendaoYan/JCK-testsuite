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

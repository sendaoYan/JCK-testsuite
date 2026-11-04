# java.io.DataInput 测试方案 (DESIGN)

## 1. API 功能
- 与机器无关的原始类型读取契约接口：`readFully`(2 重载)、`skipBytes`、`readBoolean/Byte/UnsignedByte/Short/UnsignedShort/Char/Int/Long/Float/Double/Line/UTF`。
- 全部大端序；`readUnsigned*` 无符号扩展；`readFully` 精确填满否则 EOFException。

## 2. Java 用户如何使用
- 通过 `DataInputStream` 或 `RandomAccessFile` 等实现，以 DataInput 引用多态读取二进制帧。

## 3. 开源代码实际用法调研
- 规范实现 — `jdk8u-dev/jdk/src/share/classes/java/io/DataInputStream.java`、`RandomAccessFile.java`。

## 4. 测试用例设计 + 自动化实现（DataInputTest.java，纯黑盒）
黑盒场景:
1. DataInputStream 与 RandomAccessFile 两个独立实现读同一手工字节帧，逐方法校验大端序结果。
2. readUnsignedByte/Short 无符号扩展（255/65535）。
3. readFully 精确填满 + 短流前缀填充后 EOFException；区域重载只填可用前缀。
4. skipBytes 返回值、封顶剩余字节、EOF 返回 0。
5. readLine 支持 \n、\r、\r\n 终结符；EOF 返回 null。
- 健壮性: 确定性字节帧 + UTF-8 显式；无反射白盒。

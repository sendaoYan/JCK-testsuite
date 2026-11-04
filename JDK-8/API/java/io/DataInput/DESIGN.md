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

## 深化追加（契约级，批次 ioC）

新增维度清单（testCrossImplBoundaryMatrix / testEofParity / testMalformedUtfParity / testReadLineParity / testStructure + 辅助 boundaryWire/exerciseBoundary/checkThrows）：

1. **跨实现边界值矩阵**（核心）：同一 boundaryWire 字节帧分别喂 DataInputStream(BAIS) 与 RandomAccessFile(rw)，每个 readXxx 都按**绝对值**断言（非仅两实现一致）：Byte/Short/Int/Long MIN+MAX、readUnsignedByte 255、readUnsignedShort 65535、'\u0000'/'￿'、+0.0/-0.0 位级、NaN、±Inf、readUTF("a\u0000中é")；RAF 指针恰耗尽整帧。
2. **EOF/短读 parity**：两实现的 readInt/readBoolean/readUTF/readFully 在 EOF → EOFException；readLine → null；readFully 短读**已传前缀保留**（[4,5,0,0]）双侧一致；skipBytes(-3)=0 不动指针、skipBytes(1) 推进、skipBytes(100) 钳制到文件长度（RAF 指针断言）。
3. **非法修改版 UTF-8 parity**：{00 01 80} 双实现 → UTFDataFormatException；合法 {C3 A9} 双实现 → é。
4. **readLine 行界 parity**：RAF 对 "a\r\nb\rc\nd" 与 DIS 同结果（\r\n 合一、\r、\n、无终结符尾行、EOF null）。
5. **接口形态**（反射）：DataInput 恰 15 个方法、全部 abstract、无 static/default（纯契约接口）；DataInputStream 与 RandomAccessFile 均实现之。

负例矩阵（双侧实测）：

| 操作 | 异常 | 消息子串 |
|---|---|---|
| readInt/readBoolean/readUTF/readFully at EOF（双实现） | EOFException | — |
| readUTF {00 01 80}（双实现） | UTFDataFormatException | malformed input around byte 0 |
| readFully 短读（双实现） | EOFException | 前缀保留 [4,5,0,0] |

版本差异：无（本接口行为与消息双侧逐字节一致；javap 双侧同为 15 个 abstract 方法、无 static/default）。

产出：`DataInputTest.java` 29 → 112 断言（jtreg Passed）。

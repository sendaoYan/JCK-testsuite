# javax.smartcardio.CommandAPDU 设计案

## 1. API 功能
- 构造器族（ISO 7816 case1/2/3/4 与短/扩展长度）：
  - `CommandAPDU(byte[] apdu)`、`CommandAPDU(byte[], int off, int len)`、`CommandAPDU(ByteBuffer)`。
  - `CommandAPDU(int cla,int ins,int p1,int p2)`（case1）、`(…, int ne)`（case2）、`(…, byte[] data)`（case3）、`(…, byte[] data, int ne)`（case4）、带 `dataOffset/dataLength` 的变体。
- 查询：`getCLA/getINS/getP1/getP2`（0..255 无符号）、`getNc()`、`getData()`、`getNe()`、`getBytes()`。
- 工具：`toString()`（注意实现拼写 `CommmandAPDU`）、`equals/hashCode`；`final` 且 `Serializable`。
- 头部字节只取低 8 位；Nc/Ne 分别以实际值返回（0 表示无）。

## 2. Java 用户如何使用
- 组装命令：`new CommandAPDU(0x00, 0xA4, 0x04, 0x00, aid)` 然后 `channel.transmit(apdu)`。
- 读取结果：`getBytes()` 交给底层，`getNc()/getNe()/getData()` 校验组装是否正确。
- 解析收到的 APDU：`new CommandAPDU(byte[])` 往返 `getBytes()` 校验长度编码。

## 3. 开源代码实际用法调研
来源：JDK 自带测试 `test/jdk/javax/smartcardio/CommandAPDUTest.java`、`TestCommandAPDU.java`、`Serialize.java`。
- `CommandAPDUTest.java`：对 `00 A4 04 00 07 A0 00 00 00 62 81 01 00` 断言 CLA/INS/P1/P2/Nc/Ne/Data，并覆盖 9 个构造器返回一致。
- `TestCommandAPDU.java`：对 nc∈{0,42,0x81,255,256,4242,0x8181,65535}、ne∈{…,65536} 组合，构造后 `new CommandAPDU(getBytes())` 往返断言 Nc/Ne/Data 不变（覆盖短/扩展长度）。
- `Serialize.java`：序列化往返后断言 `getNc()==7`、`getNe()==256`、`getINS()==0xA4`。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`CommandAPDUTest.testBlackBox`）：
- case1/case2/case3/case4 各短/扩展长度编码：Nc/Ne/字节长度/Lc/Le 编码（256→0x00，65536→00 00，>255→扩展）。
- 头部低 8 位掩码；null/空 data 退化为 case1。
- `byte[]`/偏移/`ByteBuffer`/编码往返构造器。
- 防御性拷贝（输入与输出）。
- `equals/hashCode/toString`。
- 异常：null NPE、<4 字节 IAE、非法长度 IAE、负偏移 IAE、ne<0 / ne>65536 IAE、dataLength>65535 IAE。

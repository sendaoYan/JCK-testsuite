# javax.smartcardio.ResponseAPDU 设计案

## 1. API 功能
- 构造器：`ResponseAPDU(byte[] apdu)` —— 完整响应 APDU（可选 body + 2 字节 SW），克隆输入。
- 查询：`getNr()`（body 字节数 = `getData().length`）、`getData()`、`getSW1()`、`getSW2()`、`getSW()`（`(SW1<<8)|SW2`）、`getBytes()`。
- 工具：`toString()`（`"ResponseAPDU: N bytes, SW=hex"`）、`equals/hashCode`；`final` 且 `Serializable`。

## 2. Java 用户如何使用
- `channel.transmit(cmd)` 返回 ResponseAPDU 后：`getSW()` 判断是否 `0x9000`，`getData()` 取响应数据。
- 典型链路：`new ResponseAPDU(apduBytes) -> getSW() -> getData()`。
- SW 解析：`getSW1()/getSW2()` 用于错误码细分。

## 3. 开源代码实际用法调研
来源：JDK 自带测试 `test/jdk/javax/smartcardio/ResponseAPDUTest.java`、`Serialize.java`。
- `ResponseAPDUTest.java`：用 21 字节 R1 断言 `getNr/getData/getSW/getSW1/getSW2/toString`（`"ResponseAPDU: 21 bytes, SW=9000"`）。
- `Serialize.java`：反序列化后断言 `r2.getSW1()==0x90`、`r2.equals(r1)`。
- `sun.security.smartcardio.ChannelImpl` 把底层响应字节构造为 `ResponseAPDU`（JDK 源码自用）。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`ResponseAPDUTest.testBlackBox`）：
- 正常路径：带数据 + SW 的往返（`getNr/getData/getSW1/getSW2/getSW/getBytes/toString`）。
- 多态：仅 SW 无 body；`0xFFFF` 高位 SW；长 body（JDK R1）。
- 防御性：输入/输出数组克隆。
- 相等性：同字节 equals/hashCode，不同字节与 null/非 ResponseAPDU 不相等。
- 异常：null NPE、空/1 字节抛 IAE。

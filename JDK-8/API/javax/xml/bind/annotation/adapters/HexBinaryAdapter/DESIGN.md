# HexBinaryAdapter 契约测试设计

## 1. API 功能

`javax.xml.bind.annotation.adapters.HexBinaryAdapter` 实现 `xs:hexBinary` 的双向编解码：`unmarshal(String)→byte[]`（十六进制 → 字节）与 `marshal(byte[])→String`（字节 → 大写十六进制）。委托 `DatatypeConverter.parseHexBinary/printHexBinary`。

## 2. 使用方式

```java
HexBinaryAdapter a = new HexBinaryAdapter();
a.unmarshal("00ff");                    // -> {0x00, 0xff}
a.marshal(new byte[]{65, 66, 67});      // -> "414243"
```

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/adapters/HexBinaryAdapter.html
- XML Schema Part 2 xs:hexBinary
- OpenJDK 8u RI（DatatypeConverterImpl 实测）；本仓库测试 `HexBinaryAdapterTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 类契约：**final**、extends `XmlAdapter<String, byte[]>`、单 public 构造器、经泛型引用桥接调用。
2. `unmarshal` 正例："00ff"→{0,0xff}、"0f"→15（小写）、"AbCd"（混合大小写）、""→空数组、"FF"、16 字节向量、"0123456789ABCDEF"→8 字节逐位。
3. `marshal` 正例：空数组→`""`、单字节补零（{1}→"01"）、大写输出（{0x0f}→"0F"）、负数按无符号（{-1,-2}→"FFFE"）、ASCII 字节、高位字节 0x80。
4. 往返：字符串→字节→字符串（"DEADBEEF"）与字节→字符串→字节双向闭环。
5. null/空：`unmarshal(null)==null`、`marshal(null)==null`。
6. 负例（IAE 消息实测锁定）：非法字符 `"zz"` → `IllegalArgumentException`，消息 `contains illegal character for hexBinary: zz`；奇数长度 `"abc"` → 消息 `hexBinary needs to be even-length: abc`；单字符 "0" 同样 IAE。

易错规范细节：

- 输出恒为**大写**且每字节两位补零。
- null 是合法直通值（两个方向都返回 null），不是异常。
- 错误消息由 RI 生成，本测试按 JDK-8 精确锁定（跨厂商字符串可能不同，但 temurin/oracle 8 共享同一 OpenJDK 实现）。
- byte[] 比较必须逐位（或 `& 0xff` 后比较），负 byte 直接 == 易错。

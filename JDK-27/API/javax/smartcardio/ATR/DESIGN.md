# javax.smartcardio.ATR 设计案

## 1. API 功能
- 构造器：`ATR(byte[] atr)` —— 从字节数组构造，内部防御性克隆并解析历史字节位置。
- 查询：`byte[] getBytes()`（返回原始 ATR 字节副本）、`byte[] getHistoricalBytes()`（返回历史字节副本，无历史字节时返回空数组）。
- 工具：`String toString()`（`"ATR: N bytes"`）、`boolean equals(Object)`、`int hashCode()`。
- 语义：`final` 且实现 `java.io.Serializable`；不验证 ATR 语义正确性，仅按 ISO 7816-3 解析历史字节。

## 2. Java 用户如何使用
- 从读卡器得到 ATR 后 `new ATR(card.getATR().getBytes())` 保存或比对。
- 典型组合链路：`new ATR(bytes) -> getHistoricalBytes() -> 解析厂商/型号`；`getBytes()` 用于协议协商。
- 比较与哈希：两个相同字节的 ATR 应 `equals` 且 `hashCode` 相等，可放入 `Set` 去重。
- 防御性：拿到 `getBytes()/getHistoricalBytes()` 后修改数组不应影响原对象。

## 3. 开源代码实际用法调研
来源：JDK-27 源码 `src/java.smartcardio/share/classes/javax/smartcardio/ATR.java` 与 JDK 自带测试 `test/jdk/javax/smartcardio/HistoricalBytes.java`、`Serialize.java`。
- `HistoricalBytes.java`（JDK test/jdk）用多组真实 ATR 断言历史字节：`new ATR(parse("3B 65 00 00 9C 02 02 07 02")).getHistoricalBytes()` 等于 `9C 02 02 07 02`；并覆盖 `3F` 起始、无效尾字节等。
- `Serialize.java`（JDK test/jdk）序列化后反序列化，断言 `a2.equals(a1)` 且 `getHistoricalBytes()` 一致。
- `sun.security.smartcardio.CardImpl` 把底层 ATR 交给 `new ATR(bytes)` 使用（JDK 源码自用）。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`ATRTest.testBlackBox`）：
- 正常路径：`{3B 00}` 往返 `getBytes`、无历史字节、`toString == "ATR: 2 bytes"`。
- 防御性：修改入参数组 / 修改返回数组，原对象不变。
- 相等性：同字节 `equals/hashCode` 相等，不同字节与 `null`/非 ATR 不相等。
- 异常：`null` 抛 NPE。
- 历史字节解析：两组 JDK 参考 ATR（`3B 65 00 00 9C 02 02 07 02` → `9C 02 02 07 02`；`3B 95 18 40 FF 62 01 02 01 04` → `62 01 02 01 04`）。
- 边界：首字节非 `3B/3F` 不解析历史；空数组返回空。

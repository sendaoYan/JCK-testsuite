# java.security.KeyRep 黑盒行为测试设计

## 1. API 功能
- 构造：`KeyRep(KeyRep.Type, String algorithm, String format, byte[] encoded)`；实现 `Serializable`。
- 作为密钥的序列化代理：反序列化时 `readResolve()` 依据 Type（SECRET/PUBLIC/PRIVATE）重建 Key。

## 2. Java 用户如何使用
通常由 `Key` 的序列化机制内部使用；也可显式构造 KeyRep 并序列化，反序列化得到相应 Key（PUBLIC→X509EncodedKeySpec、PRIVATE→PKCS8EncodedKeySpec、SECRET→SecretKeySpec）。

## 3. 开源代码实际用法调研
- `test/jdk/java/security/KeyRep/Serial.java`、`SerialDSAPubKey.java`：KeyRep 序列化/反序列化。
- 实现权威：`src/java.base/share/classes/java/security/KeyRep.java`。

## 4. 测试用例设计 + 自动化实现
- PUBLIC：用真实 RSA X509 编码构造，序列化往返后得到 Key，算法/编码一致。
- PRIVATE：PKCS#8 编码往返。
- SECRET：RAW 编码往返，getAlgorithm/getFormat 正确。
- 白盒：concrete + Serializable。

## 5. 追加契约维度（深化批）
- null 矩阵：type/algorithm/format/encoded 任一 null → 构造期 NPE。
- 延迟校验：任意字符串/空数组可构造，失败推迟到反序列化。
- 失败路径全为 NotSerializableException：未知 type/format 组合消息含 "unrecognized type/format combination: T/F"；无工厂算法/坏字节消息含 "[TYPE] [alg] [format]" 三段式。
- 真实 RSA 公私钥 writeReplace→KeyRep→readResolve 闭环：equals、algorithm/format/编码字节/hashCode 全保留。
- readResolve protected、返回 Object、throws ObjectStreamException。

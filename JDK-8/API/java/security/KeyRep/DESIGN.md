# KeyRep — 设计案

## 1. API 功能
`KeyRep` 是 Key 序列化的标准载体（implements Serializable）：构造 (Type, algorithm, format, encoded) + protected readResolve() throws ObjectStreamException。PUBLIC→X509EncodedKeySpec、PRIVATE→PKCS8EncodedKeySpec、SECRET+RAW→SecretKeySpec 的还原分派由 readResolve 完成。无 getter。

## 2. 使用方式
真实 Key 序列化时 writeReplace 自动产出 KeyRep；反序列化时 readResolve 经对应 KeyFactory/SecretKeySpec 还原等值 Key。测试也可直接构造 KeyRep 触发同一路径。

## 3. 参考来源
- javadoc `java.security.KeyRep`（NPE 条件、type/format 组合表、readResolve 语义）。
- 双侧探针实测（JDK-8/JDK-27 行为一致）。

## 4. 测试用例设计 + 易错规范细节
- null 矩阵：type/algorithm/format/encoded 任一 null → NPE（消息 "invalid null input(s)"）。
- 延迟校验：构造接受任意字符串/空数组（不解析），失败推迟到反序列化。
- readResolve 成功路径：SECRET+RAW→SecretKeySpec（算法/格式/字节完整）；真实 RSA pub/priv 序列化 round-trip equals、algorithm/format/encoding/hashCode 全保留。
- readResolve 失败矩阵（全部 **NotSerializableException**，非 InvalidKeySpecException）：无法识别的 type/format 组合（如 SECRET+WEIRD、PUBLIC+RAW）→ 消息含 "unrecognized type/format combination: <T>/<F>"；算法无工厂或字节无法解码 → 消息含 "[PUBLIC] [BogusAlg] [X.509]" 三段式。
- 结构：readResolve protected、返回 Object、throws ObjectStreamException。
- 易错点：KeyRep **没有 getType/getEncoded 等 getter**（别按习惯写 getter 断言）；解析异常在 ObjectInputStream.readObject 调用栈里抛出；equals 未覆写（identity）。

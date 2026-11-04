# KeyPair — 设计案

## 1. API 功能
公/私钥对持有容器。构造器(PublicKey,PrivateKey) + getPublic/getPrivate。

## 2. 使用方式
`new KeyPair(pub, priv)`。

## 3. 参考来源
- javadoc `java.security.KeyPair`。
- JCK runtime-8b `api/java_security/KeyPair/`。

## 4. 测试用例设计 + 易错规范细节
- 生成 RSA 密钥对非空、格式 X.509/PKCS#8、getEncoded 非平凡。
- 构造器往返 equals；null 键合法（getPublic/getPrivate 存 null）；单侧 null 混合。

## 5. 契约深化补充（本批）
- KeyPair 无 equals/hashCode 覆写：同键集不同实例不相等（identity 语义，实测修正）；自反 equals、非空/异型不等。
- 序列化 round-trip：两个 Key 字段值相等保留，但 KeyPair 本身仍 identity；null 字段的 KeyPair 序列化保留 null。
- 钥匙驱动 SHA256withRSA 签名闭环；toString 含类名。

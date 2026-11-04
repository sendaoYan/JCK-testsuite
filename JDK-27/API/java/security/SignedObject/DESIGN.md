# SignedObject — 设计案

## 1. API 功能
不可变签名对象（implements Serializable）。构造器(Serializable, PrivateKey, Signature) + getObject/getAlgorithm/getSignature/verify(PublicKey, Signature)。

## 2. 使用方式
`new SignedObject(payload, 私钥, Signature.getInstance("SHA256withRSA"))` 内部序列化并签名；`verify(公钥, 验签引擎)`。

## 3. 参考来源
- javadoc `java.security.SignedObject`。
- JCK runtime-8b `api/java_security/SignedObject/`。

## 4. 测试用例设计 + 易错规范细节
- 构造器：null 对象合法（getObject null）；null key→InvalidKeyException；null signingEngine→NPE。
- getObject/getAlgorithm/getSignature；verify 往返 true；错误公钥 false。
- 序列化往返保留 object/algorithm/signature。
- 易错点：构造器内部会 initSign(signingKey)，故不需预 initSign；不能通过"构造另一个不同 payload 的 SignedObject"测篡改（其签名对自身 payload 有效），篡改场景用错误公钥。

## 5. 契约深化补充（本批）
- DSA 闭环（ArrayList 负载）；getSignature 防御拷贝；getObject 每次返回新反序列化副本。
- verify 用 key 参数在内部 initVerify：未初始化 verifier → true、被污染 verifier 状态 → true（重置语义，实测修正）；跨算法 verifier → InvalidKeyException。
- 非 Serializable 对象 → 构造期 ClassCastException（实测，非 IAE）；签名后修改原对象/还原对象均不影响验签（深拷贝不变性）；toString 为 Object 默认形式（无算法信息）。

# java.security.Signer — 设计案

## 1. API 功能
带私钥的签名者抽象类（deprecated，extends Identity）：构造 `(String)`、`(String,IdentityScope)`（protected 无参）；`getPrivateKey()`、`setKeyPair(KeyPair)`（final，throws InvalidParameterException/KeyException）、`toString`。

## 2. 使用方式
匿名子类实例化；setKeyPair 同时安装两半密钥；JDK-8 中 `setKeyPair(null)`→NPE（消息为 null）、半空 KeyPair→InvalidParameterException、整对替换静默。

## 3. 参考来源
- javadoc `java.security.Signer`。
- 实测（JDK-8）：toString() == "[Signer]name"（无钥/有钥同）；toString(true) 无钥含 "no keys"、有钥含 "public and private keys initialized"；scope 只在构造时通知一次；序列化保两半密钥。

## 4. 测试用例设计 + 易错规范细节
- 构造矩阵：protected 无参；(String) 名字/两半密钥初始 null；(String,scope) 接线 + scope.addIdentity 恰一次。
- setKeyPair 矩阵：null→NPE（JDK-8 消息 null——与 JDK-9+ helpful message 分叉）；(pub,null)→InvalidParameterException；(null,priv)→InvalidParameterException；正常安装后 getPrivateKey 与原实例 ==（跨调用稳定）、公钥 equals；整对替换静默；不通知 scope。
- 继承契约：instanceof Identity/Principal/Serializable；equals/hashCode 按名继承（同名等、hashCode 一致）；默认 info "No further information available."；setInfo 生效。
- toString："[Signer]sig" 精确；toString(false)==toString()；toString(true) 两态内容。
- 序列化：round-trip 保 name/私钥/公钥、equals 原对象、私钥仍是 PrivateKey。
- 易错点：Signer 抽象——所有实例化都要 `{}` 匿名体；probe 输出括号包裹会把 "[Signer]sig" 误读成 "[[Signer]sig]"，断言精确串前须剥壳核对。

# java.security.interfaces.RSAKey — 设计案

## 1. API 功能
RSA 密钥公共父接口：getModulus() 抽象 + **default getParams() 恒返 null**（未覆写者）。公私各接口继承之；本身非 Key 非 Serializable。

## 2. 使用方式
`((RSAKey) key).getModulus()` 读模数；RSAKeyGenParameterSpec 可指定 keysize 与公钥指数。

## 3. 参考来源
- javadoc `java.security.interfaces.RSAKey`（getParams default 方法说明）。
- javap 双侧确认 default 方法存在于 JDK-8 与 JDK-27。

## 4. 测试用例设计 + 易错规范细节
- default 方法：记录型实现不覆写 getParams → 返回 null；真实 SunRsaSign 公私钥亦 null。
- 记录型实现：getModulus 透传（含 null 模数时 default 仍 null——两属性正交）。
- 继承格：无父接口、非 Key、非 Serializable、无 serialVersionUID；RSAPublicKey/RSAPrivateKey/RSAPrivateCrtKey 可赋值。
- 真实闭环（RSA-2048）：模数 2048 位、公私共享、正奇数；RSAKeyGenParameterSpec(1024, e=3) 转向后模数 1024 位、指数 3。
- 跨族判别：DSA 私钥非 RSAKey。
- 易错点：getParams 是 default 而非抽象（JDK-8 起即如此，易被误记为 JDK-9 新增）。

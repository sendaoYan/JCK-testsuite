# java.security.Identity — 设计案

## 1. API 功能
JDK-1.1 遗留的"身份"抽象类（deprecated，实现 Principal + Serializable）：构造 `Identity(String)`、`Identity(String, IdentityScope)`（protected 无参）；`getName/getScope`（final）、`getPublicKey/setPublicKey`、`getInfo/setInfo`、`addCertificate/removeCertificate`（throws KeyManagementException）、`certificates()`、`equals/hashCode`（final）、`identityEquals`（protected）、`toString/toString(boolean)`。**无公开 setName——名字只在构造时设定**。

## 2. 使用方式
以匿名子类实例化（抽象类）；可挂公钥与旧式 `java.security.Certificate`（接口）列表；带 scope 构造时 scope 会被通知一次。

## 3. 参考来源
- javadoc `java.security.Identity`（identityEquals 默认按 name+key；equals final）。
- 实测（JDK-8）：默认 info 为 "No further information available."；equals/hashCode **只按 name**（无视 scope 与 key）；addCertificate(null)→NPE、cert 公钥与身份公钥不一致→KME("public key different from cert public key")、重复 add 允许；removeCertificate 对缺失/null 静默；null-name 身份的 equals 抛 NPE；scope 只在构造时收到一次 addIdentity。

## 4. 测试用例设计 + 易错规范细节
- 构造矩阵：(String) 名字/无 scope/默认 info；(String)null 名合法（getName null、toString() 返回 null）；(String,scope) getScope 接线 + scope.addIdentity 恰一次 + 重复名在拒绝型 scope 下 KME 从构造器直通。
- final 元数据：getName/getScope/equals 为 final（反射 Modifier）。
- 公钥/信息：setPublicKey 往返/替换/置 null；setInfo(null) 存 null；后续变更不再通知 scope。
- 证书矩阵：初始 certificates() 空数组（非 null）；add 正常/重复（保留重复项）/null→NPE/密钥不匹配→KME 且列表不变；remove 存在收缩/缺失静默/null 静默。
- equals/hashCode：同名跨 scope、跨密钥状态均相等且 hashCode 一致；异名不等；null/异类型 false；null-name 身份 equals→NPE（实测）。identityEquals（子类暴露）：同名同钥 true、null 钥不对称 false、异名 false。
- toString()：恒等于名字（无钥/有钥/自定义 info 均同）；toString(true) 多行含 "no public key"/"public key initialized"/"no certificates"/info。
- 序列化：round-trip 保 name/info/公钥/证书列表且 equals 原对象。
- 易错点：toString() 对 null-name 返回 null 而非 "{no name}"；equals 与 identityEquals 语义不同（前者仅名、后者名+钥）；probe 打印时括号包裹易误读 toString 格式。

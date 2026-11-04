# Provider.Service — 设计案

## 1. API 功能
`java.security.Provider$Service`：提供者内一个（类型, 算法）服务条目的描述对象。构造器 `(Provider, type, algorithm, className, List<String> aliases, Map<String,String> attributes)`（四参均可非 null，null → NPE）；查询 `getType/getAlgorithm/getProvider/getClassName/getAttribute`；实例化 `newInstance(Object constructorParameter)`；能力查询 `supportsParameter(Key)`；`toString`。

## 2. 使用方式
`provider.getService(type, alg)` 取得已注册服务，`newInstance(null)` 构造实现实例；或手工 `new Provider.Service(...)` + `provider.putService(svc)`（protected，经子类暴露）注册后实例化。算法查找大小写不敏感、可经别名命中。

## 3. 参考来源
- javadoc `java.security.Provider.Service`（newInstance 的 constructorParameter 契约、SupportedKeyClasses 属性语义）。
- OpenJDK `test/jdk/java/security/Provider/`（Service 相关回归）。
- 实测记录（SUN 注册 SPI 类名而非 facade 类名）。

## 4. 测试用例设计 + 易错规范细节
- 构造矩阵：provider/type/algorithm/className 任一 null → NPE；aliases/attributes 允许 null。
- 生命周期：putService 前 getService 为 null；put 后 getService/别名/大小写不敏感查找命中；getServices 集合包含且**不可修改**（add → UnsupportedOperationException）；removeService 后消失；legacy `put("MessageDigest.X", cls)` 同样以 Service 形式暴露。
- newInstance：**必须已注册**（未注册 → NoSuchAlgorithmException）；JDK-8 无参构造器路径；传非 null constructorParameter（引擎无参构造）→ NSAE；每次调用返回**新实例**；**手工注册的服务不做引擎类型校验**（java.lang.String 注册为 MessageDigest 也能实例化——实测记录）；SUN 真实服务注册的是 **SPI 类**（sun.security.provider.MD5 instanceof MessageDigestSpi 而非 MessageDigest；SHA1PRNG 同理返回 SecureRandomSpi 实例）。
- supportsParameter：MessageDigest 引擎 → InvalidParameterException（"not used with MessageDigest engines"）；Signature/KeyAgreement/KeyPairGenerator 有效：无 SupportedKeyClasses 属性恒 true；有属性时按 Key 的类匹配（DSA true / RSA false）；非 Key 参数 → IPE。
- 元数据：getAttribute 命中/缺失（null）；toString 含 provider/type/algorithm/className/别名/属性。
- 版本差异：JDK-9+ 增 `getVersionStr()/isConfigured()`（JDK-8 反射确认缺失）。

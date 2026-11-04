# java.security.Provider.Service

## 1. API 功能
描述某个 `Provider` 中注册的一项服务的嵌套类。字段含 type（引擎类型）、algorithm、className、provider 及可选的 aliases/attributes。提供只读 getter（`getType`/`getAlgorithm`/`getProvider`/`getClassName`/`getAttribute`）、`newInstance`、`supportsParameter` 和 `toString`。公开构造器允许直接构造一个未注册的 Service 对象。

## 2. Java 用户如何使用
用户通过 `provider.getService("MessageDigest", "SHA-256")` 或遍历 `provider.getServices()` 获取服务，读取其类型/算法/实现类名/属性；也可用公开构造器 `new Provider.Service(provider, type, algorithm, className, aliases, attributes)` 自定义描述。

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/Provider.java`（JDK 源）：
  - `public Service(Provider provider, String type, String algorithm, String className, List<String> aliases, Map<String,String> attributes) { ... this.type = getEngineName(type); ... }`
  - `public final String getType() { return type; } public final String getAlgorithm() { return algorithm; } public final String getAttribute(String name) { ... }`
- `test/jdk/java/security/Provider/Turkish.java`：`p1.getServices(); ... s1 = p1.getService("Signature", "MD5withRSA");`
- `test/jdk/java/security/MessageDigest/TestCloneable.java`：`Provider.Service s = p.getService("MessageDigest", algo);`

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：反射断言 `Provider$Service` 为 public 具体类、超类 `Object`、含 getter/`newInstance`/`supportsParameter`/`toString` 方法。
- `testBlackBox()`：从 `Security.getProvider("SUN")` 取 `MessageDigest`/`SHA-256` 服务，断言 type/algorithm/className/provider 一致、未知属性返回 null、`toString()` 含算法名；再用公开构造器构造自定义服务并断言各 getter 反映构造参数。

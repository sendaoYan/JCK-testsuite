# Provider — 设计案

## 1. API 功能
安全提供者抽象类（extends Properties）。构造器(名称,版本,信息) + 继承 Hashtable 的 put/get/remove + getName/getVersion/getInfo + getService/getServices + keys()/elements() + toString。

## 2. 使用方式
子类 `class P extends Provider { P(){ super("P",1.0,"info"); } }`；`put("Cipher.AES","com.example.AESImpl")` 注册服务，`getService("Cipher","AES")` 查询。

## 3. 参考来源
- javadoc `java.security.Provider`。
- OpenJDK `test/jdk/java/security/Provider/`。
- JCK runtime-8b `api/java_security/Provider/`。

## 4. 测试用例设计 + 易错规范细节
- 抽象类 + extends Properties 层级；子类构造 + getName/getVersion/getInfo。
- Map 操作：put 新键返回 null、put 旧键返回旧值、get/containsKey/containsValue/remove。
- null：put(null key)→NPE、put(null value)→NPE。
- getService：未注册→null；注册后 Service.getType/getAlgorithm/getClassName/getProvider；算法名大小写不敏感、type 大小写敏感；getServices 计数；keys()/elements() 枚举。
- toString 含名称与版本；equals/hashCode（相同映射相等、不同映射不等）；序列化往返保留 name/version/info 与条目。
- 易错点：(名称,double版本,信息) 构造器不校验 null/空/负版本（校验在 Java 9+ 的 (name, String version, info) 构造器）；Provider 初始含 Provider.id.* 条目故不断言初始 size 0。

## 5. 契约深化补充（本批）
- Provider.id 恰 4 键（name/version/info/className），className 是具体子类名。
- Service 生命周期（经子类暴露 protected putService/removeService）：put 后按算法/别名/大小写不敏感命中、getServices 不可修改（add → UnsupportedOperationException）、remove 后清空、legacy put(MessageDigest.X,cls) 也暴露为 Service。
- Service 构造 null 矩阵全 NPE；newInstance 须已注册、返回新实例、无引擎类型校验（实测）；supportsParameter 矩阵（MD 引擎 IPE / Signature 按 SupportedKeyClasses / 非 Key IPE / 无属性恒 true）。
- JDK-9+ getVersionStr/isConfigured 反射断言（JDK-8 缺失记录）。

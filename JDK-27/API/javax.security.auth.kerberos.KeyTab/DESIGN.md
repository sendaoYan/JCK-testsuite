# KeyTab 测试设计

## 1. API 功能
`javax.security.auth.kerberos.KeyTab` 封装 keytab 文件（仅关联路径/绑定主体，惰性读取内容）。

- 静态工厂：`getInstance()`、`getInstance(File)`（绑定未知主体）、`getInstance(KerberosPrincipal)`、`getInstance(KerberosPrincipal, File)`（绑定特定主体）、`getUnboundInstance()`、`getUnboundInstance(File)`。
- 查询：`getKeys(KerberosPrincipal)`、`exists()`、`getPrincipal()`、`isBound()`、`toString/equals/hashCode`。

## 2. Java 用户如何使用
JAAS 登录模块从 keytab 读取服务长期密钥：

```java
KeyTab kt = KeyTab.getInstance(servicePrincipal, new File("/etc/krb5.keytab"));
KerberosKey[] keys = kt.getKeys(servicePrincipal);
boolean present = kt.exists();
```

## 3. 开源代码实际用法调研
- JDK 源码自用（`~/git/jdk25u-dev/src/java.security.jgss/.../kerberos/KeyTab.java`）：所有工厂只保存 `file/princ/bound`，不读文件；`getKeys` 对缺失文件返回空数组；`exists()` 通过快照 `isMissing()` 判断。
- JDK 自带测试 `~/git/jdk25u-dev/test/jdk/sun/security/krb5/ktab`：使用真实 keytab 文件；本用例按规则规避真实 keytab/KDC。
- 未发现外部开源库直接构造 KeyTab 的常见离线用法。

来源：JDK-27 对应实现与 `test/jdk` 目录结构。

## 4. 测试用例设计 + 自动化实现
对应 `KeyTabTest.java` 黑盒部分：
1. 六种工厂：unbound 默认/unbound(File)/bound(File)/bound(principal,File)/bound 默认，逐一断言 `isBound()`/`getPrincipal()`。
2. 对不存在的临时文件：`exists()==false`、`getKeys(...).length==0`（不抛异常，符合实现）。
3. bound 到特定主体时 `getKeys(其他主体).length==0`。
4. equals/hashCode：file+princ+bound 组合，bound vs unbound 不等。
5. toString 含绑定主体名。
6. null 参数（各工厂 + getKeys）抛 NPE。
7. 使用 `Files.createTempDirectory` + finally 清理，无全局副作用。
规避：不创建真实 keytab，不读默认 keytab（只用不存在/临时文件与 isBound/getPrincipal 语义）。

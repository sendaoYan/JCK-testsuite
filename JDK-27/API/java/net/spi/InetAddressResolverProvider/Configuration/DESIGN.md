# java.net.spi.InetAddressResolverProvider.Configuration 设计文档

## 1. API 功能

provider 部署时由 JDK 递交给 `InetAddressResolverProvider.get(Configuration)` 的
配置接口(JDK 18+),两个抽象方法:

- `InetAddressResolver builtinResolver()`——内置(平台)解析器,可作委托回退;
- `String lookupLocalHostName()`——本地主机名。

实测(JDK-27 javap/反射)**sealed interface**,唯一 permitted 子类
`sun.net.ResolverProviderConfiguration`:**public final**,唯一构造器
`(InetAddressResolver, Supplier<String>)`(内置解析器 + 本地主机名供应商),
但包 `sun.net` **未被 java.base 导出**——编译期不可达、反射
`newInstance` → `IllegalAccessException`("module java.base does not export sun.net")。

## 2. Java 用户如何使用

provider 实现 `get(Configuration cfg)` 时经参数取得 `cfg.builtinResolver()` 委托
内置解析、`cfg.lookupLocalHostName()` 取本机名。**用户不能构造 Configuration 实例**:
三个渠道全部封死——匿名/子类实现被 sealed 拒绝、`Proxy.newProxyInstance` 抛
`IllegalArgumentException("... is a sealed interface")`、反射实例化被模块系统拒。

## 3. 参考来源

- javadoc(JDK 18+);JDK-27 源码 `InetAddressResolverProvider.java`。
- 探针 `/tmp/probe-J/ProbeSpi.java`(permitted 名单、Proxy 拒绝)、`Ppc2.java`
  (sun.net 构造器签名与 IllegalAccessException)。

## 4. 测试用例设计 + 易错规范细节

文件 `ConfigurationTest.java`(5 → 55 断言):

- 形状:public interface/嵌套于 provider/恰 2 声明方法且均 public abstract、
  无 default/static、参数 0、无 throws、无字段、非 @FunctionalInterface、
  `getMethods()` 恰 2(接口的 getMethods 不含 Object 方法——易错)。
- sealed:`isSealed()`、permitted 唯一、名字精确 `sun.net.ResolverProviderConfiguration`、
  public final、在 java.base、实现本接口。
- 运行时强制:Proxy 两次拒绝(不同 classloader),消息含 "is a sealed interface"。
- 不可获得性:impl 唯一 public 构造器签名 (InetAddressResolver, Supplier<String>)
  断言;反射调用 → IllegalAccessException(消息含 "does not export sun.net");
  自适应分支——若运行环境 --add-exports 则转为行为断言(builtinResolver 非 null、
  supplier 生效),两种环境都稳。
- 限制记录:Configuration 实例仅在真实 ServiceLoader 部署时由 JDK 构造,
  本测试不注册 provider(需 module-info/META-INF/services,超出范围)。

## 5. 版本差异

仅 JDK 18+ 存在。JDK-8 反证(temurin jdk8u372-b07):

```
$ javap -public java.net.spi.InetAddressResolverProvider
Error: class not found: java.net.spi.InetAddressResolverProvider
```

不做 JDK-8 镜像。

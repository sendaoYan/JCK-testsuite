# java.security.AuthProvider 测试设计（JDK-8）

## 1. API 功能
- 抽象类，extends `java.security.Provider`（因此也是 `java.util.Properties`/`Hashtable`）。
- JDK-8 仅 1 个构造器 `protected AuthProvider(String,double,String)`（**JDK-9+ 增加 `(String,String,String)` 字符串版本构造器**）。
- 3 个 public abstract 方法：`login(Subject,CallbackHandler)`、`logout()`（均声明 LoginException）、`setCallbackHandler(CallbackHandler)`（无 checked 异常）。

## 2. Java 用户如何使用
登录式安全 provider（如 JGSS/Krb5）继承 AuthProvider，在 login/logout 中驱动 JAAS；仍完整继承 Provider 的服务注册表行为。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk8u-dev/jdk/src/share/classes/java/security/AuthProvider.java`
- JDK 测试 `/data/yansendao/git/jdk8u-dev/jdk/test/java/security/Provider/`
- 实际子类 `sun.security.jgss.SunProvider`（JGSS，历史版本）

## 4. 测试用例设计 + 自动化实现
- 结构：恰好 1 个声明构造器且 protected；3 个抽象方法 public abstract + 异常声明精确。
- 记录子类 RecAuth：login 收到同一 Subject/CallbackHandler 实例；LoginException 原样传播（且失败也计入调用）；setCallbackHandler 存精确 handler；logout 到达实现。
- Provider 继承面：`getName/getInfo`、**`getVersion()`（JDK-8 表面；JDK-9+ 才有 getVersionStr）**、put/get、containsKey；`setProperty/getProperty` Properties 语义（新键返回 null、旧值返回）。
- **易错点：预置键是空格分隔的 `"Provider.id name"/"Provider.id version"/"Provider.id info"/"Provider.id className"`（className=子类名），不是点分格式**；新 provider 映射恰好 4 项。
- 构造 AuthProvider 不会注册进 `Security`（`Security.getProvider(name)` 为 null）。

断言入口：`AuthProviderTest.testContractDeep()`（6→31 断言）。

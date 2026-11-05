# org.ietf.jgss.GSSCredential 测试设计

## 1. API 功能
GSSCredential 封装 GSS-API 凭证（接口，extends Cloneable）。获取凭证需要真实 Kerberos 凭据，本用例只做接口形态（白盒）+ 常量值（黑盒），不发起认证。
- 常量：usage 类 `INITIATE_AND_ACCEPT=0`、`INITIATE_ONLY=1`、`ACCEPT_ONLY=2`；lifetime 类 `DEFAULT_LIFETIME=0`、`INDEFINITE_LIFETIME=Integer.MAX_VALUE`。
- 方法族：`dispose()`、`getName()`/`getName(Oid)`、`getRemainingLifetime()`、`getRemainingInitLifetime(Oid)`、`getRemainingAcceptLifetime(Oid)`、`getUsage()`/`getUsage(Oid)`、`getMechs()`、`add(...)`、`equals`/`hashCode`。

## 2. Java 用户如何使用
真实用法（JDK test/jdk 模式）：
```java
GSSCredential cred = m.createCredential(serverName,
        GSSCredential.INDEFINITE_LIFETIME, mech, GSSCredential.ACCEPT_ONLY);
int usage = cred.getUsage();
Oid[] mechs = cred.getMechs();
cred.dispose();
```
本用例不获取真实凭证，仅断言常量与接口形态。

## 3. 开源代码实际用法调研
- `test/jdk/sun/security/krb5/auto/Context.java`：`me.cred = m.createCredential(..., GSSCredential.INDEFINITE_LIFETIME, mech, GSSCredential.ACCEPT_ONLY)`，以及 `GSSCredential.INITIATE_AND_ACCEPT`。
- JDK 源码 `org/ietf/jgss/GSSCredential.java`：接口与常量定义。
- JDK 源码 `sun/security/jgss/GSSCredentialImpl.java`：默认实现。
（来源：`~/git/jdk25u-dev`。）

## 4. 测试用例设计 + 自动化实现
白盒（`GSSCredentialTest.testWhiteBox()`）：public interface、extends `java.lang.Cloneable`、五个 `public static final int` 字段、10 个方法。
黑盒（`testBlackBox()`）：断言 usage 常量 0/1/2、lifetime 常量 0/MaxValue、usage 三常量互异。

## 5. 深化补充（本轮契约深化）
新增：用途常量（0/1/2）与生命周期哨兵（0、Integer.MAX_VALUE）精确值锁、接口方法面、
离线凭据 usage/lifetime 观察值锁定。

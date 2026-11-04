# javax.naming.ldap.LdapReferralException 设计案

## 1. API 功能
抽象类（`extends javax.naming.ReferralException`）。方法（抽象）：`getReferralContext()`、`getReferralContext(Hashtable<?,?>)`、`getReferralContext(Hashtable<?,?>, Control[])`；继承 `getReferralInfo()`、`skipReferral()`、`retryReferral()`。

## 2. Java 用户如何使用
LDAP 引用异常；用户 `catch` 后 `getReferralInfo()` 判断是否跟随，`skipReferral()`/`retryReferral()` 处理，`getReferralContext(...)` 获得引用上下文。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/LdapReferralException.java`。
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ReferralException.java`。

## 4. 测试用例设计
白盒（LdapReferralExceptionTest.testWhiteBox）：public/abstract/extends ReferralException + getReferralContext。
黑盒（testBlackBox）：匿名子类实现全部抽象方法，验证 instanceof、getReferralInfo、三个 getReferralContext、skipReferral/retryReferral 可调用。

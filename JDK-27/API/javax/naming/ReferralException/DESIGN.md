# javax.naming.ReferralException — 测试设计

## 1. API 功能
目录服务 referral（转介）异常抽象类（extends NamingException）：`getReferralInfo()`、
`getReferralContext()`（两重载）、`skipReferral()`、`retryReferral()`。

## 2. Java 用户如何使用
`try { ctx.search(...); } catch (ReferralException e) { String url = (String) e.getReferralInfo(); if (e.skipReferral()) continue; }`；
客户端读到转介 URL 后可跳过或重试（不真实联网测试中只验证契约表面）。

## 3. 开源代码实际用法调研
- `com.sun.jndi.ldap.LdapReferralException`、`com.sun.jndi.ldap.LdapCtx` 实现 referral 契约。
- Spring LDAP、UnboundID/JNDI 客户端对 referral 的 skip/retry 处理模式。

## 4. 测试用例设计（ReferralExceptionTest.java）
- 白盒：abstract、extends NamingException、5 个抽象方法清单。
- 黑盒：自实现 TestReferralException：getReferralInfo 返回固定 URL、skip/retry 钩子、getReferralContext 抛
  ServiceUnavailableException（不做网络）、客户端 skip 循环范式、继承的 remaining name 机制。

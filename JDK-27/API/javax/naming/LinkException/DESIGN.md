# javax.naming.LinkException — 测试设计

## 1. API 功能
链接解析异常（extends NamingException）：额外维护 link resolved/remaining name 与 link resolved obj
（`get/setLinkResolvedName`、`get/setLinkRemainingName`、`get/setLinkResolvedObj`），与基类非 link 状态独立；
`toString(boolean)` 输出链接部分。

## 2. Java 用户如何使用
LDAP 别名/JNDI 链接解析失败时 provider throw LinkException；客户端用 `getLinkResolvedObj()` 查已解析到的目标。

## 3. 开源代码实际用法调研
- `com.sun.jndi.ldap.LdapCtx`、`com.sun.jndi.url` 处理链接/别名失败路径 throw LinkException/LinkLoopException/MalformedLinkException。
- JDK test/jdk/javax/naming/** 覆盖 LinkException 家族。

## 4. 测试用例设计（LinkExceptionTest.java）
- 白盒：extends NamingException、link 相关方法清单、toString(boolean)、两个子类继承。
- 黑盒：link resolved/remaining name 与 obj 往返；toString 含 Link Remaining Name；link 与非 link 状态两条独立轨道；
  setLinkRemainingName clone 语义；可被 NamingException 捕获。

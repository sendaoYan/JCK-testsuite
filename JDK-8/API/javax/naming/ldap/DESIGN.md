# javax.naming.ldap 包 测试设计案

> 覆盖目标：**完整契约**（名称 RFC2253 + 控制件 BER + 扩展操作 + 未请求通知）。headless 安全，不起真 LDAP 服务。

## 1. API 功能
LDAP 扩展：LdapName/Rdn（RFC 2253 解析、转义、多值 RDN）、LdapContext（连接/请求/响应控制件、extendedOperation、
newInstance、reconnect）、InitialLdapContext 门面、BasicControl/SortControl/PagedResultsControl/
ManageReferralControl（BER 编码 + OID 精确常量）、SortResponseControl/PagedResultsResponseControl（解码）、
StartTlsRequest/Response、UnsolicitedNotification 族、LdapReferralException。

## 2. Java 用户如何使用
`LdapContext ctx = new InitialLdapContext(env, connCtls); ctx.setRequestControls(new SortControl(key, true));`
Rdn/LdapName 做属性名解析与比较；ControlFactory 把响应控制件转成具体类型。

## 3. 参考来源
RFC 2253（名字串语法）、RFC 2696（pagedResults 1.2.840.113556.1.4.319）、RFC 2891（sortRequest 1.2.840.113556.1.4.473）、
JDK javax.naming.ldap javadoc 与源码 BER 编码。

## 4. 测试用例设计（各类型子目录）
- **LdapName/Rdn**：RFC2253 round-trip（转义逗号/加号/反斜杠、十六进制 \4 不合法、首尾空格规则）、
  compareTo/equals/startsWith/getPrefix/getSuffix、**get(size)/remove(-1) 抛 IndexOutOfBoundsException**
  （JDK-8 侧 ArrayList remove 抛 AIOOBE 子类，断言用公共父类）、**Rdn("cn=a+") 尾随 + 被忽略 size=1**。
- **控制件**：OID 精确常量串；PagedResultsControl(300) BER=**3006**0202012c0400（SEQUENCE 长度=6，INTEGER 双字节 012c）；cookie 构造串对照。
- **LdapContext**：控制件按引用存取、setRequestControls(null) 清空、reconnect 计数、newInstance 收控制件、extendedOperation 请求原样转发。
- **响应控制件**：手工 BER 解码（sorted/result code/cookie）。
- **UnsolicitedNotification 族**：监听器方法名 **notificationReceived**（非 unsolicitedNotification）。
- 子包 spi/（LdapDnsProvider 族）另见其目录。

## 5. JDK-8 侧注记
本目录类型在 JDK-8 与 JDK-27 行为一致处共享断言文本；差异点（如 ArrayList 越界异常类、ldap/spi 子包缺失）
在各类型设计第 4 节内联标注。JDK-8 无 ldap/spi 子包（LdapDnsProvider 为 JDK-9+ 新增）。

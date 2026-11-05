# SOAPConnection — 设计案

## 1. API 功能
点对点连接（abstract）：`call(SOAPMessage, Object endpoint)` 同步请求响应；`get(Object endpoint)` 只取响应；`close()` 关闭。endpoint 合法形态为 URL String/URL 对象。

## 2. 使用方式
`SOAPConnection sc = SOAPConnectionFactory.newInstance().createConnection(); SOAPMessage resp = sc.call(req, new URL("http://...")); sc.close();` 本测试**不真联网**——只锁负例与生命周期语义（null/非法对象端点、关闭后调用、重复关闭）。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/soap/)、JSR 67 (SAAJ 1.3)、OpenJDK 8u 内置 RI com.sun.xml.internal.messaging.saaj（rt.jar）、本仓库测试文件

## 4. 测试用例设计
维度：createConnection 非 null/类型、call(msg,null)/call(msg,new Object())/get(null)/get(new Object()) 全部 SOAPException 且消息含 "Bad endPoint type"、close 后 call 抛 "Connection is closed"、**重复 close 抛 "Connection already closed"**（非幂等，实测锁定）、关闭后工厂仍可产新连接、新连接负例仍成立。断言 15 条（旧 6）。
- 坑1：所有端点负例抛 **SOAPException**（RI 为 SOAPExceptionImpl 子类），消息前缀 "Bad endPoint type"。
- 坑2：**close() 不幂等**：第二次 close 抛 SOAPException "Connection already closed"——与直觉的"幂等关闭"不同，实测锁定。
- 坑3：单连接关闭不影响同一工厂再产新连接。

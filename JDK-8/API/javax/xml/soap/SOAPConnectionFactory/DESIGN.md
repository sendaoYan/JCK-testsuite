# SOAPConnectionFactory — 设计案

## 1. API 功能
连接工厂（abstract）：`newInstance()` 定位实现（JDK-8 为 com.sun.xml.internal.messaging.saaj.client.p2p.HttpSOAPConnectionFactory），`createConnection()` 产 SOAPConnection。

## 2. 使用方式
`SOAPConnectionFactory cf = SOAPConnectionFactory.newInstance(); SOAPConnection sc = cf.createConnection();` 每次调用产生**新**连接实例；工厂可复用。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/soap/)、JSR 67 (SAAJ 1.3)、OpenJDK 8u 内置 RI com.sun.xml.internal.messaging.saaj（rt.jar）、本仓库测试文件

## 4. 测试用例设计
维度：newInstance 非 null、两次 createConnection 返回不同实例、连接可用性（非法端点立即 SOAPException，无网络副作用）、多次 close 后工厂仍可产连接、重复 newInstance 返回同实现类的新实例、类形态（SOAPConnectionFactory 本身 abstract，getModifiers 断言）。断言 14 条（旧 2）。
- 坑1：`newInstance()` 声明 throws UnsupportedOperationException——无 SAAJ 实现的环境会抛；JDK-8 内置 RI 恒可用。
- 坑2：断言连接"不同实例"而非工厂单例（RI 每次 newInstance 新对象、createConnection 新连接）。

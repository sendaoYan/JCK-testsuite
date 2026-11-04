# java.security.cert.LDAPCertStoreParameters 黑盒行为测试设计

## 1. API 功能
LDAP 后端参数：ctor()/ctor(server)/ctor(server,port)，getServerName/getPort/clone。

## 2. Java 用户如何使用
企业 PKI 从 LDAP 取交叉证书。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertStore/LDAP（端口/主机断言）。

## 4. 测试用例设计 + 自动化实现
黑盒：默认 server=="localhost"、port==389；自定义值往返；clone 相等且独立；equals/hashCode；负 port 被接受（实测）→ 只断言往返。

断言入口：`LDAPCertStoreParametersTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。

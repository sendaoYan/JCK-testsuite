# javax.xml.crypto.dsig.spec.RSAPSSParameterSpec — 设计案

## 1. API 功能
javax.xml.crypto.dsig.spec.RSAPSSParameterSpec 类（final，实现 SignatureMethodParameterSpec）。测试文件：RSAPSSParameterSpecTest.java。

## 2. Java 用户如何使用
包装 java.security.spec.PSSParameterSpec，作为 rsa-pss 签名方法的参数规范传入 XMLSignatureFactory.newSignatureMethod。

## 3. 开源代码实际用法调研
JDK 内建 org.jcp.xml.dsig.internal.dom 与 JDK `test/jdk/javax/xml/crypto` 用例；本仓库 RSAPSSParameterSpecTest 白盒结构 + 黑盒行为双轨。

## 4. 测试用例设计 + 自动化实现（本轮深化，JDK-27 独有目录）
JDK-27 独有：包装 java.security.spec.PSSParameterSpec；getPSSParameterSpec 恒等往返与内层参数（digest/mgf/salt/trailer）；null→NPE；rsa-pss URI 接受显式 spec 恒等往返、无参默认 RSAPSSParameterSpec、错误 spec 类型→IAE 消息含 RSAPSSParameterSpec；rsa-pss 显式参 sign→validate→篡改闭环；sha256-rsa-MGF1 隐式族往返

## 5. 版本说明
JDK-8 无此类；rsa-pss URI 在 JDK-8 抛 NoSuchAlgorithmException。

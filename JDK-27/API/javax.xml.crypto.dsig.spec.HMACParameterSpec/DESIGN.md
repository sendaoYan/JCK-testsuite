# javax.xml.crypto.dsig.spec.HMACParameterSpec — 设计案

## 1. API 功能
javax.xml.crypto.dsig.spec.HMACParameterSpec 类。实例/声明方法：getOutputLength。

## 2. Java 用户如何使用
HMACParameterSpec(int) → getOutputLength() 精确。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.crypto.dsig.spec.HMACParameterSpec）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 HMACParameterSpecTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`HMACParameterSpecTest.testBlackBox()`）覆盖：两个输出长度构造。。

# javax.xml.crypto.KeySelector — 设计案

## 1. API 功能
javax.xml.crypto.KeySelector 抽象类。静态入口：singletonKeySelector。 实例/声明方法：select。

## 2. Java 用户如何使用
singletonKeySelector(公钥) → select(null, Purpose.SIGN, ...) → getKey() 等于原公钥。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.crypto.KeySelector）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 KeySelectorTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`KeySelectorTest.testBlackBox()`）覆盖：单例选择器往返、Purpose.SIGN 选择。。

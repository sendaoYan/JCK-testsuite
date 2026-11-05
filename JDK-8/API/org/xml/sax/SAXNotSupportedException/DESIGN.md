# SAXNotSupportedException — 设计案

## 1. API 功能
SAXException 子类：ID 识别但当前值不支持。

## 2. Java 用户如何使用
如对 JAXP reader 设只读特征。

## 3. 开源代码实际用法调研
SAX 2.0.2 javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：真实触发——JAXPSAXParser 的 is-standalone 特征 get/set 均抛本异常（识别但不可变）；lexical-handler 属性赋错型值抛本异常；与 SAXNotRecognizedException 区分断言。

# SAXNotRecognizedException — 设计案

## 1. API 功能
SAXException 子类：特征/属性 ID 不被识别。

## 2. Java 用户如何使用
setFeature/getFeature/setProperty/getProperty 传未知 ID。

## 3. 开源代码实际用法调研
SAX 2.0.2 javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：真实触发矩阵——reader 四访问器 × 未知 ID 全部抛本异常（非 SAXNotSupportedException）；XMLFilterImpl 孤儿 filter 上同样触发；构造链默认。

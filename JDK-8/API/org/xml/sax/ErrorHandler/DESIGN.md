# ErrorHandler — 设计案

## 1. API 功能
错误分级回调：warning/error/fatalError，均收 SAXParseException。

## 2. Java 用户如何使用
fatalError 抛出终止解析；error 可恢复继续；warning 仅提示。

## 3. 开源代码实际用法调研
JDK test/jdk/org/xml/sax/ptests；SAX 2.0.2 javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：良性 fatalError 返回后 parse 抛新 SAXParseException（非同一实例、行号一致）；handler 抛 RuntimeException 原样穿透 parse；非验证 DTD 下 content-model 失配走 error() 且解析完成；接口形态锁定。

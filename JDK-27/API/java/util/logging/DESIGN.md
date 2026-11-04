# java.util.logging 包（JDK-27 侧） 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

同 JDK-8 面 + LogRecord getInstant/setInstant/getLongThreadID、LogManager updateConfiguration/addConfigurationListener、FileHandler long-limit ctor、Logger logrb(ResourceBundle,...) 无 source-class 变体、XMLFormatter <nanos> 元素。

## 2. Java 用户如何使用

setInstant(EPOCH)→getMillis()==0；updateConfiguration 重映射 ".level"；监听器增删对称。

## 3. 参考来源

JDK-9+ JUL 增量 javadoc。

## 4. 测试用例设计

JDK-8 拷贝 + LogRecord/LogManager/FileHandler/Logger 各 testJdk27Additions() + XMLFormatter 形态差异断言。

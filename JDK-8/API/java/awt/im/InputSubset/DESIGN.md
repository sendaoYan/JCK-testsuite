# java.awtim/InputSubset 黑盒行为测试设计

## 1. API 功能
输入法子集：Character.Subset 子类、9 个常量（LATIN/LATIN_DIGITS/.../HALFWIDTH_KATAKANA/FULLWIDTH_LATIN/FULLWIDTH_DIGITS；无 KANA）、toString 恒类名、equals 恒等。

## 2. Java 用户如何使用
```java
InputSubset.LATIN_DIGITS
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 InputSubset.java；注意无 KANA 常量。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：9 常量非空；Subset 层级；toString；恒等语义。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。

# java.awtBufferCapabilities 黑盒行为测试设计

## 1. API 功能
缓冲能力：两 ImageCapabilities+F lipContents、null caps IAE（非 NPE，规范偏离锁定）、isPageFlipping、getFlipContents 对 4 个 FlipContents 常量恒等返回、FlipContents toString 小写（background/copied）、clone、无 equals。

## 2. Java 用户如何使用
```java
new BufferCapabilities(front, back, FlipContents.BACKGROUND)
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 BufferCapabilities.java：构造器抛 IllegalArgumentException（javadoc 未写明类型——偏离锁定）。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：null caps IAE 精确；isPageFlipping 分支；FlipContents 恒等；toString 小写；clone；无 equals。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。

# java.awtAWTEvent 黑盒行为测试设计

## 1. API 功能
AWT 事件基类：RESERVED_ID_MAX=1999、事件掩码精确（MOUSE_WHEEL=0x20000、WINDOW_FOCUS=0x80000 等）、consume() 对未知 ID 无效、setSource(null) 被接受、ctor null source IAE 'null source'、Event 旧构造器经子类、protected 字段子类可见。

## 2. Java 用户如何使用
```java
new KeyEvent(src, KEY_PRESSED, 0L, 0, VK_A, 'a'); ev.consume()
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 AWTEvent.java：consume 仅对可消费事件类型生效；setSource 无 null 校验（与构造器不对称——锁定）。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：RESERVED_ID_MAX；掩码表；consume 未知 ID 无效；KeyEvent consume 有效；setSource(null) 接受；ctor null IAE 消息精确；旧 Event 构造器 target 作 source；protected id/consumed。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。

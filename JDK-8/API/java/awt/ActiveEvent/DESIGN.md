# java.awtActiveEvent 黑盒行为测试设计

## 1. API 功能
主动事件接口：dispatch() 抽象 + isDispatched() 默认实现（JDK9+）；最小实现 dispatch 计数 0→1→2；EventQueue 上直接派发无需事件派发线程同步。

## 2. Java 用户如何使用
```java
new ActiveEvent() { public void dispatch() { ran++; } }
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 ActiveEvent.java：接口供 InvocationEvent 等使用。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：接口方法；最小实现 dispatch 计数；可重复调用。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。

# java.awtAlphaComposite 黑盒行为测试设计

## 1. API 功能
合成规则封装：12 条规则常量（rule 值乱序 {1,2,9,3,4,5,6,7,8,10,11,12}）、alpha=1 常量族、getInstance 缓存（alpha=1 恒同一实例）+equals（alpha≠1 值语义）、IAE（rule 0/13、alpha 越界）、createContext headless 可用、非 Serializable。

## 2. Java 用户如何使用
```java
AlphaComposite.getInstance(SRC_OVER, 0.5f).createContext(cm,cm,hints)
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 AlphaComposite.java：规则表乱序；getInstance 缓存 alpha=1；createContext 返回上下文。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：规则数组乱序映射；alpha 1.0 常量族；getInstance 同实例缓存+equals 语义；IAE 消息；createContext；非 Serializable。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。

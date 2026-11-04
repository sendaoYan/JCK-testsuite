# java.util.TooManyListenersException — 测试设计案

## 1. API 功能
受检异常：单播事件源（同一时刻最多一个监听器）收到第二个监听器注册时抛出。两个构造器：()、String。

## 2. Java 用户如何使用
实现单播事件模型（如 AWT 某些组件）时，在 addXxxListener 中检测已存在监听器则抛出。
链路：已有监听器 -> 再次 addListener -> 抛出。

## 3. 开源代码实际用法调研
- OpenJDK `java.util` 之外的 AWT 单播监听器（如 `Toolkit.addAWTEventListener` 的部分实现）会抛
  `new TooManyListenersException()`——第 1/2 节对齐。
- 接口约定：`EventMulticaster` 相关代码遵循单播约束。

## 4. 测试用例设计 + 自动化实现（TooManyListenersExceptionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 继承 | 受检异常，extends Exception |
| 构造器 | ()/String 两个构造、消息回传 |
| catch 可用性 | 作为 Exception 被 catch |
| P0 | 契约深化：受检异常语义/cause 缺省 | testTooManyContracts |

共 3 组场景、约 5 个 check（`testBlackBox()`）。

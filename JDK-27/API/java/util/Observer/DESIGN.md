# java.util.Observer — 测试设计案

## 1. API 功能
观察者回调接口：`void update(Observable o, Object arg)`。配合 Observable 使用。

## 2. Java 用户如何使用
实现 `update` 接收被观察对象与通知参数；注册到 Observable.addObserver。
链路：Observable.setChanged -> notifyObservers -> update(o, arg)。

## 3. 开源代码实际用法调研
- OpenJDK `Observer`（本地 `~/git/jdk25u-dev`）仅一个抽象方法 update；
  `Observable.notifyObservers` 遍历调用——第 1/2/4 节对齐。

## 4. 测试用例设计 + 自动化实现（ObserverTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 接口性 | 单抽象方法 update |
| 回调 | lambda 实现收到 Observable 与参数 |
| 多观察者 | 多个 Observer 都收到通知 |
| P0 | 契约深化：update 回调参数矩阵（直接调用契约） | testObserverContracts |

共 3 组场景、约 5 个 check（`testBlackBox()`）。

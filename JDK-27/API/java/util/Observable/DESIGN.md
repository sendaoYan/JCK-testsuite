# java.util.Observable — 测试设计案

## 1. API 功能
观察者模式的被观察对象基类（deprecated for removal）。维护 Observer 列表与 changed 标记。

- addObserver/deleteObserver/deleteObservers/countObservers/hasChanged/notifyObservers/notifyObservers(Object)。
- setChanged/clearChanged（protected）。

## 2. Java 用户如何使用
子类 `setChanged(); notifyObservers(arg);` 通知所有注册的 Observer；
Observer 用 `update(Observable, Object)` 接收。
链路：addObserver -> setChanged -> notifyObservers -> update。

## 3. 开源代码实际用法调研
- OpenJDK `Observable`（本地 `~/git/jdk25u-dev`）：notifyObservers 只有在 hasChanged 时才通知，
  通知后 clearChanged；arg 为 null 时 update 收到 null——第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/util/Observable/`.

## 4. 测试用例设计 + 自动化实现（ObservableTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| changed 门控 | 未 setChanged 不通知、setChanged 后通知 |
| 参数传递 | notifyObservers()/notifyObservers(arg) |
| changed 复位 | 通知后 hasChanged false |
| 观察者管理 | add/delete/count/deleteObservers |
| P0 | 契约深化：未 setChanged 不通知/参数 null 语义/deleteObservers/NPE | testObservableContracts |

共 4 组场景、约 10 个 check（`testBlackBox()`）。

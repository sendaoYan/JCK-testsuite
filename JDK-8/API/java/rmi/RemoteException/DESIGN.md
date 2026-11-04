# java.rmi.RemoteException 测试设计案

> 覆盖目标：完整契约（extends IOException、三构造器、public detail、getMessage 嵌套后缀、initCause 禁止、RMI 异常层次）。

## 1. API 功能
- 构造：`()`、`(String)`、`(String, Throwable)`。
- 字段：`public Throwable detail`；`getMessage()`、`getCause()`。

## 2. 使用方式
- 远程调用失败的公共超类；带 cause 时 detail 字段保存被包裹异常。

## 3. 参考来源
- Java SE 8/27 javadoc（`java/rmi/RemoteException.html`）。
- JCK `api/java_rmi/RemoteException` + 各子类目录。

## 4. 测试用例设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | extends IOException + 三构造器 + message/detail/cause | testConstructors |
| P0 | 带 cause 时 getMessage 含 "nested exception" 后缀 | testConstructors |
| P0 | initCause→IllegalStateException | testInitCause |
| P1 | RMI 异常层次（Access/Connect/Marshal/Server 等均 extends RemoteException） | testHierarchy |

## 关键规范细节
- `RemoteException(String, Throwable)` 置 `detail=cause`；`getCause()` 返回 `detail`。
- 有 detail 时 `getMessage()` 返回 `"msg; nested exception is: \n\t" + detail.toString()`（不是裸 msg）。
- 构造器内部 `initCause(null)` 禁止后续 `initCause`，再调用 `initCause` 抛 `IllegalStateException`。

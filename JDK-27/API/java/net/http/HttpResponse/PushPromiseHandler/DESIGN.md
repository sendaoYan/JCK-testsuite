# java.net.http.HttpResponse.PushPromiseHandler 设计文档

## 1. API 功能

HTTP/2 server push 回调接口。抽象方法 `applyPushPromise(initiating, pushPromise, acceptor)`；
JDK-27 增加 default `applyPushPromise(..., PushId, acceptor)` 与 `notifyAdditionalPromise(...)`（HTTP/3）。
工厂 `of(pushPromiseHandler, ConcurrentMap)` 收集推送。

## 2. 使用方式

`sendAsync(req, handler, PushPromiseHandler.of(r -> BodyHandlers.ofString(), map))`。

## 3. 参考来源

- JDK-27 `javap`；`jdk/internal/net/http/ResponseBodyHandlers$PushPromisesHandlerWithMap.java`。

## 4. 测试用例设计 + 易错规范细节

文件：`PushPromiseHandlerTest.java`（全部离线手动调用回调）。

- 形状：3-arg 抽象、4-arg 与 notifyAdditionalPromise 为 default（probed）。
- of() 语义（probed）：acceptor 每个 (推送) 调一次；返回的 future 存入 map（同一实例）；
  同一请求再次推送 acceptor 再调且 map 单条目被覆盖；预置条目被覆盖。
- 源过滤（probed）：仅同 scheme+host(大小写不敏感)+port 的推送被记录，异 host/port/scheme 全拒。
- 工厂不做 null 校验（probed）：of(null, map)/of(fn, null) 不抛；apply 时才 NPE。
- acceptor 返回 null → ConcurrentHashMap.put NPE（probed）；acceptor 抛异常同步传播。
- default 4-arg 委托 3-arg（顺序验证）；notifyAdditionalPromise 为 no-op。

## 5. 版本差异

- JDK-27 新增 PushId 维度的 default 方法（HTTP/3 push）；JDK 11–24 仅 3-arg。

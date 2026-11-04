# java.net.http.HttpResponse.PushPromiseHandler.PushId 设计文档

## 1. API 功能

推送标识的 sealed 标记接口（JDK-27 新增，无方法）：唯一许可子类为嵌套 record `Http3PushId`。

## 2. 使用方式

由 HTTP/3 实现创建并传给 `applyPushPromise(..., PushId, ...)` / `notifyAdditionalPromise(...)`；
用户代码只读。

## 3. 参考来源

- JDK-27 `javap`；`PushPromiseHandler.java` 源码（`permits PushId.Http3PushId`）。

## 4. 测试用例设计 + 易错规范细节

文件：`PushIdTest.java`。

- 标记接口：0 方法、0 字段、非泛型、不继承其它接口。
- sealed（probed）：`isSealed()` true；`getPermittedSubclasses()` 恰一个，二进制名
  `java.net.http.HttpResponse$PushPromiseHandler$PushId$Http3PushId`（注意 `$` 而非 `.`）。
- Http3PushId 是 record 且非 sealed、同 nest；值相等语义（equals/hashCode 两个分量）。
- 经 default 4-arg 方法把 id 透传给 3-arg 回调；多态 List 组合。

## 5. 版本差异

- JDK-27 新增类型；早期版本不存在。

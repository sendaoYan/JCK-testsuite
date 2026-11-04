# java.net.http.HttpResponse.BodySubscriber 设计文档

## 1. API 功能

`Flow.Subscriber<List<ByteBuffer>>` + `getBody()` 返回 `CompletionStage<T>`；
由 BodyHandler.apply 产出，由 HttpClient 驱动（测试中手动驱动）。

## 2. 使用方式

主要经 `BodySubscribers.*` 工厂获得；自定义实现可在 onComplete 时完成自己的 future。

## 3. 参考来源

- JDK-27 `javap`；`jdk/internal/net/http/common/`。

## 4. 测试用例设计 + 易错规范细节

文件：`BodySubscriberTest.java`。

- 接口形状：extends Flow.Subscriber、仅一个声明方法 getBody 返回 CompletionStage、非 sealed。
- 生命周期：字节组装成 body；getBody() 每次返回同一 stage（probed）；
  完成前 stage 未完成；onError 精确 cause；无 onSubscribe 的 onError 仍失败 body；
  多个 onNext 列表按序拼接；空体 → 空 byte[]。
- 用户实现：getBody 由自身 future 驱动（"custom-2" 计数验证）。

## 5. 版本差异

- 自 JDK 11 稳定；JDK-8 无此包。

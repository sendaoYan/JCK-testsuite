# java.net.http.HttpResponse.BodySubscribers 设计文档

## 1. API 功能

静态工厂（返回 `BodySubscriber`）：ofString/ofByteArray/ofFile/ofInputStream/ofLines/ofPublisher/
replacing/discarding/buffering/mapping/limiting/ofByteArrayConsumer/fromSubscriber/fromLineSubscriber。

## 2. 使用方式

`BodySubscribers.mapping(BodySubscribers.ofByteArray(), bytes -> new String(bytes, UTF_8))` 组合。

## 3. 参考来源

- JDK-27 `javap`；`jdk/internal/net/http/ResponseBodySubscribers.java`。

## 4. 测试用例设计 + 易错规范细节

文件：`BodySubscribersTest.java`。

- 16 个工厂非空；20+ 负例（精确异常）：各 null NPE；buffering 0/-2 IAE；limiting -1 IAE（0 容忍 probed）；
  fromLineSubscriber null 分隔符容忍（系统默认，probed）。
- 行为驱动：ofString 分块/charset/空、ofByteArray、discarding、replacing(null)（probed）、
  mapping 可组合、ofLines 跨块+空、ofInputStream、ofByteArrayConsumer 终止标记、
  fromSubscriber finisher 在完成后应用（probed）、fromLineSubscriber 行内容、ofFile 写盘。
- ofPublisher（probed）：无数据先 onComplete 后订阅照常完成；有数据必须先订阅并 request 再 onNext，
  否则内部 NPE；文本 "p1p2q"；下游需求逐级上传（demanded==3）；第二个订阅者收到 onError ISE。
- 错误传播：ofString 与 mapping 的 onError 精确 cause。

## 5. 版本差异

- 自 JDK 11 稳定；JDK-8 无此包。

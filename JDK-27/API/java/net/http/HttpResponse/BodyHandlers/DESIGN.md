# java.net.http.HttpResponse.BodyHandlers 设计文档

## 1. API 功能

静态工厂（返回 `BodyHandler`）：discarding/ofString/ofByteArray/ofLines/ofInputStream/ofPublisher/
replacing/ofByteArrayConsumer/ofFile/ofFileDownload/buffering/limiting/fromSubscriber/fromLineSubscriber。

## 2. 使用方式

`client.send(req, HttpResponse.BodyHandlers.ofString())`。

## 3. 参考来源

- JDK-27 `javap`；`jdk/internal/net/http/ResponseBodyHandlers.java`。

## 4. 测试用例设计 + 易错规范细节

文件：`BodyHandlersTest.java`。

- 17 个工厂非空 + 每个对固定 ResponseInfo apply 出可用 subscriber。
- 全 Flow 周期驱动：discarding→null、replacing→哨兵与 replacing(null)→null（probed）、
  ofString 分块拼接、ofByteArray 字节、ofLines 跨块切行、ofInputStream 可读、
  ofByteArrayConsumer 2 数据回调+Optional.empty 终止标记（probed）、
  fromLineSubscriber+finisher、ofFile 写盘（/tmp scratch）、buffering/limiting 透明。
- 负例：buffering null/0、limiting -1（0 容忍 probed）、各工厂 null NPE；
  onError 精确 cause 传播到 getBody()。

## 5. 版本差异

- 自 JDK 11 稳定（JDK 12 补充 ofFileDownload 等）；JDK-8 无此包。

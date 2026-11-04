# java.net.http.HttpRequest.BodyPublisher 设计文档

## 1. API 功能

请求体的 Flow.Publisher 形态接口：`contentLength()`（long，未知为 -1）+ 继承
`Flow.Publisher<ByteBuffer>` 的 `subscribe(Flow.Subscriber)`。

## 2. 使用方式

`request.POST(HttpRequest.BodyPublishers.ofString("..."))`；自定义实现仅需提供
contentLength 与订阅逻辑，接口未 sealed，可被用户实现。

## 3. 参考来源

- JDK-27 `javap -public java.net.http.HttpRequest$BodyPublisher`；
  `jdk/internal/net/http/common/` 发布器实现。

## 4. 测试用例设计 + 易错规范细节

文件：`BodyPublisherTest.java`。

- 接口形状：extends Flow.Publisher、仅一个声明方法 contentLength、非 sealed。
- 订阅驱动：Collector 订阅者按单位需求拉取；ofString("hi") 单次 onNext 同步送达精确字节；
  `request(100)` 不改变 onNext 次数；noBody 无任何 onNext 直接完成。
- 字节精确性：ofByteArray 字节序、UTF_16BE 精确字节 [0,104,0,105]、ofByteArrays 分块顺序。
- 负例（Flow 语义，probed）：`request(0)`/`request(-1)` → onError IAE
  "non-positive subscription request: N"。
- 两次独立订阅互不干扰；用户实现（contentLength 42）可订阅；ofString("") 空完成。

## 5. 版本差异

- 自 JDK 11 稳定；JDK-8 无此包。

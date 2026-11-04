# java.net.http.HttpResponse.BodyHandler 设计文档

## 1. API 功能

函数式接口：`apply(ResponseInfo) -> BodySubscriber<T>`，在收到响应头后决定如何消费响应体。

## 2. 使用方式

直接用 `BodyHandlers.*` 工厂；或按状态码分支自定义：
`ri -> ri.statusCode() < 400 ? BodySubscribers.ofString() : BodySubscribers.replacing("err")`。

## 3. 参考来源

- JDK-27 `javap`；`jdk/internal/net/http/ResponseBodyHandlers.java`。

## 4. 测试用例设计 + 易错规范细节

文件：`BodyHandlerTest.java`。

- 接口形状：仅 apply 抽象、非 sealed、可 lambda 实现。
- 用户实现按 ResponseInfo.statusCode 分支（2xx/5xx）；对 {200,301,404,500,505} 与三种 Version 均 apply。
- 驱动行为（直接驱动返回的 subscriber）：ofString 组装、replacing 哨兵/`replacing(null)`→null body（probed）、
  discarding→null、ofByteArray 字节、ofString(UTF_16BE) 解码、buffering/limiting 透明、
  fromSubscriber finisher 在交付后应用（probed "items:1"）。
- 负例矩阵：buffering null/0/-1、limiting null/-1（0 容忍）、ofFile/ofFileDownload/ofString/
  ofByteArrayConsumer/fromSubscriber×3/fromLineSubscriber×2 的 null NPE。

## 5. 版本差异

- 自 JDK 11 稳定；JDK-8 无此包。

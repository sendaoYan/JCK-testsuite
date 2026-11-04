# java.net.http.HttpRequest.BodyPublishers 设计文档

## 1. API 功能

静态工厂集合：`ofString(s[, charset])`、`ofInputStream(Supplier)`、`ofByteArray([b, off, len])`、
`ofFile(Path)`、`ofByteArrays(Iterable)`、`noBody()`、`fromPublisher(p[, contentLength])`；
JDK-27 另有 `ofFileChannel(FileChannel, offset, length)` 与 `concat(BodyPublisher...)`。

## 2. 使用方式

`BodyPublishers.ofString("hi", StandardCharsets.UTF_16BE)`、`ofFile(path)`（FileNotFoundException 延迟到订阅）。

## 3. 参考来源

- JDK-27 `javap`；`jdk/internal/net/http/RequestPublishers.java`。

## 4. 测试用例设计 + 易错规范细节

文件：`BodyPublishersTest.java`。

- contentLength 精确矩阵：ofString 按 charset（UTF-8 2 / UTF-16 8 含 BOM / BE 6 / LE 6）、
  ofByteArray 偏移窗口、ofFile 真实文件长度+内容、ofFileChannel 窗口字节、concat 求和、
  未知长度（ofByteArrays/fromPublisher 无长度）恒 -1（probed）。
- 负例（精确异常）：ofInputStream null NPE；ofByteArray 越界 IndexOutOfBoundsException；ofFile 缺失
  FileNotFoundException；fromPublisher (p,0)/(p,-3) IAE "non-positive contentLength"；concat null 数组 NPE；
  ofFileChannel 窗口越过 EOF IndexOutOfBounds。
- 延迟校验（probed）：ofByteArrays null 元素 → onNext 1 后 onError NPE；ofInputStream null 供应者 →
  onError IOException "Stream supplier returned null"。
- concat 顺序、混合未知长度 → -1；ofFile 两次订阅均完整完成。

## 5. 版本差异

- JDK-27 新增 `ofFileChannel` 与 `concat`；JDK-8 无此包。

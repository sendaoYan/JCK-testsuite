# java.net.FileNameMap 测试设计案（JDK-8）

## 1. API 功能

- 接口，单方法 `String getContentTypeFor(String fileName)`：按文件名（后缀）映射 MIME 类型。
- JDK 提供 impl：`URLConnection.getFileNameMap()`（读 lib/content-types.properties）。
- 相关静态工具：`URLConnection.guessContentTypeFromName(String)`、`guessContentTypeFromStream(InputStream)`。

## 2. 使用方式

`URLConnection.getFileNameMap().getContentTypeFor("x.txt")` → "text/plain"；可用
`URLConnection.setFileNameMap` 换成自定义映射；`guessContentTypeFrom*` 一次性格式探测。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/net/FileNameMap.html、URLConnection.html）。
- OpenJDK jdk8u `test/java/net/URLConnection/ContentType.html`（后缀表回归）。
- 探针：/tmp/probe-B/PUrl2.java（双侧运行，矩阵逐项实测）。

## 4. 测试用例设计（FileNameMapTest.java，89 断言）

| 测试点 | 方法 | 断言要点 |
|---|---|---|
| 接口形状 | testInterfaceShape | isInterface、public、恰好 1 个方法、签名 `String getContentTypeFor(String)`、abstract |
| 自定义实现 | testCustomImplementations | 匿名类/精确表/lambda 三实现互不干扰；null 参数契约由实现自定 |
| get/setFileNameMap | testGetSetFileNameMap | 默认表非空；**setter 后 getter 返回包装器（行为委托，非同一对象）**；自定义表生效；finally 恢复默认表；setFileNameMap(null) 不抛 |
| 后缀矩阵（稳定集） | 同上 + testGuessContentTypeFromName | .txt/.html/.htm/.gif/.jpg/.jpeg/.png/.pdf/.xml/.zip/.tar/.wav 精确类型；大小写不敏感（.TXT/.HTML/.GIF/.PNG/.Jpg）；未知后缀/无后缀/空串 null；裸后缀 ".txt" 与路径 "a/b/c.txt" 均命中 |
| 流嗅探 | testGuessContentTypeFromStream | `<HTML>`/`<html>`/`<!doctype`/`<head>`/`<body>` → text/html；`<?xml` → application/xml；GIF87a/GIF89a → image/gif；纯文本/二进制/空流/1-2 字符流 → null（不抛）；**嗅探后流完全复位**（mark/reset，available 恢复全长）；null → NPE |
| guessContentTypeFromName 负例 | testGuessContentTypeFromName | null → NPE |

## 易错规范细节

- `guessContentTypeFromName` 后缀匹配**大小写不敏感**（properties 表按小写归一）。
- 嗅探要求流支持 mark/reset；ByteArrayInputStream 契约下 sniff 后 available()== 原全长。
- setFileNameMap 之后 getFileNameMap 不保证返回传入实例（JDK-8 返回内部包装类），断言行为而非身份。

## 版本差异（JDK-8 vs JDK-27，探针实测）

content-types.properties 在 JDK-9+ 扩充了后缀表，JDK-8 返回 null 的项在 JDK-27 有类型：

| 后缀 | JDK-8 | JDK-27 |
|---|---|---|
| .css | null | text/css |
| .js | null | text/javascript |
| .json | null | application/json |
| .svg | null | image/svg+xml |
| .doc | null | application/msword |
| .mp3 | null | audio/mpeg |
| .mp4 | null | video/mp4 |

两侧各自断言本版真实值；稳定集（.txt/.html/.gif/.jpg/.png/.pdf/.xml/.zip/.tar/.wav）两侧一致。

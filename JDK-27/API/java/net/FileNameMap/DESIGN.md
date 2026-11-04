# FileNameMap 设计文档

## 一、API 功能

接口，根据文件名返回 MIME 内容类型。

## 二、Java 用户如何使用

用户实现 getContentTypeFor(String) 或使用 URLConnection.getFileNameMap() 的默认实现。

## 三、开源代码实际用法调研

JDK 默认 FileNameMap 基于 mime.types 判断类型。

## 四、测试用例设计

验证为接口；URLConnection.getFileNameMap() 非空且对 .txt/.html 返回 text/plain、text/html。

## 缺口分析与深化（批次 B，2026-11-05）

原版 7 断言。深化后 92 断言，新增：

| 深化点 | 方法 | 断言要点 |
|---|---|---|
| 接口形状 | testInterfaceShape | 恰好 1 个方法、签名/返回类型/abstract |
| 自定义实现 | testCustomImplementations | 匿名/精确表/lambda 互不干扰 |
| get/setFileNameMap | testGetSetFileNameMap | 默认表稳定集精确类型；大小写不敏感；setter 后 getter 返回包装器（行为委托非身份）；恢复；setFileNameMap(null) 不抛 |
| 稳定后缀矩阵 | 同上 | .txt/.html/.htm/.gif/.jpg/.jpeg/.png/.pdf/.xml/.zip/.tar/.wav 与 JDK-8 一致 |
| JDK-9+ 新增后缀 | testGuessContentTypeFromName | **.css→text/css、.js→text/javascript、.json→application/json、.svg→image/svg+xml、.doc→application/msword、.mp3→audio/mpeg、.mp4→video/mp4**（JDK-8 全为 null——版本差异） |
| 流嗅探 | testGuessContentTypeFromStream | html 五种前缀、xml、GIF87a/89a；纯文本/二进制/空/短流 null 不抛；嗅探后流完全复位；null → NPE |

## 版本差异（JDK-8 vs JDK-27）

content-types.properties 扩充（.css/.js/.json/.svg/.doc/.mp3/.mp4 七项 JDK-8 null → JDK-27 有类型，探针逐项实测）；
稳定集与流嗅控行为两侧一致。

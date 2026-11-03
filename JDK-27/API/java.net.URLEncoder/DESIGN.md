# URLEncoder 设计文档

## 一、API 功能

把字符串按 application/x-www-form-urlencoded 规则做百分号编码，空格编码为 +，用于表单/查询串参数编码。

## 二、Java 用户如何使用

用户调用 URLEncoder.encode(String, Charset) 把表单字段值编码后拼接到 URL 查询串。

## 三、开源代码实际用法调研

几乎所有 Web 客户端/爬虫在构造 GET 查询串时使用 URLEncoder；Spring 的 UriUtils 亦基于同规则。

## 四、测试用例设计

空格→+、字母数字不变、非 ASCII 用 UTF-8 百分号编码、不同 Charset 产生不同结果、往返 encode/decode 一致。

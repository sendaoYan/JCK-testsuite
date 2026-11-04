# URLDecoder 设计文档

## 一、API 功能

URLEncoder 的逆操作，把百分号编码与 + 解码回原始字符串。

## 二、Java 用户如何使用

用户调用 URLDecoder.decode(String, Charset) 解码查询串或路径中的百分号编码内容。

## 三、开源代码实际用法调研

Servlet 容器、Web 框架解析请求参数时使用 URLDecoder（或等价实现）。

## 四、测试用例设计

+ → 空格、%XX 解码、UTF-8 多字节往返一致、非法 % 序列抛 IllegalArgumentException。

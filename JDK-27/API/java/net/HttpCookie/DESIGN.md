# HttpCookie 设计文档

## 一、API 功能

表示单个 HTTP Cookie 的键值及域/路径/有效期/安全等属性，可解析与序列化 Set-Cookie 头。

## 二、Java 用户如何使用

用户 new HttpCookie(name, value) 或用 HttpCookie.parse(header) 解析，设置 setDomain/setPath/setMaxAge/setSecure/setHttpOnly。

## 三、开源代码实际用法调研

Cookie 解析与生成在 Web 客户端/服务器与爬虫中广泛使用。

## 四、测试用例设计

构造与 getName/getValue/getDomain/getPath/getMaxAge/getSecure/isHttpOnly；parse 单/多头；domainMatches；toString 生成头格式。

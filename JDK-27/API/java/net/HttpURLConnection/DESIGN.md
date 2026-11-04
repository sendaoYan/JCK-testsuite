# HttpURLConnection 设计文档

## 一、API 功能

URLConnection 的 HTTP/HTTPS 专用子类，提供请求方法、响应码、重定向、跟随跳转、断开连接等 HTTP 语义。

## 二、Java 用户如何使用

用户把 URL.openConnection() 结果强转为 HttpURLConnection，调用 setRequestMethod/getResponseCode/getHeaderField/disconnect() 做 HTTP 请求。

## 三、开源代码实际用法调研

Apache HttpClient 之前的 JDK 原生 HTTP 客户端即为 HttpURLConnection，被无数轻量客户端与工具类使用。

## 四、测试用例设计

验证为 abstract 且继承 URLConnection；静态常量 HTTP_OK=200 等；子类实例上 setRequestMethod/getRequestMethod、followRedirects 默认与设置。

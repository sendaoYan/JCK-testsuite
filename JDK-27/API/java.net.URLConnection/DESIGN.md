# URLConnection 设计文档

## 一、API 功能

抽象基类，表示到 URL 的通信连接，定义请求属性、超时、缓存、内容类型猜测等通用行为，子类实现 connect()。

## 二、Java 用户如何使用

用户通过 URL.openConnection() 获取子类实例，设置 setDoInput/setDoOutput/setConnectTimeout/setReadTimeout 与请求属性后 connect() 并 getInputStream()。

## 三、开源代码实际用法调研

JDK 内建 http/file/jar 协议实现均继承 URLConnection；框架在其上封装 HTTP 客户端。

## 四、测试用例设计

用匿名子类实现 connect()；验证 connect/read 超时、doInput/doOutput/useCaches 默认与设置、请求属性 set/add/get 语义、guessContentTypeFromName。

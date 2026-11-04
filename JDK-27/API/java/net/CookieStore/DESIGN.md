# CookieStore 设计文档

## 一、API 功能

接口，表示 Cookie 存储，支持增删查与按 URI 匹配。

## 二、Java 用户如何使用

用户 new InMemoryCookieStore() 或自定义，传给 CookieManager。

## 三、开源代码实际用法调研

CookieManager 默认使用 InMemoryCookieStore。

## 四、测试用例设计

add/get/remove/removeAll/getURIs 语义；get 按 URI+请求属性返回匹配 Cookie；用 new InMemoryCookieStore() 实例验证。

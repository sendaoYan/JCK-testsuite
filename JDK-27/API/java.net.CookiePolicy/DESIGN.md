# CookiePolicy 设计文档

## 一、API 功能

接口，判定是否接受来自某 URI 的 Cookie。

## 二、Java 用户如何使用

用户实现 shouldAccept(uri, cookie) 或使用内建 ACCEPT_ALL/ACCEPT_NONE/ACCEPT_ORIGINAL_SERVER 传给 CookieManager。

## 三、开源代码实际用法调研

浏览器/客户端按隐私策略定制 Cookie 接受规则。

## 四、测试用例设计

三个内建策略常量与 shouldAccept 行为；自定义策略被调用。

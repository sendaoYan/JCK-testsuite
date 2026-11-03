# CookieManager 设计文档

## 一、API 功能

默认 CookieHandler 实现，内置内存 CookieStore 与 CookiePolicy 管理会话/持久 Cookie。

## 二、Java 用户如何使用

用户 new CookieManager() 或指定 store/policy，用 CookieManager.setDefault 注册后由 HttpURLConnection 自动管理 Cookie。

## 三、开源代码实际用法调研

Web 客户端(爬虫、测试框架)用 CookieManager 维持会话。

## 四、测试用例设计

get/setCookiePolicy、getCookieStore 非空；put 后 get 返回对应 Cookie；默认策略 ACCEPT_ORIGINAL_SERVER。

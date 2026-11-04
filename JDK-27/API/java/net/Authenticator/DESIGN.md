# Authenticator 设计文档

## 一、API 功能

抽象类，为需要认证的连接提供用户名/密码凭据。

## 二、Java 用户如何使用

用户继承 Authenticator 重写 getPasswordAuthentication()，用 Authenticator.setDefault 注册，供 URL/HttpURLConnection 认证。

## 三、开源代码实际用法调研

需要 HTTP Basic/代理认证的客户端注册默认认证器。

## 四、测试用例设计

setDefault/getDefault；子类 requestPasswordAuthentication 返回 PasswordAuthentication；getRequestingHost 等上下文。

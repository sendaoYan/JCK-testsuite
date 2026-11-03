# PasswordAuthentication 设计文档

## 一、API 功能

封装用户名与密码的不可变凭据对象(密码以 char[] 保存)。

## 二、Java 用户如何使用

用户在 Authenticator 子类中返回 new PasswordAuthentication(user, pass)。

## 三、开源代码实际用法调研

认证回调返回此类型给 JDK 网络栈。

## 四、测试用例设计

getUserName/getPassword/getChars；构造对输入密码做防御性拷贝、返回内部引用；equals 语义。

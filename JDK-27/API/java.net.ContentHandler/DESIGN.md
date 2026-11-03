# ContentHandler 设计文档

## 一、API 功能

抽象类，把 URLConnection 的内容转换为特定对象类型。

## 二、Java 用户如何使用

用户继承实现 getContent(URLConnection) 或 getContent(URLConnection, Class[])。

## 三、开源代码实际用法调研

JDK 内建文本/图片内容处理器派生自此。

## 四、测试用例设计

验证 abstract 及 getContent 重载；匿名子类返回固定对象。

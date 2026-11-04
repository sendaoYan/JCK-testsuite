# URLPermission 设计文档

## 一、API 功能

表示访问某个 URL 及其动作的权限，用于安全管理器下对 URLConnection 访问做授权。

## 二、Java 用户如何使用

用户在策略文件中写 permission java.net.URLPermission "http://host/*", "GET:HEAD" 授予网络访问。

## 三、开源代码实际用法调研

Java EE/安全管理器时代的服务器与沙箱程序用 URLPermission 限制网络。

## 四、测试用例设计

构造 name 与 actions，getName/getActions/implies 层级判断；getMethods 返回动作集合；非法参数抛异常。

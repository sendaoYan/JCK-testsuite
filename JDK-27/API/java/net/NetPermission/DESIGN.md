# NetPermission 设计文档

## 一、API 功能

网络相关安全权限的名称表示(如 setDefaultAuthenticator)。

## 二、Java 用户如何使用

用户在策略文件或代码中构造 NetPermission("name") 授予网络操作。

## 三、开源代码实际用法调研

安全管理器时代用于控制网络全局状态修改。

## 四、测试用例设计

构造 name/actions(空)、implies 同名、equals/hashCode、空名/空指针异常。

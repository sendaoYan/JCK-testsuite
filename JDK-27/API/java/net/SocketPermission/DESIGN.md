# SocketPermission 设计文档

## 一、API 功能

按主机+端口范围与动作(accept/connect/listen/resolve)定义的网络权限。

## 二、Java 用户如何使用

用户在策略文件写 java.net.SocketPermission "host:port", "connect,accept"。

## 三、开源代码实际用法调研

安全管理器下沙箱网络访问控制。

## 四、测试用例设计

构造 name/actions、getActions 规范化、implies 主机/端口/动作层级、equals/hashCode、非法参数异常。

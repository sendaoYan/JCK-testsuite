# DatagramPacket 设计文档

## 一、API 功能

UDP 数据报的载体，包含数据缓冲、偏移、长度与远端/本地地址端口。

## 二、Java 用户如何使用

用户 new DatagramPacket(buf, len, addr, port) 发送；new DatagramPacket(buf, len) 接收后用 getData/getAddress/getPort 读取。

## 三、开源代码实际用法调研

所有基于 DatagramSocket 的 UDP 应用都使用 DatagramPacket。

## 四、测试用例设计

构造与 getData/getOffset/getLength/getAddress/getPort；setAddress/setPort/setData/setLength 修改；getSocketAddress 对应远端地址。

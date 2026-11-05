# jdk.dynalink.linker.LinkRequest 设计案

## 1. API 功能
链接请求接口：descriptor+arguments+receiver。

## 2. Java 用户如何使用
SimpleLinkRequest 实现。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/linker/LinkRequest.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 interface；黑盒降级。
## 5. 契约深化（JDK-27）
- 接口面精确集合 {getArguments, getCallSiteDescriptor, getReceiver, isCallSiteUnstable, replaceArguments}。
- 真实观察：自定义 linker 收到的 request 的 receiver=实参0、参数含 receiver、descriptor 类型来自 site、首链 stable。
- replaceArguments 新实例+共享 descriptor；SimpleLinkRequest unstable=true 通道。

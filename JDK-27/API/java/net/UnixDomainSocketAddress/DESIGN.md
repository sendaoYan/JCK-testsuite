# UnixDomainSocketAddress 设计文档

## 一、API 功能

Unix 域套接字地址，表示文件系统路径端点，用于本机进程间通信(JDK 16+)。

## 二、Java 用户如何使用

用户用 UnixDomainSocketAddress.of(path) 构造，传给 SocketChannel.open(StandardProtocolFamily.UNIX) 或 ServerSocketChannel.bind。

## 三、开源代码实际用法调研

本机高性能 IPC(如 Docker daemon、systemd)使用 Unix 域套接字。

## 四、测试用例设计

of/getPath/equals/hashCode/toString；不同路径不相等；继承 SocketAddress。

## 5. 深化缺口分析(批次 J,2026-11-05)

原 16 断言。深化到 144 断言(7 个新部分):

- **形状精确化**(probed):唯一**私有**构造器 (Path)(纯工厂封装);私有
  `writeReplace`/`readObject`/`readObjectNoData`(后两者声明 throws
  InvalidObjectException——禁止默认反序列化路径);serialVersionUID 实测
  `92902496589351288L`;**无 compareTo、不实现 Comparable**(任务简报猜测
  implements Comparable<SocketAddress>,javap + 反射双重反证——差异记录)。
- **of(String) 矩阵**:9 样例(绝对/深层/相对/嵌套相对/空串/带空格/Unicode/
  点段/尾斜杠)round-trip + isAbsolute 分类;尾斜杠被 Path 折叠("/tmp/x.sock/"
  → "/tmp/x.sock");Path 组件逐段断言。
- **of(Path) 语义**:`getPath() == 原引用`(实测存引用,断言 ==);跨工厂重载
  equals/hashCode 一致;相对 Path;空 Path 合法;**of(String) 每次新实例**
  (无 intern,断言 !=),内部 Path 也非缓存。
- **equals/hashCode 矩阵**:对称/自反/异类(String、InetSocketAddress、
  unresolved)/null;hashCode 委托 path(实测 `== path.hashCode()`);点段不归一
  ("/tmp/./a.sock" != "/tmp/a.sock");空路径互等、与绝对路径不等;稳定性。
- **负例精确表**:of((String)null)/of((Path)null) → NPE;含 \u0000(首/中/尾)
  → InvalidPathException,IPE payload 精确:input 原样回显、
  reason=="Nul character not allowed"、index==-1。
- **序列化 round-trip**(writeReplace 代理形式):类保持、equals 对称、path/
  toString/hashCode 全保持;相对路径、空路径均存活。
- **真实 AF_UNIX 集成**(临时目录,全部有界):bind 后 socket 特殊文件存在于
  文件系统;`getLocalAddress()` 返回 UnixDomainSocketAddress 且 equals 构造值;
  **同路径二次 bind → BindException("Address already in use")**;client 经同一
  地址值 connect 成功且 `getRemoteAddress()` equals;acceptor(daemon 线程,
  join 3000ms)accept 后单字节回显 42;accept 侧 peer 地址类型;close 后
  `getLocalAddress()` → ClosedChannelException;**bind 到不存在目录 →
  SocketException(平台 OS 原因文本，随 locale 本地化，不含路径)**(注意不是
  NoSuchFileException);目录清理。

## 6. 版本差异

JDK 16+ 存在(JDK-8245856)。JDK-8 反证(temurin jdk8u372-b07):

```
$ javap -public java.net.UnixDomainSocketAddress
Error: class not found: java.net.UnixDomainSocketAddress
```

不做 JDK-8 镜像。关联 `StandardProtocolFamily.UNIX`(批次 F 已覆盖,不重复)。

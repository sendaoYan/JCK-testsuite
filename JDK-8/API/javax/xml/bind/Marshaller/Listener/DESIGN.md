# Marshaller.Listener 设计案（JDK-8 契约测试）

## 1. API 功能
marshal 生命周期钩子抽象类：beforeMarshal(source)/afterMarshal(source) 两个**空默认实现**的具体方法（非抽象），子类选择性覆写。

## 2. 使用方式
Marshaller.setListener(subclass) → marshal 时回调。规范措辞是"可能调用"；JDK-8 RI 对每个对象至少调用一次 before+after（实测根对象回调 2 次）。

## 3. 参考来源
Oracle javadoc javax.xml.bind.Marshaller.Listener、JSR 222、OpenJDK8 com.sun.xml.internal.bind.v2.runtime（XMLSerializer 驱动）、本仓库 ListenerTest/MarshallerTest。

## 4. 测试用例设计 + 易错规范细节
- 结构：abstract 类、恰 2 个声明方法、均具体 no-op、(Object) 单参、public 构造可子类化。
- 生命周期：记录型子类 before/after 各 >=1、首 before 末 after、参数即被序列化对象（Person）。
- 行为闭环：beforeMarshal 内改字段 → 输出含 mutated（钩子在序列化前生效）；setListener(null) 停止回调；空子类（不覆写）等价无 listener。
- 易错点：**勿断言精确调用次数**（RI 对根对象回调 2 次）；Listener 是抽象类不是接口（接口版不存在）。

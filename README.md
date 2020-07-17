# xc-zookeeper
## Tutorial List
### tutorial-curator
Zookeeper Tutorial Base On Curator

|||
|---|---|
|/leader|Example leader selector code|
|/cache|Example PathChildrenCache usage|
|/locking|Example of using InterProcessMutex|
|/discovery|Example usage of the Curator's ServiceDiscovery|
|/framework|A few examples of how to use the CuratorFramework class|
|/async|Example AsyncCuratorFramework code|
|/modeled|ModeledFramework and Modeled Cache examples|

本次使用`Curator-4.2.*`针对`zookeeper-3.5.*`版本

---

## Cloud List
### spring cloud zookeeper
```bash
- service : 服务注册中心,服务提供者
- client : 消费者,Fegin
- config : 配置中心
- gateway : 网关服务
- zuul : 网关服务zuul
``` 

需要提前在`zk`中执行初始化命令
```bash
create /config/xc-zookeeper-config,dev/hello.message "Hello World from dev profile"
create /config/xc-zookeeper-config,test/hello.message "Hello World from dev profile"
```

---

## Reference

[Apache-Curator](http://curator.apache.org/curator-examples/index.html)

[curator-examples](http://curator.apache.org/curator-examples/index.html)

[使用Curator时对应的zookeeper版本](http://curator.apache.org/zk-compatibility.html)

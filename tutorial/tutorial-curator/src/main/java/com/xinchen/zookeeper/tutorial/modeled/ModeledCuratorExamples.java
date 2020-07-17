package com.xinchen.zookeeper.tutorial.modeled;

import com.xinchen.zookeeper.tutorial.modeled.model.PersonModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.x.async.modeled.JacksonModelSerializer;
import org.apache.curator.x.async.modeled.ModelSpec;
import org.apache.curator.x.async.modeled.ModeledFramework;
import org.apache.curator.x.async.modeled.ZPath;

import java.util.function.Consumer;

/**
 *
 * Curator的模型使用例子
 *
 * @author xinchen
 * @version 1.0
 * @date 08/07/2019 10:50
 */
@Slf4j
public class ModeledCuratorExamples {

    public static ModeledFramework<PersonModel> wrap(AsyncCuratorFramework client){

        JacksonModelSerializer<PersonModel> serializer = JacksonModelSerializer.build(PersonModel.class);

        // 构建模型规范 - 您可以在启动时预先为应用程序构建所有模型规范
        ModelSpec<PersonModel> modelSpec = ModelSpec.builder(ZPath.parse("/example/path"), serializer).build();


        // 包装CuratorFramework实例，以便可以使用“建模”。
        // 执行一次并重新使用返回的ModeledFramework实例。
        // ModeledFramework实例绑定到给定路径
        return ModeledFramework.wrap(client, modelSpec);
    }


    public static void createOrUpdate(ModeledFramework<PersonModel> modeled,PersonModel model){

        // 将受影响的路径改为建模的基本路径加上id
        // "/example/path/{id}"
        ModeledFramework<PersonModel> atId = modeled.child(model.getId().getId());

        // 默认情况下,ModeledFramework实例将会更新节点(如果存在)
        // 所以这里将创建或者更新节点
        // 注意: 这个操作是异步的
        atId.set(model);
    }


    public static void readPerson(ModeledFramework<PersonModel> modeled, String id, Consumer<PersonModel> receiver){
        //读取具有给定ID的人，并在读取后异步调用接收者

        modeled.child(id).read().whenComplete(((person, exception) -> {
            if (null != exception){
                log.error("Error: {}",exception);
            } else {
                log.info("receiver : {}",person);
                receiver.accept(person);
            }
        }));
    }

}

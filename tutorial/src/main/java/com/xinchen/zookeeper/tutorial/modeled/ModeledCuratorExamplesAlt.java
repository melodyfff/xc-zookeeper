package com.xinchen.zookeeper.tutorial.modeled;

import com.xinchen.zookeeper.tutorial.modeled.model.ContainerType;
import com.xinchen.zookeeper.tutorial.modeled.model.PersonId;
import com.xinchen.zookeeper.tutorial.modeled.model.PersonModel;
import com.xinchen.zookeeper.tutorial.modeled.model.PersonModelSpec;
import org.apache.curator.x.async.modeled.ModeledFramework;

import java.util.function.Consumer;

/**
 *
 *
 * @author xinchen
 * @version 1.0
 * @date 08/07/2019 11:37
 */
public class ModeledCuratorExamplesAlt {
    public static void createOrUpdate(PersonModelSpec modelSpec, PersonModel model) {
        // change the affected path to be modeled's base path plus id: i.e. "/example/path/{id}"
        ModeledFramework<PersonModel> resolved = modelSpec.resolved(model.getContainerType(), model.getId());

        // by default ModeledFramework instances update the node if it already exists
        // so this will either create or update the node
        // note - this is async
        resolved.set(model);
    }

    public static void readPerson(PersonModelSpec modelSpec, ContainerType containerType, PersonId id, Consumer<PersonModel> receiver) {
        ModeledFramework<PersonModel> resolved = modelSpec.resolved(containerType, id);

        // read the person with the given ID and asynchronously call the receiver after it is read
        resolved.read().whenComplete((person, exception) -> {
            if (exception != null) {
                // handle the error
                exception.printStackTrace();
            } else {
                receiver.accept(person);
            }
        });
    }
}

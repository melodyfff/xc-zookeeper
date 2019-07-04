package com.xinchen.zookeeper.tutorial.discovery;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * 负载细节
 *
 * @author xinchen
 * @version 1.0
 * @date 04/07/2019 08:51
 */
@Getter
@Setter
public class InstanceDetails {

    private String description;

    public InstanceDetails(){
        this("");
    }

    public InstanceDetails(String description) {
        this.description = description;
    }

}

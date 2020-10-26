package com.rengu.operationsmanagementsuitev3.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

/**
 * @program: operations-management-suite-v3
 * @author: hanch
 * @create: 2018-09-12 17:14
 **/

@Data
public class DeployProgressEntity {

    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private String hostAddress;
    private double speed;
    private double progress;
    private int state;
    private String description;

    public DeployProgressEntity(String hostAddress, double speed, double progress, int state, String description) {
        this.hostAddress = hostAddress;
        this.speed = speed;
        this.progress = progress;
        this.state = state;
        this.description = description;
    }
}

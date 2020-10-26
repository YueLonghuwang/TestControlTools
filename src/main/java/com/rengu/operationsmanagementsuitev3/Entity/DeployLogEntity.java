package com.rengu.operationsmanagementsuitev3.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-05 13:07
 **/

@Data
@Entity
public class DeployLogEntity implements Serializable {

    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime = new Date();
    private Date finishTime;
    private long totalFileSize;
    private long totalSendSize;
    private double speed;
    private double progress;
    private boolean complete = true;
    private String message;
    private String type;
    private String version; //  组件版本
    @ManyToOne
    private ProjectEntity projectEntity;
}

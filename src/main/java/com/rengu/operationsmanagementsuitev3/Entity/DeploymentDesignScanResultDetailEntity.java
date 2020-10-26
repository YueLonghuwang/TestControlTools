package com.rengu.operationsmanagementsuitev3.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-07 10:12
 **/

@Data
@Entity
public class DeploymentDesignScanResultDetailEntity implements Serializable {

    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private int type;
    private String name;
    private String targetPath;
    private String md5;
}

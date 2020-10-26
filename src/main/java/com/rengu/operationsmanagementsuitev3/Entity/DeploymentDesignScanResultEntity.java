package com.rengu.operationsmanagementsuitev3.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-07 10:10
 **/

@Data
@Entity
public class DeploymentDesignScanResultEntity implements Serializable {

    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private String orderId;
    @ManyToOne
    private DeploymentDesignDetailEntity deploymentDesignDetailEntity;
    @OneToMany(cascade = CascadeType.ALL)
    private List<DeploymentDesignScanResultDetailEntity> result;
}
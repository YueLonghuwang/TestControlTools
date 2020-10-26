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
 * Author: zhangqiankun
 * Date: 2020/8/10 8:55
 */
@Data
@Entity
public class CustomParametersEntity implements Serializable {

    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime = new Date();
    private String attributeType;        //参数类型
    private String attributeNames;       //属性名
    private String attributeValues;       //属性值
    private String description;             //描述

}

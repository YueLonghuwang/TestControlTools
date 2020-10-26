package com.rengu.operationsmanagementsuitev3.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Author: Zhangqiankun
 * Date: 2020/8/4 15:41
 */
@Data
public class ResultString implements Serializable {
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date date = new Date();
    private String data;
}

package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Entity.DeviceEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ResultEntity;
import com.rengu.operationsmanagementsuitev3.Service.DeviceService;
import com.rengu.operationsmanagementsuitev3.Utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-23 11:09
 **/

@RestController
@RequestMapping(value = "/devices")
public class DeviceController {

    private final DeviceService deviceService;

    @Autowired
    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    // 根据Id复制设备
    @PostMapping(value = "/{deviceId}/copy")
    public ResultEntity copyDeviceById(@PathVariable(value = "deviceId") String deviceId) {
        return ResultUtils.build(deviceService.copyDeviceById(deviceId));
    }

    // 根据Id删除设备
    @DeleteMapping(value = "/{deviceId}")
    public ResultEntity deleteDeviceById(@PathVariable(value = "deviceId") String deviceId) {
        return ResultUtils.build(deviceService.deleteDeviceById(deviceId));
    }

    // 根据Id撤销删除设备
    @PatchMapping(value = "/{deviceId}/restore")
    public ResultEntity restoreDeviceById(@PathVariable(value = "deviceId") String deviceId) {
        return ResultUtils.build(deviceService.restoreDeviceById(deviceId));
    }

    // 根据Id清除设备
    @DeleteMapping(value = "/{deviceId}/clean")
    public ResultEntity cleanDeviceById(@PathVariable(value = "deviceId") String deviceId) {
        return ResultUtils.build(deviceService.cleanDeviceById(deviceId));
    }

    // 根据Id修改设备
    @PatchMapping(value = "/{deviceId}")
    public ResultEntity updateDeviceById(@PathVariable(value = "deviceId") String deviceId, DeviceEntity deviceArgs) {
        return ResultUtils.build(deviceService.updateDeviceById(deviceId, deviceArgs));
    }

    // 根据Id查询设备
    @GetMapping(value = "/{deviceId}")
    public ResultEntity getDeviceById(@PathVariable(value = "deviceId") String deviceId) {
        return ResultUtils.build(deviceService.getDeviceById(deviceId));
    }

    // 查询所有设备
    @GetMapping
    @PreAuthorize(value = "hasRole('admin')")
    public ResultEntity getDevices(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResultUtils.build(deviceService.getDevices(pageable));
    }

    // 获取进程信息
    @GetMapping(value = "/{deviceId}/process")
    public ResultEntity getProcessById(@PathVariable(value = "deviceId") String deviceId) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        return ResultUtils.build(deviceService.getProcessById(deviceId));
    }

    // 获取磁盘信息
    @GetMapping(value = "/{deviceId}/disks")
    public ResultEntity getDisksById(@PathVariable(value = "deviceId") String deviceId) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        return ResultUtils.build(deviceService.getDisksById(deviceId));
    }
}

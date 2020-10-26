package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Entity.ProjectEntity;
import com.rengu.operationsmanagementsuitev3.Service.SimEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @Author YJH
 * @Date 2019/3/20 10:12
 */
@RestController
@RequestMapping("/SimEngine")
public class SimEngineController {
    private final SimEngineService simEngineService;


    @Autowired
    public SimEngineController(SimEngineService simEngineService) {
        this.simEngineService = simEngineService;
    }

    @PostMapping
    public void getSimEngineCmd(@RequestParam(value = "simEngineCmd") String simEngineCmd,@RequestBody ProjectEntity pid) {
        try {
            simEngineService.getSimEngineCmd(simEngineCmd,pid);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

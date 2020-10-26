package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Entity.ResultEntity;
import com.rengu.operationsmanagementsuitev3.Service.ComponentFileHistoryService;
import com.rengu.operationsmanagementsuitev3.Utils.ResultUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-03 14:23
 **/

@RestController
@RequestMapping(value = "/componentfilehistorys")
public class ComponentFileHistoryController {

    private final ComponentFileHistoryService componentFileHistoryService;

    @Autowired
    public ComponentFileHistoryController(ComponentFileHistoryService componentFileHistoryService) {
        this.componentFileHistoryService = componentFileHistoryService;
    }

    // 根据Id查询组件文件
    @GetMapping(value = "/{componentFileHistoryId}")
    public ResultEntity getComponentFileHistoryById(@PathVariable(value = "componentFileHistoryId") String componentFileHistoryId) {
        return ResultUtils.build(componentFileHistoryService.getComponentFileHistoryById(componentFileHistoryId));
    }

    // 根据Id导出组件文件
    @GetMapping(value = "/{componentFileHistoryId}/export")
    public void exportComponentFileHistoryById(@PathVariable(value = "componentFileHistoryId") String componentFileHistoryId, HttpServletResponse httpServletResponse) throws IOException {
        File exportFile = componentFileHistoryService.exportComponentFileHistoryById(componentFileHistoryId);
        String mimeType = URLConnection.guessContentTypeFromName(exportFile.getName()) == null ? "application/octet-stream" : URLConnection.guessContentTypeFromName(exportFile.getName());
        httpServletResponse.setContentType(mimeType);
        httpServletResponse.setHeader("Content-Disposition", "attachment;filename=" + new String(exportFile.getName().getBytes(StandardCharsets.UTF_8), "ISO8859-1"));
        httpServletResponse.setContentLengthLong(exportFile.length());
        // 文件流输出
        IOUtils.copy(new FileInputStream(exportFile), httpServletResponse.getOutputStream());
        httpServletResponse.flushBuffer();
    }
}

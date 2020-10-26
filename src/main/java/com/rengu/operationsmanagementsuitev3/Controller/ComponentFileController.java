package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentFileEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ResultEntity;
import com.rengu.operationsmanagementsuitev3.Service.ComponentFileService;
import com.rengu.operationsmanagementsuitev3.Service.ComponentService;
import com.rengu.operationsmanagementsuitev3.Utils.ResultUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-31 09:40
 **/

@RestController
@RequestMapping(value = "/componentfiles")
public class ComponentFileController {

    private final ComponentFileService componentFileService;
    private final ComponentService componentService;

    @Autowired
    public ComponentFileController(ComponentFileService componentFileService, ComponentService componentService) {
        this.componentFileService = componentFileService;
        this.componentService = componentService;
    }

    // 根据Id复制组件文件
    @PatchMapping(value = "/{sourceNodeId}/copy")
    public ResultEntity copyComponentFileById(@PathVariable(value = "sourceNodeId") String sourceNodeId, @RequestParam(value = "targetComponentId") String targetComponentId, @RequestParam(value = "targetNodeId", required = false, defaultValue = "") String targetNodeId) {
        return ResultUtils.build(componentFileService.copyComponentFileById(sourceNodeId, targetNodeId, componentService.getComponentById(targetComponentId)));
    }

    // 根据Id移动组件文件
    @PatchMapping(value = "/{sourceNodeId}/move")
    public ResultEntity moveComponentFileById(@PathVariable(value = "sourceNodeId") String sourceNodeId, @RequestParam(value = "targetComponentId") String targetComponentId, @RequestParam(value = "targetNodeId", required = false, defaultValue = "") String targetNodeId) throws IOException {
        return ResultUtils.build(componentFileService.moveComponentFileById(sourceNodeId, targetNodeId, componentService.getComponentById(targetComponentId)));
    }

    // 根据Id删除组件文件
    @DeleteMapping(value = "/{componentfileId}")
    public ResultEntity deleteComponentFileById(@PathVariable(value = "componentfileId") String componentfileId) throws IOException {
        return ResultUtils.build(componentFileService.deleteComponentFileById(componentfileId));
    }

    // 根据Id修改组件文件
    @PatchMapping(value = "/{componentfileId}")
    public ResultEntity updateComponentFileById(@PathVariable(value = "componentfileId") String componentfileId, ComponentFileEntity componentFileArgs) {
        return ResultUtils.build(componentFileService.updateComponentFileById(componentfileId, componentFileArgs));
    }

    // 根据Id查询组件文件
    @GetMapping(value = "/{componentfileId}")
    public ResultEntity getComponentFileById(@PathVariable(value = "componentfileId") String componentfileId) {
        return ResultUtils.build(componentFileService.getComponentFileById(componentfileId));
    }

    // 根据Id导出组件文件
    @GetMapping(value = "/{componentfileId}/export")
    public void exportComponentFileById(@PathVariable(value = "componentfileId") String componentfileId, HttpServletResponse httpServletResponse) throws IOException {
        File exportFile = componentFileService.exportComponentFileById(componentfileId);
        String mimeType = URLConnection.guessContentTypeFromName(exportFile.getName()) == null ? "application/octet-stream" : URLConnection.guessContentTypeFromName(exportFile.getName());
        httpServletResponse.setContentType(mimeType);
        httpServletResponse.setHeader("Content-Disposition", "attachment;filename=" + new String(exportFile.getName().getBytes(StandardCharsets.UTF_8), "ISO8859-1"));
        httpServletResponse.setContentLengthLong(exportFile.length());
        // 文件流输出
        IOUtils.copy(new FileInputStream(exportFile), httpServletResponse.getOutputStream());
        httpServletResponse.flushBuffer();
    }
}

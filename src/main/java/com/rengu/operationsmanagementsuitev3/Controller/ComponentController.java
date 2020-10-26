package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ComponentFileEntity;
import com.rengu.operationsmanagementsuitev3.Entity.FileMetaEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ResultEntity;
import com.rengu.operationsmanagementsuitev3.Service.ComponentFileService;
import com.rengu.operationsmanagementsuitev3.Service.ComponentHistoryService;
import com.rengu.operationsmanagementsuitev3.Service.ComponentService;
import com.rengu.operationsmanagementsuitev3.Thread.UDPReceiveThread;
import com.rengu.operationsmanagementsuitev3.Utils.ResultUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-27 14:42
 **/

@RestController
@RequestMapping(value = "/components")
public class ComponentController {

    private final ComponentService componentService;
    private final ComponentFileService componentFileService;
    private final ComponentHistoryService componentHistoryService;
    private final UDPReceiveThread udpReceiveThread;

    @Autowired
    public ComponentController(ComponentService componentService, ComponentFileService componentFileService, ComponentHistoryService componentHistoryService, UDPReceiveThread udpReceiveThread) {
        this.componentService = componentService;
        this.componentFileService = componentFileService;
        this.componentHistoryService = componentHistoryService;
        this.udpReceiveThread = udpReceiveThread;
    }

    // 根据id复制组件
    @PostMapping(value = "/{componentId}/copy")
    public ResultEntity copyComponentById(@PathVariable(value = "componentId") String componentId) {
        ComponentEntity componentArgs = componentService.getComponentById(componentId);
        ComponentEntity componentEntity = componentService.copyComponentById(componentArgs);
        componentFileService.copyComponentFileByComponent(componentArgs, componentEntity);
        return ResultUtils.build(componentEntity);
    }

    // 根据Id删除组件
    @DeleteMapping(value = "/{componentId}")
    public ResultEntity deleteComponentById(@PathVariable(value = "componentId") String componentId) {
        return ResultUtils.build(componentService.deleteComponentById(componentId));
    }

    // 根据Id撤销删除组件
    @PatchMapping(value = "/{componentId}/restore")
    public ResultEntity restoreComponentById(@PathVariable(value = "componentId") String componentId) {
        return ResultUtils.build(componentService.restoreComponentById(componentId));
    }

    // 根据id清除组件
    @DeleteMapping(value = "/{componentId}/clean")
    public ResultEntity cleanComponentById(@PathVariable(value = "componentId") String componentId) throws IOException {
        return ResultUtils.build(componentService.cleanComponentById(componentId));
    }


    // 根据Id修改组件
    @PatchMapping(value = "/{componentId}")
    public ResultEntity updateComponentById(@PathVariable(value = "componentId") String componentId, ComponentEntity componentArgs) {
        return ResultUtils.build(componentService.updateComponentById(componentId, componentArgs));
    }

//    //根据自定义id修改自定义
//    @PatchMapping(value = "customParametersId")
//    public ResultEntity updateCustomParamentersById(List<String> customParamentersId, List<Map<String,Object>> map){
//       for(String a:customParamentersId){
//           componentService.updateCustomParamentersById(a,map);
//       }
//        return ResultUtils.build();
//    }

    // 根据Id查询组件
    @GetMapping(value = "/{componentId}")
    public ResultEntity getComponentById(@PathVariable(value = "componentId") String componentId) {
        return ResultUtils.build(componentService.getComponentById(componentId));
    }

    // 查询所有组件
    @GetMapping
    @PreAuthorize(value = "hasRole('admin')")
    public ResultEntity getComponents(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResultUtils.build(componentService.getComponents(pageable));
    }

    // 根据Id查询组件历史
    @GetMapping(value = "/{componentId}/history")
    public ResultEntity getComponentHistorysByComponent(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable, @PathVariable(value = "componentId") String componentId) {
        return ResultUtils.build(componentHistoryService.getComponentHistorysByComponent(pageable, componentService.getComponentById(componentId)));
    }

    // 根据Id查询组件历史
    @GetMapping(value = "/{componentId}/historys")
    public ResultEntity getComponentHistorysByComponent(@PathVariable(value = "componentId") String componentId) {
        return ResultUtils.build(componentHistoryService.getComponentHistorysByComponent(componentService.getComponentById(componentId)));
    }

    // 根据id和父节点Id创建文件夹
    @PostMapping(value = "/{componentId}/createfolder")
    public ResultEntity saveComponentFileByParentNodeAndComponent(@PathVariable(value = "componentId") String componentId, @RequestHeader(value = "parentNodeId", required = false, defaultValue = "") String parentNodeId, ComponentFileEntity componentFileEntity) {
        return ResultUtils.build(componentFileService.saveComponentFileByParentNodeAndComponent(componentService.getComponentById(componentId), parentNodeId, componentFileEntity));
    }

    // 根据id和父节点Id创建文件
    @PostMapping(value = "/{componentId}/uploadfiles")
    public ResultEntity saveComponentFilesByParentNodeAndComponent(@PathVariable(value = "componentId") String componentId, @RequestHeader(value = "parentNodeId", required = false, defaultValue = "") String parentNodeId, @RequestBody List<FileMetaEntity> fileMetaEntityList) {
        return ResultUtils.build(componentFileService.saveComponentFilesByParentNodeAndComponent(componentService.getComponentById(componentId), parentNodeId, fileMetaEntityList));
    }

    // 根据id和父节点查询组件文件
    @GetMapping(value = "/{componentId}/files")
    public ResultEntity getComponentFilesByParentNodeAndComponent(@PathVariable(value = "componentId") String componentId, @RequestHeader(value = "parentNodeId", required = false, defaultValue = "") String parentNodeId) {
        return ResultUtils.build(componentFileService.getComponentFilesByParentNodeAndComponent(parentNodeId, componentService.getComponentById(componentId)));
    }

    // 根据id导出组件文件
    @GetMapping(value = "/{componentId}/export")
    public void exportComponentFileByComponent(@PathVariable(value = "componentId") String componentId, HttpServletResponse httpServletResponse) throws IOException {
        File exportFile = componentFileService.exportComponentFileByComponent(componentService.getComponentById(componentId));
        String mimeType = URLConnection.guessContentTypeFromName(exportFile.getName()) == null ? "application/octet-stream" : URLConnection.guessContentTypeFromName(exportFile.getName());
        httpServletResponse.setContentType(mimeType);
        httpServletResponse.setHeader("Content-Disposition", "attachment;filename=" + new String(exportFile.getName().getBytes(StandardCharsets.UTF_8), "ISO8859-1"));
        httpServletResponse.setContentLengthLong(exportFile.length());
        // 文件流输出
        IOUtils.copy(new FileInputStream(exportFile), httpServletResponse.getOutputStream());
        httpServletResponse.flushBuffer();
    }

    //根据文件id查询测试方案文件下的所有文件
    @GetMapping(value = "/{componentId}/filesText")
    public ResultEntity getFileByComponent(@PathVariable(value = "componentId")String componentId,@RequestHeader(value = "parentNodeId", required = false, defaultValue = "")String parentNodeId){
       List<ComponentFileEntity> componentFileList = componentFileService.getComponentFilesByComponents(parentNodeId,componentService.getComponentById(componentId));
        return ResultUtils.build(componentFileList);
    }

    // 集中管控发送初始化状态命令
    @PostMapping(value = "/{fileName}/centralizedControl/{status}")
    public void Component(@PathVariable(value = "fileName") String fileName, @PathVariable(value = "status") Integer status) throws IOException{
         udpReceiveThread.UDPInitializationState(fileName,status);
    }

    // 集中管控发送初始化状态命令
    @PostMapping(value = "/centralizedControl")
    public void Component(){
        udpReceiveThread.TCPMessageValuse();
    }

    // 集中管控发送状态命令（2.开始、3.暂定、4.结束）
    @PostMapping(value = "/centralizedControl/{status}")
    public void Component(@PathVariable(value = "status") Integer status) throws IOException{
        udpReceiveThread.UDPState(status);
    }
}

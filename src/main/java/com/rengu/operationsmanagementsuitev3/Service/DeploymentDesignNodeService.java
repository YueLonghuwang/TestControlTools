package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.*;
import com.rengu.operationsmanagementsuitev3.Repository.DeploymentDesignNodeRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import com.rengu.operationsmanagementsuitev3.Utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-04 10:01
 **/

@Slf4j
@Service
@Transactional
public class DeploymentDesignNodeService {

    private final DeploymentDesignNodeRepository deploymentDesignNodeRepository;
    private final DeploymentDesignDetailService deploymentDesignDetailService;

    @Autowired
    public DeploymentDesignNodeService(DeploymentDesignNodeRepository deploymentDesignNodeRepository, DeploymentDesignDetailService deploymentDesignDetailService) {
        this.deploymentDesignNodeRepository = deploymentDesignNodeRepository;
        this.deploymentDesignDetailService = deploymentDesignDetailService;
    }

    // 根据部署设计保存部署节点
    @CachePut(value = "DeploymentDesignNode_Cache", key = "#deploymentDesignNodeEntity.id")
    public DeploymentDesignNodeEntity saveDeploymentDesignNodeByDeploymentDesign(DeploymentDesignEntity deploymentDesignEntity, DeploymentDesignNodeEntity deploymentDesignNodeEntity) {
        deploymentDesignNodeEntity.setDeploymentDesignEntity(deploymentDesignEntity);
        return deploymentDesignNodeRepository.save(deploymentDesignNodeEntity);
    }

    // 根据部署设计复制部署设计节点
    public List<DeploymentDesignNodeEntity> copyDeploymentDesignNodesByDeploymentDesign(DeploymentDesignEntity sourceDeploymentDesign, DeploymentDesignEntity targetDeploymentDesign) {
        List<DeploymentDesignNodeEntity> deploymentDesignNodeEntityList = new ArrayList<>();
        for (DeploymentDesignNodeEntity sourceDeploymentDesignNodeEntity : getDeploymentDesignNodesByDeploymentDesign(sourceDeploymentDesign)) {
            DeploymentDesignNodeEntity deploymentDesignNodeEntity = new DeploymentDesignNodeEntity();
            BeanUtils.copyProperties(sourceDeploymentDesignNodeEntity, deploymentDesignNodeEntity, "id", "createTime", "deploymentDesignEntity");
            deploymentDesignNodeEntity.setDeploymentDesignEntity(targetDeploymentDesign);
            deploymentDesignNodeRepository.save(deploymentDesignNodeEntity);
            deploymentDesignDetailService.copyDeploymentDesignDetailsByDeploymentDesignNode(sourceDeploymentDesignNodeEntity, deploymentDesignNodeEntity);
            deploymentDesignNodeEntityList.add(deploymentDesignNodeEntity);
        }
        return deploymentDesignNodeEntityList;
    }

    // 根据Id绑定设备
    @CachePut(value = "DeploymentDesignNode_Cache", key = "#deploymentDesignNodeId")
    public DeploymentDesignNodeEntity bindDeviceById(String deploymentDesignNodeId, DeviceEntity deviceEntity) {
        DeploymentDesignNodeEntity deploymentDesignNodeEntity = getDeploymentDesignNodeById(deploymentDesignNodeId);
        if (hasDeploymentDesignNodeByDeviceAndDeploymentDesign(deviceEntity, deploymentDesignNodeEntity.getDeploymentDesignEntity())) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NODE_DEVICE_EXISTED + deviceEntity.getHostAddress());
        }
        deploymentDesignNodeEntity.setDeviceEntity(deviceEntity);
        return deploymentDesignNodeRepository.save(deploymentDesignNodeEntity);
    }

    @CacheEvict(value = "DeploymentDesignNode_Cache", key = "#deploymentDesignNodeEntity.id")
    public DeploymentDesignNodeEntity deleteDeploymentDesignNodeById(DeploymentDesignNodeEntity deploymentDesignNodeEntity) {
        for (DeploymentDesignDetailEntity deploymentDesignDetailEntity : deploymentDesignDetailService.getDeploymentDesignDetailsByDeploymentDesignNode(deploymentDesignNodeEntity)) {
            deploymentDesignDetailService.deleteDeploymentDesignDetailById(deploymentDesignDetailEntity);
        }
        deploymentDesignNodeRepository.delete(deploymentDesignNodeEntity);
        return deploymentDesignNodeEntity;
    }

    @CacheEvict(value = "DeploymentDesignNode_Cache", allEntries = true)
    public void deleteDeploymentDesignNodeByDevice(DeviceEntity deviceEntity) {
        for (DeploymentDesignNodeEntity deploymentDesignNodeEntity : getDeploymentDesignNodesByDevice(deviceEntity)) {
            unbindDeviceById(deploymentDesignNodeEntity.getId());
        }
    }

    // 根据Id解绑设备
    @CachePut(value = "DeploymentDesignNode_Cache", key = "#deploymentDesignNodeId")
    public DeploymentDesignNodeEntity unbindDeviceById(String deploymentDesignNodeId) {
        DeploymentDesignNodeEntity deploymentDesignNodeEntity = getDeploymentDesignNodeById(deploymentDesignNodeId);
        deploymentDesignNodeEntity.setDeviceEntity(null);
        return deploymentDesignNodeRepository.save(deploymentDesignNodeEntity);
    }

    // 根据Id判断部署设计节点是否存在
    public boolean hasDeploymentDesignNodeById(String deploymentDesignNodeId) {
        if (StringUtils.isEmpty(deploymentDesignNodeId)) {
            return false;
        }
        return deploymentDesignNodeRepository.existsById(deploymentDesignNodeId);
    }

    // 根据设备和部署设计查询实存已存在该部署节点
    public boolean hasDeploymentDesignNodeByDeviceAndDeploymentDesign(DeviceEntity deviceEntity, DeploymentDesignEntity deploymentDesignEntity) {
        return deploymentDesignNodeRepository.existsByDeviceEntityAndDeploymentDesignEntity(deviceEntity, deploymentDesignEntity);
    }

    // 根据部署设计查询部署设计节点
    public Page<DeploymentDesignNodeEntity> getDeploymentDesignNodesByDeploymentDesign(Pageable pageable, DeploymentDesignEntity deploymentDesignEntity) {
        return deploymentDesignNodeRepository.findAllByDeploymentDesignEntity(pageable, deploymentDesignEntity);
    }

    // 根据部署设计查询部署设计节点
    public List<DeploymentDesignNodeEntity> getDeploymentDesignNodesByDeploymentDesign(DeploymentDesignEntity deploymentDesignEntity) {
        return deploymentDesignNodeRepository.findAllByDeploymentDesignEntity(deploymentDesignEntity);
    }

    public List<DeploymentDesignNodeEntity> getDeploymentDesignNodesByDevice(DeviceEntity deviceEntity) {
        return deploymentDesignNodeRepository.findAllByDeviceEntity(deviceEntity);
    }

    // 根据id查询部署设计节点
    @Cacheable(value = "DeploymentDesignNode_Cache", key = "#deploymentDesignNodeId")
    public DeploymentDesignNodeEntity getDeploymentDesignNodeById(String deploymentDesignNodeId) {
        if (!hasDeploymentDesignNodeById(deploymentDesignNodeId)) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NODE_ID_NOT_FOUND + deploymentDesignNodeId);
        }
        return deploymentDesignNodeRepository.findById(deploymentDesignNodeId).get();
    }

//    @Async
//    public void deployDeploymentsDesignNodeById(String deploymentDesignNodeId) throws IOException {
//
//        DeploymentDesignNodeEntity deploymentDesignNodeEntity = getDeploymentDesignNodeById(deploymentDesignNodeId);
//        if (deploymentDesignNodeEntity.getDeviceEntity() == null) {
//            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NODE_DEVICE_ARGS_NOT_FOUND);
//        }
//        DeviceEntity deviceEntity = deploymentDesignNodeEntity.getDeviceEntity();
////        if (!DeviceService.ONLINE_HOST_ADRESS.containsKey(deviceEntity.getHostAddress())) {
////            throw new RuntimeException(ApplicationMessages.DEVICE_NOT_ONLINE + deviceEntity.getHostAddress());
////        }
//
////        String componentId = deploymentDesignNodeEntity.getDeploymentDesignEntity().getComponentEntity().getId();
//
//        String componentId = deploymentDesignNodeEntity.getDeploymentDesignEntity().getComponentEntity().getId();
//        ComponentEntity componentEntity = new ComponentEntity();
//        componentEntity.setId(componentId);
//        ComponentFileEntity componentFileEntity = componentFileRepository.findAllByIsHistoryAndComponentEntity(false,componentEntity).get(0);
//
//
//         // 发送文件
//
//
//        if(sendFile(componentFileEntity.getFileEntity(),deviceEntity.getHostAddress(),ApplicationConfig.CLIENT2_TCP_RECEIVE,deploymentDesignNodeEntity.getId()) == false){
//            return;
//        }
//
//         //发送参数
//
//        {
////            List<DeploymentDesignParamEntity> params = new ArrayList<>();
////            params = deploymentDesignParamRepository.findAllByDeploymentDesignEntityId(deploymentDesignNodeEntity.getDeploymentDesignEntity().getId());
//            String jsonString = deploymentDesignNodeEntity.getParams();
//
//
//            Socket sp = new Socket(deviceEntity.getHostAddress(), ApplicationConfig.CLIENT2_TCP_RECEIVE);
//
//            ByteBuffer byteBuffer = ByteBuffer.allocate(268+jsonString.getBytes().length);
//            byteBuffer.put(FormatUtils.getString("C004",4).getBytes());
//            byteBuffer.put(FormatUtils.toLH(jsonString.getBytes().length));
//            System.out.println("dataSize:"+jsonString.getBytes().length);
//            //1:UTF-8, 2:GBK
//            byteBuffer.put(FormatUtils.toLH(1));
//
//            byte[] nodeIdbyte = FormatUtils.getString(deploymentDesignNodeEntity.getId(),256).getBytes();
//
//            byteBuffer.put(nodeIdbyte);
//
//
//            byteBuffer.put(jsonString.getBytes());
//
//            DataOutputStream dos = new DataOutputStream(sp.getOutputStream());
//            dos.write(byteBuffer.array(),0,byteBuffer.limit());
//
//            IOUtils.closeQuietly(sp);
//            IOUtils.closeQuietly(dos);
//
//        }
//
//
//
//    }
}

package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.*;
import com.rengu.operationsmanagementsuitev3.Repository.ComponentFileRepository;
import com.rengu.operationsmanagementsuitev3.Repository.DeploymentDesignDetailRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import com.rengu.operationsmanagementsuitev3.Utils.FormatUtils;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-04 11:25
 **/

@Slf4j
@Service
@Transactional
public class DeploymentDesignDetailService {

    private final DeploymentDesignDetailRepository deploymentDesignDetailRepository;
    private final DeployMetaService deployMetaService;
    private final OrderService orderService;
    private final ScanHandlerService scanHandlerService;
    private final DeploymentDesignScanResultService deploymentDesignScanResultService;
    private final ComponentFileRepository componentFileRepository;

    @Autowired
    public DeploymentDesignDetailService(DeploymentDesignDetailRepository deploymentDesignDetailRepository, DeployMetaService deployMetaService, OrderService orderService, ScanHandlerService scanHandlerService, DeploymentDesignScanResultService deploymentDesignScanResultService, ComponentFileRepository componentFileRepository) {
        this.deploymentDesignDetailRepository = deploymentDesignDetailRepository;
        this.deployMetaService = deployMetaService;
        this.orderService = orderService;
        this.scanHandlerService = scanHandlerService;
        this.deploymentDesignScanResultService = deploymentDesignScanResultService;
        this.componentFileRepository = componentFileRepository;
    }

    // 部署设计节点绑定组件历史
    public DeploymentDesignDetailEntity bindComponentHistoryByDeploymentDesignNode(DeploymentDesignNodeEntity deploymentDesignNodeEntity, ComponentHistoryEntity componentHistoryEntity, boolean keepLatest) {
        if (hasDeploymentDesignDetailByDeploymentDesignNodeAndComponent(deploymentDesignNodeEntity, componentHistoryEntity.getComponentEntity())) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_DETAIL_COMPONENT_EXISTED + componentHistoryEntity.getComponentEntity().getName() + "-" + componentHistoryEntity.getComponentEntity().getVersion());
        }
        DeploymentDesignDetailEntity deploymentDesignDetailEntity = new DeploymentDesignDetailEntity();
        deploymentDesignDetailEntity.setKeepLatest(keepLatest);
        deploymentDesignDetailEntity.setComponentHistoryEntity(componentHistoryEntity);
        deploymentDesignDetailEntity.setComponentEntity(componentHistoryEntity.getComponentEntity());
        deploymentDesignDetailEntity.setDeploymentDesignNodeEntity(deploymentDesignNodeEntity);
        deploymentDesignDetailEntity.setDeploymentDesignEntity(deploymentDesignNodeEntity.getDeploymentDesignEntity());
        return deploymentDesignDetailRepository.save(deploymentDesignDetailEntity);
    }

    // 根据部署设计节点复制部署设计详情
    public List<DeploymentDesignDetailEntity> copyDeploymentDesignDetailsByDeploymentDesignNode(DeploymentDesignNodeEntity sourceDeploymentDesignNode, DeploymentDesignNodeEntity targetDeploymentDesignNode) {
        List<DeploymentDesignDetailEntity> deploymentDesignDetailEntityList = new ArrayList<>();
        for (DeploymentDesignDetailEntity sourceDeploymentDesignDetailEntity : getDeploymentDesignDetailsByDeploymentDesignNode(sourceDeploymentDesignNode)) {
            DeploymentDesignDetailEntity deploymentDesignDetailEntity = new DeploymentDesignDetailEntity();
            BeanUtils.copyProperties(sourceDeploymentDesignDetailEntity, deploymentDesignDetailEntity, "id", "createTime", "deploymentDesignNodeEntity", "deploymentDesignEntity");
            deploymentDesignDetailEntity.setDeploymentDesignNodeEntity(targetDeploymentDesignNode);
            deploymentDesignDetailEntity.setDeploymentDesignEntity(targetDeploymentDesignNode.getDeploymentDesignEntity());
            deploymentDesignDetailEntityList.add(deploymentDesignDetailRepository.save(deploymentDesignDetailEntity));
        }
        return deploymentDesignDetailEntityList;
    }

    // 根据Id删除部署设计详情
    public DeploymentDesignDetailEntity deleteDeploymentDesignDetailById(String deploymentDesignDetailId) {
        DeploymentDesignDetailEntity deploymentDesignDetailEntity = getDeploymentDesignDetailById(deploymentDesignDetailId);
        return deleteDeploymentDesignDetailById(deploymentDesignDetailEntity);
    }

    public DeploymentDesignDetailEntity deleteDeploymentDesignDetailById(DeploymentDesignDetailEntity deploymentDesignDetailEntity) {
        deploymentDesignScanResultService.deleteDeploymentDesignScanResultByDeploymentDesignDetail(deploymentDesignDetailEntity);
        deploymentDesignDetailRepository.delete(deploymentDesignDetailEntity);
        return deploymentDesignDetailEntity;
    }

    public void deleteDeploymentDesignDetailByComponent(ComponentEntity componentEntity) {
        for (DeploymentDesignDetailEntity deploymentDesignDetailEntity : getDeploymentDesignDetailsByComponent(componentEntity)) {
            deleteDeploymentDesignDetailById(deploymentDesignDetailEntity);
        }
    }

    // 更新绑定组件历史版本
    public DeploymentDesignDetailEntity updateComponentHistoryById(String deploymentDesignDetailId, ComponentHistoryEntity componentHistoryEntity) {
        DeploymentDesignDetailEntity deploymentDesignDetailEntity = getDeploymentDesignDetailById(deploymentDesignDetailId);
        if (!deploymentDesignDetailEntity.getComponentHistoryEntity().equals(componentHistoryEntity)) {
            deploymentDesignDetailEntity.setComponentHistoryEntity(componentHistoryEntity);
            deploymentDesignDetailEntity.setComponentEntity(componentHistoryEntity.getComponentEntity());
        }
        return deploymentDesignDetailRepository.save(deploymentDesignDetailEntity);
    }

    // 更新是否保持最新版本状态
    public DeploymentDesignDetailEntity updateKeepLatestById(String deploymentDesignDetailId, boolean keepLatest) {
        DeploymentDesignDetailEntity deploymentDesignDetailEntity = getDeploymentDesignDetailById(deploymentDesignDetailId);
        if (deploymentDesignDetailEntity.isKeepLatest() != keepLatest) {
            deploymentDesignDetailEntity.setKeepLatest(keepLatest);
        }
        return deploymentDesignDetailRepository.save(deploymentDesignDetailEntity);
    }

    // 根据组件和部署设计节点判断是否存在
    public boolean hasDeploymentDesignDetailByDeploymentDesignNodeAndComponent(DeploymentDesignNodeEntity deploymentDesignNodeEntity, ComponentEntity componentEntity) {
        return deploymentDesignDetailRepository.existsByDeploymentDesignNodeEntityAndComponentEntity(deploymentDesignNodeEntity, componentEntity);
    }

    // 根据id查询部署设计详情是否存在
    public boolean hasDeploymentDesignDetailById(String deploymentDesignDetailId) {
        if (StringUtils.isEmpty(deploymentDesignDetailId)) {
            return false;
        }
        return deploymentDesignDetailRepository.existsById(deploymentDesignDetailId);
    }

    // 根据Id查询部署设计详情
    public DeploymentDesignDetailEntity getDeploymentDesignDetailById(String deploymentDesignDetailId) {
        if (!hasDeploymentDesignDetailById(deploymentDesignDetailId)) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_DETAIL_ID_NOT_FOUND + deploymentDesignDetailId);
        }
        return deploymentDesignDetailRepository.findById(deploymentDesignDetailId).get();
    }

    //todo:根据部署设计获取部署设计详情
    public List<DeploymentDesignDetailEntity> getDeploymentDesignDetailByDeploymentDesign(DeploymentDesignEntity deploymentDesignEntity){
        return deploymentDesignDetailRepository.findAllByDeploymentDesignEntity(deploymentDesignEntity);
    }

    // 根据部署设计节点查询部署设计详情
    public List<DeploymentDesignDetailEntity> getDeploymentDesignDetailsByDeploymentDesignNode(DeploymentDesignNodeEntity deploymentDesignNodeEntity) {
        return deploymentDesignDetailRepository.findAllByDeploymentDesignNodeEntity(deploymentDesignNodeEntity);
    }

    public List<DeploymentDesignDetailEntity> getDeploymentDesignDetailsByComponent(ComponentEntity componentEntity) {
        return deploymentDesignDetailRepository.findAllByComponentEntity(componentEntity);
    }

    // 部署单个组件
    @Async
    public void deployDeploymentDesignDetailById(String deploymentDesignDetailId) {
        DeploymentDesignDetailEntity deploymentDesignDetailEntity = getDeploymentDesignDetailById(deploymentDesignDetailId);
        DeploymentDesignNodeEntity deploymentDesignNodeEntity = deploymentDesignDetailEntity.getDeploymentDesignNodeEntity();
        if (deploymentDesignNodeEntity.getDeviceEntity() == null) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NODE_DEVICE_ARGS_NOT_FOUND);
        }
        DeviceEntity deviceEntity = deploymentDesignNodeEntity.getDeviceEntity();
        if (!DeviceService.ONLINE_HOST_ADRESS.containsKey(deviceEntity.getHostAddress())) {
            throw new RuntimeException(ApplicationMessages.DEVICE_NOT_ONLINE + deviceEntity.getHostAddress());
        }
        List<DeployMetaEntity> deployMetaEntityList = deployMetaService.createDeployMeta(deploymentDesignDetailEntity);
        deployMetaService.deployMeta(deploymentDesignNodeEntity.getDeploymentDesignEntity(), deviceEntity, deployMetaEntityList);
    }

    // 部署·单个节点
    @Async
    public void deployDeploymentDesignDetailByDeploymentDesignNode(DeploymentDesignNodeEntity deploymentDesignNodeEntity) {
        //TODO：获取部署设计详情
        List<DeploymentDesignDetailEntity> deploymentDesignDetailEntityList = getDeploymentDesignDetailsByDeploymentDesignNode(deploymentDesignNodeEntity);
        if (deploymentDesignNodeEntity.getDeviceEntity() == null) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NODE_DEVICE_ARGS_NOT_FOUND);
        }
        DeviceEntity deviceEntity = deploymentDesignNodeEntity.getDeviceEntity();
        if (!DeviceService.ONLINE_HOST_ADRESS.containsKey(deviceEntity.getHostAddress())) {
            throw new RuntimeException(ApplicationMessages.DEVICE_NOT_ONLINE + deviceEntity.getHostAddress());
        }
        List<DeployMetaEntity> deployMetaEntityList = deployMetaService.createDeployMeta(deploymentDesignDetailEntityList.toArray(new DeploymentDesignDetailEntity[deploymentDesignDetailEntityList.size()]));
        deployMetaService.deployMeta(deploymentDesignNodeEntity.getDeploymentDesignEntity(), deviceEntity, deployMetaEntityList);
    }


    public List<DeploymentDesignScanResultEntity> scanDeploymentDesignDetailsByDeploymentDesignNode(DeploymentDesignNodeEntity deploymentDesignNodeEntity, String[] extensions) throws InterruptedException, ExecutionException, IOException {
        List<DeploymentDesignDetailEntity> deploymentDesignDetailEntityList = getDeploymentDesignDetailsByDeploymentDesignNode(deploymentDesignNodeEntity);
        List<DeploymentDesignScanResultEntity> deploymentDesignScanResultEntityList = new ArrayList<>();
        for (DeploymentDesignDetailEntity deploymentDesignDetailEntity : deploymentDesignDetailEntityList) {
            deploymentDesignScanResultEntityList.add(scanDeploymentDesignDetail(deploymentDesignDetailEntity, extensions));
        }
        return deploymentDesignScanResultEntityList;
    }

//    public DeploymentDesignScanResultEntity scanDeploymentDesignDetail(DeploymentDesignDetailEntity deploymentDesignDetailEntity, String[] extensions) throws IOException, ExecutionException, InterruptedException {
//        String [] extendions = extensions;
//        if(extendions == null || extendions.length<0){
//            scanDeploymentDesignDetails(deploymentDesignDetailEntity, extensions);
//        }
//        scanDeploymentDesignDetails(deploymentDesignDetailEntity, extensions);
//        return
//    }

    // 扫描设备下的某个组件
    public DeploymentDesignScanResultEntity scanDeploymentDesignDetail(DeploymentDesignDetailEntity deploymentDesignDetailEntity, String[] extensions) throws IOException, ExecutionException, InterruptedException {
        //判断部署设计节点下的设备存不存在
        DeploymentDesignNodeEntity deploymentDesignNodeEntity = deploymentDesignDetailEntity.getDeploymentDesignNodeEntity();
        if (deploymentDesignNodeEntity.getDeviceEntity() == null) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NODE_DEVICE_ARGS_NOT_FOUND);
        }
        DeviceEntity deviceEntity = deploymentDesignNodeEntity.getDeviceEntity();
        if (!DeviceService.ONLINE_HOST_ADRESS.containsKey(deviceEntity.getHostAddress())) {
            throw new RuntimeException(ApplicationMessages.DEVICE_NOT_ONLINE + deviceEntity.getHostAddress());
        }

        OrderEntity orderEntity = new OrderEntity();
        if (extensions == null || extensions.length == 0) {
            orderEntity.setTag(OrderService.DEPLOY_DESIGN_SCAN);
        } else {
            orderEntity.setTag(OrderService.DEPLOY_DESIGN_SCAN_WITH_EXTENSIONS);
            orderEntity.setExtension(Arrays.toString(extensions).replace("[", "").replace("]", "").replaceAll("\\s*", ""));
        }
        orderEntity.setDeploymentDesignNodeEntity(deploymentDesignNodeEntity);
        orderEntity.setDeploymentDesignDetailEntity(deploymentDesignDetailEntity);
        //todo:拼接路径
        orderEntity.setTargetPath(deviceEntity.getDeployPath() + deploymentDesignDetailEntity.getComponentHistoryEntity().getRelativePath());
        orderEntity.setTargetDevice(deploymentDesignNodeEntity.getDeviceEntity());
        orderService.sendDeployDesignScanOrderByUDP(orderEntity);
        Future<DeploymentDesignScanResultEntity> scanResult;
        if(extensions == null || extensions.length==0){
            scanResult = scanHandlerService.deploymentDesignDetailScanHandler(orderEntity);
        }else{
            scanResult = scanHandlerService.deploymentDesignDetailScanHandlers(orderEntity);
        }
        long scanStartTime = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - scanStartTime >= ApplicationConfig.SCAN_TIME_OUT * 6) {
                if (!ScanHandlerService.DEPLOY_DESIGN_SCAN_RESULT.containsKey(orderEntity.getId())) {
                    ScanHandlerService.DEPLOY_DESIGN_SCAN_RESULT.get(orderEntity.getId());
                    log.info("扫描Id：" + orderEntity.getId() + ",扫描超时，未接收到客户端返回结果，程序退出。");
                    throw new RuntimeException(ApplicationMessages.SCAN_DEPLOY_DESIGN_TIME_OUT);
                }
            }
            //TODO :idDone()判断Future当前方法是否完成
            //System.out.println("判断是否已经执行完成："+scanResult.isDone());
            if (scanResult.isDone()) {
                DeploymentDesignScanResultEntity deploymentDesignScanResultEntity = scanResult.get();//TODO:调用get()是返回Futurte的方法执行结果
                System.out.println("扫描结果："+deploymentDesignScanResultEntity);
                ScanHandlerService.DEPLOY_DESIGN_SCAN_RESULT.remove(orderEntity.getId());
                log.info("扫描Id：" + orderEntity.getId() + ",处理时间：" + ((System.currentTimeMillis() - scanStartTime) / 1000.0) + "s,扫描结束。");
                return deploymentDesignScanResultService.saveDeploymentDesignScanResult(deploymentDesignScanResultEntity);
            }
        }
    }




//    //发送文件路径
//    @Async
//    public void deployDeploymentsDesignById(String deploymentDesignDetailId){
//
//        DeploymentDesignDetailEntity deploymentDesignNodeEntity = getDeploymentDesignDetailById(deploymentDesignDetailId);
//        if (deploymentDesignNodeEntity.getDeploymentDesignNodeEntity().getDeviceEntity() == null) {
//            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NODE_DEVICE_ARGS_NOT_FOUND);
//        }
//        DeviceEntity deviceEntity = deploymentDesignNodeEntity.getDeploymentDesignNodeEntity().getDeviceEntity();
//        String componentId = deploymentDesignNodeEntity.getComponentEntity().getId();
//        ComponentEntity componentEntity = new ComponentEntity();
//        componentEntity.setId(componentId);
//        List<ComponentFileEntity> componentFileEntity = componentFileRepository.findAllByComponentEntity(componentEntity);
//
//        /*
//         * 发送文件
//         * */
//
//        if (sendFile(componentFileEntity, deviceEntity.getHostAddress(), ApplicationConfig.TCP_DEPLOY_PORT, deviceEntity) == false) {
//            return;
//        }
//    }
//
//    public boolean sendFile(List<ComponentFileEntity> componentFileEntityList, String ip, int port,DeviceEntity deviceEntity){
//        //FileInputStream fis = null;
//        Socket s = null;
//        DataOutputStream oos= null;
//        String exe = "exe";
//        try{
//
//
//
//            for(ComponentFileEntity componentFile:componentFileEntityList){
//                String a = componentFile.getExtension();
//                if(a.equals(exe)) {
//                    s = new Socket(ip, port);
//                    oos = new DataOutputStream(s.getOutputStream());
//
//                    ByteBuffer byteBuffer = ByteBuffer.allocate(604);
//                    //4
//                    byteBuffer.put(FormatUtils.getString("S104",4).getBytes());
//                    //String removePlaceholder=FormatUtils.formatPath(deviceEntity.getDeployPath() + componentFile.getName() + componentFile.getExtension());
//                    System.out.println();
//                    byte[] path = FormatUtils.getString(deviceEntity.getDeployPath() + componentFile.getName() + componentFile.getExtension(), 600-FormatUtils.filterChinese(deviceEntity.getDeployPath() + componentFile.getName() + componentFile.getExtension())*2).getBytes();
//                    System.out.println("filename = " + path.length);
//                    byteBuffer.put(path);
//                    oos.write(byteBuffer.array(),0,604);
//                    oos.flush();
//
////                    IOUtils.closeQuietly(s);
////                    IOUtils.closeQuietly(oos);
//                    s.close();
//                    oos.close();
//                }
//            }
//                return true;
//        }catch(IOException e){
//            e.printStackTrace();
//            return false;
//        }finally {
//
//        }
//    }

//    //发送文件路径
//    @Async
//    public void deployDeploymentsDesignById(String deploymentDesignDetailId){
//        //通过部署设计获取部署设计详情
//        List<DeploymentDesignDetailEntity> deploymentDesignDetailEntityList =
//
//        DeploymentDesignDetailEntity deploymentDesignDetailEntity = getDeploymentDesignDetailById(deploymentDesignDetailId);
//        if (deploymentDesignDetailEntity.getDeploymentDesignNodeEntity().getDeviceEntity() == null) {
//            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NODE_DEVICE_ARGS_NOT_FOUND);
//        }
//        DeviceEntity deviceEntity = deploymentDesignDetailEntity.getDeploymentDesignNodeEntity().getDeviceEntity();
//        String componentId = deploymentDesignDetailEntity.getComponentEntity().getId();
//        ComponentEntity componentEntity = new ComponentEntity();
//        componentEntity.setId(componentId);
//        List<ComponentFileEntity> componentFileEntity = componentFileRepository.findAllByComponentEntity(componentEntity);
//
//        deployMetaService.deployMetas(componentFileEntity, deviceEntity);
//    }

}
package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.*;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import com.rengu.operationsmanagementsuitev3.Utils.FormatUtils;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-05 12:44
 **/

@Slf4j
@Service
public class DeployMetaService {

    public static final Map<String, DeviceEntity> DEPLOYING_DEVICE = new ConcurrentHashMap<>();

    // 部署状态报告信息
    public static final int DEPLOYING_ERROR = 0;
    public static final int DEPLOYING_SUCCEED = 1;
    public static final int DEPLOY_FINISHED = 2;
    public static final int DEPLOYING = 3;

    private final ComponentFileHistoryService componentFileHistoryService;
    private final DeployLogService deployLogService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ComponentFileService componentFileService;

    @Autowired
    public DeployMetaService(ComponentFileHistoryService componentFileHistoryService, DeployLogService deployLogService, SimpMessagingTemplate simpMessagingTemplate, ComponentFileService componentFileService) {
        this.componentFileHistoryService = componentFileHistoryService;
        this.deployLogService = deployLogService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.componentFileService = componentFileService;
    }

    // 根据部署设计详情创建部署信息
    public List<DeployMetaEntity> createDeployMeta(DeploymentDesignDetailEntity... deploymentDesignDetailEntities) {
        List<DeployMetaEntity> deployMetaEntityList = new ArrayList<>();
        for (DeploymentDesignDetailEntity deploymentDesignDetailEntity : deploymentDesignDetailEntities) {
            DeviceEntity deviceEntity = deploymentDesignDetailEntity.getDeploymentDesignNodeEntity().getDeviceEntity();
            if (deploymentDesignDetailEntity.isKeepLatest()) {
                for (ComponentFileEntity componentFileEntity : componentFileService.getComponentFilesByComponent(deploymentDesignDetailEntity.getComponentEntity())) {
                    if (!componentFileEntity.isFolder()) {
                        DeployMetaEntity deployMetaEntity = new DeployMetaEntity();
                        deployMetaEntity.setDeviceEntity(deviceEntity);
                        deployMetaEntity.setTargetPath(FormatUtils.formatPath(deviceEntity.getDeployPath() + deploymentDesignDetailEntity.getComponentEntity().getRelativePath() + FormatUtils.getComponentFileRelativePath(componentFileEntity, "")));
                        deployMetaEntity.setFileEntity(componentFileEntity.getFileEntity());
                        deployMetaEntity.setVersion(componentFileEntity.getComponentEntity().getVersion());
                        deployMetaEntityList.add(deployMetaEntity);
                    }
                }
            } else {
                for (ComponentFileHistoryEntity componentFileHistoryEntity : componentFileHistoryService.getComponentFileHistorysByComponentHistory(deploymentDesignDetailEntity.getComponentHistoryEntity())) {
                    if (!componentFileHistoryEntity.isFolder()) {
                        DeployMetaEntity deployMetaEntity = new DeployMetaEntity();
                        deployMetaEntity.setDeviceEntity(deviceEntity);
                        deployMetaEntity.setTargetPath(FormatUtils.formatPath(deviceEntity.getDeployPath() + deploymentDesignDetailEntity.getComponentHistoryEntity().getRelativePath() + FormatUtils.getComponentFileHistoryRelativePath(componentFileHistoryEntity, "")));
                        deployMetaEntity.setFileEntity(componentFileHistoryEntity.getFileEntity());
                        deployMetaEntityList.add(deployMetaEntity);
                    }
                }
            }
        }
        return deployMetaEntityList;
    }

    public void deployMeta(DeploymentDesignEntity deploymentDesignEntity, DeviceEntity deviceEntity, List<DeployMetaEntity> deployMetaEntityList) {
        // 初始化发送进度信息
        long totalSendSize = 0;
        double sendProgress = 0.0;
        double sendSpeed = 0.0;
        //TODO：1. 初始化日志记录
        DeployLogEntity deployLogEntity = new DeployLogEntity();
        deployLogEntity.setTotalFileSize(getTotalFileSize(deployMetaEntityList));
        deployLogEntity.setProjectEntity(deploymentDesignEntity.getProjectEntity());
        deployLogEntity.setType("部署设计");
        for(DeployMetaEntity deployMetaEntity:deployMetaEntityList){

            deployLogEntity.setVersion(deployMetaEntity.getVersion());
        }

        // 设备是否在部署的检查
        if (DEPLOYING_DEVICE.containsKey(deviceEntity.getHostAddress())) {
            simpMessagingTemplate.convertAndSend("/deployProgress/" + deploymentDesignEntity.getId(), new DeployProgressEntity(deviceEntity.getHostAddress(), sendSpeed, sendProgress, DEPLOYING_ERROR, ApplicationMessages.DEVICE_IS_DEPOLOYING + deviceEntity.getHostAddress()));
            deployLogService.saveDeployLog(deployLogEntity, false, "当前部署组件版本为："+deployLogEntity.getVersion()+ApplicationMessages.DEVICE_IS_DEPOLOYING + deviceEntity.getHostAddress(), totalSendSize);
            log.info(deviceEntity.getHostAddress() + "设备正在部署，程序退出");
            return;
        }
        DEPLOYING_DEVICE.put(deviceEntity.getHostAddress(), deviceEntity);
        try {
            //初始化Socket、输入输出流
            @Cleanup Socket socket = new Socket(deviceEntity.getHostAddress(), ApplicationConfig.TCP_DEPLOY_PORT);
            socket.setTcpNoDelay(true);
            socket.setSoTimeout(25);
            @Cleanup InputStream inputStream = socket.getInputStream();
            @Cleanup OutputStream outputStream = socket.getOutputStream();
            deployLoop:
            for (DeployMetaEntity deployMetaEntity : deployMetaEntityList) {
                //检测设备是否在线
                if (!DeviceService.ONLINE_HOST_ADRESS.containsKey(deployMetaEntity.getDeviceEntity().getHostAddress())) {
                    simpMessagingTemplate.convertAndSend("/deployProgress/" + deploymentDesignEntity.getId(), new DeployProgressEntity(deviceEntity.getHostAddress(), sendSpeed, sendProgress, DEPLOYING_ERROR, ApplicationMessages.DEVICE_IS_OFFLINE + deployMetaEntity.getDeviceEntity().getHostAddress()));
                    deployLogService.saveDeployLog(deployLogEntity, false, ApplicationMessages.DEVICE_IS_OFFLINE + deployMetaEntity.getDeviceEntity().getHostAddress(), totalSendSize);
                    log.info(deviceEntity.getHostAddress() + "设备不在线，程序退出");
                    return;
                }
                //读取发送文件
                File file = new File(deployMetaEntity.getFileEntity().getLocalPath());
                // 1、发送校验数据包
                DeployPackaegEntity checkEntity = new DeployPackaegEntity(file.length(), deployMetaEntity.getTargetPath(), deployMetaEntity.getFileEntity().getMD5());
                outputStream.write(checkEntity.getCheckBuffer());
                outputStream.flush();
                // 2、接受回复判断是否继续发送
                long pathCheckTime = System.currentTimeMillis();
                long witeTime = file.length() / 104857600 * 1500 <= ApplicationConfig.REPLY_TIME_OUT ? ApplicationConfig.REPLY_TIME_OUT : (file.length() / 104857600 * 1500);
                while (true) {
                    try {
                        if (inputStream.read() == 116) {
                            log.info(deployMetaEntity.getTargetPath() + "不存在，发送文件内容。");
                            break;
                        } else {
                            // 更新发送进度
                            totalSendSize = totalSendSize + file.length();
                            sendProgress = ((double) totalSendSize / deployLogEntity.getTotalFileSize()) * 100;
//                            log.info("发送速度：" + sendSpeed + ",发送进度：" + sendProgress + ",总发送大小：" + totalSendSize + ",总大小：" + deployLogEntity.getTotalFileSize());
                            simpMessagingTemplate.convertAndSend("/deployProgress/" + deploymentDesignEntity.getId(), new DeployProgressEntity(deviceEntity.getHostAddress(), sendSpeed, sendProgress, DEPLOYING_SUCCEED, FilenameUtils.getName(deployMetaEntity.getTargetPath()) + "-部署成功"));
                            continue deployLoop;
                        }
                    } catch (IOException exception) {
                        if (System.currentTimeMillis() - pathCheckTime >= witeTime) {
                            String deployMessage = "当前部署组件版本号："+deployMetaEntity.getVersion()+"、"+deviceEntity.getHostAddress() + ":" + deployMetaEntity.getTargetPath() + ",部署失败，校验包回复超时。当前进度：" + sendProgress + "%";
                            //simpMessagingTemplate.convertAndSend("/deployProgress/" + deploymentDesignEntity.getId(), new DeployProgressEntity(deviceEntity.getHostAddress(), sendSpeed, sendProgress, DEPLOYING_ERROR, FilenameUtils.getName(deployMetaEntity.getTargetPath()) + "-部署失败"));
                            simpMessagingTemplate.convertAndSend("/deployProgress/" + deploymentDesignEntity.getId(), new DeployProgressEntity(deviceEntity.getHostAddress(), sendSpeed, sendProgress, DEPLOYING_ERROR, FilenameUtils.getName(deployMetaEntity.getTargetPath()) + "-部署失败，校验包回复超时"));
                            deployLogService.saveDeployLog(deployLogEntity, false, deployMessage, totalSendSize);
                            log.info(deployMessage + ",程序退出");
                            return;
                        }
                    }
                }
                // 3、读取并发送文件
                @Cleanup RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
                long fileSendSize = 0;
                for (int i = 1; fileSendSize < file.length(); i++) {
                    long fileSendStart = System.currentTimeMillis();
                    // 设置读取缓冲区域大小
                    byte[] fileReadBuffer = new byte[ApplicationConfig.FILE_READ_BUFFER_SIZE];
                    int readSize = randomAccessFile.read(fileReadBuffer);
                    if (readSize != -1) {
                        byte[] data = new byte[readSize];
                        System.arraycopy(fileReadBuffer, 0, data, 0, readSize);
                        DeployPackaegEntity deployPackaegEntity = new DeployPackaegEntity(i, file.length(), data);
                        outputStream.write(deployPackaegEntity.getDataBuffer());
                        outputStream.flush();
                        // 更新发送大小
                        fileSendSize = fileSendSize + readSize;
                        totalSendSize = totalSendSize + readSize;
                        // 发送时间单位:秒
                        double sendTime = ((System.currentTimeMillis() - fileSendStart) == 0 ? 1 : System.currentTimeMillis() - fileSendStart) / 1000.0;
                        // 发送大小单位:kb
                        double sendSize = readSize / 1024.0;
                        sendSpeed = sendSize / sendTime;
                        sendProgress = ((double) totalSendSize / deployLogEntity.getTotalFileSize()) * 100;
//                        log.info("发送速度：" + sendSpeed + ",发送进度：" + sendProgress + ",总发送大小：" + totalSendSize + ",总大小：" + deployLogEntity.getTotalFileSize());
                        if (file.length() > FileUtils.ONE_MB * 10 && fileSendSize % (ApplicationConfig.FILE_READ_BUFFER_SIZE * 10) == 0) {
                            simpMessagingTemplate.convertAndSend("/deployProgress/" + deploymentDesignEntity.getId(), new DeployProgressEntity(deviceEntity.getHostAddress(), sendSpeed, sendProgress, DEPLOYING, FilenameUtils.getName(deployMetaEntity.getTargetPath()) + "-部署中"));
                        }
                    } else {
                        break;
                    }
                }
                // 4、判断文件是否合并成功
                long finishCheckTime = System.currentTimeMillis();
                while (true) {
                    try {
                        if (inputStream.read() == 99) {
                            break;
                        }
                    } catch (IOException exception) {
                        if (System.currentTimeMillis() - finishCheckTime >= ApplicationConfig.DEPLOY_PORT) {
                            String deployMessage = deviceEntity.getHostAddress() + ":" + deployMetaEntity.getTargetPath() + ",部署失败，合并回复超时。当前进度：" + sendProgress + "%";
                            //simpMessagingTemplate.convertAndSend("/deployProgress/" + deploymentDesignEntity.getId(), new DeployProgressEntity(deviceEntity.getHostAddress(), sendSpeed, sendProgress, DEPLOYING_ERROR, FilenameUtils.getName(deployMetaEntity.getTargetPath()) + "-部署失败"));
                            simpMessagingTemplate.convertAndSend("/deployProgress/" + deploymentDesignEntity.getId(), new DeployProgressEntity(deviceEntity.getHostAddress(), sendSpeed, sendProgress, DEPLOYING_ERROR, FilenameUtils.getName(deployMetaEntity.getTargetPath()) + "-部署失败，合并回复超时"));
                            deployLogService.saveDeployLog(deployLogEntity, false, deployMessage, totalSendSize);
                            log.info(deployMessage + ",程序退出");
                            return;
                        }
                    }
                }
                simpMessagingTemplate.convertAndSend("/deployProgress/" + deploymentDesignEntity.getId(), new DeployProgressEntity(deviceEntity.getHostAddress(), sendSpeed, sendProgress, DEPLOYING_SUCCEED, FilenameUtils.getName(deployMetaEntity.getTargetPath()) + "-部署成功"));
            }
            // 1、发送校验数据包
            DeployPackaegEntity finishData = new DeployPackaegEntity();
            outputStream.write(finishData.getFinishBuffer());
            outputStream.flush();
            simpMessagingTemplate.convertAndSend("/deployProgress/" + deploymentDesignEntity.getId(), new DeployProgressEntity(deviceEntity.getHostAddress(), sendSpeed, sendProgress, DEPLOY_FINISHED, "部署结束"));
        } catch (IOException e) {
            e.printStackTrace();
            //todo 修改后版本 2020/7/1
            simpMessagingTemplate.convertAndSend("/deployProgress/" + deploymentDesignEntity.getId(), new DeployProgressEntity(deviceEntity.getHostAddress(), sendSpeed, sendProgress, DEPLOYING_ERROR, e.getMessage())+"-部署中断");
            deployLogService.saveDeployLog(deployLogEntity, false, "此"+deviceEntity.getHostAddress()+"由于设备突然离线，导致部署中断", totalSendSize);
        } finally {
            DEPLOYING_DEVICE.remove(deviceEntity.getHostAddress());
            log.info("从正在部署的设备中移除：" + deviceEntity.getHostAddress());
        }
    }

    private byte[] reReadFile(File file, int serialNum) throws IOException {
        int maxSerialNum = (int) (file.length() / ApplicationConfig.FILE_READ_BUFFER_SIZE) + 1;
        if (serialNum == 0 || serialNum > maxSerialNum) {
            throw new IOException(file.getAbsolutePath() + "数据包序号异常，最大值：" + maxSerialNum + ",当前值：" + serialNum);
        }
        @Cleanup RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        long startPoint = (serialNum - 1) * ApplicationConfig.FILE_READ_BUFFER_SIZE;
        byte[] fileReadBuffer = new byte[ApplicationConfig.FILE_READ_BUFFER_SIZE];
        randomAccessFile.seek(startPoint);
        int readSize = randomAccessFile.read(fileReadBuffer);
        if (readSize != -1) {
            byte[] data = new byte[readSize];
            System.arraycopy(fileReadBuffer, 0, data, 0, readSize);
            return data;
        } else {
            throw new IOException(file.getAbsolutePath() + "文件读取异常");
        }
    }

    private long getTotalFileSize(List<DeployMetaEntity> deployMetaEntityList) {
        long totalFileSize = 0;
        for (DeployMetaEntity deployMetaEntity : deployMetaEntityList) {
            totalFileSize = totalFileSize + deployMetaEntity.getFileEntity().getSize();
        }
        return totalFileSize;
    }


    //todo :一键远程启动（发送路径）
    public void deployPaths(List<ComponentFileEntity> componentFileEntities, DeviceEntity deviceEntity,ComponentEntity componentEntity) {
        // 初始化发送进度信息
        double sendProgress = 0.0;
        double sendSpeed = 0.0;
        String exe = "exe";
        if (DEPLOYING_DEVICE.containsKey(deviceEntity.getHostAddress())) {
            simpMessagingTemplate.convertAndSend("/deployProgress/" + componentFileEntities, new DeployProgressEntity(deviceEntity.getHostAddress(), sendSpeed, sendProgress, DEPLOYING_ERROR, ApplicationMessages.DEVICE_IS_DEPOLOYING + deviceEntity.getHostAddress()));
        }
        DEPLOYING_DEVICE.put(deviceEntity.getHostAddress(), deviceEntity);
        try {
            for (ComponentFileEntity componentFileEntity:componentFileEntities) {

                //后缀
                String extension = componentFileEntity.getExtension();
                if(exe.equals(extension)) {
                    @Cleanup Socket socket = new Socket(deviceEntity.getHostAddress(), ApplicationConfig.TCP_DEPLOY_PORT);
                    socket.setTcpNoDelay(true);
                    socket.setSoTimeout(25);
                    @Cleanup OutputStream outputStream = socket.getOutputStream();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(604);
                    //读取发送文件
                    byteBuffer.put(FormatUtils.getString("S104", 4).getBytes());
                    String p = deviceEntity.getDeployPath();
                    String s1 = p.substring(0, p.lastIndexOf("/"));
                    System.out.println("s1:"+s1);
                    String s2 = p.substring(p.lastIndexOf("/") + 1);
                    System.out.println("s2:"+s2);
                    String path = componentEntity.getRelativePath();
                    String s3 = path.substring(path.lastIndexOf("/")+1);
                    System.out.println("s3:"+s3);
                    String s4= s1+File.separator+s2 + File.separator + s3;
                    String s5 = FormatUtils.formatPath(s4);
                    String s6 = s5+FormatUtils.getComponentFileRelativePaths(componentFileEntity,"");
                    System.out.println("路径："+s6);
                    byte[] SplicingPath;
                    SplicingPath = FormatUtils.getString(s5+FormatUtils.getComponentFileRelativePaths(componentFileEntity,""), 600 - FormatUtils.filterChinese(s6) * 2).getBytes();

//                    SplicingPath = FormatUtils.getString(s1+File.separator+s2 + File.separator + s3+File.separator+componentFileEntity.getName()+"."+ componentFileEntity.getExtension(), 600 - FormatUtils.filterChinese(deviceEntity.getDeployPath() + componentEntity.getRelativePath()+componentFileEntity.getName() + componentFileEntity.getExtension()) * 2).getBytes();
                    log.info("S104---->一键远程启动路径发送成功");
                    byteBuffer.put(SplicingPath);
                    outputStream.write(byteBuffer.array(), 0, 604);
                    outputStream.flush();
                    simpMessagingTemplate.convertAndSend("/deployProgress/" + componentFileEntities, new DeployProgressEntity(deviceEntity.getHostAddress(), sendSpeed, sendProgress, DEPLOY_FINISHED, "部署结束"));
                }
                }
            } catch(IOException e){
                e.printStackTrace();
                simpMessagingTemplate.convertAndSend("/deployProgress/" + componentFileEntities, new DeployProgressEntity(deviceEntity.getHostAddress(), sendSpeed, sendProgress, DEPLOYING_ERROR, e.getMessage()) + "-部署中断");
            } finally{
                DEPLOYING_DEVICE.remove(deviceEntity.getHostAddress());
                log.info("从正在部署的设备中移除：" + deviceEntity.getHostAddress());
        }
    }
}

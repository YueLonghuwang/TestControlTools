package com.rengu.operationsmanagementsuitev3.Aspect;

import com.rengu.operationsmanagementsuitev3.Controller.ComponentController;
import com.rengu.operationsmanagementsuitev3.Controller.ComponentFileController;
import com.rengu.operationsmanagementsuitev3.Controller.ProjectController;
import com.rengu.operationsmanagementsuitev3.Controller.SimEngineController;
import com.rengu.operationsmanagementsuitev3.Entity.ComponentEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ComponentFileEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ResultEntity;
import com.rengu.operationsmanagementsuitev3.Service.ComponentHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Aspect
@Component
public class ComponentHistoryAspect {

    private final ComponentHistoryService componentHistoryService;

    @Autowired
    public ComponentHistoryAspect(ComponentHistoryService componentHistoryService) {
        this.componentHistoryService = componentHistoryService;
    }

    @Pointcut(value = "execution(public * com.rengu.operationsmanagementsuitev3.Controller..*(..))")
    public void componentPointCut() {
    }

    @AfterReturning(pointcut = "componentPointCut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, ResultEntity result) {
        if (joinPoint.getTarget().getClass().equals(ProjectController.class)) {
            switch (joinPoint.getSignature().getName()) {
                case "saveComponentByProject": {
                    ComponentEntity componentEntity = (ComponentEntity) result.getData();
                    componentHistoryService.saveComponentHistoryByComponent(componentEntity);
                    break;
                }
                default:
            }
        }
        if (joinPoint.getTarget().getClass().equals(ComponentController.class)) {
            switch (joinPoint.getSignature().getName()) {
                case "copyComponentById": {
                    ComponentEntity componentEntity = (ComponentEntity) result.getData();
                    componentHistoryService.saveComponentHistoryByComponent(componentEntity);
                    break;
                }
                case "updateComponentById": {
                    ComponentEntity componentEntity = (ComponentEntity) result.getData();
                    componentHistoryService.saveComponentHistoryByComponent(componentEntity);
                    break;
                }
                case "saveComponentFileByParentNodeAndComponent": {
                    ComponentEntity componentEntity = ((ComponentFileEntity) result.getData()).getComponentEntity();
                    componentHistoryService.saveComponentHistoryByComponent(componentEntity);
                    break;
                }
                case "saveComponentFilesByParentNodeAndComponent": {
                    List<ComponentFileEntity> componentFileEntityList = (List<ComponentFileEntity>) result.getData();
                    if (!componentFileEntityList.isEmpty()) {
                        ComponentFileEntity componentFileEntity = componentFileEntityList.get(0);
                        ComponentEntity componentEntity = componentFileEntity.getComponentEntity();
                        componentHistoryService.saveComponentHistoryByComponent(componentEntity);
                    }
                    break;
                }
                default:
            }
        }
        if (joinPoint.getTarget().getClass().equals(ComponentFileController.class)) {
            switch (joinPoint.getSignature().getName()) {
                case "copyComponentFileById": {
                    ComponentFileEntity componentFileEntity = (ComponentFileEntity) result.getData();
                    ComponentEntity componentEntity = componentFileEntity.getComponentEntity();
                    componentHistoryService.saveComponentHistoryByComponent(componentEntity);
                    break;
                }
                case "moveComponentFileById": {
                    ComponentFileEntity componentFileEntity = (ComponentFileEntity) result.getData();
                    ComponentEntity componentEntity = componentFileEntity.getComponentEntity();
                    componentHistoryService.saveComponentHistoryByComponent(componentEntity);
                    break;
                }
                case "deleteComponentFileById": {
                    ComponentFileEntity componentFileEntity = (ComponentFileEntity) result.getData();
                    ComponentEntity componentEntity = componentFileEntity.getComponentEntity();
                    componentHistoryService.saveComponentHistoryByComponent(componentEntity);
                    break;
                }
                case "updateComponentFileById": {
                    ComponentFileEntity componentFileEntity = (ComponentFileEntity) result.getData();
                    ComponentEntity componentEntity = componentFileEntity.getComponentEntity();
                    componentHistoryService.saveComponentHistoryByComponent(componentEntity);
                    break;
                }
                default:
            }
        }
    }
}

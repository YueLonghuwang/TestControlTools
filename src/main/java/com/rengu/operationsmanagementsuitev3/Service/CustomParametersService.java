package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentEntity;
import com.rengu.operationsmanagementsuitev3.Entity.CustomParametersEntity;
import com.rengu.operationsmanagementsuitev3.Repository.CustomParametersRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Author: Zhangqiankun
 * Date: 2020/8/10 9:18
 */
@Service
@Transactional
public class CustomParametersService {

    private final CustomParametersRepository customParametersRepository;

    @Autowired
    public CustomParametersService(CustomParametersRepository customParametersRepository) {
        this.customParametersRepository = customParametersRepository;
    }
//
//    //通过组件删除组件自定义
//    public List<CustomParametersEntity> deleteCustomParameters(ComponentEntity componentEntity){
//        List<CustomParametersEntity> customParametersEntityList = getCustomParametersByComponent(componentEntity);
//        for(CustomParametersEntity customParametersEntity:customParametersEntityList){
//            customParametersRepository.delete(customParametersEntity);
//        }
//        return customParametersEntityList;
//    }
//
//    //通过组件id获取自定义文本
//    public List<CustomParametersEntity> getCustomParametersByComponent(ComponentEntity component){
//        return customParametersRepository.findAllByComponentEntity(component);
//    }

    //通过自定义id获取自定义
    public CustomParametersEntity getCustomParametersById(String customParameterId){
        if(!hasCustomParameterById(customParameterId)){
            throw new RuntimeException(ApplicationMessages.No_CUSTOM_ID_COMPONENT_FOUND+customParameterId);
        }
        return customParametersRepository.findById(customParameterId).get();
    }

    //根据自定义组件id判断自定义组件是否存在
    public boolean hasCustomParameterById(String customParameterId){
        if(StringUtils.isEmpty(customParameterId)){
            return false;
        }
        return customParametersRepository.existsById(customParameterId);
    }

/*    //保存自定义
    public Map<String,List<CustomParametersEntity,CustomParametersEntity>> saveCustomParameters(String componentId, List<CustomParametersEntity> customParametersList){
        CustomParametersEntity customParameters=null;
        for(CustomParametersEntity customParametersEntity:customParametersList){
            customParameters = new CustomParametersEntity();
            customParameters.setAttributeValues(customParametersEntity.getAttributeValues());
            customParameters.setAttributeType(customParametersEntity.getAttributeType());
            customParameters.setAttributeNames(customParametersEntity.getAttributeNames());
            customParameters.setDescription(customParametersEntity.getDescription());
            customParametersRepository.save(customParameters);
        }
        return customParameters;
    }*/
}

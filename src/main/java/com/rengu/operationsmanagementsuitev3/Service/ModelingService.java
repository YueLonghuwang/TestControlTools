package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.ModelingEntity;
import com.rengu.operationsmanagementsuitev3.Repository.ModelingRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * TODO
 *
 * @author yyc
 * @version 1.0
 * @date 2020/10/26 11:13
 */
@Service
public class ModelingService {

    private final ModelingRepository modelingRepository;

    public ModelingService(ModelingRepository modelingRepository) {
        this.modelingRepository = modelingRepository;
    }

    //分页查询导出体系建模设计方案
    public Page findAllModeling(Pageable pageable) {
        return modelingRepository.findAll(pageable);
    }

    public List<ModelingEntity> saveModeling(List<ModelingEntity> list) {
        for (ModelingEntity s : list
        ) {
            if (StringUtils.isEmpty(s.getName())) {
                throw new RuntimeException(ApplicationMessages.MODELING_NAME_ARGS_NAME_NOT_FOUND);
            }
        }
        return modelingRepository.saveAll(list);
    }
}

package com.rengu.operationsmanagementsuitev3.Repository;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ComponentHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-24 14:00
 **/

@Repository
public interface ComponentHistoryRepository extends JpaRepository<ComponentHistoryEntity, String> {

    Page<ComponentHistoryEntity> findAllByComponentEntity(Pageable pageable, ComponentEntity componentEntity);

    List<ComponentHistoryEntity> findAllByComponentEntity(ComponentEntity componentEntity);

    ComponentHistoryEntity findFirstByComponentEntityOrderByTagDesc(ComponentEntity componentEntity);
}

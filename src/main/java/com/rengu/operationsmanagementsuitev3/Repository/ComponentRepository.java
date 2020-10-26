package com.rengu.operationsmanagementsuitev3.Repository;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-24 14:04
 **/

@Repository
public interface ComponentRepository extends JpaRepository<ComponentEntity, String> {

    boolean existsByNameAndVersionAndDeletedAndProjectEntity(String name, String version, boolean deleted, ProjectEntity projectEntity);

    Page<ComponentEntity> findByDeletedAndProjectEntity(Pageable pageable, boolean deleted, ProjectEntity projectEntity);

    List<ComponentEntity> findByDeletedAndProjectEntity(boolean deleted, ProjectEntity projectEntity);

    List<ComponentEntity> findAllByProjectEntity(ProjectEntity projectEntity);

    long countByDeletedAndProjectEntity(boolean deleted, ProjectEntity projectEntity);
}

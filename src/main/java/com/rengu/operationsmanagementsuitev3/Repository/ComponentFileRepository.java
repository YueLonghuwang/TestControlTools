package com.rengu.operationsmanagementsuitev3.Repository;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ComponentFileEntity;
import com.rengu.operationsmanagementsuitev3.Entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-24 14:03
 **/

@Repository
public interface ComponentFileRepository extends JpaRepository<ComponentFileEntity, String> {

    boolean existsByNameAndExtensionAndParentNodeAndComponentEntity(String name, String extension, ComponentFileEntity parentNode, ComponentEntity componentEntity);

    boolean existsByFileEntity(FileEntity fileEntity);

    Optional<ComponentFileEntity> findByNameAndExtensionAndParentNodeAndComponentEntity(String name, String extension, ComponentFileEntity parentNode, ComponentEntity componentEntity);

    List<ComponentFileEntity> findByParentNodeAndComponentEntity(ComponentFileEntity parentNode, ComponentEntity componentEntity);

    List<ComponentFileEntity> findAllByComponentEntity(ComponentEntity componentEntity);

}

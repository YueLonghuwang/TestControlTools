package com.rengu.operationsmanagementsuitev3.Repository;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignDetailEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignNodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-04 11:24
 **/

@Repository
public interface DeploymentDesignDetailRepository extends JpaRepository<DeploymentDesignDetailEntity, String> {

    boolean existsByDeploymentDesignNodeEntityAndComponentEntity(DeploymentDesignNodeEntity deploymentDesignNodeEntity, ComponentEntity componentEntity);

    List<DeploymentDesignDetailEntity> findAllByDeploymentDesignNodeEntity(DeploymentDesignNodeEntity deploymentDesignNodeEntity);

    List<DeploymentDesignDetailEntity> findAllByComponentEntity(ComponentEntity componentEntity);

    List<DeploymentDesignDetailEntity> findAllByDeploymentDesignEntity(DeploymentDesignEntity deploymentDesignEntity);
}

package com.rengu.operationsmanagementsuitev3.Repository;

import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignDetailEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignScanResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-10 15:54
 **/

@Repository
public interface DeploymentDesignScanResultRepository extends JpaRepository<DeploymentDesignScanResultEntity, String> {

    boolean existsByOrderId(String orderId);

    List<DeploymentDesignScanResultEntity> findAllByOrderId(String orderId);

    List<DeploymentDesignScanResultEntity> findAllByDeploymentDesignDetailEntity(DeploymentDesignDetailEntity deploymentDesignDetailEntity);
}

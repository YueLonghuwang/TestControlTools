package com.rengu.operationsmanagementsuitev3.Repository;

import com.rengu.operationsmanagementsuitev3.Entity.DeployLogEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * @program: operations-management-suite-v3
 * @author: hanch
 * @create: 2018-09-06 18:17
 **/

@Repository
public interface DeployLogRepository extends JpaRepository<DeployLogEntity, String> {

    Page<DeployLogEntity> findAllByProjectEntity(Pageable pageable, ProjectEntity projectEntity);

    void deleteAllByProjectEntity(ProjectEntity projectEntity);

    List<DeployLogEntity> findAllByProjectEntity(ProjectEntity projectEntity, Sort sort);
}

package com.rengu.operationsmanagementsuitev3.Repository;

import com.rengu.operationsmanagementsuitev3.Entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-22 17:00
 **/

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, String> {

    boolean existsByName(String name);

    Optional<RoleEntity> findByName(String name);
}

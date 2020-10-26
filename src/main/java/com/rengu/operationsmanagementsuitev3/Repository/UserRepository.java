package com.rengu.operationsmanagementsuitev3.Repository;

import com.rengu.operationsmanagementsuitev3.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-22 17:01
 **/

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    boolean existsByUsername(String username);

    Optional<UserEntity> findByUsername(String username);
}

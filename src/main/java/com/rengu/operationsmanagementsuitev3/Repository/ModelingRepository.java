package com.rengu.operationsmanagementsuitev3.Repository;

import com.rengu.operationsmanagementsuitev3.Entity.ModelingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * TODO
 *
 * @author yyc
 * @version 1.0
 * @date 2020/10/26 11:10
 */
@Repository
public interface ModelingRepository extends JpaRepository<ModelingEntity, String> {
}

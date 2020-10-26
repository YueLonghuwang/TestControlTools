package com.rengu.operationsmanagementsuitev3.Repository;
import com.rengu.operationsmanagementsuitev3.Entity.CustomParametersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CustomParametersRepository extends JpaRepository<CustomParametersEntity,String> {
    @Query(value = "SELECT t.* from custom_parameters_entity t LEFT JOIN  component_entity_custom_parameters_entity c on t.id = c.custom_parameters_entity_id JOIN component_entity d ON d.id = c.component_entity_id WHERE d.id = ?1",nativeQuery = true)
    List<CustomParametersEntity> findAllById(String componentId);
}

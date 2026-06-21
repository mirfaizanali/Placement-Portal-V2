package com.placement.portal.repository;

import com.placement.portal.domain.TrainingModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingModuleRepository extends JpaRepository<TrainingModule, String> {

    List<TrainingModule> findAllByOrderByDisplayOrderAsc();
}

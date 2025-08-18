package io.sci.citizen.model.repository;

import io.sci.citizen.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findAllByOrderBySequenceAsc();

    List<Section> findByProject_IdOrderBySequenceAsc(Long projectId);
}

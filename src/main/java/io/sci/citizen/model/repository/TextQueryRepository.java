package io.sci.citizen.model.repository;

import io.sci.citizen.model.TextQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TextQueryRepository extends JpaRepository<TextQuery, Long> {
    List<TextQuery> findAllByOrderBySequenceAsc();
    List<TextQuery> findBySection_IdOrderBySequenceAsc(Long sectionId);
}

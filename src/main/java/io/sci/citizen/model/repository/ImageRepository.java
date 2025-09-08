package io.sci.citizen.model.repository;

import io.sci.citizen.model.Image;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Transactional
public interface ImageRepository extends JpaRepository<Image, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = "delete from image where data_id = :dataId and section_id = :sectionId and original_file_name = :name",
            nativeQuery = true)
    int deleteObsoleteImage(@Param("dataId") long dataId, @Param("sectionId") long sectionId, @Param("name") String name);

    List<Image> findByDataId(Long dataId);
}
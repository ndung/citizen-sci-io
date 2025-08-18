package io.sci.citizen.model.repository;

import io.sci.citizen.model.Image;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

@Transactional
public interface ImageRepository extends JpaRepository<Image, Long> {

    void deleteImagesByData_Id(Long id);
}
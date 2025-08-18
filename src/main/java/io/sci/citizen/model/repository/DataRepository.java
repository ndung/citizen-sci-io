package io.sci.citizen.model.repository;

import io.sci.citizen.model.Data;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

@Transactional
public interface DataRepository extends JpaRepository<Data, Long> {

    @Query("select d from Data d where d.user.id = :userId and d.createdAt between :from and :to")
    List<Data> findByUserIdAndDate(@Param("userId") long userId, @Param("from") Date from, @Param("to") Date to);

    @Query(
            value = "select distinct(user.id) from data",
            nativeQuery = true)
    List<BigInteger> getUniqueUserIds();

    @Query(
            value = "select count(*) from data where user.id = :userId",
            nativeQuery = true)
    Integer getRecordCount(@Param("userId") long userId);

    Data findDataByUser_IdAndUuidOrderByCreatedAtDesc(Long userId, String uuid);

    List<Data> findDataByUser_IdOrderByCreatedAtDesc(Long userId);

    List<Data> findDataByUser_IdAndStatusOrderByCreatedAtDesc(Long userId, int status);

    List<Data> findByProject_IdOrderByCreatedAtDesc(Long projectId);
}

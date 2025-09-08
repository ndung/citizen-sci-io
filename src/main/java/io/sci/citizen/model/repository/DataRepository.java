package io.sci.citizen.model.repository;

import io.sci.citizen.model.Data;
import io.sci.citizen.model.User;
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
            value = "select distinct(user_id) from data",
            nativeQuery = true)
    List<BigInteger> getUniqueUserIds();

    @Query(
            value = "select count(distinct(user_id)) from data where project_id = :projectId",
            nativeQuery = true)
    Integer getUserCountByProjectId(@Param("projectId") long projectId);

    @Query(
            value = "select count(*) from data",
            nativeQuery = true)
    Integer getRecordCount();

    @Query(
            value = "select count(*) from data where user_id = :userId",
            nativeQuery = true)
    Integer getRecordCountByUserId(@Param("userId") long userId);

    @Query(
            value = "select count(*) from data where user_id = :userId and status = :status",
            nativeQuery = true)
    Integer getRecordCountByUserIdAndStatus(@Param("userId") long userId, @Param("status") int status);

    @Query(
            value = "select count(*) from data where project_id = :projectId",
            nativeQuery = true)
    Integer getRecordCountByProjectId(@Param("projectId") long projectId);

    @Query(
            value = "select count(*) from data where project_id = :projectId and user_id = :userId ",
            nativeQuery = true)
    Integer getRecordCountByProjectIdAndUserId(@Param("projectId") long projectId, @Param("userId") long userId);

    @Query(
            value = "select count(*) from data where project_id = :projectId and status = :status",
            nativeQuery = true)
    Integer getRecordCountByProjectIdAndStatus(@Param("projectId") long projectId, @Param("status") int status);

    @Query(
            value = "select count(*) from data where project_id = :projectId and user_id = :userId and status = :status",
            nativeQuery = true)
    Integer getRecordCountByProjectIdAndUserIdAndStatus(@Param("projectId") long projectId, @Param("userId") long userId, @Param("status") int status);

    List<Data> findByUser_IdAndUuidOrderByCreatedAtDesc(Long userId, String uuid);

    List<Data> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<Data> findByUser_IdAndStatusOrderByCreatedAtDesc(Long userId, int status);

    List<Data> findByProject_IdOrderByCreatedAtDesc(Long projectId);

    List<Data> findByProject_IdAndUser_IdOrderByCreatedAtDesc(Long projectId, Long userId);

    List<Data> findByProject_IdAndUser_IdAndStatusOrderByCreatedAtDesc(Long projectId, Long userId, int status);

    long user(User user);
}

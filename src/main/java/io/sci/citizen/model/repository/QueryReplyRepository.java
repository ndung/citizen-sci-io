package io.sci.citizen.model.repository;

import io.sci.citizen.model.QueryReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface QueryReplyRepository extends JpaRepository<QueryReply, Integer> {

    QueryReply findByData_idAndQuestion_Id(Long dataId, Long questionId);
}

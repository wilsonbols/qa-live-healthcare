package com.leansofx.qaservicequestion.repository;

import com.leansofx.qaservicequestion.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByPatientIdOrderBySubmitTimeDesc(Long patientId);

    List<Question> findByDoctorIdOrderBySubmitTimeDesc(Long doctorId);

    List<Question> findByStatusOrderBySubmitTimeDesc(Question.QuestionStatus status);

    /**
     * 按患者 + 医生 + 状态 + 时间区间组合查询。
     * 使用 nativeQuery：每个 :param 在 SQL 中出现 2 次（IS NULL + 比较），
     * 两处都必须 CAST，否则 NULL 时 PostgreSQL 无法推断参数类型。
     */
    @Query(value = "SELECT * FROM questions q WHERE " +
           "(CAST(:patientId AS BIGINT) IS NULL OR q.patient_id = CAST(:patientId AS BIGINT)) AND " +
           "(CAST(:doctorId AS BIGINT) IS NULL OR q.doctor_id = CAST(:doctorId AS BIGINT)) AND " +
           "(CAST(:status AS VARCHAR) IS NULL OR q.status = CAST(:status AS VARCHAR)) AND " +
           "(CAST(:startTime AS TIMESTAMP) IS NULL OR q.submit_time >= CAST(:startTime AS TIMESTAMP)) AND " +
           "(CAST(:endTime AS TIMESTAMP) IS NULL OR q.submit_time <= CAST(:endTime AS TIMESTAMP)) " +
           "ORDER BY q.submit_time DESC",
           nativeQuery = true)
    List<Question> findFiltered(
            @Param("patientId") Long patientId,
            @Param("doctorId") Long doctorId,
            @Param("status") String status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}

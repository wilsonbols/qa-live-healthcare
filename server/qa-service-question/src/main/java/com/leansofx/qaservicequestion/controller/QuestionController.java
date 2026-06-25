package com.leansofx.qaservicequestion.controller;

import com.leansofx.qaservicequestion.dto.AnswerRequest;
import com.leansofx.qaservicequestion.dto.QuestionRequest;
import com.leansofx.qaservicequestion.entity.Question;
import com.leansofx.qaservicequestion.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "*")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * 获取问题列表，支持多条件组合筛选：
     * - patientId: 患者ID
     * - doctorId: 医生ID
     * - status: 状态 (pending/answered)
     * - startDate: 起始日期 (ISO 格式 yyyy-MM-dd'T'HH:mm:ss)
     * - endDate: 结束日期
     */
    @GetMapping
    public ResponseEntity<List<Question>> getQuestions(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        return ResponseEntity.ok(
                questionService.getFilteredQuestions(patientId, doctorId, status, startDate, endDate));
    }

    /**
     * 根据ID获取问题
     */
    @GetMapping("/{id}")
    public ResponseEntity<Question> getQuestionById(@PathVariable Long id) {
        return questionService.getQuestionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建问题
     */
    @PostMapping
    public ResponseEntity<Question> createQuestion(@Valid @RequestBody QuestionRequest request) {
        Question question = questionService.createQuestion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(question);
    }

    /**
     * 更新问题
     */
    @PutMapping("/{id}")
    public ResponseEntity<Question> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequest request) {
        try {
            Question question = questionService.updateQuestion(id, request);
            return ResponseEntity.ok(question);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 回答问题
     */
    @PutMapping("/{id}/answer")
    public ResponseEntity<Question> answerQuestion(
            @PathVariable Long id,
            @Valid @RequestBody AnswerRequest request) {
        try {
            Question question = questionService.answerQuestion(id, request.getAnswer());
            return ResponseEntity.ok(question);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 删除问题
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteQuestion(@PathVariable Long id) {
        try {
            questionService.deleteQuestion(id);
            return ResponseEntity.ok(Map.of("message", "删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

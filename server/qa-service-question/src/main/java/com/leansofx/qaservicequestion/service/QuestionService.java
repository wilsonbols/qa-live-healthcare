package com.leansofx.qaservicequestion.service;

import com.leansofx.qaservicequestion.dto.QuestionRequest;
import com.leansofx.qaservicequestion.entity.Question;
import com.leansofx.qaservicequestion.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public Optional<Question> getQuestionById(Long id) {
        return questionRepository.findById(id);
    }

    public List<Question> getQuestionsByPatientId(Long patientId) {
        return questionRepository.findByPatientIdOrderBySubmitTimeDesc(patientId);
    }

    public List<Question> getQuestionsByDoctorId(Long doctorId) {
        return questionRepository.findByDoctorIdOrderBySubmitTimeDesc(doctorId);
    }

    public List<Question> getQuestionsByStatus(Question.QuestionStatus status) {
        return questionRepository.findByStatusOrderBySubmitTimeDesc(status);
    }

    /**
     * 组合过滤：按患者、医生、状态、时间区间查询
     */
    public List<Question> getFilteredQuestions(
            Long patientId,
            Long doctorId,
            String status,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        return questionRepository.findFiltered(patientId, doctorId, status, startTime, endTime);
    }

    @Transactional
    public Question createQuestion(QuestionRequest request) {
        Question question = new Question(
                request.getPatientId(),
                request.getPatientName(),
                request.getDoctorId(),
                request.getDoctorName(),
                request.getQuestion()
        );
        return questionRepository.save(question);
    }

    @Transactional
    public Question updateQuestion(Long id, QuestionRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("问题不存在: " + id));
        question.setPatientId(request.getPatientId());
        question.setPatientName(request.getPatientName());
        question.setDoctorId(request.getDoctorId());
        question.setDoctorName(request.getDoctorName());
        question.setQuestion(request.getQuestion());
        return questionRepository.save(question);
    }

    @Transactional
    public Question answerQuestion(Long id, String answer) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("问题不存在: " + id));
        question.setAnswer(answer);
        question.setAnswerTime(LocalDateTime.now());
        question.setStatus(Question.QuestionStatus.answered);
        return questionRepository.save(question);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new RuntimeException("问题不存在: " + id);
        }
        questionRepository.deleteById(id);
    }
}

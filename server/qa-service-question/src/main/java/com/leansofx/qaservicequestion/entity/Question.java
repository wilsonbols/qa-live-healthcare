package com.leansofx.qaservicequestion.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "patient_name", nullable = false)
    private String patientName;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "doctor_name")
    private String doctorName;

    @Column(nullable = false, length = 2000)
    private String question;

    @Column(name = "submit_time", nullable = false)
    private LocalDateTime submitTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionStatus status = QuestionStatus.pending;

    @Column(length = 2000)
    private String answer;

    @Column(name = "answer_time")
    private LocalDateTime answerTime;

    public enum QuestionStatus {
        pending, answered
    }

    public Question() {}

    public Question(Long patientId, String patientName, Long doctorId, String doctorName, String question) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.question = question;
        this.submitTime = LocalDateTime.now();
        this.status = QuestionStatus.pending;
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public LocalDateTime getSubmitTime() { return submitTime; }
    public void setSubmitTime(LocalDateTime submitTime) { this.submitTime = submitTime; }

    public QuestionStatus getStatus() { return status; }
    public void setStatus(QuestionStatus status) { this.status = status; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public LocalDateTime getAnswerTime() { return answerTime; }
    public void setAnswerTime(LocalDateTime answerTime) { this.answerTime = answerTime; }
}

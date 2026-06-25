package com.leansofx.qaservicequestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class QuestionRequest {

    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    @NotBlank(message = "患者姓名不能为空")
    private String patientName;

    @NotNull(message = "医生ID不能为空")
    private Long doctorId;

    private String doctorName;

    @NotBlank(message = "问题内容不能为空")
    private String question;

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
}

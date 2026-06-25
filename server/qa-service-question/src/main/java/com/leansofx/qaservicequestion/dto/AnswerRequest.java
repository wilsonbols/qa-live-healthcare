package com.leansofx.qaservicequestion.dto;

import jakarta.validation.constraints.NotBlank;

public class AnswerRequest {

    @NotBlank(message = "回复内容不能为空")
    private String answer;

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
}

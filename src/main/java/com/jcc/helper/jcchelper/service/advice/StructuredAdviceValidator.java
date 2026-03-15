package com.jcc.helper.jcchelper.service.advice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class StructuredAdviceValidator {

    private final Validator validator;

    public StructuredAdviceValidator(Validator validator) {
        this.validator = validator;
    }

    public StructuredAdvice validateOrFallback(StructuredAdvice candidate) {
        Set<ConstraintViolation<StructuredAdvice>> violations = validator.validate(candidate);
        if (violations.isEmpty()) {
            return candidate;
        }
        String violationSummary = violations.stream()
                .map(v -> v.getPropertyPath() + ":" + v.getMessage())
                .collect(Collectors.joining("; "));
        return new StructuredAdvice(
                "输出结构校验失败，已降级为保守建议。",
                "优先稳血与经济平衡",
                java.util.List.of("先补当前战力缺口，再评估是否投入刷新。"),
                java.util.List.of("结构校验失败: " + violationSummary),
                java.util.List.of("输出可信度下降，建议人工复核。"),
                java.util.List.of("本次返回为兜底模板。"),
                0.45
        );
    }
}

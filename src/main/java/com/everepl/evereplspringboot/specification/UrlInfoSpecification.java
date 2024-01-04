package com.everepl.evereplspringboot.specification;


import com.everepl.evereplspringboot.entity.UrlInfo;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class UrlInfoSpecification {

    public static Specification<UrlInfo> hasKeywordInUrl(List<String> keywords) {
        return (root, query, criteriaBuilder) -> {
            if (keywords == null || keywords.isEmpty()) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true)); // 모든 데이터 반환
            }

            Predicate[] predicates = new Predicate[keywords.size()];
            for (int i = 0; i < keywords.size(); i++) {
                predicates[i] = criteriaBuilder.like(root.get("url"), "%" + keywords.get(i) + "%");
            }
            return criteriaBuilder.or(predicates); // 주어진 키워드 중 하나라도 url에 포함되면 반환
        };
    }
}
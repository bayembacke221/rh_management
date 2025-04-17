package sn.bmbacke.rh.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import sn.bmbacke.rh.entity.Contract;
import sn.bmbacke.rh.entity.enums.ContratStatus;
import sn.bmbacke.rh.entity.enums.Type;

import java.time.LocalDate;

public class ContractSpecification {

    public static Specification<Contract> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            String pattern = "%" + keyword.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("employee").get("firstName")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("employee").get("lastName")), pattern)
            );
        };
    }

    public static Specification<Contract> hasType(Type type) {
        return (root, query, criteriaBuilder) -> {
            if (type == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("type"), type);
        };
    }

    public static Specification<Contract> hasStatus(ContratStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Contract> hasStartDateAfter(LocalDate startDateMin) {
        return (root, query, criteriaBuilder) -> {
            if (startDateMin == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), startDateMin);
        };
    }

    public static Specification<Contract> hasStartDateBefore(LocalDate startDateMax) {
        return (root, query, criteriaBuilder) -> {
            if (startDateMax == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), startDateMax);
        };
    }
}
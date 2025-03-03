package sn.bmbacke.rh.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import sn.bmbacke.rh.common.BaseEntity;
import sn.bmbacke.rh.entity.enums.DocEnum;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@SuperBuilder
@Table(name = "document")
@Data
@NoArgsConstructor
public class Document extends BaseEntity {
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "type", nullable = false)
    private DocEnum type;
    @Column(name = "path", nullable = false)
    private String path;
    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;
    @Column(name = "size", nullable = false)
    private Long size;
    @Column(name = "content_type", nullable = false)
    private String contentType;
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
    @ManyToOne
    @JoinColumn(name = "contract_id")
    private Contract contract;
}


package sn.bmbacke.rh.payload.mapper;


import org.mapstruct.*;
import sn.bmbacke.rh.payload.dto.*;
import sn.bmbacke.rh.entity.Contract;
import sn.bmbacke.rh.entity.Document;
import sn.bmbacke.rh.entity.Employee;


@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(target = "downloadUrl", expression = "java(getDownloadUrl(document))")
    DocumentDTO toDto(Document document);

    @Named("toDocumentShort")
    DocumentShortDTO toShortDto(Document document);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "uploadDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "path", ignore = true)
    @Mapping(target = "size", ignore = true)
    @Mapping(target = "contentType", ignore = true)
    @Mapping(target = "employee", source = "employeeId", qualifiedByName = "documentMapperEmployeeFromId")
    @Mapping(target = "contract", source = "contractId", qualifiedByName = "contractFromId")
    Document toEntity(DocumentCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "uploadDate", ignore = true)
    @Mapping(target = "path", ignore = true)
    @Mapping(target = "size", ignore = true)
    @Mapping(target = "contentType", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "contract", ignore = true)
    void updateEntityFromDto(DocumentUpdateDTO dto, @MappingTarget Document document);

    @Named("documentMapperEmployeeFromId")
    default Employee employeeFromId(Long id) {
        if (id == null) {
            return null;
        }
        Employee employee = new Employee();
        employee.setId(id);
        return employee;
    }

    @Named("contractFromId")
    default Contract contractFromId(Long id) {
        if (id == null) {
            return null;
        }
        Contract contract = new Contract();
        contract.setId(id);
        return contract;
    }

    /**
     * Génère l'URL de téléchargement pour un document
     */
    default String getDownloadUrl(Document document) {
        if (document == null || document.getId() == null) {
            return null;
        }

        StringBuilder url = new StringBuilder("/api");

        if (document.getEmployee() != null) {
            url.append("/employees/")
                    .append(document.getEmployee().getId());
        } else if (document.getContract() != null) {
            url.append("/contracts/")
                    .append(document.getContract().getId());
        } else {
            return null;
        }

        url.append("/documents/")
                .append(document.getId());

        return url.toString();
    }
}

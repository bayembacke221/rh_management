package sn.bmbacke.rh.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sn.bmbacke.rh.payload.dto.DocumentDTO;
import sn.bmbacke.rh.payload.dto.DocumentShortDTO;
import sn.bmbacke.rh.payload.dto.DocumentUpdateDTO;
import sn.bmbacke.rh.entity.Contract;
import sn.bmbacke.rh.entity.Document;
import sn.bmbacke.rh.entity.Employee;
import sn.bmbacke.rh.entity.enums.DocEnum;
import sn.bmbacke.rh.exception.BusinessException;
import sn.bmbacke.rh.exception.ResourceNotFoundException;
import sn.bmbacke.rh.helper.file.FileStorageService;
import sn.bmbacke.rh.helper.file.FileUtils;
import sn.bmbacke.rh.helper.file.ResourceUtils;
import sn.bmbacke.rh.payload.mapper.DocumentMapper;
import sn.bmbacke.rh.repository.ContractRepository;
import sn.bmbacke.rh.repository.DocumentRepository;
import sn.bmbacke.rh.repository.EmployeeRepository;
import sn.bmbacke.rh.service.DocumentService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;
    private final DocumentMapper documentMapper;
    private final FileStorageService fileStorageService;
    private final ResourceUtils resourceUtils;

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentShortDTO> getAllDocuments(Pageable pageable) {
        return documentRepository.findAll(pageable)
                .map(documentMapper::toShortDto);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDTO getDocumentById(Long id) {
        return documentRepository.findById(id)
                .map(documentMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Document non trouvé avec l'ID : " + id));
    }

    @Override
    public DocumentDTO uploadEmployeeDocument(Long employeeId, MultipartFile file, DocEnum type, String name) {
        // Vérifier que l'employé existe
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé avec l'ID : " + employeeId));

        // Valider le type de document
        if (!resourceUtils.isValidDocumentType(file, type)) {
            throw new BusinessException("Type de fichier non pris en charge pour ce type de document");
        }

        // Sauvegarder le fichier
        String filePath = fileStorageService.saveFile(file, employee.getId().intValue());
        if (filePath == null) {
            throw new BusinessException("Échec de l'enregistrement du fichier");
        }

        // Créer le document en base de données
        Document document = new Document();
        document.setName(name != null ? name : file.getOriginalFilename());
        document.setType(type);
        document.setPath(filePath);
        document.setUploadDate(LocalDateTime.now());
        document.setSize(file.getSize());
        document.setContentType(file.getContentType());
        document.setEmployee(employee);

        Document savedDocument = documentRepository.save(document);
        return documentMapper.toDto(savedDocument);
    }

    @Override
    public DocumentDTO uploadContractDocument(Long contractId, MultipartFile file, DocEnum type, String name) {
        // Vérifier que le contrat existe
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat non trouvé avec l'ID : " + contractId));

        // Vérifier que l'employé associé au contrat existe
        Employee employee = contract.getEmployee();
        if (employee == null) {
            throw new BusinessException("Aucun employé associé à ce contrat");
        }

        // Valider le type de document
        if (!resourceUtils.isValidDocumentType(file, type)) {
            throw new BusinessException("Type de fichier non pris en charge pour ce type de document");
        }

        // Sauvegarder le fichier
        String filePath = fileStorageService.saveFile(file, employee.getId().intValue());
        if (filePath == null) {
            throw new BusinessException("Échec de l'enregistrement du fichier");
        }

        // Créer le document en base de données
        Document document = new Document();
        document.setName(name != null ? name : file.getOriginalFilename());
        document.setType(type);
        document.setPath(filePath);
        document.setUploadDate(LocalDateTime.now());
        document.setSize(file.getSize());
        document.setContentType(file.getContentType());
        document.setContract(contract);

        Document savedDocument = documentRepository.save(document);
        return documentMapper.toDto(savedDocument);
    }

    @Override
    public DocumentDTO updateDocument(Long id, DocumentUpdateDTO documentUpdateDTO) {
        // Vérifier que le document existe
        Document existingDocument = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document non trouvé avec l'ID : " + id));

        // Mise à jour des informations
        documentMapper.updateEntityFromDto(documentUpdateDTO, existingDocument);

        Document updatedDocument = documentRepository.save(existingDocument);
        return documentMapper.toDto(updatedDocument);
    }

    @Override
    public void deleteDocument(Long id) {
        // Vérifier que le document existe
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document non trouvé avec l'ID : " + id));

        // Supprimer le document physiquement
        fileStorageService.deleteFile(document.getPath());

        documentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentShortDTO> getEmployeeDocuments(Long employeeId) {
        // Vérifier que l'employé existe
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employé non trouvé avec l'ID : " + employeeId);
        }

        return documentRepository.findByEmployee_Id(employeeId).stream()
                .map(documentMapper::toShortDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentShortDTO> getContractDocuments(Long contractId) {
        // Vérifier que le contrat existe
        if (!contractRepository.existsById(contractId)) {
            throw new ResourceNotFoundException("Contrat non trouvé avec l'ID : " + contractId);
        }

        return documentRepository.findByContract_Id(contractId).stream()
                .map(documentMapper::toShortDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean documentExists(Long id) {
        return documentRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource getDocumentContent(Long id) {
        // Vérifier que le document existe
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document non trouvé avec l'ID : " + id));

        // Lire le contenu du fichier
        byte[] fileData = FileUtils.readFileFromLocation(document.getPath());
        if (fileData == null || fileData.length == 0) {
            throw new BusinessException("Le contenu du document est vide ou inaccessible");
        }

        return resourceUtils.createResourceFromBytes(fileData, document.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentShortDTO> getDocumentsByType(DocEnum type) {
        return documentRepository.findByType(type).stream()
                .map(documentMapper::toShortDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentShortDTO> searchDocuments(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return documentRepository.findAll().stream()
                    .map(documentMapper::toShortDto)
                    .toList();
        }

        return documentRepository.searchDocuments(keyword).stream()
                .map(documentMapper::toShortDto)
                .toList();
    }
}

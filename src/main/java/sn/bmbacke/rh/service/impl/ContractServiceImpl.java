package sn.bmbacke.rh.service.impl;


import lombok.RequiredArgsConstructor;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.bmbacke.rh.payload.dto.*;
import sn.bmbacke.rh.entity.Contract;
import sn.bmbacke.rh.entity.Employee;
import sn.bmbacke.rh.entity.enums.Type;
import sn.bmbacke.rh.entity.enums.ContratStatus;
import sn.bmbacke.rh.exception.BusinessException;
import sn.bmbacke.rh.exception.ResourceNotFoundException;
import sn.bmbacke.rh.payload.mapper.ContractMapper;
import sn.bmbacke.rh.repository.ContractRepository;
import sn.bmbacke.rh.repository.EmployeeRepository;
import sn.bmbacke.rh.service.ContractService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.apache.poi.xwpf.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Transactional
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;
    private final ContractMapper contractMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ContractDTO> getAllContracts(Pageable pageable) {
        return contractRepository.findAll(pageable)
                .map(contractMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractDTO getContractById(Long id) {
        return contractRepository.findById(id)
                .map(contractMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat non trouvé avec l'ID : " + id));
    }

    @Override
    public ContractDTO createContract(ContractCreateDTO contractCreateDTO) {
        // Vérifier que l'employé existe
        Employee employee = employeeRepository.findById(contractCreateDTO.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé avec l'ID : " +
                        contractCreateDTO.getEmployeeId()));

        // Valider les dates de contrat
        validateContractDates(contractCreateDTO.getStartDate(), contractCreateDTO.getEndDate(),
                contractCreateDTO.getType());

        // Vérifier si l'employé a déjà un contrat actif
        if (contractCreateDTO.getStatus() == ContratStatus.ACTIVE) {
            Optional<Contract> activeContract = contractRepository.findByEmployee_IdAndStatus(
                    contractCreateDTO.getEmployeeId(), ContratStatus.ACTIVE);

            if (activeContract.isPresent()) {
                throw new BusinessException("L'employé a déjà un contrat actif. Veuillez d'abord terminer ou mettre à jour ce contrat.");
            }
        }

        // Conversion et sauvegarde
        Contract contract = contractMapper.toEntity(contractCreateDTO);

        // Valeurs par défaut
        if (contract.getStatus() == null) {
            contract.setStatus(ContratStatus.DRAFT);
            contract.setEmployee(employee);
        }

        Contract savedContract = contractRepository.save(contract);
        return contractMapper.toDto(savedContract);
    }

    @Override
    public ContractDTO updateContract(Long id, ContractUpdateDTO contractUpdateDTO) {
        // Vérifier que le contrat existe
        Contract existingContract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat non trouvé avec l'ID : " + id));

        // Valider les dates de contrat
        validateContractDates(contractUpdateDTO.getStartDate(), contractUpdateDTO.getEndDate(),
                contractUpdateDTO.getType());

        // Mise à jour
        contractMapper.updateEntityFromDto(contractUpdateDTO, existingContract);
        Contract updatedContract = contractRepository.save(existingContract);
        return contractMapper.toDto(updatedContract);
    }

    @Override
    public ContractDTO updateContractStatus(Long id, ContractStatusUpdateDTO statusUpdateDTO) {
        // Vérifier que le contrat existe
        Contract existingContract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat non trouvé avec l'ID : " + id));

        // Valider la transition de statut
        validateStatusTransition(existingContract.getStatus(), statusUpdateDTO.getStatus());

        // Mise à jour du statut
        existingContract.setStatus(statusUpdateDTO.getStatus());

        // Si le contrat est terminé ou expiré, mettre à jour la date de fin et la raison
        if (statusUpdateDTO.getStatus() == ContratStatus.TERMINATED) {
            if (statusUpdateDTO.getEndDate() == null) {
                existingContract.setEndDate(LocalDate.now());
            } else {
                existingContract.setEndDate(statusUpdateDTO.getEndDate());
            }
            existingContract.setTerminationReason(statusUpdateDTO.getTerminationReason());
        }

        Contract updatedContract = contractRepository.save(existingContract);
        return contractMapper.toDto(updatedContract);
    }

    @Override
    public void deleteContract(Long id) {
        // Vérifier que le contrat existe
        if (!contractRepository.existsById(id)) {
            throw new ResourceNotFoundException("Contrat non trouvé avec l'ID : " + id);
        }

        // Supprimer le contrat
        contractRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractDTO> getEmployeeContracts(Long employeeId) {
        // Vérifier que l'employé existe
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employé non trouvé avec l'ID : " + employeeId);
        }

        return contractRepository.findByEmployee_Id(employeeId).stream()
                .map(contractMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean contractExists(Long id) {
        return contractRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractDTO getActiveEmployeeContract(Long employeeId) {
        // Vérifier que l'employé existe
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employé non trouvé avec l'ID : " + employeeId);
        }

        return contractRepository.findByEmployee_IdAndStatus(employeeId, ContratStatus.ACTIVE)
                .map(contractMapper::toDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractDTO> getContractsByType(Type type) {
        return contractRepository.findByType(type).stream()
                .map(contractMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractDTO> getContractsByStatus(ContratStatus status) {
        return contractRepository.findByStatus(status).stream()
                .map(contractMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractDTO> getExpiringContracts(int daysThreshold) {
        LocalDate today = LocalDate.now();
        LocalDate expiryDate = today.plusDays(daysThreshold);

        return contractRepository.findExpiringContracts(today, expiryDate).stream()
                .map(contractMapper::toDto)
                .toList();
    }

    @Override
    public byte[] generateContractDocument(Long contractId) {
        // Vérifier que le contrat existe
        ContractDTO contract = getContractById(contractId);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // Créer un nouveau document Word
            XWPFDocument document = new XWPFDocument();

            // Ajouter un en-tête avec logo (à remplacer par votre logo)
            XWPFHeader header = document.createHeader(HeaderFooterType.DEFAULT);
            XWPFParagraph headerParagraph = header.createParagraph();
            headerParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun headerRun = headerParagraph.createRun();
            headerRun.setText("ENTREPRISE XYZ");
            headerRun.setBold(true);
            headerRun.setFontSize(16);

            // Titre du document
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            titleParagraph.setSpacingAfter(500);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText("CONTRAT DE TRAVAIL");
            titleRun.setBold(true);
            titleRun.setFontSize(18);
            titleRun.addBreak();

            // Sous-titre avec type de contrat
            XWPFRun subtitleRun = titleParagraph.createRun();
            subtitleRun.setText(getContractTypeLabel(contract.getType()));
            subtitleRun.setFontSize(14);
            subtitleRun.addBreak();
            subtitleRun.addBreak();

            // Préambule
            XWPFParagraph preambleParagraph = document.createParagraph();
            preambleParagraph.setSpacingAfter(200);
            XWPFRun preambleRun = preambleParagraph.createRun();
            preambleRun.setText("Entre les soussignés :");
            preambleRun.addBreak();
            preambleRun.addBreak();

            // Partie employeur
            preambleRun.setText("La société ENTREPRISE XYZ, immatriculée au RCS sous le numéro XXX XXX XXX, ");
            preambleRun.setText("dont le siège social est situé au [Adresse complète], représentée par M./Mme [Nom du représentant] en sa qualité de [Fonction],");
            preambleRun.addBreak();
            preambleRun.addBreak();
            preambleRun.setText("Ci-après dénommée « l'Employeur »,");
            preambleRun.addBreak();
            preambleRun.addBreak();
            preambleRun.setText("D'une part,");
            preambleRun.addBreak();
            preambleRun.addBreak();

            // Partie employé
            preambleRun.setText("Et");
            preambleRun.addBreak();
            preambleRun.addBreak();
            preambleRun.setText("M./Mme " + contract.getEmployee().getFirstName() + " " + contract.getEmployee().getLastName() + ", ");
            preambleRun.setText("demeurant au [Adresse de l'employé], Numéro de sécurité sociale : [Numéro],");
            preambleRun.addBreak();
            preambleRun.addBreak();
            preambleRun.setText("Ci-après dénommé(e) « le Salarié »,");
            preambleRun.addBreak();
            preambleRun.addBreak();
            preambleRun.setText("D'autre part,");
            preambleRun.addBreak();
            preambleRun.addBreak();

            // Accord
            XWPFParagraph agreementParagraph = document.createParagraph();
            agreementParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun agreementRun = agreementParagraph.createRun();
            agreementRun.setText("IL A ÉTÉ CONVENU CE QUI SUIT :");
            agreementRun.setBold(true);
            agreementRun.addBreak();
            agreementRun.addBreak();

            // Articles
            addArticle(document, "Article 1 - Engagement",
                    "L'Employeur engage le Salarié qui accepte, à compter du " +
                            formatDate(contract.getStartDate()) +
                            (contract.getEndDate() != null ? ", et jusqu'au " + formatDate(contract.getEndDate()) : "") + ".");

            addArticle(document, "Article 2 - Fonction",
                    "Le Salarié est engagé en qualité de [Fonction], statut [Statut], " +
                            "coefficient [Coefficient], niveau [Niveau], échelon [Échelon].");

            addArticle(document, "Article 3 - Durée du travail",
                    "La durée hebdomadaire de travail du Salarié est fixée à " +
                            contract.getWorkHoursPerWeek() + " heures. " +
                            "Cette durée pourra être modifiée conformément aux dispositions légales et conventionnelles en vigueur.");

            addArticle(document, "Article 4 - Rémunération",
                    "En contrepartie de son travail, le Salarié percevra une rémunération mensuelle brute de " +
                            contract.getSalary() + " euros, pour la durée de travail prévue à l'article 3.");

            // Ajout des articles spécifiques selon le type de contrat
            if (contract.getType() == Type.CDD || contract.getType() == Type.INTERIM) {
                addArticle(document, "Article 5 - Motif du recours au contrat",
                        "Le présent contrat est conclu pour le motif suivant : [Motif précis].");

                if (contract.getEndDate() != null) {
                    addArticle(document, "Article 6 - Durée du contrat",
                            "Le présent contrat est conclu pour une durée déterminée de [X] mois, du " +
                                    formatDate(contract.getStartDate()) + " au " + formatDate(contract.getEndDate()) + ".");
                }
            }

            // Articles communs
            addArticle(document, "Article 7 - Période d'essai",
                    "Le présent contrat est soumis à une période d'essai de [Durée] " +
                            "pendant laquelle chacune des parties pourra rompre le contrat sans indemnité ni préavis.");

            addArticle(document, "Article 8 - Lieu de travail",
                    "Le Salarié exercera ses fonctions à [Lieu de travail]. " +
                            "Toutefois, compte tenu de la nature de ses fonctions, il pourra être amené à se déplacer ou à exercer " +
                            "temporairement ses fonctions en tout autre lieu selon les besoins de l'Employeur.");

            addArticle(document, "Article 9 - Convention collective",
                    "Le présent contrat est régi par la Convention Collective [Nom de la convention] " +
                            "dont le Salarié reconnaît avoir pris connaissance.");

            // Signature
            XWPFParagraph signatureParagraph = document.createParagraph();
            signatureParagraph.setSpacingBefore(1000);
            XWPFRun signatureRun = signatureParagraph.createRun();
            signatureRun.setText("Fait en deux exemplaires à [Lieu], le [Date]");
            signatureRun.addBreak();
            signatureRun.addBreak();
            signatureRun.addBreak();

            // Créer un tableau pour les signatures
            XWPFTable signatureTable = document.createTable(1, 2);
            signatureTable.getRow(0).getCell(0).setText("L'Employeur\n(Signature et cachet)");
            signatureTable.getRow(0).getCell(1).setText("Le Salarié\n(Signature précédée de la mention « Lu et approuvé »)");

            // Génération du document
            document.write(outputStream);

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("Erreur lors de la génération du document de contrat: " + e.getMessage());
        }
    }

    /**
     * Ajoute un article au document
     */
    private void addArticle(XWPFDocument document, String title, String content) {
        XWPFParagraph titleParagraph = document.createParagraph();
        titleParagraph.setSpacingBefore(200);
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText(title);
        titleRun.setBold(true);

        XWPFParagraph contentParagraph = document.createParagraph();
        contentParagraph.setSpacingAfter(200);
        XWPFRun contentRun = contentParagraph.createRun();
        contentRun.setText(content);
    }

    /**
     * Formate une date pour l'affichage
     */
    private String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * Obtient un libellé pour le type de contrat
     */
    private String getContractTypeLabel(Type type) {
        if (type == null) {
            return "CONTRAT DE TRAVAIL";
        }

        return switch (type) {
            case CDI -> "CONTRAT À DURÉE INDÉTERMINÉE";
            case CDD -> "CONTRAT À DURÉE DÉTERMINÉE";
            case STAGE -> "CONVENTION DE STAGE";
            case INTERIM -> "CONTRAT DE TRAVAIL TEMPORAIRE";
            case FREELANCE -> "CONTRAT DE PRESTATION DE SERVICES";
            case APPRENTICESHIP -> "CONTRAT D'APPRENTISSAGE";
            case PART_TIME -> "CONTRAT À TEMPS PARTIEL";
            default -> "CONTRAT DE TRAVAIL";
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContractDTO> searchContracts(String keyword, Type type,
                                             ContratStatus status, LocalDate startDateMin,
                                             LocalDate startDateMax, Pageable pageable) {
        return contractRepository.searchContracts(keyword, type, status, startDateMin, startDateMax, pageable)
                .map(contractMapper::toDto);
    }

    /**
     * Valide les dates de contrat selon le type
     */
    private void validateContractDates(LocalDate startDate, LocalDate endDate, Type type) {
        if (startDate == null) {
            throw new BusinessException("La date de début du contrat est obligatoire");
        }

        if (startDate.isBefore(LocalDate.now().minusYears(1))) {
            throw new BusinessException("La date de début ne peut pas être antérieure à un an");
        }

        // Pour les CDD, stage, interim, la date de fin est obligatoire
        if ((type == Type.CDD || type == Type.STAGE || type == Type.INTERIM) && endDate == null) {
            throw new BusinessException("La date de fin est obligatoire pour les contrats de type " + type);
        }

        // Si date de fin fournie, vérifier qu'elle est postérieure à la date de début
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException("La date de fin doit être postérieure à la date de début");
        }
    }

    /**
     * Valide la transition de statut
     */
    private void validateStatusTransition(ContratStatus currentStatus, ContratStatus newStatus) {
        if (currentStatus == newStatus) {
            return; // Pas de changement, c'est valide
        }

        // Règles de transition
        switch (currentStatus) {
            case DRAFT:
                // Un brouillon peut passer à en attente ou actif
                if (newStatus != ContratStatus.PENDING && newStatus != ContratStatus.ACTIVE) {
                    throw new BusinessException("Un contrat en brouillon ne peut passer qu'à en attente ou actif");
                }
                break;
            case PENDING:
                // En attente peut passer à actif ou brouillon (retour)
                if (newStatus != ContratStatus.ACTIVE && newStatus != ContratStatus.DRAFT) {
                    throw new BusinessException("Un contrat en attente ne peut passer qu'à actif ou brouillon");
                }
                break;
            case ACTIVE:
                // Actif peut passer à expiré, résilié ou renouvelé
                if (newStatus != ContratStatus.EXPIRED && newStatus != ContratStatus.TERMINATED
                        && newStatus != ContratStatus.RENEWED) {
                    throw new BusinessException("Un contrat actif ne peut passer qu'à expiré, résilié ou renouvelé");
                }
                break;
            case EXPIRED:
            case TERMINATED:
                // Les statuts finaux ne peuvent pas être modifiés
                throw new BusinessException("Un contrat " + currentStatus + " ne peut plus changer de statut");
            case RENEWED:
                // Renouvelé peut passer à actif (nouveau contrat créé)
                if (newStatus != ContratStatus.ACTIVE) {
                    throw new BusinessException("Un contrat renouvelé ne peut passer qu'à actif");
                }
                break;
            default:
                throw new BusinessException("Statut de contrat non reconnu: " + currentStatus);
        }
    }
}
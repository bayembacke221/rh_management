package sn.bmbacke.rh.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.bmbacke.rh.payload.dto.*;
import sn.bmbacke.rh.entity.Departement;
import sn.bmbacke.rh.entity.Employee;
import sn.bmbacke.rh.exception.ResourceNotFoundException;
import sn.bmbacke.rh.exception.BusinessException;
import sn.bmbacke.rh.payload.mapper.DepartementMapper;
import sn.bmbacke.rh.payload.mapper.EmployeeMapper;
import sn.bmbacke.rh.repository.DepartmentRepository;
import sn.bmbacke.rh.repository.EmployeeRepository;
import sn.bmbacke.rh.service.DepartementService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartementServiceImpl implements DepartementService {

    private final DepartmentRepository departementRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartementMapper departementMapper;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<DepartementDTO> getAllDepartements(Pageable pageable) {
        return departementRepository.findAll(pageable)
                .map(departementMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartementDTO getDepartementById(Long id) {
        return departementRepository.findById(id)
                .map(departementMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Département non trouvé avec l'ID : " + id));
    }

    @Override
    public DepartementDTO createDepartement(DepartementCreateDTO departementCreateDTO) {
        // Vérification du code unique
        Departement existingByCode = departementRepository.findByCode(departementCreateDTO.getCode());
        if (existingByCode != null) {
            throw new BusinessException("Un département avec le code " + departementCreateDTO.getCode() + " existe déjà");
        }

        // Vérifications pour les relations
        if (departementCreateDTO.getManagerId() != null) {
            Optional<Employee> manager = employeeRepository.findById(departementCreateDTO.getManagerId());
            if (manager.isEmpty()) {
                throw new ResourceNotFoundException("Employé non trouvé pour manager avec l'ID : " +
                        departementCreateDTO.getManagerId());
            }
        }

        if (departementCreateDTO.getParentDepartementId() != null) {
            Optional<Departement> parent = departementRepository.findById(departementCreateDTO.getParentDepartementId());
            if (parent.isEmpty()) {
                throw new ResourceNotFoundException("Département parent non trouvé avec l'ID : " +
                        departementCreateDTO.getParentDepartementId());
            }
        }

        // Conversion et sauvegarde
        Departement departement = departementMapper.toEntity(departementCreateDTO);

        // Valeurs par défaut
        if (departement.getActive() == null) {
            departement.setActive(true);
        }

        Departement savedDepartement = departementRepository.save(departement);
        return departementMapper.toDto(savedDepartement);
    }

    @Override
    public DepartementDTO updateDepartement(Long id, DepartementUpdateDTO departementUpdateDTO) {
        // Vérifier que le département existe
        Departement existingDepartement = departementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Département non trouvé avec l'ID : " + id));

        // Vérification du code unique (si modifié)
        if (!existingDepartement.getCode().equals(departementUpdateDTO.getCode())) {
            Departement existingByCode = departementRepository.findByCode(departementUpdateDTO.getCode());
            if (existingByCode != null) {
                throw new BusinessException("Un département avec le code " + departementUpdateDTO.getCode() + " existe déjà");
            }
        }

        // Vérifications pour les relations
        if (departementUpdateDTO.getManagerId() != null) {
            Optional<Employee> manager = employeeRepository.findById(departementUpdateDTO.getManagerId());
            if (manager.isEmpty()) {
                throw new ResourceNotFoundException("Employé non trouvé pour manager avec l'ID : " +
                        departementUpdateDTO.getManagerId());
            }
        }

        // Vérifier la hiérarchie pour éviter les références circulaires
        if (departementUpdateDTO.getParentDepartementId() != null) {
            if (departementUpdateDTO.getParentDepartementId().equals(id)) {
                throw new BusinessException("Un département ne peut pas être son propre parent");
            }

            // Vérification que le parent existe
            Optional<Departement> parent = departementRepository.findById(departementUpdateDTO.getParentDepartementId());
            if (parent.isEmpty()) {
                throw new ResourceNotFoundException("Département parent non trouvé avec l'ID : " +
                        departementUpdateDTO.getParentDepartementId());
            }

            // Vérifier que le département n'est pas un ancêtre de son futur parent (pour éviter les cycles)
            if (isAncestor(id, departementUpdateDTO.getParentDepartementId())) {
                throw new BusinessException("Référence circulaire détectée dans la hiérarchie des départements");
            }
        }

        // Mise à jour
        departementMapper.updateEntityFromDto(departementUpdateDTO, existingDepartement);
        Departement updatedDepartement = departementRepository.save(existingDepartement);
        return departementMapper.toDto(updatedDepartement);
    }

    @Override
    public void deleteDepartement(Long id) {
        // Vérifier que le département existe
        if (!departementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Département non trouvé avec l'ID : " + id);
        }

        // Vérifier que le département n'a pas d'employés
        if (employeeRepository.existsByDepartement_Id(id)) {
            throw new BusinessException("Impossible de supprimer le département car il contient des employés");
        }

        // Vérifier que le département n'a pas de sous-départements
        if (departementRepository.existsByParentDepartement_Id(id)) {
            throw new BusinessException("Impossible de supprimer le département car il contient des sous-départements");
        }

        departementRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean departementExists(Long id) {
        return departementRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeShortDTO> getEmployeesByDepartement(Long departementId, Pageable pageable) {
        // Vérifier que le département existe
        if (!departementRepository.existsById(departementId)) {
            throw new ResourceNotFoundException("Département non trouvé avec l'ID : " + departementId);
        }

        return employeeRepository.findByDepartement_Id(departementId, pageable)
                .map(employeeMapper::toShortDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartementTreeDTO> getDepartementTree() {
        List<Departement> allDepartements = departementRepository.findAll();
        return departementMapper.toDepartementTree(allDepartements);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartementDTO> getTopLevelDepartements() {
        return departementRepository.findByParentDepartementIsNull().stream()
                .map(departementMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartementDTO> getSubDepartements(Long parentId) {
        // Vérifier que le département parent existe
        if (!departementRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("Département parent non trouvé avec l'ID : " + parentId);
        }

        return departementRepository.findByParentDepartement_Id(parentId).stream()
                .map(departementMapper::toDto)
                .toList();
    }

    @Override
    public DepartementDTO updateDepartementStatus(Long id, Boolean active) {
        // Vérifier que le département existe
        Departement departement = departementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Département non trouvé avec l'ID : " + id));

        departement.setActive(active);
        Departement updatedDepartement = departementRepository.save(departement);
        return departementMapper.toDto(updatedDepartement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartementDTO> searchDepartements(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return departementRepository.findAll().stream()
                    .map(departementMapper::toDto)
                    .toList();
        }

        return departementRepository.searchDepartements(keyword).stream()
                .map(departementMapper::toDto)
                .toList();
    }

    /**
     * Vérifie si un département est un ancêtre d'un autre (pour éviter les cycles dans la hiérarchie)
     */
    private boolean isAncestor(Long potentialAncestorId, Long departementId) {
        if (departementId == null) {
            return false;
        }

        Optional<Departement> departement = departementRepository.findById(departementId);
        if (departement.isEmpty() || departement.get().getParentDepartement() == null) {
            return false;
        }

        Long parentId = departement.get().getParentDepartement().getId();

        // Si le parent est le potentiel ancêtre, c'est un ancêtre
        if (parentId.equals(potentialAncestorId)) {
            return true;
        }

        // Sinon, vérifier récursivement avec le parent
        return isAncestor(potentialAncestorId, parentId);
    }
}

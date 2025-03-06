package sn.bmbacke.rh.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.bmbacke.rh.payload.dto.*;
import sn.bmbacke.rh.entity.Position;
import sn.bmbacke.rh.exception.BusinessException;
import sn.bmbacke.rh.exception.ResourceNotFoundException;
import sn.bmbacke.rh.payload.mapper.PositionMapper;
import sn.bmbacke.rh.repository.DepartmentRepository;
import sn.bmbacke.rh.repository.EmployeeRepository;
import sn.bmbacke.rh.repository.PositionRepository;
import sn.bmbacke.rh.service.PositionService;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;
    private final DepartmentRepository departementRepository;
    private final EmployeeRepository employeeRepository;
    private final PositionMapper positionMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<PositionDTO> getAllPositions(Pageable pageable) {
        return positionRepository.findAll(pageable)
                .map(positionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public PositionDTO getPositionById(Long id) {
        return positionRepository.findById(id)
                .map(positionMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Position non trouvée avec l'ID : " + id));
    }

    @Override
    public PositionDTO createPosition(PositionCreateDTO positionCreateDTO) {
        // Vérifier l'unicité du titre
        if (positionRepository.findByTitle(positionCreateDTO.getTitle()).isPresent()) {
            throw new BusinessException("Une position avec le titre '" + positionCreateDTO.getTitle() + "' existe déjà");
        }

        // Vérifier que le département existe
        if (positionCreateDTO.getDepartmentId() != null) {
            if (!departementRepository.existsById(positionCreateDTO.getDepartmentId())) {
                throw new ResourceNotFoundException("Département non trouvé avec l'ID : " + positionCreateDTO.getDepartmentId());
            }
        }

        // Valider la plage de salaire
        validateSalaryRange(positionCreateDTO.getMinSalary(), positionCreateDTO.getMaxSalary());

        // Conversion et sauvegarde
        Position position = positionMapper.toEntity(positionCreateDTO);

        // Valeurs par défaut
        if (position.getActive() == null) {
            position.setActive(true);
        }

        Position savedPosition = positionRepository.save(position);
        return positionMapper.toDto(savedPosition);
    }

    @Override
    public PositionDTO updatePosition(Long id, PositionUpdateDTO positionUpdateDTO) {
        // Vérifier que la position existe
        Position existingPosition = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Position non trouvée avec l'ID : " + id));

        // Vérifier l'unicité du titre (si modifié)
        if (positionUpdateDTO.getTitle() != null && !positionUpdateDTO.getTitle().equals(existingPosition.getTitle())) {
            if (positionRepository.existsByTitleAndIdNot(positionUpdateDTO.getTitle(), id)) {
                throw new BusinessException("Une position avec le titre '" + positionUpdateDTO.getTitle() + "' existe déjà");
            }
        }

        // Vérifier que le département existe (si modifié)
        if (positionUpdateDTO.getDepartmentId() != null) {
            if (!departementRepository.existsById(positionUpdateDTO.getDepartmentId())) {
                throw new ResourceNotFoundException("Département non trouvé avec l'ID : " + positionUpdateDTO.getDepartmentId());
            }
        }

        // Valider la plage de salaire
        BigDecimal minSalary = positionUpdateDTO.getMinSalary() != null ?
                positionUpdateDTO.getMinSalary() : existingPosition.getMinSalary();
        BigDecimal maxSalary = positionUpdateDTO.getMaxSalary() != null ?
                positionUpdateDTO.getMaxSalary() : existingPosition.getMaxSalary();
        validateSalaryRange(minSalary, maxSalary);

        // Mise à jour
        positionMapper.updateEntityFromDto(positionUpdateDTO, existingPosition);
        Position updatedPosition = positionRepository.save(existingPosition);
        return positionMapper.toDto(updatedPosition);
    }

    @Override
    public void deletePosition(Long id) {
        // Vérifier que la position existe
        if (!positionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Position non trouvée avec l'ID : " + id);
        }

        // Vérifier que la position n'est pas utilisée par des employés
        if (employeeRepository.existsByPosition_Id(id)) {
            throw new BusinessException("Impossible de supprimer cette position car elle est assignée à un ou plusieurs employés");
        }

        positionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean positionExists(Long id) {
        return positionRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionDTO> getPositionsByDepartment(Long departmentId) {
        // Vérifier que le département existe
        if (!departementRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Département non trouvé avec l'ID : " + departmentId);
        }

        return positionRepository.findByDepartment_Id(departmentId).stream()
                .map(positionMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionDTO> getActivePositions() {
        return positionRepository.findByActiveTrue().stream()
                .map(positionMapper::toDto)
                .toList();
    }

    @Override
    public PositionDTO updatePositionStatus(Long id, Boolean active) {
        // Vérifier que la position existe
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Position non trouvée avec l'ID : " + id));

        // Si on désactive une position, vérifier qu'elle n'est pas utilisée par des employés actifs
        if (active != null && !active && employeeRepository.existsByPosition_Id(id)) {
            throw new BusinessException("Impossible de désactiver cette position car elle est assignée à un ou plusieurs employés");
        }

        position.setActive(active);
        Position updatedPosition = positionRepository.save(position);
        return positionMapper.toDto(updatedPosition);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PositionDTO> searchPositions(String keyword, Boolean active, Long departmentId, Pageable pageable) {
        return positionRepository.searchPositions(keyword, active, departmentId, pageable)
                .map(positionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionDTO> getPositionsBySalaryRange(BigDecimal minSalary, BigDecimal maxSalary) {
        validateSalaryRange(minSalary, maxSalary);

        return positionRepository.findByMinSalaryGreaterThanEqualAndMaxSalaryLessThanEqual(minSalary, maxSalary)
                .stream()
                .map(positionMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean titleExists(String title, Long excludeId) {
        return positionRepository.existsByTitleAndIdNot(title, excludeId);
    }

    /**
     * Valide la plage de salaire
     */
    private void validateSalaryRange(BigDecimal minSalary, BigDecimal maxSalary) {
        if (minSalary == null || maxSalary == null) {
            return; // Validation ignorée si l'une des valeurs est nulle
        }

        if (minSalary.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Le salaire minimum ne peut pas être négatif");
        }

        if (maxSalary.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Le salaire maximum ne peut pas être négatif");
        }

        if (minSalary.compareTo(maxSalary) > 0) {
            throw new BusinessException("Le salaire minimum ne peut pas être supérieur au salaire maximum");
        }
    }
}
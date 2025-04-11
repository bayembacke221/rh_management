package sn.bmbacke.rh.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.bmbacke.rh.entity.LeavePolicy;
import sn.bmbacke.rh.entity.enums.LeaveType;
import sn.bmbacke.rh.exception.BusinessException;
import sn.bmbacke.rh.exception.ResourceNotFoundException;
import sn.bmbacke.rh.payload.dto.LeavePolicyCreateDTO;
import sn.bmbacke.rh.payload.dto.LeavePolicyDTO;
import sn.bmbacke.rh.payload.mapper.LeavePolicyMapper;
import sn.bmbacke.rh.repository.LeavePolicyRepository;
import sn.bmbacke.rh.service.LeavePolicyService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implémentation du service de gestion des politiques de congés
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LeavePolicyServiceImpl implements LeavePolicyService {

    private final LeavePolicyRepository leavePolicyRepository;
    private final LeavePolicyMapper leavePolicyMapper;

    @Override
    @Transactional(readOnly = true)
    public List<LeavePolicyDTO> getAllLeavePolicies() {
        return leavePolicyRepository.findAll().stream()
                .map(leavePolicyMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LeavePolicyDTO getLeavePolicyById(Long id) {
        return leavePolicyRepository.findById(id)
                .map(leavePolicyMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Politique de congés non trouvée avec l'ID : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public LeavePolicyDTO getLeavePolicyByTypeAndYear(LeaveType leaveType, Integer year) {
        LeavePolicy policy = leavePolicyRepository.findByLeaveTypeAndYear(leaveType, year);
        if (policy == null) {
            return null;
        }
        return leavePolicyMapper.toDto(policy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeavePolicyDTO> getLeavePoliciesByYear(Integer year) {
        return leavePolicyRepository.findByYear(year).stream()
                .map(leavePolicyMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LeavePolicyDTO createLeavePolicy(LeavePolicyCreateDTO leavePolicyCreateDTO) {
        // Vérifier si une politique existe déjà pour ce type et cette année
        LeavePolicy existingPolicy = leavePolicyRepository.findByLeaveTypeAndYear(
                leavePolicyCreateDTO.getLeaveType(), leavePolicyCreateDTO.getYear());

        if (existingPolicy != null) {
            throw new BusinessException("Une politique de congés existe déjà pour ce type et cette année");
        }

        // Conversion et sauvegarde
        LeavePolicy leavePolicy = leavePolicyMapper.toEntity(leavePolicyCreateDTO);
        LeavePolicy savedLeavePolicy = leavePolicyRepository.save(leavePolicy);

        return leavePolicyMapper.toDto(savedLeavePolicy);
    }

    @Override
    @Transactional
    public LeavePolicyDTO updateLeavePolicy(Long id, LeavePolicyCreateDTO leavePolicyUpdateDTO) {
        // Vérifier que la politique existe
        LeavePolicy existingPolicy = leavePolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Politique de congés non trouvée avec l'ID : " + id));

        // Vérifier l'unicité si le type ou l'année a changé
        if (!existingPolicy.getLeaveType().equals(leavePolicyUpdateDTO.getLeaveType()) ||
                !existingPolicy.getYear().equals(leavePolicyUpdateDTO.getYear())) {

            LeavePolicy duplicatePolicy = leavePolicyRepository.findByLeaveTypeAndYear(
                    leavePolicyUpdateDTO.getLeaveType(), leavePolicyUpdateDTO.getYear());

            if (duplicatePolicy != null && !duplicatePolicy.getId().equals(id)) {
                throw new BusinessException("Une politique de congés existe déjà pour ce type et cette année");
            }
        }

        // Mise à jour des champs
        existingPolicy.setLeaveType(leavePolicyUpdateDTO.getLeaveType());
        existingPolicy.setYear(leavePolicyUpdateDTO.getYear());
        existingPolicy.setDefaultDays(leavePolicyUpdateDTO.getDefaultDays());
        existingPolicy.setMaxConsecutiveDays(leavePolicyUpdateDTO.getMaxConsecutiveDays());
        existingPolicy.setMinRequestNoticeDays(leavePolicyUpdateDTO.getMinRequestNoticeDays());
        existingPolicy.setRequiresApproval(leavePolicyUpdateDTO.getRequiresApproval());
        existingPolicy.setRequiresDocumentation(leavePolicyUpdateDTO.getRequiresDocumentation());
        existingPolicy.setIsPaid(leavePolicyUpdateDTO.getIsPaid());
        existingPolicy.setCarryForwardAllowed(leavePolicyUpdateDTO.getCarryForwardAllowed());
        existingPolicy.setMaxCarryForwardDays(leavePolicyUpdateDTO.getMaxCarryForwardDays());
        existingPolicy.setDescription(leavePolicyUpdateDTO.getDescription());

        LeavePolicy updatedPolicy = leavePolicyRepository.save(existingPolicy);
        return leavePolicyMapper.toDto(updatedPolicy);
    }

    @Override
    @Transactional
    public void deleteLeavePolicy(Long id) {
        // Vérifier que la politique existe
        if (!leavePolicyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Politique de congés non trouvée avec l'ID : " + id);
        }

        leavePolicyRepository.deleteById(id);
    }

    @Override
    @Transactional
    public List<LeavePolicyDTO> duplicateLeavePolicies(Integer sourceYear, Integer targetYear) {
        // Récupérer les politiques de l'année source
        List<LeavePolicy> sourcePolicies = leavePolicyRepository.findByYear(sourceYear);

        if (sourcePolicies.isEmpty()) {
            throw new BusinessException("Aucune politique de congés trouvée pour l'année source : " + sourceYear);
        }

        List<LeavePolicy> newPolicies = new ArrayList<>();

        // Dupliquer chaque politique
        for (LeavePolicy sourcePolicy : sourcePolicies) {
            // Vérifier si une politique existe déjà pour ce type et l'année cible
            LeavePolicy existingPolicy = leavePolicyRepository.findByLeaveTypeAndYear(
                    sourcePolicy.getLeaveType(), targetYear);

            if (existingPolicy == null) {
                LeavePolicy newPolicy = new LeavePolicy();
                newPolicy.setLeaveType(sourcePolicy.getLeaveType());
                newPolicy.setYear(targetYear);
                newPolicy.setDefaultDays(sourcePolicy.getDefaultDays());
                newPolicy.setMaxConsecutiveDays(sourcePolicy.getMaxConsecutiveDays());
                newPolicy.setMinRequestNoticeDays(sourcePolicy.getMinRequestNoticeDays());
                newPolicy.setRequiresApproval(sourcePolicy.getRequiresApproval());
                newPolicy.setRequiresDocumentation(sourcePolicy.getRequiresDocumentation());
                newPolicy.setIsPaid(sourcePolicy.getIsPaid());
                newPolicy.setCarryForwardAllowed(sourcePolicy.getCarryForwardAllowed());
                newPolicy.setMaxCarryForwardDays(sourcePolicy.getMaxCarryForwardDays());
                newPolicy.setDescription(sourcePolicy.getDescription());

                newPolicies.add(leavePolicyRepository.save(newPolicy));
            }
        }

        return newPolicies.stream()
                .map(leavePolicyMapper::toDto)
                .collect(Collectors.toList());
    }
}


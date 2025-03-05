package sn.bmbacke.rh.repository;

import sn.bmbacke.rh.entity.Contract;

import java.util.List;

public interface ContractRepository  extends GenericRepository<Contract, Long> {
    List<Contract> findByEmployee_Id(Long employeeId);
}

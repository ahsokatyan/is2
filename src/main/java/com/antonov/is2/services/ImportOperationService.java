package com.antonov.is2.services;

import com.antonov.is2.entities.ImportOperation;
import com.antonov.is2.entities.ImportStatus;
import com.antonov.is2.repos.ImportOperationRepository;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@Stateless
public class ImportOperationService {

    @Inject
    private ImportOperationRepository importOperationRepo;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public ImportOperation startOperation(String username) {
        ImportOperation operation = new ImportOperation();
        operation.setUsername(username);
        operation.setStatus(ImportStatus.IN_PROGRESS);
        return importOperationRepo.save(operation);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void markSuccess(Long id, int addedCount) {
        Optional<ImportOperation> operationOpt = importOperationRepo.findById(id);
        if (operationOpt.isPresent()) {
            ImportOperation operation = operationOpt.get();
            operation.setStatus(ImportStatus.SUCCESS);
            operation.setAddedCount(addedCount);
            operation.setErrorMessage(null);
            importOperationRepo.update(operation);
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void markFailure(Long id, String errorMessage) {
        Optional<ImportOperation> operationOpt = importOperationRepo.findById(id);
        if (operationOpt.isPresent()) {
            ImportOperation operation = operationOpt.get();
            operation.setStatus(ImportStatus.FAILED);
            operation.setAddedCount(null);
            operation.setErrorMessage(errorMessage);
            importOperationRepo.update(operation);
        }
    }

    public List<ImportOperation> getAllOperations() {
        return importOperationRepo.findAllOrdered();
    }

    public List<ImportOperation> getOperationsByUser(String username) {
        return importOperationRepo.findByUsernameOrdered(username);
    }
}

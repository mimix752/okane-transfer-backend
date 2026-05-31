package com.okanetransfer.repository;

import com.okanetransfer.entity.MobileMoneyTransfer;
import com.okanetransfer.enums.MobileMoneyOperator;
import com.okanetransfer.enums.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MobileMoneyTransferRepository extends JpaRepository<MobileMoneyTransfer, Long> {
    List<MobileMoneyTransfer> findByOperator(MobileMoneyOperator operator);
    List<MobileMoneyTransfer> findByReconciliationStatus(TransferStatus status);
    Optional<MobileMoneyTransfer> findByTransferId(Long transferId);
    List<MobileMoneyTransfer> findByTransfer_Sender_Id(Long agentId);
}

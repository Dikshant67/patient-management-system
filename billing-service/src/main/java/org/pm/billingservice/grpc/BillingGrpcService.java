package org.pm.billingservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class BillingGrpcService extends BillingServiceGrpc.BillingServiceImplBase {
  private static final Logger log = LoggerFactory.getLogger(BillingGrpcService.class);

  @Override
  public void createBillingAccount(BillingRequest billingRequest,
                                   StreamObserver<BillingResponse> responseStreamObserver){
    log.info("gRPC createBillingAccount request received for Patient ID: {}, Name: {}, Email: {}",
            billingRequest.getPatientId(), billingRequest.getName(), billingRequest.getEmail());
    
    // Business logic e.g. save to DB, perform calculations etc.
    BillingResponse response = BillingResponse.newBuilder()
            .setAccountId("ACC-" + billingRequest.getPatientId())
            .setStatus("ACTIVE")
            .build();
            
    log.info("Sending gRPC BillingResponse: Account ID {}, Status {}", response.getAccountId(), response.getStatus());
    responseStreamObserver.onNext(response);
    responseStreamObserver.onCompleted();
    log.info("Successfully completed gRPC createBillingAccount call for Patient ID: {}", billingRequest.getPatientId());
  }
}

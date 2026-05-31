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
    log.info("createBillingAccount request received for {}", billingRequest.toString());
    //Bussiness logic eg. save to db , perform calculates etc
    BillingResponse response= BillingResponse.newBuilder()
            .setAccountId("fsdfds")
            .setStatus("ACTIVE")
            .build();
    responseStreamObserver.onNext(response);
    responseStreamObserver.onCompleted();
  }
}

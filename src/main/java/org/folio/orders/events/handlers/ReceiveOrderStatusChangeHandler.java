package org.folio.orders.events.handlers;

import org.folio.rest.jaxrs.model.PurchaseOrder;
import org.folio.service.finance.transaction.EncumbranceService;
import org.folio.service.orders.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@Component("receiveOrderStatusChangeHandler")
public class ReceiveOrderStatusChangeHandler extends AbstractOrderStatusHandler {

  @Autowired
  public ReceiveOrderStatusChangeHandler(Vertx vertx, EncumbranceService encumbranceService, PurchaseOrderService purchaseOrderService) {
    super(vertx.getOrCreateContext(), encumbranceService, purchaseOrderService);
  }

  @Override
  protected boolean isOrdersStatusChangeSkip(PurchaseOrder purchaseOrder, JsonObject ordersPayload){
    return  (purchaseOrder.getWorkflowStatus() == PurchaseOrder.WorkflowStatus.PENDING);
  }
}

package org.folio.service.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.folio.models.CompositeOrderRetrieveHolder;
import org.folio.rest.acq.model.finance.Encumbrance;
import org.folio.rest.acq.model.finance.FiscalYear;
import org.folio.rest.acq.model.finance.Transaction;
import org.folio.rest.acq.model.finance.TransactionCollection;
import org.folio.rest.core.models.RequestContext;
import org.folio.rest.jaxrs.model.CompositePurchaseOrder;
import org.folio.service.finance.transaction.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TransactionsTotalFieldsPopulateServiceTest {

  @InjectMocks
  private TransactionsTotalFieldsPopulateService populateService;
  @Mock
  private TransactionService transactionService;

  @Mock
  private RequestContext requestContext;

  @BeforeEach
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void shouldPopulateTotalEncumberedFieldWithSumOfEncumbrancesAmount() {
    Transaction transaction1 = new Transaction().withId(UUID.randomUUID().toString());
    Transaction transaction2 = new Transaction().withId(UUID.randomUUID().toString());
    CompositePurchaseOrder order = new CompositePurchaseOrder().withId(UUID.randomUUID().toString());
    CompositeOrderRetrieveHolder holder = new CompositeOrderRetrieveHolder(order)
            .withFiscalYear(new FiscalYear().withId(UUID.randomUUID().toString()));

    Transaction paidEncumbrance = new Transaction()
            .withTransactionType(Transaction.TransactionType.ENCUMBRANCE)
            .withPaymentEncumbranceId(transaction1.getId())
            .withAmount(14.11d)
            .withCurrency("USD")
            .withEncumbrance(new Encumbrance().withAmountExpended(13.45));
    Transaction notPaidEncumbrance = new Transaction()
            .withTransactionType(Transaction.TransactionType.ENCUMBRANCE)
            .withPaymentEncumbranceId(transaction2.getId())
            .withAmount(13.43d)
            .withCurrency("USD")
            .withEncumbrance(new Encumbrance().withAmountExpended(0d));

    List<Transaction> transactions = List.of(paidEncumbrance, notPaidEncumbrance);
    TransactionCollection transactionCollection = new TransactionCollection().withTransactions(transactions);

    when(transactionService.getTransactions(anyString(), anyInt(), anyInt(), any())).thenReturn(CompletableFuture.completedFuture(transactionCollection));

    CompositeOrderRetrieveHolder resultHolder = populateService.populate(holder, requestContext)
      .join();

    assertEquals(27.54, resultHolder.getOrder().getTotalEncumbered());
    assertEquals(13.45, resultHolder.getOrder().getTotalExpended());
  }

  @Test
  void shouldPopulateTotalEncumberedFieldWithZeroWhenFiscalYearIsEmpty() {

    CompositePurchaseOrder order = new CompositePurchaseOrder().withId(UUID.randomUUID().toString());
    CompositeOrderRetrieveHolder holder = new CompositeOrderRetrieveHolder(order);

    CompositeOrderRetrieveHolder resultHolder = populateService.populate(holder, requestContext).join();

    assertEquals(0d, resultHolder.getOrder()
            .getTotalExpended());
    assertEquals(0d, resultHolder.getOrder()
            .getTotalEncumbered());
  }

  @Test
  void shouldPopulateTotalEncumberedAndTotalExpendedFieldsWithZeroWhenTransactionsNotFound() {
    CompositePurchaseOrder order = new CompositePurchaseOrder().withId(UUID.randomUUID().toString());
    CompositeOrderRetrieveHolder holder = new CompositeOrderRetrieveHolder(order)
            .withFiscalYear(new FiscalYear().withId(UUID.randomUUID().toString()));

    when(transactionService.getTransactions(anyString(), anyInt(), anyInt(), any())).thenReturn(CompletableFuture.completedFuture(new TransactionCollection()));

    CompositeOrderRetrieveHolder resultHolder = populateService.populate(holder, requestContext).join();

    assertEquals(0d, resultHolder.getOrder().getTotalExpended());
  }
}

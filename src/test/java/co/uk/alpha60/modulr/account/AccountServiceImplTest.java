package co.uk.alpha60.modulr.account;

import co.uk.alpha60.modulr.exceptions.InvalidAmountException;
import co.uk.alpha60.modulr.exceptions.NoSuchAccountException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static co.uk.alpha60.modulr.account.AccountNumbers.ACCOUNT_NUMBER_1;
import static co.uk.alpha60.modulr.account.AccountNumbers.ACCOUNT_NUMBER_2;
import static co.uk.alpha60.modulr.account.AccountNumbers.ACCOUNT_NUMBER_3;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Account Service Tests")
class AccountServiceImplTest {

   private static final BigDecimal BALANCE_1 = new BigDecimal(2738.57).setScale(2, BigDecimal.ROUND_FLOOR);
   private static final BigDecimal BALANCE_2 = new BigDecimal(23).setScale(2, BigDecimal.ROUND_FLOOR);

   private AccountService accountService;

   @BeforeEach
   void setup() {
       accountService = new AccountServiceImpl();
   }

   @Test
   @DisplayName("Check balance amount for account")
   void checkBalance() throws NoSuchAccountException {
      assertEquals(accountService.checkBalance(ACCOUNT_NUMBER_2), BALANCE_2);
   }

   @Test
   @DisplayName("Check balance amount for non-existent account")
   void checkBalanceInvalidAccount() {
      Throwable exception = assertThrows(NoSuchAccountException.class, () ->
            accountService.checkBalance(""));
      assertEquals("The account number  does not exist", exception.getMessage());
   }

   @Test
   @DisplayName("Successful withdrawal from account")
   void withdrawAmount() throws NoSuchAccountException, InvalidAmountException {
      assertEquals(accountService.checkBalance(ACCOUNT_NUMBER_1).setScale(2, BigDecimal.ROUND_FLOOR),
            BALANCE_1);
      accountService.withdrawAmount(ACCOUNT_NUMBER_1, BALANCE_1);
      assertEquals(0, accountService.checkBalance(ACCOUNT_NUMBER_1).compareTo(BigDecimal.ZERO));
   }

   @Test
   @DisplayName("Attempt to withdraw amount from a non-existent account")
   void withdrawAmountNoSuchAccount() {
      Throwable exception = assertThrows(NoSuchAccountException.class, () ->
            accountService.withdrawAmount("", BigDecimal.valueOf(1)));
      assertEquals("The account number  does not exist", exception.getMessage());
   }

   @Test
   @DisplayName("Attempt to withdraw an amount that exceeds the account balance after initial successful withdrawal")
   void withdrawMultipleAmounts() throws InvalidAmountException, NoSuchAccountException {
      accountService.withdrawAmount(ACCOUNT_NUMBER_2, BALANCE_2);

      Throwable exception = assertThrows(InvalidAmountException.class, () ->
            accountService.withdrawAmount(ACCOUNT_NUMBER_2, BigDecimal.valueOf(10)));
      assertEquals("Insufficient funds", exception.getMessage());
   }

   @Test
   @DisplayName("Attempt to withdraw an amount that exceeds the account balance")
   void withdrawInvalidAmount() {
      Throwable exception = assertThrows(InvalidAmountException.class, () ->
            accountService.withdrawAmount(ACCOUNT_NUMBER_3, BigDecimal.valueOf(1)));
      assertEquals("Insufficient funds", exception.getMessage());
   }
}
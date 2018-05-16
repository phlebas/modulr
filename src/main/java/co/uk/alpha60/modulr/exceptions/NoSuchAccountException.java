package co.uk.alpha60.modulr.exceptions;

public class NoSuchAccountException extends ATMException {
   public NoSuchAccountException(String accountNumber) {
      super("The account number " + accountNumber + " does not exist");
   }
}

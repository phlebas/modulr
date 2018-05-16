package co.uk.alpha60.modulr.exceptions;

import co.uk.alpha60.modulr.atm.ATMService;

public class InvalidAmountException extends ATMException {
   public InvalidAmountException(String message) {
      super(message);
   }
}

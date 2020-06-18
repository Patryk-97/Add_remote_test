package edu.iis.mto.testreactor.atm;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.iis.mto.testreactor.atm.bank.AuthorizationException;
import edu.iis.mto.testreactor.atm.bank.AuthorizationToken;
import edu.iis.mto.testreactor.atm.bank.Bank;

@ExtendWith(MockitoExtension.class)
public class ATMachineTest {

    @Mock
    private Bank bank;

    private ATMachine atMachine;

    private Currency currency;

    private PinCode pinCode;

    private Card card;

    private Money amount;

    @BeforeEach
    public void setUp() {
        currency = Money.DEFAULT_CURRENCY;
        atMachine = new ATMachine(bank, currency);
    }

    @Test
    public void withdrawShouldReturnProperBanknotes() throws ATMOperationException, AuthorizationException {
        pinCode = PinCode.createPIN(1, 2, 3, 4);
        card = Card.create("card1");
        amount = new Money(70, Money.DEFAULT_CURRENCY);

        List<BanknotesPack> banknotesPack = new ArrayList<>();
        banknotesPack.add(BanknotesPack.create(3, Banknote.PL_50));
        banknotesPack.add(BanknotesPack.create(2, Banknote.PL_20));
        banknotesPack.add(BanknotesPack.create(4, Banknote.PL_10));
        MoneyDeposit deposit = MoneyDeposit.create(currency, banknotesPack);
        atMachine.setDeposit(deposit);

        AuthorizationToken token = AuthorizationToken.create("token");
        Mockito.when(bank.autorize(pinCode.getPIN(), card.getNumber()))
               .thenReturn(token);

        Withdrawal withdrawal = atMachine.withdraw(pinCode, card, amount);
        List<Banknote> expected = Arrays.asList(Banknote.PL_50, Banknote.PL_20);
        List<Banknote> actual = withdrawal.getBanknotes();
        assertThat(actual, is(expected));
    }

    @Test
    public void withdrawShouldThrowATMOperationExceptionWhenAuthorizeFailed() throws ATMOperationException, AuthorizationException {
        pinCode = PinCode.createPIN(1, 2, 3, 4);
        card = Card.create("card1");
        amount = new Money(70, Money.DEFAULT_CURRENCY);

        Mockito.doThrow(AuthorizationException.class)
               .when(bank)
               .autorize(pinCode.getPIN(), card.getNumber());

        assertThrows(ATMOperationException.class, () -> atMachine.withdraw(pinCode, card, amount));
    }

}

package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.entity.Account;
import com.hashtag.ngo.example.bank.entity.AccountType;
import com.hashtag.ngo.example.bank.entity.CheckingAccount;
import com.hashtag.ngo.example.bank.entity.SavingsAccount;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Conversion entite <-> DTO pour les comptes.
 * <p>
 * MapStruct ne sait pas nativement repartir sur les sous-types d'une
 * hierarchie scellee : on fournit donc soi-meme, en methode {@code default},
 * le mapping Account -> AccountResponse via un pattern matching for switch
 * (JEP 441) exhaustif sur CheckingAccount/SavingsAccount. MapStruct reutilise
 * ensuite cette methode pour generer {@link #toResponseList}.
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {

    default AccountResponse toResponse(Account account) {
        if (account == null) {
            return null;
        }
        return switch (account) {
            case CheckingAccount checking -> new AccountResponse(
                    checking.getId(),
                    AccountType.COURANT,
                    checking.getOwner(),
                    checking.getBalance(),
                    checking.getCreatedAt(),
                    checking.getOverdraftLimit(),
                    null);
            case SavingsAccount savings -> new AccountResponse(
                    savings.getId(),
                    AccountType.EPARGNE,
                    savings.getOwner(),
                    savings.getBalance(),
                    savings.getCreatedAt(),
                    null,
                    savings.getInterestRate());
        };
    }

    List<AccountResponse> toResponseList(List<Account> accounts);
}

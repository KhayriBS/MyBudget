package tn.esprit.mybudget.data.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

public class TransactionWithAccount {
    @Embedded
    public Transaction transaction;

    @Relation(parentColumn = "accountId", entityColumn = "id")
    public Account account;
}

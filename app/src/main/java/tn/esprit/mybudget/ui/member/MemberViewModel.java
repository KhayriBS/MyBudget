package tn.esprit.mybudget.ui.member;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tn.esprit.mybudget.data.AppDatabase;
import tn.esprit.mybudget.data.dao.AccountDao;
import tn.esprit.mybudget.data.dao.CategoryDao;
import tn.esprit.mybudget.data.dao.MemberDao;
import tn.esprit.mybudget.data.dao.TransactionDao;
import tn.esprit.mybudget.data.entity.Account;
import tn.esprit.mybudget.data.entity.Category;
import tn.esprit.mybudget.data.entity.Member;
import tn.esprit.mybudget.data.entity.Transaction;

public class MemberViewModel extends AndroidViewModel {

    private MemberDao memberDao;
    private AccountDao accountDao;
    private TransactionDao transactionDao;
    private CategoryDao categoryDao;
    private LiveData<List<Member>> allMembers;
    private LiveData<Double> totalLent;
    private LiveData<Double> totalBorrowed;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public MemberViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        memberDao = db.memberDao();
        accountDao = db.accountDao();
        transactionDao = db.transactionDao();
        categoryDao = db.categoryDao();
        allMembers = memberDao.getAllMembers();
        totalLent = memberDao.getTotalLent();
        totalBorrowed = memberDao.getTotalBorrowed();
    }

    public LiveData<List<Member>> getAllMembers() {
        return allMembers;
    }

    public LiveData<Double> getTotalLent() {
        return totalLent;
    }

    public LiveData<Double> getTotalBorrowed() {
        return totalBorrowed;
    }

    public void insert(Member member, int userId) {
        executorService.execute(() -> {
            // Insert member record
            memberDao.insert(member);

            // Create a transaction for the account
            if (member.accountId != null && member.accountId > 0) {
                Account account = accountDao.getAccountById(member.accountId);
                if (account != null) {
                    String categoryName;
                    String note;
                    int categoryId = 0;

                    if ("Lent".equals(member.type)) {
                        // Lent money = expense (money goes out)
                        categoryName = "Lending";
                        note = "Lent to " + member.name;
                        account.balance -= member.amount;
                    } else {
                        // Borrowed money = income (money comes in)
                        categoryName = "Borrowing";
                        note = "Borrowed from " + member.name;
                        account.balance += member.amount;
                    }

                    // Get category ID
                    Category category = categoryDao.getCategoryByName(categoryName);
                    if (category != null) {
                        categoryId = category.id;
                    }

                    // Update account balance
                    accountDao.update(account);

                    // Create transaction record
                    String fullNote = note + (member.note != null && !member.note.isEmpty() ? " - " + member.note : "");
                    Transaction transaction = new Transaction(userId, categoryId, member.accountId, member.amount,
                            member.date, fullNote);
                    transactionDao.insert(transaction);
                }
            }
        });
    }

    public void update(Member member, Member oldMember, int userId) {
        executorService.execute(() -> {
            // First, reverse the old transaction effect on account
            if (oldMember.accountId != null && oldMember.accountId > 0) {
                Account oldAccount = accountDao.getAccountById(oldMember.accountId);
                if (oldAccount != null) {
                    if ("Lent".equals(oldMember.type)) {
                        oldAccount.balance += oldMember.amount; // Restore lent amount
                    } else {
                        oldAccount.balance -= oldMember.amount; // Remove borrowed amount
                    }
                    accountDao.update(oldAccount);
                }
            }

            // Apply new transaction effect
            if (member.accountId != null && member.accountId > 0) {
                Account newAccount = accountDao.getAccountById(member.accountId);
                if (newAccount != null) {
                    if ("Lent".equals(member.type)) {
                        newAccount.balance -= member.amount;
                    } else {
                        newAccount.balance += member.amount;
                    }
                    accountDao.update(newAccount);
                }
            }

            // Update member record
            memberDao.update(member);
        });
    }

    public void settleDebt(Member member, int userId) {
        executorService.execute(() -> {
            if (member.accountId != null && member.accountId > 0) {
                Account account = accountDao.getAccountById(member.accountId);
                if (account != null) {
                    String categoryName;
                    String note;
                    int categoryId = 0;

                    if ("Lent".equals(member.type)) {
                        // Getting money back = income
                        categoryName = "Repayment Received";
                        note = "Received repayment from " + member.name;
                        account.balance += member.amount;
                    } else {
                        // Paying back = expense
                        categoryName = "Debt Repaid";
                        note = "Repaid to " + member.name;
                        account.balance -= member.amount;
                    }

                    // Get category ID
                    Category category = categoryDao.getCategoryByName(categoryName);
                    if (category != null) {
                        categoryId = category.id;
                    }

                    accountDao.update(account);

                    // Create settlement transaction
                    Transaction transaction = new Transaction(userId, categoryId, member.accountId, member.amount,
                            System.currentTimeMillis(), note);
                    transactionDao.insert(transaction);
                }
            }

            // Mark as settled
            member.isSettled = true;
            memberDao.update(member);
        });
    }

    public void delete(Member member) {
        executorService.execute(() -> {
            // If not settled, reverse the transaction effect
            if (!member.isSettled && member.accountId != null && member.accountId > 0) {
                Account account = accountDao.getAccountById(member.accountId);
                if (account != null) {
                    if ("Lent".equals(member.type)) {
                        account.balance += member.amount; // Restore lent amount
                    } else {
                        account.balance -= member.amount; // Remove borrowed amount
                    }
                    accountDao.update(account);
                }
            }
            memberDao.delete(member);
        });
    }
}

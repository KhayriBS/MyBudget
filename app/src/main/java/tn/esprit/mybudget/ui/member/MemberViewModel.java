package tn.esprit.mybudget.ui.member;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tn.esprit.mybudget.data.AppDatabase;
import tn.esprit.mybudget.data.dao.MemberDao;
import tn.esprit.mybudget.data.entity.Member;

public class MemberViewModel extends AndroidViewModel {

    private MemberDao memberDao;
    private LiveData<List<Member>> allMembers;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public MemberViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        memberDao = db.memberDao();
        allMembers = memberDao.getAllMembers();
    }

    public LiveData<List<Member>> getAllMembers() {
        return allMembers;
    }

    public void insert(Member member) {
        executorService.execute(() -> memberDao.insert(member));
    }

    public void update(Member member) {
        executorService.execute(() -> memberDao.update(member));
    }

    public void delete(Member member) {
        executorService.execute(() -> memberDao.delete(member));
    }
}

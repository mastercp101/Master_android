package nom.cp101.master.master.Account.AccountMain;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import nom.cp101.master.master.Master.MasterActivity;
import nom.cp101.master.master.R;

import static nom.cp101.master.master.Master.MasterActivity.LOGIN_BACK;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login_act);
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLogSign, new LoginFragment()).commit();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameLogSign);
        if (fragment instanceof LoginFragment) {
            //當登入頁面返回主頁,判斷當前bottomNavigation-select?
            finish();
            Intent intent = new Intent(this, MasterActivity.class);
            intent.putExtra(LOGIN_BACK, LOGIN_BACK);
            startActivity(intent);

        } else {
        }
        super.onBackPressed();

    }

}

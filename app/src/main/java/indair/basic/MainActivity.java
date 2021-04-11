package indair.basic;

import androidx.appcompat.app.AppCompatActivity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements MainCallBack
{
    private Network net;
    private LoginActivity loginScreen;
    private AppActivity   appScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        net = new Network();
        loginScreen = new LoginActivity();
        loginScreen.init(net, this);

        appScreen = new AppActivity();
        appScreen.init(net, this);

        changeActivity(0);
    }

    @Override
    public void changeActivity(int act)
    {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();

        switch (act) {
            case 0:
                fragmentTransaction.replace(R.id.mainLayout, loginScreen);
                fragmentTransaction.commit();
                loginScreen.setNetState(LoginActivity.State.nothing);
                net.setCallBack(loginScreen);
                break;
            case 1:
                fragmentTransaction.replace(R.id.mainLayout, appScreen);
                fragmentTransaction.commit();
                net.setCallBack(appScreen);
                appScreen.startListenig();
                break;
        }
    }
}

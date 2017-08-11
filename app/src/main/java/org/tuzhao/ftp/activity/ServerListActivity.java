package org.tuzhao.ftp.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.tuzhao.ftp.R;
import org.tuzhao.ftp.db.RsDBHelper;
import org.tuzhao.ftp.entity.ServerEntity;
import org.tuzhao.ftp.fragment.ServerAddFragment;

public class ServerListActivity extends BaseActivity implements ServerAddFragment.OnCompleteListener {

    private ServerAddFragment addFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_server, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.server_add) {
            if (null == addFragment) {
                addFragment = ServerAddFragment.newInstance();
                addFragment.setOnCompleteListener(this);
            }
            if (!addFragment.isShowing()) {
                addFragment.show(getFragmentManager(), "ServerAddFragment");
            }
        }
        return true;
    }

    @Override
    public void onComplete(ServerEntity server) {
        log(server.toString());
        long result = new RsDBHelper(this).addServer(server);
        log("add server: " + result);
    }
}

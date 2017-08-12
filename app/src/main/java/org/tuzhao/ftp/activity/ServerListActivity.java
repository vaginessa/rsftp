package org.tuzhao.ftp.activity;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.tuzhao.ftp.R;
import org.tuzhao.ftp.db.RsDBHelper;
import org.tuzhao.ftp.entity.ServerEntity;
import org.tuzhao.ftp.fragment.ServerAddFragment;
import org.tuzhao.ftp.util.ServerListRecyclerAdapter;

import java.util.ArrayList;

public class ServerListActivity extends BaseActivity implements ServerAddFragment.OnCompleteListener,
                                                                    ServerListRecyclerAdapter.OnItemClickListener,
                                                                    ServerListRecyclerAdapter.OnItemLongClickListener {

    private ArrayList<ServerEntity> list = new ArrayList<>();
    private ServerAddFragment addFragment;
    private RecyclerView mListRv;
    private ServerListRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_list);
        mListRv = (RecyclerView) findViewById(R.id.server_list_rv);

        list.addAll(new RsDBHelper(this).getServerList());
        log(list.toString());
        adapter = new ServerListRecyclerAdapter(this, list);
        adapter.setOnItemClickListener(this);
        adapter.setOnItemLongClickListener(this);
        mListRv.setLayoutManager(new GridLayoutManager(this, 3));
        mListRv.setAdapter(adapter);
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
            showServerInfoDialog(null, ServerAddFragment.START_TYPE_ADD);
        }
        return true;
    }

    private void showServerInfoDialog(ServerEntity entity, int TYPE) {
        ServerAddFragment.show(getActivity(), entity, TYPE, this);
    }

    @Override
    public void onComplete(ServerEntity server, int TYPE) {
        RsDBHelper helper = new RsDBHelper(this);
        log(server.toString());
        long result = helper.addServer(server);
        log("add server: " + result);
        if (result != -1) {
            list.add(server);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(View v, Object data, int position) {
        log("item click position: " + position + " data: " + data);
        ServerItemActivity.start(getActivity(), (ServerEntity) data);
    }

    @Override
    public boolean onItemLongClick(View v, Object data, int position) {
        log("item long click position: " + position + " data: " + data);
        return true;
    }
}

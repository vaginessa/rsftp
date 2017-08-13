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
import org.tuzhao.ftp.fragment.ServerControlFragment;
import org.tuzhao.ftp.util.ServerListRecyclerAdapter;

import java.util.ArrayList;

public class ServerListActivity extends BaseActivity implements ServerAddFragment.OnCompleteListener,
                                                                    ServerListRecyclerAdapter.OnItemClickListener,
                                                                    ServerListRecyclerAdapter.OnItemLongClickListener,
                                                                    ServerControlFragment.OnMenuClickListener {

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
        log(server.toString());
        RsDBHelper helper = new RsDBHelper(this);
        if (TYPE == ServerAddFragment.START_TYPE_ADD) {
            long result = helper.addServer(server);
            log("add server: " + result);
            if (result != -1) {
                list.add(server);
            }
        } else if (TYPE == ServerAddFragment.START_TYPE_EDIT) {
            int update = helper.updateServer(server);
            log("update server: " + update);
            if (update != -1) {
                updateList(server);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(View v, Object data, int position) {
        log("item click position: " + position + " data: " + data);
        ServerItemActivity.start(getActivity(), (ServerEntity) data);
    }

    @Override
    public boolean onItemLongClick(View v, Object data, int position) {
        log("item long click position: " + position + " data: " + data);
        ServerControlFragment.show(getActivity(), (ServerEntity) data, this);
        return true;
    }

    @Override
    public void onMenu(ServerEntity server, int position) {
        switch (position) {
            case 0:
                showServerInfoDialog(server, ServerAddFragment.START_TYPE_EDIT);
                break;
            case 1:
                int result = new RsDBHelper(this).deleteServer(server);
                log("delete result: " + result);
                int index = getSelectIndex(server);
                if (result >= 1 && index != -1) {
                    list.remove(index);
                    adapter.notifyDataSetChanged();
                    showMsg("delete server successful");
                }
                break;
        }
    }

    private int getSelectIndex(ServerEntity server) {
        int id = server.getId();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    private void updateList(ServerEntity server) {
        int selectIndex = getSelectIndex(server);
        if (selectIndex != -1) {
            ServerEntity entity = list.get(selectIndex);
            entity.update(server);
        }
    }
}

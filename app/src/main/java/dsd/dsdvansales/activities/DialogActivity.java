package dsd.dsdvansales.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPingManager;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import dsd.dsdvansales.R;
import dsd.dsdvansales.chat.ChatHelper;
import dsd.dsdvansales.chat.DialogsManager;
import dsd.dsdvansales.chat.QbDialogHolder;
import dsd.dsdvansales.fragment.ChatsFragment;
import dsd.dsdvansales.fragment.ContactsFragment;
import dsd.dsdvansales.fragment.GroupChatsFragment;
import dsd.dsdvansales.global.Utils;


public class DialogActivity extends AppCompatActivity implements DialogsManager.ManagingDialogsCallbacks{

    SmartTabLayout tabHost;
    public ViewPager pager;
    ViewPagerAdapter adapter;

    public ChatsFragment mChatFragment;
    public GroupChatsFragment mGroupChatFragment;
    public ContactsFragment mContactFragment;
    ProgressDialog progressDialog = null;

    private FloatingActionButton fab;

    public Map<Integer, Boolean> mOnlineStatus = new HashMap<>();
    public boolean isRunning = true;

    public void refreshOnlineStatus()
    {
        final QBPingManager pingManager = QBChatService.getInstance().getPingManager();
        List<Integer> keySet = new ArrayList<>();
        keySet.addAll(mOnlineStatus.keySet());
        for (Integer key: keySet)
        {
            try {
                final CountDownLatch latch = new CountDownLatch(1);
                pingManager.pingUser(key, new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        int a = 10;
                        latch.countDown();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        int b = 10;
                        latch.countDown();
                    }
                });

                latch.await();
                //boolean pingUser = pingManager.pingUser(key);
                //mOnlineStatus.put(key, pingUser);
            } catch (Exception e) {

            }
        }
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try
                {
                    mChatFragment.refreshData();
                    mContactFragment.refreshData();
                }catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*new Thread(){
            @Override
            public void run() {
                while(isRunning)
                {
                    if(mOnlineStatus.size() == 0)
                    {
                        try
                        {
                            Thread.sleep(1000);
                        }catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    refreshOnlineStatus();
                }
            }
        }.start();*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
    }

    @Override
    public void onDialogCreated(QBChatDialog chatDialog) {
        mChatFragment.updateDialogsAdapter();
        mGroupChatFragment.updateDialogsAdapter();
    }

    @Override
    public void onDialogUpdated(String chatDialog) {
        mChatFragment.updateDialogsAdapter();
        mGroupChatFragment.updateDialogsAdapter();
    }

    @Override
    public void onNewDialogLoaded(QBChatDialog chatDialog) {
        mChatFragment.updateDialogsAdapter();
        mGroupChatFragment.updateDialogsAdapter();
    }



    public void showProgressDialog(String strMessage)
    {
        try {
            if( progressDialog == null )
            {
                progressDialog = new ProgressDialog(this);
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
            progressDialog.setMessage(strMessage);
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void hideProgressDialog()
    {
        try {
            if( progressDialog != null )
            {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_dialog);


        tabHost = (SmartTabLayout) this.findViewById(R.id.viewpagertab);
        pager = (ViewPager) this.findViewById(R.id.pager );

        // init view pager
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onPageSelected(int position) {
                if( position == 1 )
                {
                    fab.setVisibility(View.GONE);
                }
                else
                {
                    fab.setVisibility(View.GONE);
                }

            }
        });
        tabHost.setViewPager(pager);

        initFragment();
        fab = (FloatingActionButton)findViewById(R.id.fab_dialogs_new_chat);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartNewChatClick();
            }
        });

        registerQbChatListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.dialogsManager.removeManagingDialogsCallbackListener(this);
    }

    public void registerQbChatListeners() {
        Utils.dialogsManager.addManagingDialogsCallbackListener(this);
    }



    public void onStartNewChatClick() {

        SelectUsersActivity.startForResult(this, ChatsFragment.REQUEST_SELECT_PEOPLE);
    }

    private boolean isPrivateDialogExist(ArrayList<QBUser> allSelectedUsers) {
        ArrayList<QBUser> selectedUsers = new ArrayList<>();
        selectedUsers.addAll(allSelectedUsers);
        selectedUsers.remove(ChatHelper.getCurrentUser());
        return selectedUsers.size() == 1 && QbDialogHolder.getInstance().hasPrivateDialogWithUser(selectedUsers.get(0));
    }

    private void createDialog(final ArrayList<QBUser> selectedUsers) {
        ChatHelper.getInstance().createDialogWithSelectedUsers(selectedUsers,
                new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog dialog, Bundle args) {
                        if(dialog.getType() == QBDialogType.PRIVATE)
                        {
                            pager.setCurrentItem(0);
                            Utils.dialogsManager.sendSystemMessageAboutCreatingDialog(QBChatService.getInstance().getSystemMessagesManager(), dialog);
                            ChatActivity.startForResult(DialogActivity.this, mChatFragment, ChatsFragment.REQUEST_DIALOG_ID_FOR_UPDATE, dialog);
                        }
                        else
                        {
                            pager.setCurrentItem(1);
                            Utils.dialogsManager.sendSystemMessageAboutCreatingDialog(QBChatService.getInstance().getSystemMessagesManager(), dialog);
                            ChatActivity.startForResult(DialogActivity.this, mGroupChatFragment, ChatsFragment.REQUEST_DIALOG_ID_FOR_UPDATE, dialog);
                        }

                        hideProgressDialog();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        hideProgressDialog();
                    }
                }
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
        {
            if (requestCode == ChatsFragment.REQUEST_SELECT_PEOPLE){
                ArrayList<QBUser> selectedUsers = (ArrayList<QBUser>) data
                        .getSerializableExtra(SelectUsersActivity.EXTRA_QB_USERS);

                if (isPrivateDialogExist(selectedUsers)) {
                    selectedUsers.remove(ChatHelper.getCurrentUser());
                    QBChatDialog existingPrivateDialog = QbDialogHolder.getInstance().getPrivateDialogWithUser(selectedUsers.get(0));
                    if(existingPrivateDialog.getType() == QBDialogType.PRIVATE)
                    {
                        pager.setCurrentItem(0);
                        ChatActivity.startForResult(DialogActivity.this, mChatFragment, ChatsFragment.REQUEST_DIALOG_ID_FOR_UPDATE, existingPrivateDialog);
                    }
                    else
                    {
                        pager.setCurrentItem(1);
                        ChatActivity.startForResult(DialogActivity.this, mGroupChatFragment, ChatsFragment.REQUEST_DIALOG_ID_FOR_UPDATE, existingPrivateDialog);
                    }
                } else {
                    showProgressDialog("Creating Dialog...");
                    createDialog(selectedUsers);
                }
            }
        }
    }


    public void initFragment()
    {
        mChatFragment = ChatsFragment.newInstance(this);
        mGroupChatFragment = GroupChatsFragment.newInstance(this);
        mContactFragment = ContactsFragment.newInstance(this);
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        public Fragment getItem(int num) {
            switch (num)
            {
                case 0:
                    return mChatFragment;
                case 1:
                    return mGroupChatFragment;
                case 2:
                    return mContactFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if( position == 0 )
            {
                return "CHATS";
            }
            else if( position == 1)
            {
                return "GROUPS";
            }
            else if( position == 2)
            {
                return "CONTACTS";
            }
            return "";
        }

    }
}

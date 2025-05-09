package dsd.dsdvansales.activities;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPingManager;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dsd.dsdvansales.MainApplication;
import dsd.dsdvansales.R;
import dsd.dsdvansales.chat.UsersAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Utils;

public class SelectUsersActivity extends AppCompatActivity {
    public static final String EXTRA_QB_USERS = "qb_users";
    public static final int MINIMUM_CHAT_OCCUPANTS_SIZE = 2;
    private static final long CLICK_DELAY = TimeUnit.SECONDS.toMillis(2);

    private static final String EXTRA_QB_DIALOG = "qb_dialog";

    private ListView usersListView;
    private ProgressBar progressBar;
    private CheckboxUsersAdapter usersAdapter;
    private long lastClickTime = 0l;

    public static void start(Context context) {
        Intent intent = new Intent(context, SelectUsersActivity.class);
        context.startActivity(intent);
    }

    /**
     * Start activity for picking users
     *
     * @param activity activity to return result
     * @param code request code for onActivityResult() method
     *
     * in onActivityResult there will be 'ArrayList<QBUser>' in the intent extras
     * which can be obtained with SelectPeopleActivity.EXTRA_QB_USERS key
     */
    public static void startForResult(Activity activity, int code) {
        startForResult(activity, code, null);
    }

    public static void startForResult(Activity activity, int code, QBChatDialog dialog) {
        Intent intent = new Intent(activity, SelectUsersActivity.class);

        intent.putExtra(EXTRA_QB_DIALOG, dialog);
        activity.startActivityForResult(intent, code);
    }

    public static void startForResult(Activity activity, Fragment fragment, int code, QBChatDialog dialog) {
        Intent intent = new Intent(activity, SelectUsersActivity.class);
        intent.putExtra(EXTRA_QB_DIALOG, dialog);
        fragment.startActivityForResult(intent, code);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_select_users);

        progressBar = (ProgressBar)findViewById(R.id.progress_select_users);
        usersListView = (ListView)findViewById(R.id.list_select_users);
        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        findViewById(R.id.btnDone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usersAdapter != null) {
                    List<QBUser> users = usersAdapter.getSelectedUsers();
                    if (users.size() >= MINIMUM_CHAT_OCCUPANTS_SIZE) {
                        passResultToCallerActivity();
                    } else {

                    }
                }
            }
        });
        loadUsersFromQb();
    }


    private void passResultToCallerActivity() {
        Intent result = new Intent();
        ArrayList<QBUser> selectedUsers = new ArrayList<>(usersAdapter.getSelectedUsers());
        result.putExtra(EXTRA_QB_USERS, selectedUsers);
        setResult(RESULT_OK, result);
        finish();
    }

    private void loadUsersFromQb() {
        List<String> tags = new ArrayList<>();
        tags.add(Utils.getStringSetting(SelectUsersActivity.this, Global.PREF_COMPANY_NAME));

        progressBar.setVisibility(View.VISIBLE);
        QBUsers.getUsersByTags(tags, null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> result, Bundle params) {
                QBChatDialog dialog = (QBChatDialog) getIntent().getSerializableExtra(EXTRA_QB_DIALOG);

                usersAdapter = new CheckboxUsersAdapter(SelectUsersActivity.this, result);
                if (dialog != null) {
                    usersAdapter.addSelectedUsers(dialog.getOccupants());
                }
                usersListView.setAdapter(usersAdapter);

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(QBResponseException e) {
                /*showErrorSnackbar(R.string.select_users_get_users_error, e,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loadUsersFromQb();
                            }
                        });*/
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private boolean isEditingChat() {
        return getIntent().getSerializableExtra(EXTRA_QB_DIALOG) != null;
    }

    public class CheckboxUsersAdapter extends UsersAdapter {

        private List<Integer> initiallySelectedUsers;
        private List<QBUser> selectedUsers;

        public CheckboxUsersAdapter(Context context, List<QBUser> users) {
            super(context, users);
            this.selectedUsers = new ArrayList<>();
            this.selectedUsers.add(currentUser);

            this.initiallySelectedUsers = new ArrayList<>();
        }

        public void addSelectedUsers(List<Integer> userIds) {
            for (QBUser user : objectsList) {
                for (Integer id : userIds) {
                    if (user.getId().equals(id)) {
                        selectedUsers.add(user);
                        initiallySelectedUsers.add(user.getId());
                        break;
                    }
                }
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            final QBUser user = getItem(position);
            final ViewHolder holder = (ViewHolder) view.getTag();
            final QBPingManager pingManager = QBChatService.getInstance().getPingManager();
            new Thread()
            {
                @Override
                public void run() {
                    boolean bOnline = false;
                    try {
                        bOnline = pingManager.pingUser(user.getId());
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                    final boolean finalBOnline = bOnline;
                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("User", user.getFullName() + ":" + (finalBOnline ? "online" : "offline"));
                        }
                    });
                }
            }.start();


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isAvailableForSelection(user)) {
                        return;
                    }

                    holder.userCheckBox.setChecked(!holder.userCheckBox.isChecked());
                    if (holder.userCheckBox.isChecked()) {
                        selectedUsers.add(user);
                    } else {
                        selectedUsers.remove(user);
                    }
                }
            });

            holder.userCheckBox.setVisibility(View.VISIBLE);
            holder.statusImageView.setVisibility(View.GONE);
            holder.userCheckBox.setChecked(selectedUsers.contains(user));

            return view;
        }

        public List<QBUser> getSelectedUsers() {
            return selectedUsers;
        }

        @Override
        protected boolean isAvailableForSelection(QBUser user) {
            return super.isAvailableForSelection(user) && !initiallySelectedUsers.contains(user.getId());
        }
    }
}

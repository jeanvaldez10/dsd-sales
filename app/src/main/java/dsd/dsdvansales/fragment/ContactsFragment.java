package dsd.dsdvansales.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.listeners.QBRosterListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.ChatActivity;
import dsd.dsdvansales.activities.DialogActivity;
import dsd.dsdvansales.chat.ChatHelper;
import dsd.dsdvansales.chat.ErrorUtils;
import dsd.dsdvansales.chat.QbDialogHolder;
import dsd.dsdvansales.chat.UsersAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;

public class ContactsFragment extends Fragment {

    View mRootView;
    DialogActivity mContext;

    private ListView usersListView;
    private ProgressBar progressBar;
    private UsersAdapter usersAdapter;

    public QBPagedRequestBuilder qbPagedRequestBuilder = new QBPagedRequestBuilder();

    QBRosterListener rosterListener = new QBRosterListener() {
        @Override
        public void entriesDeleted(Collection<Integer> userIds) {
            if( usersAdapter != null )
            {
                usersAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void entriesAdded(Collection<Integer> userIds) {
            if( usersAdapter != null )
            {
                usersAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void entriesUpdated(Collection<Integer> userIds) {
            if( usersAdapter != null )
            {
                usersAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void presenceChanged(QBPresence presence) {
            if( usersAdapter != null )
            {
                usersAdapter.notifyDataSetChanged();
            }
        }
    };

    public ContactsFragment() {
        // Required empty public constructor
    }

    public void setContext(DialogActivity context)
    {
        mContext = context;
    }

    // TODO: Rename and change types and number of parameters
    public static ContactsFragment newInstance(DialogActivity context) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.setContext(context);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    protected View getSnackbarAnchorView() {
        return mRootView.findViewById(R.id.layout_root);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        progressBar = (ProgressBar)mRootView.findViewById(R.id.progress_select_users);
        usersListView = (ListView)mRootView.findViewById(R.id.list_select_users);

        usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mContext.pager.setCurrentItem(0);
                QBUser targetUser = usersAdapter.getItem(position);
                if (QbDialogHolder.getInstance().hasPrivateDialogWithUser(targetUser)){
                    QBChatDialog existingPrivateDialog = QbDialogHolder.getInstance().getPrivateDialogWithUser(targetUser);
                    ChatActivity.startForResult(mContext, mContext.mChatFragment, ChatsFragment.REQUEST_DIALOG_ID_FOR_UPDATE, existingPrivateDialog);
                }
                else
                {
                    mContext.showProgressDialog(Str.CreatingDialog[Global.LANG]);
                    List<QBUser> selectedUsers = new ArrayList<>();
                    selectedUsers.add(targetUser);
                    ChatHelper.getInstance().createDialogWithSelectedUsers(selectedUsers,
                            new QBEntityCallback<QBChatDialog>() {
                                @Override
                                public void onSuccess(QBChatDialog dialog, Bundle args) {
                                    mContext.hideProgressDialog();
                                    mContext.pager.setCurrentItem(0);
                                    Utils.dialogsManager.sendSystemMessageAboutCreatingDialog(QBChatService.getInstance().getSystemMessagesManager(), dialog);
                                    ChatActivity.startForResult(mContext, mContext.mChatFragment, ChatsFragment.REQUEST_DIALOG_ID_FOR_UPDATE, dialog);
                                }

                                @Override
                                public void onError(QBResponseException e) {
                                    mContext.hideProgressDialog();
                                }
                            }
                    );
                }
            }
        });

        qbPagedRequestBuilder.setPerPage(100);
        loadUsersFromQb();
        return mRootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        Global.chatRoster.removeRosterListener(rosterListener);
    }

    private boolean isPrivateDialogExist(ArrayList<QBUser> allSelectedUsers) {
        ArrayList<QBUser> selectedUsers = new ArrayList<>();
        selectedUsers.addAll(allSelectedUsers);
        selectedUsers.remove(ChatHelper.getCurrentUser());
        return selectedUsers.size() == 1 && QbDialogHolder.getInstance().hasPrivateDialogWithUser(selectedUsers.get(0));
    }

    protected Snackbar showErrorSnackbar(@StringRes int resId, Exception e,
                                         View.OnClickListener clickListener) {
        return ErrorUtils.showSnackbar(getSnackbarAnchorView(), resId, e,
                R.string.retry, clickListener);
    }

    public void refreshData()
    {
        usersAdapter.notifyDataSetChanged();
    }

    private void loadUsersFromQb() {
        List<String> tags = new ArrayList<>();
        tags.add(Utils.getStringSetting(mContext, Global.PREF_COMPANY_NAME));

        progressBar.setVisibility(View.VISIBLE);
        QBUsers.getUsersByTags(tags, qbPagedRequestBuilder).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> result, Bundle params) {

                QBUser curUser = QBChatService.getInstance().getUser();
                for(QBUser user : result)
                {
                    if( user.getId() == curUser.getId() )
                    {
                        curUser = user;
                    }
                    else
                    {
                        if(mContext.mOnlineStatus.get(user.getId()) == null)
                        {
                            mContext.mOnlineStatus.put(user.getId(), false);
                        }
                    }
                }
                result.remove(curUser);

                Collections.sort(result, new Comparator<QBUser>() {
                    @Override
                    public int compare(QBUser o1, QBUser o2) {
                        try
                        {
                            return o1.getFullName().compareTo(o2.getFullName());
                        }catch(Exception e)
                        {
                            return 0;
                        }
                    }
                });

                usersAdapter = new UsersAdapter(mContext, result);

                usersListView.setAdapter(usersAdapter);
//                Global.chatRoster.addRosterListener(rosterListener);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(QBResponseException e) {
                showErrorSnackbar(R.string.select_users_get_users_error, e,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loadUsersFromQb();
                            }
                        });
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}

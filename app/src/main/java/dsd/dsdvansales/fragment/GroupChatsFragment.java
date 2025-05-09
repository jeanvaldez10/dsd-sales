package dsd.dsdvansales.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.ChatActivity;
import dsd.dsdvansales.activities.DialogActivity;
import dsd.dsdvansales.activities.SelectUsersActivity;
import dsd.dsdvansales.chat.BaseSelectableListAdapter;
import dsd.dsdvansales.chat.ChatHelper;
import dsd.dsdvansales.chat.QbDialogHolder;
import dsd.dsdvansales.chat.QbDialogUtils;
import dsd.dsdvansales.chat.QbEntityCallbackImpl;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;

public class GroupChatsFragment extends Fragment {

    View mRootView;
    DialogActivity mContext;

    public static final int REQUEST_SELECT_PEOPLE = 174;
    public static final int REQUEST_DIALOG_ID_FOR_UPDATE = 165;

    private ProgressBar progressBar;
    private SwipyRefreshLayout setOnRefreshListener;
    private QBRequestGetBuilder requestBuilder;

    private int skipRecords = 0;

    private DialogsAdapter dialogsAdapter;


    public void updateDialogsAdapter() {
        List<QBChatDialog> dialogList = new ArrayList<>(QbDialogHolder.getInstance().getDialogs().values());
        List<QBChatDialog> privateChatDialog = new ArrayList<>();
        for(QBChatDialog  dialog : dialogList)
        {
            if(dialog.getType() != QBDialogType.PRIVATE)
            {
                privateChatDialog.add(dialog);
            }
        }
        dialogsAdapter.updateList(privateChatDialog);
    }



    public GroupChatsFragment() {
        // Required empty public constructor
    }

    public void setContext(DialogActivity context)
    {
        mContext = context;
    }

    // TODO: Rename and change types and number of parameters
    public static GroupChatsFragment newInstance(DialogActivity context) {
        GroupChatsFragment fragment = new GroupChatsFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_chats, container, false);



        initUi();

        if (QbDialogHolder.getInstance().getDialogs().size() > 0) {
            loadDialogsFromQb(true, true);
        } else {
            loadDialogsFromQb(false, true);
        }

        return mRootView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }



    private void initUi() {
        
        ListView dialogsListView = (ListView)mRootView.findViewById(R.id.list_dialogs_chats);
        progressBar = (ProgressBar)mRootView.findViewById(R.id.progress_dialogs);
        setOnRefreshListener = (SwipyRefreshLayout)mRootView.findViewById(R.id.swipy_refresh_layout);

        List<QBChatDialog> dialogList = new ArrayList<>(QbDialogHolder.getInstance().getDialogs().values());
        List<QBChatDialog> privateChatDialog = new ArrayList<>();
        for(QBChatDialog  dialog : dialogList)
        {
            if(dialog.getType() != QBDialogType.PRIVATE)
            {
                privateChatDialog.add(dialog);
            }
        }

        dialogsAdapter = new DialogsAdapter(privateChatDialog);

        dialogsListView.setAdapter(dialogsAdapter);

        dialogsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QBChatDialog selectedDialog = (QBChatDialog) parent.getItemAtPosition(position);
                ChatActivity.startForResult(mContext, GroupChatsFragment.this, REQUEST_DIALOG_ID_FOR_UPDATE, selectedDialog);

                /*if (currentActionMode == null) {
                    //ChatActivity.startForResult(DialogsActivity.this, REQUEST_DIALOG_ID_FOR_UPDATE, selectedDialog);
                } else {
                    dialogsAdapter.toggleSelection(selectedDialog);
                }*/
            }
        });
        /*dialogsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                QBChatDialog selectedDialog = (QBChatDialog) parent.getItemAtPosition(position);
                dialogsAdapter.selectItem(selectedDialog);
                return true;
            }
        });*/
        requestBuilder = new QBRequestGetBuilder();

        setOnRefreshListener.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                requestBuilder.setSkip(skipRecords += ChatHelper.DIALOG_ITEMS_PER_PAGE);
                loadDialogsFromQb(true, false);
            }
        });

    }

    private void loadDialogsFromQb(final boolean silentUpdate, final boolean clearDialogHolder) {
        if (!silentUpdate) {
            progressBar.setVisibility(View.VISIBLE);
        }

        ChatHelper.getInstance().getDialogs(requestBuilder, new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> dialogs, Bundle bundle) {
                progressBar.setVisibility(View.GONE);
                setOnRefreshListener.setRefreshing(false);

                if (clearDialogHolder) {
                    QbDialogHolder.getInstance().clear();
                }
                QbDialogHolder.getInstance().addDialogs(dialogs);
                updateDialogsAdapter();
            }

            @Override
            public void onError(QBResponseException e) {
                progressBar.setVisibility(View.GONE);
                setOnRefreshListener.setRefreshing(false);
                if(!mContext.isFinishing() && !mContext.isDestroyed())
                {
                    try {
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }catch (Exception e1)
                    {
                        e.printStackTrace();
                    }
                }

            }
        });
    }


    public class DialogsAdapter extends BaseSelectableListAdapter<QBChatDialog> {

        private static final String EMPTY_STRING = "";

        public DialogsAdapter(List<QBChatDialog> dialogs) {
            super(mContext, dialogs);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_dialog, parent, false);

                holder = new ViewHolder();
                holder.rootLayout = (ViewGroup) convertView.findViewById(R.id.root);
                holder.nameTextView = (TextView) convertView.findViewById(R.id.text_dialog_name);
                holder.lastMessageTextView = (TextView) convertView.findViewById(R.id.text_dialog_last_message);
                holder.unreadCounterTextView = (TextView) convertView.findViewById(R.id.text_dialog_unread_count);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            QBChatDialog dialog = getItem(position);

            holder.nameTextView.setText(QbDialogUtils.getDialogName(dialog));
            holder.lastMessageTextView.setText(prepareTextLastMessage(dialog));

            int unreadMessagesCount = getUnreadMsgCount(dialog);
            if (unreadMessagesCount == 0) {
                holder.unreadCounterTextView.setVisibility(View.GONE);
            } else {
                holder.unreadCounterTextView.setVisibility(View.VISIBLE);
                holder.unreadCounterTextView.setText(String.valueOf(unreadMessagesCount > 99 ? 99 : unreadMessagesCount));
            }

            holder.rootLayout.setBackgroundColor(isItemSelected(position) ? mContext.getResources().getColor(R.color.selected_list_item_color) :
                    mContext.getResources().getColor(android.R.color.transparent));

            return convertView;
        }

        private int getUnreadMsgCount(QBChatDialog chatDialog){
            Integer unreadMessageCount = chatDialog.getUnreadMessageCount();
            if (unreadMessageCount == null) {
                return 0;
            } else {
                return unreadMessageCount;
            }
        }

        private boolean isLastMessageAttachment(QBChatDialog dialog) {
            String lastMessage = dialog.getLastMessage();
            Integer lastMessageSenderId = dialog.getLastMessageUserId();
            return TextUtils.isEmpty(lastMessage) && lastMessageSenderId != null;
        }

        private String prepareTextLastMessage(QBChatDialog chatDialog){
            if (isLastMessageAttachment(chatDialog)){
                return Str.Attachment[Global.LANG];
            } else {
                return chatDialog.getLastMessage();
            }
        }

        private class ViewHolder {
            ViewGroup rootLayout;
            TextView nameTextView;
            TextView lastMessageTextView;
            TextView unreadCounterTextView;
        }
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
                        Utils.dialogsManager.sendSystemMessageAboutCreatingDialog(Utils.systemMessagesManager, dialog);
                        ChatActivity.startForResult(mContext, GroupChatsFragment.this, REQUEST_DIALOG_ID_FOR_UPDATE, dialog);
                        mContext.hideProgressDialog();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        mContext.hideProgressDialog();
                    }
                }
        );
    }

    private void loadUpdatedDialog(String dialogId) {
        ChatHelper.getInstance().getDialogById(dialogId, new QbEntityCallbackImpl<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog result, Bundle bundle) {
                QbDialogHolder.getInstance().addDialog(result);
                updateDialogsAdapter();
            }

            @Override
            public void onError(QBResponseException e) {
            }
        });
    }

    private void updateDialogsList() {
        requestBuilder.setSkip(skipRecords = 0);
        loadDialogsFromQb(true, true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_SELECT_PEOPLE) {
                ArrayList<QBUser> selectedUsers = (ArrayList<QBUser>) data
                        .getSerializableExtra(SelectUsersActivity.EXTRA_QB_USERS);

                if (isPrivateDialogExist(selectedUsers)) {
                    selectedUsers.remove(ChatHelper.getCurrentUser());
                    QBChatDialog existingPrivateDialog = QbDialogHolder.getInstance().getPrivateDialogWithUser(selectedUsers.get(0));
                    ChatActivity.startForResult(mContext, this, REQUEST_DIALOG_ID_FOR_UPDATE, existingPrivateDialog);
                } else {
                    mContext.showProgressDialog(Str.CreatingDialog[Global.LANG]);
                    createDialog(selectedUsers);
                }
            } else if (requestCode == REQUEST_DIALOG_ID_FOR_UPDATE) {
                if (data != null) {
                    String dialogId = data.getStringExtra(ChatActivity.EXTRA_DIALOG_ID);
                    loadUpdatedDialog(dialogId);
                } else {
                    updateDialogsList();
                }
            }
        } else {
            updateDialogsAdapter();
        }
    }
}

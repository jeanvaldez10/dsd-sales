package dsd.dsdvansales.chat;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.users.model.QBUser;

import java.util.List;
import java.util.Locale;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.DialogActivity;
import dsd.dsdvansales.global.Global;

public class UsersAdapter extends BaseListAdapter<QBUser> {

    protected QBUser currentUser;
    public UsersAdapter(Context context, List<QBUser> users) {
        super(context, users);
        currentUser = QBChatService.getInstance().getUser();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        QBUser user = getItem(position);

        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_user, parent, false);
            holder = new ViewHolder();
            holder.loginTextView = (TextView) convertView.findViewById(R.id.text_user_login);
            holder.userCheckBox = (CheckBox) convertView.findViewById(R.id.checkbox_user);
            holder.statusImageView = (ImageView) convertView.findViewById(R.id.status);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (isUserMe(user)) {
            holder.loginTextView.setText(String.format(Locale.ENGLISH, "%s (you)", user.getFullName()));
        } else {
            holder.loginTextView.setText(user.getFullName());
        }

        if (isAvailableForSelection(user)) {
            holder.loginTextView.setTextColor(context.getResources().getColor(R.color.black_color));
        } else {
            holder.loginTextView.setTextColor(context.getResources().getColor(R.color.black_color));
        }

        holder.userCheckBox.setVisibility(View.GONE);

        /*if( context instanceof DialogActivity )
        {
            Boolean bStatus = ((DialogActivity)context).mOnlineStatus.get(user.getId());
            if( bStatus != null && bStatus)
            {
                holder.statusImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_green));
            }
            else
            {
                holder.statusImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_red));
            }
        }
        else
        {
            holder.statusImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_red));
        }*/
        /*QBPresence presence = Global.chatRoster.getPresence(user.getId());
        if (presence == null) {
            holder.statusImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_red));
        }

        if (presence.getType() == QBPresence.Type.online) {
            holder.statusImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_green));
        }else{
            holder.statusImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.circle_red));
        }*/

        holder.statusImageView.setVisibility(View.GONE);


        return convertView;
    }

    protected boolean isUserMe(QBUser user) {
        return currentUser != null && currentUser.getId().equals(user.getId());
    }

    protected boolean isAvailableForSelection(QBUser user) {
        return currentUser == null || !currentUser.getId().equals(user.getId());
    }

    public static class ViewHolder {
        public TextView loginTextView;
        public CheckBox userCheckBox;
        public ImageView statusImageView;
    }
}

package dsd.dsdvansales.chat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.helper.CollectionsUtil;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.List;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.AttachmentImageActivity;
import dsd.dsdvansales.activities.MainActivity;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.TimeUtils;
import dsd.dsdvansales.global.Utils;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class ChatAdapter extends BaseListAdapter<QBChatMessage> implements StickyListHeadersAdapter {

    private static final String TAG = ChatAdapter.class.getSimpleName();
    private final QBChatDialog chatDialog;
    private OnItemInfoExpandedListener onItemInfoExpandedListener;
    private PaginationHistoryListener paginationListener;
    private int previousGetCount = 0;

    public ChatAdapter(Context context, QBChatDialog chatDialog, List<QBChatMessage> chatMessages) {
        super(context, chatMessages);
        this.chatDialog = chatDialog;
    }

    public void setOnItemInfoExpandedListener(OnItemInfoExpandedListener onItemInfoExpandedListener) {
        this.onItemInfoExpandedListener = onItemInfoExpandedListener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.list_item_chat_message, parent, false);

            holder.messageBodyTextView = (TextView) convertView.findViewById(R.id.text_image_message);
            holder.messageAuthorTextView = (TextView) convertView.findViewById(R.id.text_message_author);
            holder.messageContainerLayout = (LinearLayout) convertView.findViewById(R.id.layout_chat_message_container);
            holder.messageBodyContainerLayout = (RelativeLayout) convertView.findViewById(R.id.layout_message_content_container);
            holder.messageInfoTextView = (TextView) convertView.findViewById(R.id.text_message_info);
            holder.attachmentImageView = (MaskedImageView) convertView.findViewById(R.id.image_message_attachment);
            holder.attachmentProgressBar = (ProgressBar) convertView.findViewById(R.id.progress_message_attachment);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final QBChatMessage chatMessage = getItem(position);

        setIncomingOrOutgoingMessageAttributes(holder, chatMessage);
        setMessageBody(holder, chatMessage);
        setMessageInfo(chatMessage, holder);
        setMessageAuthor(holder, chatMessage);


        holder.messageContainerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasAttachments(chatMessage)) {
                    Collection<QBAttachment> attachments = chatMessage.getAttachments();
                    final QBAttachment attachment = attachments.iterator().next();
                    if(attachment.getType() != null && attachment.getType().toLowerCase().startsWith("image"))
                    {
                        AttachmentImageActivity.start(context, attachment.getUrl());
                    }
                    else
                    {
                        Utils.createDirectory(Global.DOWNLOAD_DIR);
                        //Downloading File and show this.
                        if( Utils.isFileExist(Global.DOWNLOAD_DIR + attachment.getName()) )
                        {
                            Uri path = Uri.fromFile(new File(Global.DOWNLOAD_DIR + attachment.getName()));
                            Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
                            pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            pdfOpenintent.setDataAndType(path, attachment.getType());
                            try {
                                context.startActivity(pdfOpenintent);
                            }
                            catch (Exception e) {
                                Toast.makeText(context, Str.does_not_exist_this_file_type[Global.LANG], Toast.LENGTH_LONG).show();
                            }
                        }
                        else
                        {
                            final ProgressDialog dialog = new ProgressDialog(context);
                            dialog.setMessage(Str.Downloading_Data[Global.LANG]);
                            dialog.setCancelable(false);
                            dialog.show();
                            new Thread(){
                                @Override
                                public void run() {
                                    try {
                                        String outputFile = Global.DOWNLOAD_DIR + attachment.getName();
                                        URL u = new URL(attachment.getUrl());
                                        URLConnection conn = u.openConnection();
                                        int contentLength = conn.getContentLength();

                                        DataInputStream stream = new DataInputStream(u.openStream());

                                        byte[] buffer = new byte[contentLength];
                                        stream.readFully(buffer);
                                        stream.close();

                                        DataOutputStream fos = new DataOutputStream(new FileOutputStream(outputFile));
                                        fos.write(buffer);
                                        fos.flush();
                                        fos.close();

                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                dialog.dismiss();
                                                Uri path = Uri.fromFile(new File(Global.DOWNLOAD_DIR + attachment.getName()));
                                                Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
                                                pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                pdfOpenintent.setDataAndType(path, attachment.getType());
                                                try {
                                                    context.startActivity(pdfOpenintent);
                                                }
                                                catch (Exception e) {
                                                    Toast.makeText(context, Str.does_not_exist_this_file_type[Global.LANG], Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    } catch(FileNotFoundException e) {
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                dialog.dismiss();
                                            }
                                        });
                                        return; // swallow a 404
                                    } catch (IOException e) {
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                dialog.dismiss();
                                            }
                                        });
                                        return; // swallow a 404
                                    }
                                }
                            }.start();
                        }
                    }

                } else {
                    toggleItemInfo(holder, position);
                }
            }
        });
        holder.messageContainerLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (hasAttachments(chatMessage)) {
                    toggleItemInfo(holder, position);
                    return true;
                }

                return false;
            }
        });
        holder.messageInfoTextView.setVisibility(View.VISIBLE);

        if (isIncoming(chatMessage) && !isRead(chatMessage)){
            readMessage(chatMessage);
        }

        downloadMore(position);

        return convertView;
    }

    private void downloadMore(int position) {
        if (position == 0) {
            if (getCount() != previousGetCount) {
                paginationListener.downloadMore();
                previousGetCount = getCount();
            }
        }
    }

    public void setPaginationHistoryListener(PaginationHistoryListener paginationListener) {
        this.paginationListener = paginationListener;
    }

    private void toggleItemInfo(ViewHolder holder, int position) {
        boolean isMessageInfoVisible = holder.messageInfoTextView.getVisibility() == View.VISIBLE;
        holder.messageInfoTextView.setVisibility(isMessageInfoVisible ? View.GONE : View.VISIBLE);

        if (onItemInfoExpandedListener != null) {
            onItemInfoExpandedListener.onItemInfoExpanded(position);
        }
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = inflater.inflate(R.layout.view_chat_message_header, parent, false);
            holder.dateTextView = (TextView) convertView.findViewById(R.id.header_date_textview);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        QBChatMessage chatMessage = getItem(position);
        holder.dateTextView.setText(TimeUtils.getDate(chatMessage.getDateSent() * 1000));

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) holder.dateTextView.getLayoutParams();
        if (position == 0) {
            lp.topMargin = Math.round(context.getResources().getDimension(R.dimen.dip_16));
        } else {
            lp.topMargin = 0;
        }
        holder.dateTextView.setLayoutParams(lp);

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        QBChatMessage chatMessage = getItem(position);
        return TimeUtils.getDateAsHeaderId(chatMessage.getDateSent() * 1000);
    }

    private void setMessageBody(final ViewHolder holder, QBChatMessage chatMessage) {
        if (hasAttachments(chatMessage)) {
            Collection<QBAttachment> attachments = chatMessage.getAttachments();
            QBAttachment attachment = attachments.iterator().next();

            holder.messageBodyTextView.setVisibility(View.GONE);

            holder.attachmentImageView.setVisibility(View.VISIBLE);
            if(attachment.getType() != null && attachment.getType().toLowerCase().startsWith("image"))
            {
                holder.attachmentProgressBar.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(attachment.getUrl())
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                holder.attachmentImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                                holder.attachmentProgressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                holder.attachmentImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                holder.attachmentProgressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .override(Math.round(context.getResources().getDimension(R.dimen.dip_100)), Math.round(context.getResources().getDimension(R.dimen.dip_100)))
                        .dontTransform()
                        .error(R.drawable.ic_error)
                        .into(holder.attachmentImageView);
            }
            else
            {
                holder.attachmentProgressBar.setVisibility(View.GONE);
                holder.messageBodyTextView.setVisibility(View.VISIBLE);
                holder.messageBodyTextView.setTextColor(context.getResources().getColor(R.color.black_color));
                holder.messageBodyTextView.setText(attachment.getName());
                holder.attachmentImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_terms));
            }

        } else {
            holder.messageBodyTextView.setText(chatMessage.getBody());
            holder.messageBodyTextView.setVisibility(View.VISIBLE);
            holder.attachmentImageView.setVisibility(View.GONE);
            holder.attachmentProgressBar.setVisibility(View.GONE);
        }
    }

    private void setMessageAuthor(ViewHolder holder, QBChatMessage chatMessage) {
        if (isIncoming(chatMessage)) {
            QBUser sender = QbUsersHolder.getInstance().getUserById(chatMessage.getSenderId());
            holder.messageAuthorTextView.setText(sender.getFullName());
            holder.messageAuthorTextView.setVisibility(View.VISIBLE);

            if (hasAttachments(chatMessage)) {
                holder.messageAuthorTextView.setBackgroundResource(R.drawable.shape_rectangle_semi_transparent);
                holder.messageAuthorTextView.setTextColor(context.getResources().getColor(R.color.white_color));
            } else {
                holder.messageAuthorTextView.setBackgroundResource(0);
                holder.messageAuthorTextView.setTextColor(context.getResources().getColor(R.color.text_color_dark_grey));
            }
        } else {
            holder.messageAuthorTextView.setVisibility(View.GONE);
        }
    }

    private void setMessageInfo(QBChatMessage chatMessage, ViewHolder holder) {
        holder.messageInfoTextView.setText(TimeUtils.getTime(chatMessage.getDateSent() * 1000));
    }

    @SuppressLint("RtlHardcoded")
    private void setIncomingOrOutgoingMessageAttributes(ViewHolder holder, QBChatMessage chatMessage) {
        boolean isIncoming = isIncoming(chatMessage);
        int gravity = isIncoming ? Gravity.LEFT : Gravity.RIGHT;
        holder.messageContainerLayout.setGravity(gravity);
        holder.messageInfoTextView.setGravity(gravity);

        int messageBodyContainerBgResource = isIncoming
                ? R.drawable.bg_incoming
                : R.drawable.bg_outcoming;
        if (hasAttachments(chatMessage)) {
            holder.messageBodyContainerLayout.setBackgroundResource(0);
            holder.messageBodyContainerLayout.setPadding(0, 0, 0, 0);
            holder.attachmentImageView.setMaskResourceId(messageBodyContainerBgResource);
        } else {
            holder.messageBodyContainerLayout.setBackgroundResource(messageBodyContainerBgResource);
        }

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) holder.messageBodyContainerLayout.getLayoutParams();
        if (isIncoming && hasAttachments(chatMessage)) {
            lp.rightMargin = (int)context.getResources().getDimension(R.dimen.dip_40);
            lp.leftMargin = 0;
            lp.topMargin = (int)context.getResources().getDimension(R.dimen.dip_16);
        } else if (isIncoming) {
            lp.rightMargin = (int)context.getResources().getDimension(R.dimen.dip_40);
            lp.leftMargin = 0;
            lp.topMargin = 0;
        }
        else if (!isIncoming && hasAttachments(chatMessage)) {
            lp.leftMargin = (int)context.getResources().getDimension(R.dimen.dip_40);
            lp.rightMargin = 0;
            lp.topMargin = (int)context.getResources().getDimension(R.dimen.dip_16);
        } else if (!isIncoming) {
            lp.rightMargin = 0;
            lp.leftMargin = (int)context.getResources().getDimension(R.dimen.dip_40);
            lp.topMargin = 0;
        }

        holder.messageBodyContainerLayout.setLayoutParams(lp);

        int textColorResource = isIncoming
                ? R.color.black_color
                : R.color.black_color;
        holder.messageBodyTextView.setTextColor((int)context.getResources().getColor(textColorResource));
    }

    private boolean hasAttachments(QBChatMessage chatMessage) {
        Collection<QBAttachment> attachments = chatMessage.getAttachments();
        return attachments != null && !attachments.isEmpty();
    }

    private boolean isIncoming(QBChatMessage chatMessage) {
        QBUser currentUser = ChatHelper.getCurrentUser();
        return chatMessage.getSenderId() != null && !chatMessage.getSenderId().equals(currentUser.getId());
    }

    private boolean isRead(QBChatMessage chatMessage){
        Integer currentUserId = ChatHelper.getCurrentUser().getId();
        return !CollectionsUtil.isEmpty(chatMessage.getReadIds()) && chatMessage.getReadIds().contains(currentUserId);
    }

    private void readMessage(QBChatMessage chatMessage){
        try {
            chatDialog.readMessage(chatMessage);
        } catch (XMPPException | SmackException.NotConnectedException e) {
            Log.w(TAG, e);
        }
    }

    private static class HeaderViewHolder {
        public TextView dateTextView;
    }

    private static class ViewHolder {
        public TextView messageBodyTextView;
        public TextView messageAuthorTextView;
        public TextView messageInfoTextView;
        public LinearLayout messageContainerLayout;
        public RelativeLayout messageBodyContainerLayout;
        public MaskedImageView attachmentImageView;
        public ProgressBar attachmentProgressBar;
    }

    public interface OnItemInfoExpandedListener {
        void onItemInfoExpanded(int position);
    }
}

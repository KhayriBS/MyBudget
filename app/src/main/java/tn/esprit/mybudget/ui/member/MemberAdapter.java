package tn.esprit.mybudget.ui.member;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.data.entity.Member;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<Member> members = new ArrayList<>();
    private OnMemberClickListener clickListener;
    private OnMemberDeleteListener deleteListener;
    private OnMemberSettleListener settleListener;

    public interface OnMemberClickListener {
        void onClick(Member member);
    }

    public interface OnMemberDeleteListener {
        void onDelete(Member member);
    }

    public interface OnMemberSettleListener {
        void onSettle(Member member);
    }

    public void setOnMemberClickListener(OnMemberClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnMemberDeleteListener(OnMemberDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setOnMemberSettleListener(OnMemberSettleListener listener) {
        this.settleListener = listener;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Member member = members.get(position);

        holder.tvName.setText(member.name);

        // Format amount with type
        String amountText = String.format(Locale.getDefault(), "%.2f", member.amount);
        if ("Lent".equals(member.type)) {
            holder.tvAmount.setText("+" + amountText);
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50")); // Green - they owe you
            holder.tvType.setText("Lent to");
        } else {
            holder.tvAmount.setText("-" + amountText);
            holder.tvAmount.setTextColor(Color.parseColor("#F44336")); // Red - you owe them
            holder.tvType.setText("Borrowed from");
        }

        // Format date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(member.date)));

        // Show note if available
        if (member.note != null && !member.note.isEmpty()) {
            holder.tvNote.setVisibility(View.VISIBLE);
            holder.tvNote.setText(member.note);
        } else {
            holder.tvNote.setVisibility(View.GONE);
        }

        // Show settled status
        if (member.isSettled) {
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setText("Settled");
            holder.tvStatus.setTextColor(Color.parseColor("#9E9E9E"));
            holder.btnSettle.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setVisibility(View.GONE);
            holder.btnSettle.setVisibility(View.VISIBLE);
        }

        // Click to edit
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null && !member.isSettled) {
                clickListener.onClick(member);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(member);
            }
        });

        holder.btnSettle.setOnClickListener(v -> {
            if (settleListener != null && !member.isSettled) {
                settleListener.onSettle(member);
            }
        });
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAmount, tvType, tvDate, tvNote, tvStatus;
        ImageView btnDelete, btnSettle;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMemberName);
            tvAmount = itemView.findViewById(R.id.tvMemberAmount);
            tvType = itemView.findViewById(R.id.tvMemberType);
            tvDate = itemView.findViewById(R.id.tvMemberDate);
            tvNote = itemView.findViewById(R.id.tvMemberNote);
            tvStatus = itemView.findViewById(R.id.tvMemberStatus);
            btnDelete = itemView.findViewById(R.id.btnDeleteMember);
            btnSettle = itemView.findViewById(R.id.btnSettleMember);
        }
    }
}

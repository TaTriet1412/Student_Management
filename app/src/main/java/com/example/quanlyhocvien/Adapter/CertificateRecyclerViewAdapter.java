package com.example.quanlyhocvien.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlyhocvien.Fragment.CertificateFragment;
import com.example.quanlyhocvien.R;
import com.example.quanlyhocvien.StudentFragment;
import com.example.quanlyhocvien.object.Account;
import com.example.quanlyhocvien.object.Certificate;
import com.example.quanlyhocvien.object.Student;

import java.util.ArrayList;
import java.util.List;

public class CertificateRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<Certificate> certificates;
    public int positionSelected;
    public Fragment fragment;
    private final int role;

    public void setCertificates(ArrayList<Certificate> certificates) {
        this.certificates.clear();
        this.certificates.addAll(certificates);
        notifyDataSetChanged();
    }

    private enum abc {
        Add,
        Change,
        information
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(role == 2) {
            CardView view = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_certificate, parent, false);
            return new CertificateRecyclerViewAdapter.MyViewHolder(view);
        }
        if (viewType == CertificateRecyclerViewAdapter.abc.Add.ordinal()){
            CardView view = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_add, parent, false);
            return new CertificateRecyclerViewAdapter.AddViewHolder(view);
        } else if (viewType == CertificateRecyclerViewAdapter.abc.Change.ordinal()){
            CardView view = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_change, parent, false);
            return new CertificateRecyclerViewAdapter.ChangeViewHolder(view);
        } else {
            CardView view = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_certificate, parent, false);
            return new CertificateRecyclerViewAdapter.MyViewHolder(view);
        }
    }

   public CertificateRecyclerViewAdapter(List<Certificate> certificates, Fragment fragment, int role) {
        this.certificates = certificates;
        this.fragment = fragment;
        this.role = role;
   }


    public int getItemViewType(int position) {
        if(position == 0){
            return CertificateRecyclerViewAdapter.abc.Add.ordinal();
        } else if(position == 1){
            return CertificateRecyclerViewAdapter.abc.Change.ordinal();
        } else {
            return CertificateRecyclerViewAdapter.abc.information.ordinal();
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int realPosition;
        boolean isEmployee;
        if(role != 2) {
            isEmployee = false;
            realPosition = holder.getAdapterPosition() - 2;
        }
        else {
            realPosition = holder.getAdapterPosition();
            isEmployee = true;
        }

        if(holder instanceof CertificateRecyclerViewAdapter.MyViewHolder){
            CertificateRecyclerViewAdapter.MyViewHolder myViewHolder = (CertificateRecyclerViewAdapter.MyViewHolder) holder;
            CardView cardView = myViewHolder.cardView;
            Certificate certificate = certificates.get(realPosition);
            TextView name = cardView.findViewById(R.id.tvName);
            TextView order = cardView.findViewById(R.id.tvOrder);
            TextView  status = cardView.findViewById(R.id.tvStatus);
            name.setText(certificate.getName());
            status.setText(certificate.statusToString());
            order.setText(String.valueOf(realPosition + 1));
            // Đăng ký context menu cho item
            holder.itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                // Inflate menu từ file XML menu
                MenuInflater inflater = ((Activity) v.getContext()).getMenuInflater();
                inflater.inflate(R.menu.certificate_menu, menu);
                // Lấy thông tin của item được chọn
                positionSelected = realPosition;
                if (this.role == 2) {
                    menu.removeItem(R.id.delete);  // Xóa item có ID là 2 nếu điều kiện thỏa mãn
                }
            });
            holder.itemView.setOnClickListener(v -> {
                CertificateFragment studentFragment = (CertificateFragment)fragment;
                if(this.role == 2) {
                    Certificate certificate1 = certificates.get(holder.getAdapterPosition());
                    studentFragment.MyHandleClick(certificate1);
                }
                else {
                    Certificate certificate1 = certificates.get(holder.getAdapterPosition() - 2);
                    studentFragment.MyHandleClick(certificate1);
                }

            });
        }
        if (holder instanceof CertificateRecyclerViewAdapter.AddViewHolder){
            CertificateRecyclerViewAdapter.AddViewHolder addViewHolder = (CertificateRecyclerViewAdapter.AddViewHolder) holder;
            CardView cardView = addViewHolder.cardView;
            cardView.setOnClickListener(v -> {
                CertificateFragment studentFragment = (CertificateFragment)fragment;
                studentFragment.addClick();
            });
        }
        if (holder instanceof CertificateRecyclerViewAdapter.ChangeViewHolder){
            CertificateRecyclerViewAdapter.ChangeViewHolder changeViewHolder = (CertificateRecyclerViewAdapter.ChangeViewHolder) holder;
            CardView cardView = changeViewHolder.cardView;
            cardView.setOnClickListener(v -> {
                CertificateFragment studentFragment = (CertificateFragment)fragment;
                studentFragment.changeClick();
            });
        }
    }

    @Override
    public int getItemCount() {
        if(role != 2) {
            return certificates.size() + 2;
        }
        return certificates.size();
    }



    public static class MyViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        public MyViewHolder(@NonNull CardView itemView) {
            super(itemView);
            cardView = itemView;
        }
    }

    public static class AddViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        public AddViewHolder(@NonNull CardView itemView) {
            super(itemView);
            cardView = itemView;
        }
    }
    public static class ChangeViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        public ChangeViewHolder(@NonNull CardView itemView) {
            super(itemView);
            cardView = itemView;
        }
    }

}

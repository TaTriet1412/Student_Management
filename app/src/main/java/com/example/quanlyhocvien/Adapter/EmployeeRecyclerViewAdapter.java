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

import com.example.quanlyhocvien.EmployeeFragment;
import com.example.quanlyhocvien.R;
import com.example.quanlyhocvien.object.Account;

import java.util.List;

public class EmployeeRecyclerViewAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<Account> employees;
    public int positionSelected;
    public Fragment fragment;
    private final int role;

    public void setEmployees(List<Account> employees) {
        this.employees.clear();
        this.employees.addAll(employees);
        notifyDataSetChanged();
    }

    private enum ViewType {
        Add,
        information
    }

    public EmployeeRecyclerViewAdapter(List<Account> employees, Fragment fragment, int role) {
        this.employees = employees;
        this.fragment = fragment;
        this.role = role;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(role == 2) {
            CardView view = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_account, parent, false);
            return new ViewHolder(view);
        }
        if (viewType == ViewType.Add.ordinal()) {
            CardView view = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_add, parent, false);
            return new AddViewHolder(view);
        }
        CardView view = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_account, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0){
            return EmployeeRecyclerViewAdapter.ViewType.Add.ordinal();
        } else {
            return EmployeeRecyclerViewAdapter.ViewType.information.ordinal();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(role == 2) {
            position++;
        }

        if(holder instanceof ViewHolder){
            Account employee = employees.get(position - 1);
            CardView cardView = ((ViewHolder) holder).cardView;
            TextView name = cardView.findViewById(R.id.tvName);
            TextView email = cardView.findViewById(R.id.tvEmail);
            TextView order = cardView.findViewById(R.id.tvOrder);
            TextView role = cardView.findViewById(R.id.tvRole);
            name.setText(employee.getDisplayName());
            email.setText(employee.getEmail());
            String roles = employee.getRole() == 0 ? "Quản trị viên" : employee.getRole() == 1 ? "Quản lý" : "Nhân viên";
            role.setText(roles);
            order.setText(String.valueOf(position));
            int finalPosition = position;
            holder.itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                // Inflate menu từ file XML menu
                MenuInflater inflater = ((Activity) v.getContext()).getMenuInflater();
                inflater.inflate(R.menu.account_menu, menu);
                // Lấy thông tin của item được chọn
                positionSelected = finalPosition   - 1;
                if (this.role == 2) {
                    menu.removeItem(R.id.delete);  // Xóa item có ID là 2 nếu điều kiện thỏa mãn
                }
            });
            holder.itemView.setOnClickListener(v -> {
               EmployeeFragment studentFragment = (EmployeeFragment)fragment;
               if(this.role == 2) {
                   Account account = employees.get(holder.getAdapterPosition());
                   studentFragment.MyHandleClick(account);
               }
               else {
                   Account account = employees.get(holder.getAdapterPosition() - 1);
                   studentFragment.MyHandleClick(account);
               }

            });
        }
        if(holder instanceof AddViewHolder){
            holder.itemView.setOnClickListener(v -> {
                EmployeeFragment studentFragment = (EmployeeFragment)fragment;
                studentFragment.addClick();
            });
        }

    }

    @Override
    public int getItemCount() {
        if(role != 2) {
            return employees.size() + 1;
        }
        return employees.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        public ViewHolder(@NonNull CardView itemView) {
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

}

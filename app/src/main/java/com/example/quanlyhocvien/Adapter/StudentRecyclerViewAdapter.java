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

import com.example.quanlyhocvien.R;
import com.example.quanlyhocvien.StudentFragment;
import com.example.quanlyhocvien.object.Student;

import java.util.ArrayList;
import java.util.List;

public class StudentRecyclerViewAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<Student> students;
    public int positionSelected = 0;
    private final Fragment fragment;
    private final int role;

    public void setStudents(ArrayList<Student> students) {
        this.students.clear();
        this.students.addAll(students);
        notifyDataSetChanged();
    }

     private enum ViewType {
        Add,
        Change,
        information
    }
    public StudentRecyclerViewAdapter(List<Student> employees, Fragment fragment, int role) {
        this.students = employees;
        this.fragment = fragment;
        this.role = role;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(role == 2) {
            CardView view = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_student, parent, false);
            return new MyViewHolder(view);
        }
        if (viewType == ViewType.Add.ordinal()){
            CardView view = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_add, parent, false);
            return new AddViewHolder(view);
        } else if (viewType == ViewType.Change.ordinal()){
            CardView view = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_change, parent, false);
            return new ChangeViewHolder(view);
        } else {
            CardView view = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_student, parent, false);
            return new MyViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0){
            return ViewType.Add.ordinal();
        } else if(position == 1){
            return ViewType.Change.ordinal();
        } else {
            return ViewType.information.ordinal();
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

        if(holder instanceof MyViewHolder){
            MyViewHolder myViewHolder = (MyViewHolder) holder;
            CardView cardView = myViewHolder.cardView;
            Student student = students.get(realPosition);
            TextView name = cardView.findViewById(R.id.tvName);
            TextView email = cardView.findViewById(R.id.tvEmail);
            TextView order = cardView.findViewById(R.id.tvOrder);
            TextView role = cardView.findViewById(R.id.tvRole);
            name.setText(student.getName());
            email.setText(student.getEmail());
            role.setText(student.getId());
            order.setText(String.valueOf(realPosition + 1));
            // Đăng ký context menu cho item
            holder.itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                // Inflate menu từ file XML menu
                MenuInflater inflater = ((Activity) v.getContext()).getMenuInflater();

                inflater.inflate(R.menu.student_menu, menu);
                // Lấy thông tin của item được chọn
                positionSelected = realPosition;
                if (this.role == 2) {
                    menu.removeItem(R.id.delete);  // Xóa item có ID là 2 nếu điều kiện thỏa mãn
                }
            });
            holder.itemView.setOnClickListener(v -> {
                StudentFragment studentFragment = (StudentFragment)fragment;
                if(this.role == 2) {
                    Student student1 = students.get(holder.getAdapterPosition());
                    studentFragment.MyHandleClick(student1);
                }
                else {
                    Student student1 = students.get(holder.getAdapterPosition() - 2);
                    studentFragment.MyHandleClick(student1);
                }

            });
        }
        if (holder instanceof AddViewHolder){
            AddViewHolder addViewHolder = (AddViewHolder) holder;
            CardView cardView = addViewHolder.cardView;
            cardView.setOnClickListener(v -> {
                StudentFragment studentFragment = (StudentFragment)fragment;
                studentFragment.addClick();
            });
        }
        if (holder instanceof ChangeViewHolder){
            ChangeViewHolder changeViewHolder = (ChangeViewHolder) holder;
            CardView cardView = changeViewHolder.cardView;
            cardView.setOnClickListener(v -> {
                StudentFragment studentFragment = (StudentFragment)fragment;
                studentFragment.changeClick();
            });
        }
    }

    @Override
    public int getItemCount() {
        if(role != 2) {
            return students.size() + 2;
        }
        return students.size();
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

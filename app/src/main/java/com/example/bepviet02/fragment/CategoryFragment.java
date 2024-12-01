package com.example.bepviet02.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bepviet02.adapters.CategoryAdapter;
import com.example.bepviet02.databinding.FragmentCategoryBinding;
import com.example.bepviet02.models.Category;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment {

    private FragmentCategoryBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Inflate the fragment's layout and return the root view
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadCategories();
    }

    private void loadCategories() {
        binding.rvCategories.setAdapter(new CategoryAdapter());
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("Categories");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Category> categories = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get the category data from the snapshot
                    Category category = ds.getValue(Category.class);
                    categories.add(category);
                }
                CategoryAdapter categoryAdapter = (CategoryAdapter) binding.rvCategories.getAdapter();
                if (categoryAdapter != null) {
                    // Set the list of categories to the adapter
                    categoryAdapter.setCategoryList(categories);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CategoryFragment", "onCancelled: " + error.getMessage());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
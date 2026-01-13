package com.gym.fitconnectpro.activities.member;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.dao.MealLogDAO;
import com.gym.fitconnectpro.dao.MemberDashboardDAO;
import com.gym.fitconnectpro.models.Food;
import com.gym.fitconnectpro.models.MealPlanFood;
import com.gym.fitconnectpro.services.Session;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogMealActivity extends AppCompatActivity {

    private MealLogDAO mealLogDAO;
    private Session session;
    private int memberId;
    
    private TextView tvDate;
    private TextView tvTime; // Time View
    private Spinner spinnerMealType;
    // AutoCompleteTextView declared above as actvFoodSearch
    // Removed unused search views
    private RecyclerView rvAddedItems;
    private TextInputEditText etNotes;
    private TextView tvTotalCalories, tvTotalMacros;
    private Button btnSaveMeal;
    
    private List<MealPlanFood> addedItems = new ArrayList<>();
    private AddedItemsAdapter addedItemsAdapter;
    // Removed searchResultsAdapter
    
    private Calendar selectedDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private android.widget.AutoCompleteTextView actvFoodSearch;
    private List<Food> allFoods;

    private RecyclerView rvTodayMeals;
    private TodayMealsAdapter todayMealsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_meal);
        
        session = Session.getInstance(this);
        mealLogDAO = new MealLogDAO(this);
        
        // Fetch Member & Init Views
        MemberDashboardDAO dashboardDAO = new MemberDashboardDAO(this);
        int userId = session.getUserId();
        var memberInfo = dashboardDAO.getMemberHeaderInfo(userId);
        if (memberInfo != null) memberId = Integer.parseInt(memberInfo.get("member_id"));
        
        initViews();
        setupListeners();
        loadAllFoods();
        loadTodayMeals(); // NEW: Load history
        
        // Handle Intent
        String type = getIntent().getStringExtra("MEAL_TYPE");
        if (type != null) {
            String[] types = getResources().getStringArray(R.array.meal_types);
            for(int i=0; i<types.length; i++) if(types[i].equalsIgnoreCase(type)) spinnerMealType.setSelection(i);
        }
        updateDateDisplay();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        spinnerMealType = findViewById(R.id.spinnerMealType);
        
        // AutoCompleteTextView
        actvFoodSearch = findViewById(R.id.actvFoodSearch);
        
        rvAddedItems = findViewById(R.id.rvAddedItems);
        rvTodayMeals = findViewById(R.id.rvTodayMeals); // NEW
        
        etNotes = findViewById(R.id.etNotes);
        tvTotalCalories = findViewById(R.id.tvTotalCalories);
        tvTotalMacros = findViewById(R.id.tvTotalMacros);
        btnSaveMeal = findViewById(R.id.btnSaveMeal);
        
        // Setup Meal Type Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.meal_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMealType.setAdapter(adapter);
        
        // Setup Added Items List
        rvAddedItems.setLayoutManager(new LinearLayoutManager(this));
        addedItemsAdapter = new AddedItemsAdapter();
        rvAddedItems.setAdapter(addedItemsAdapter);

        // Setup History List
        rvTodayMeals.setLayoutManager(new LinearLayoutManager(this));
        todayMealsAdapter = new TodayMealsAdapter(new ArrayList<>());
        rvTodayMeals.setAdapter(todayMealsAdapter);
        
        updateTimeDisplay();
    }
    
    private void setupListeners() {
        tvTime.setOnClickListener(v -> showTimePicker());
        btnSaveMeal.setOnClickListener(v -> saveMeal());
        
        actvFoodSearch.setOnItemClickListener((parent, view, position, id) -> {
            Food selectedFood = (Food) parent.getItemAtPosition(position);
            addFoodItem(selectedFood);
            actvFoodSearch.setText("");
        });
        
        actvFoodSearch.setOnClickListener(v -> actvFoodSearch.showDropDown());
    }

    private void loadTodayMeals() {
        if (mealLogDAO == null) return;
        String date = dbDateFormat.format(selectedDate.getTime());
        List<com.gym.fitconnectpro.models.MealLogEntry> meals = mealLogDAO.getTodayMeals(memberId, date);
        todayMealsAdapter.setMeals(meals);
    }
    
    // ... (rest of methods)



    private void showTimePicker() {
        int hour = selectedDate.get(Calendar.HOUR_OF_DAY);
        int minute = selectedDate.get(Calendar.MINUTE);
        
        android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(this,
                (view, hourOfDay, minute1) -> {
                    selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDate.set(Calendar.MINUTE, minute1);
                    updateTimeDisplay();
                }, hour, minute, false);
        timePickerDialog.show();
    }
    
    private void updateTimeDisplay() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        tvTime.setText(timeFormat.format(selectedDate.getTime()));
    }

    private void loadAllFoods() {
        allFoods = mealLogDAO.getAllFoods();
        ArrayAdapter<Food> foodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, allFoods);
        actvFoodSearch.setAdapter(foodAdapter);
    }
    
    private void addFoodItem(Food food) {
        // Create new item with default quantity 1.0
        MealPlanFood item = new MealPlanFood();
        item.setFood(food);
        item.setFoodId(food.getId());
        item.setQuantity(1.0);
        
        addedItems.add(item);
        addedItemsAdapter.notifyItemInserted(addedItems.size() - 1);
        updateTotals();
        
        // Clear search is handled in OnItemClickListener now
        Toast.makeText(this, food.getName() + " added", Toast.LENGTH_SHORT).show();
    }
    
    private void removeFoodItem(int position) {
        addedItems.remove(position);
        addedItemsAdapter.notifyItemRemoved(position);
        updateTotals();
    }
    
    private void updateTotals() {
        int cals = 0;
        double prot = 0, carbs = 0, fats = 0;
        
        for (MealPlanFood item : addedItems) {
            double qty = item.getQuantity();
            Food f = item.getFood();
            
            cals += (int)(f.getCalories() * qty);
            prot += f.getProtein() * qty;
            carbs += f.getCarbs() * qty;
            fats += f.getFats() * qty;
        }
        
        tvTotalCalories.setText(cals + " cal");
        tvTotalMacros.setText(String.format(Locale.getDefault(), 
                "P: %.1fg  C: %.1fg  F: %.1fg", prot, carbs, fats));
    }
    


    private void saveMeal() {
        try {
            if (addedItems.isEmpty()) {
                Toast.makeText(this, "Add at least one food item", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String date = dbDateFormat.format(selectedDate.getTime());
            SimpleDateFormat timeFormatStr = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String time = timeFormatStr.format(selectedDate.getTime());
            
            String type = spinnerMealType.getSelectedItem().toString();
            String notes = etNotes.getText() != null ? etNotes.getText().toString() : "";
            
            boolean success = mealLogDAO.logMeal(memberId, date, time, type, addedItems, notes);
            
            if (success) {
                Toast.makeText(this, "Meal logged!", Toast.LENGTH_SHORT).show();
                // Reset UI
                addedItems.clear();
                addedItemsAdapter.notifyDataSetChanged();
                updateTotals();
                etNotes.setText("");
                
                // Refresh History
                loadTodayMeals();
            } else {
                Toast.makeText(this, "Failed to log meal (DB Error)", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            android.util.Log.e("LogMealActivity", "Error saving meal", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void updateDateDisplay() {
        tvDate.setText(dateFormat.format(selectedDate.getTime()));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // --- Adapters ---

    
    // Added Items Adapter (with Quantity Edit)
    class AddedItemsAdapter extends RecyclerView.Adapter<AddedItemsAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_meal_log_food, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MealPlanFood item = addedItems.get(position);
            Food food = item.getFood();
            
            holder.tvName.setText(food.getName());
            holder.tvUnit.setText(food.getServingUnit());
            holder.tvCalories.setText((int)(food.getCalories() * item.getQuantity()) + " cal");
            
            // Change Add button to Remove button look
            holder.btnAdd.setImageResource(android.R.drawable.ic_delete);
            holder.btnAdd.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFCDD2)); // Light Red
            holder.btnAdd.setImageTintList(android.content.res.ColorStateList.valueOf(0xFFD32F2F)); // Red
            
            holder.btnAdd.setOnClickListener(v -> removeFoodItem(holder.getAdapterPosition()));
            
            // Show Quantity Input
            holder.layoutQuantity.setVisibility(View.VISIBLE);
            holder.etQuantity.setText(String.valueOf(item.getQuantity()));
            
            // Handle Quantity Change
            holder.etQuantity.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    try {
                        double qty = Double.parseDouble(holder.etQuantity.getText().toString());
                        if (qty > 0) {
                            item.setQuantity(qty);
                            notifyItemChanged(holder.getAdapterPosition());
                            updateTotals();
                        }
                    } catch (NumberFormatException e) {
                        holder.etQuantity.setText(String.valueOf(item.getQuantity()));
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return addedItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvUnit, tvCalories;
            ImageButton btnAdd;
            LinearLayout layoutQuantity;
            EditText etQuantity;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvFoodName);
                tvUnit = itemView.findViewById(R.id.tvServingUnit);
                tvCalories = itemView.findViewById(R.id.tvCalories);
                btnAdd = itemView.findViewById(R.id.btnAddOrRemove);
                layoutQuantity = itemView.findViewById(R.id.layoutQuantity);
                etQuantity = itemView.findViewById(R.id.etQuantity);
            }
        }
    }
}

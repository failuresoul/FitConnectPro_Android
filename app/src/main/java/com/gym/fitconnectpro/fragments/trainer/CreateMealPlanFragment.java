package com.gym.fitconnectpro.fragments.trainer;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.dao.MealPlanDAO;
import com.gym.fitconnectpro.dao.TrainerDAO;
import com.gym.fitconnectpro.database.entities.Member;
import com.gym.fitconnectpro.models.Food;
import com.gym.fitconnectpro.models.MealPlan;
import com.gym.fitconnectpro.models.MealPlanFood;
import com.gym.fitconnectpro.services.Session;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateMealPlanFragment extends Fragment {

    // UI Components
    private Spinner spinnerClient;
    private TextView tvPlanDate, tvAddFoodHeader, tvUnit;
    private AutoCompleteTextView actvFoodSearch;
    private EditText etQuantity, etInstructions;
    private Button btnAddFood, btnAssignPlan;
    private android.widget.ImageButton btnViewAllFoods;
    private ListView lvAddedFoods;
    private TabLayout tabLayoutMeals;
    private TextView tvTotalCalories, tvTotalProtein, tvTotalCarbs, tvTotalFats;

    // Data
    private TrainerDAO trainerDAO;
    private MealPlanDAO mealPlanDAO;
    private Session session;
    private int trainerId;
    private List<Member> clientList;
    private LocalDate selectedDate;
    private List<Food> allFoods;
    
    // State
    private String currentMealType = "Breakfast";
    private Map<String, List<MealPlanFood>> mealMap; // Maps meal type to list of added foods
    private Map<String, String> instructionsMap; // Maps meal type to instructions
    private Food selectedFood = null;
    private ArrayAdapter<String> addedFoodsAdapter;
    private List<String> currentDisplayList; // Strings for ListView

    public static CreateMealPlanFragment newInstance() {
        return new CreateMealPlanFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_meal_plan, container, false);
        
        initViews(view);
        initData();
        setupListeners();
        
        return view;
    }

    private void initViews(View view) {
        spinnerClient = view.findViewById(R.id.spinnerClient);
        tvPlanDate = view.findViewById(R.id.tvPlanDate);
        tvAddFoodHeader = view.findViewById(R.id.tvAddFoodHeader);
        tvUnit = view.findViewById(R.id.tvUnit);
        
        actvFoodSearch = view.findViewById(R.id.actvFoodSearch);
        etQuantity = view.findViewById(R.id.etQuantity);
        etInstructions = view.findViewById(R.id.etInstructions);
        
        btnAddFood = view.findViewById(R.id.btnAddFood);
        btnViewAllFoods = view.findViewById(R.id.btnViewAllFoods);
        btnAssignPlan = view.findViewById(R.id.btnAssignPlan);
        lvAddedFoods = view.findViewById(R.id.lvAddedFoods);
        tabLayoutMeals = view.findViewById(R.id.tabLayoutMeals);
        
        tvTotalCalories = view.findViewById(R.id.tvTotalCalories);
        tvTotalProtein = view.findViewById(R.id.tvTotalProtein);
        tvTotalCarbs = view.findViewById(R.id.tvTotalCarbs);
        tvTotalFats = view.findViewById(R.id.tvTotalFats);

        // Setup Tabs
        tabLayoutMeals.addTab(tabLayoutMeals.newTab().setText("Breakfast"));
        tabLayoutMeals.addTab(tabLayoutMeals.newTab().setText("Lunch"));
        tabLayoutMeals.addTab(tabLayoutMeals.newTab().setText("Dinner"));
        tabLayoutMeals.addTab(tabLayoutMeals.newTab().setText("Snacks"));
    }

    private void initData() {
        trainerDAO = new TrainerDAO(getContext());
        mealPlanDAO = new MealPlanDAO(getContext());
        session = Session.getInstance(getContext());
        trainerId = session.getUserId();
        
        selectedDate = LocalDate.now();
        tvPlanDate.setText(selectedDate.toString());

        // Initialize State Maps
        mealMap = new HashMap<>();
        mealMap.put("Breakfast", new ArrayList<>());
        mealMap.put("Lunch", new ArrayList<>());
        mealMap.put("Dinner", new ArrayList<>());
        mealMap.put("Snacks", new ArrayList<>());
        
        instructionsMap = new HashMap<>();
        instructionsMap.put("Breakfast", "");
        instructionsMap.put("Lunch", "");
        instructionsMap.put("Dinner", "");
        instructionsMap.put("Snacks", "");

        currentDisplayList = new ArrayList<>();
        addedFoodsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, currentDisplayList);
        lvAddedFoods.setAdapter(addedFoodsAdapter);

        loadClients();
        loadFoods();
    }

    private void loadClients() {
        clientList = trainerDAO.getMyAssignedClients(trainerId);
        if (clientList == null) clientList = new ArrayList<>();
        
        List<String> names = new ArrayList<>();
        for (Member m : clientList) names.add(m.getFullName());
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClient.setAdapter(adapter);
    }

    private void loadFoods() {
        allFoods = mealPlanDAO.getAllFoods();
        // Setup AutoComplete
        // Note: For large DB, use CursorAdapter or custom Filter. For 100 items, basic Array is fine.
        List<String> foodNames = new ArrayList<>();
        // Helper map to lookup Food object by name quickly
        // Assuming unique names for simplicity
        for (Food f : allFoods) {
            foodNames.add(f.getName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, foodNames);
        actvFoodSearch.setAdapter(adapter);
    }
    
    // Helper to find food by name
    private Food findFoodByName(String name) {
        if(allFoods == null) return null;
        for(Food f : allFoods) {
            if(f.getName().equalsIgnoreCase(name)) return f;
        }
        return null;
    }

    private void setupListeners() {
        // Date Picker
        tvPlanDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    tvPlanDate.setText(selectedDate.toString());
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });

        // Tab Selection
        tabLayoutMeals.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Save instructions for previous tab
                instructionsMap.put(currentMealType, etInstructions.getText().toString());
                
                // Switch
                currentMealType = tab.getText().toString();
                tvAddFoodHeader.setText("Add Food to " + currentMealType);
                
                // Restore logic
                etInstructions.setText(instructionsMap.get(currentMealType));
                refreshFoodList();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Food Selection
        actvFoodSearch.setOnItemClickListener((parent, view, position, id) -> {
            String selection = (String) parent.getItemAtPosition(position);
            selectedFood = findFoodByName(selection);
            if (selectedFood != null) {
                tvUnit.setText(selectedFood.getServingUnit());
            }
        });

        // View All Foods
        btnViewAllFoods.setOnClickListener(v -> showAllFoodsDialog());

        // Add Food Button
        btnAddFood.setOnClickListener(v -> addFood());
        
        // Assign Button
        btnAssignPlan.setOnClickListener(v -> assignMealPlan());
    }

    private void addFood() {
        if (selectedFood == null) {
            Toast.makeText(getContext(), "Please select a food", Toast.LENGTH_SHORT).show();
            return;
        }
        String qtyStr = etQuantity.getText().toString();
        if (qtyStr.isEmpty()) {
            Toast.makeText(getContext(), "Enter quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        double qty = Double.parseDouble(qtyStr);
        
        MealPlanFood mpFood = new MealPlanFood();
        mpFood.setFoodId(selectedFood.getId());
        mpFood.setFood(selectedFood);
        mpFood.setQuantity(qty);
        
        mealMap.get(currentMealType).add(mpFood);
        
        // Reset inputs
        actvFoodSearch.setText("");
        etQuantity.setText("");
        selectedFood = null;
        
        refreshFoodList();
        updateTotals();
    }

    private void refreshFoodList() {
        currentDisplayList.clear();
        List<MealPlanFood> foods = mealMap.get(currentMealType);
        
        if (foods != null) {
            for (MealPlanFood mpf : foods) {
                Food f = mpf.getFood();
                double q = mpf.getQuantity();
                String item = f.getName() + " x " + q + " (" + (int)(f.getCalories() * q) + " kcal)";
                currentDisplayList.add(item);
            }
        }
        addedFoodsAdapter.notifyDataSetChanged();
    }

    private void updateTotals() {
        int totalCals = 0;
        double totalProt = 0, totalCarbs = 0, totalFats = 0;

        for (String type : mealMap.keySet()) {
            for (MealPlanFood mpf : mealMap.get(type)) {
                Food f = mpf.getFood();
                double q = mpf.getQuantity(); // Assuming multiplier for simplicity here. 
                // Note: user prompt said "100g". If serving unit is "100g", and user enters "2", it means 200g.
                // Assuming logic: Quantity is multiplier of base unit.
                
                totalCals += f.getCalories() * q;
                totalProt += f.getProtein() * q;
                totalCarbs += f.getCarbs() * q;
                totalFats += f.getFats() * q;
            }
        }

        tvTotalCalories.setText("Cals: " + totalCals);
        tvTotalProtein.setText(String.format("Prot: %.1fg", totalProt));
        tvTotalCarbs.setText(String.format("Carbs: %.1fg", totalCarbs));
        tvTotalFats.setText(String.format("Fats: %.1fg", totalFats));
    }

    private void assignMealPlan() {
        if (spinnerClient.getSelectedItem() == null) return;
        
        // Save current instructions
        instructionsMap.put(currentMealType, etInstructions.getText().toString());

        int clientIndex = spinnerClient.getSelectedItemPosition();
        Member member = clientList.get(clientIndex);

        List<MealPlan> plans = new ArrayList<>();
        String[] types = {"Breakfast", "Lunch", "Dinner", "Snacks"};
        boolean hasAnyFood = false;

        for (String type : types) {
            List<MealPlanFood> foods = mealMap.get(type);
            String inst = instructionsMap.get(type);

            // Create plan even if empty? Usually yes, to clear or show empty. 
            // Or only if has foods/instructions. Let's create all 4 to be safe structure.
            
            MealPlan plan = new MealPlan();
            plan.setTrainerId(trainerId);
            plan.setMemberId(member.getMemberId());
            plan.setPlanDate(selectedDate.toString());
            plan.setMealType(type);
            plan.setInstructions(inst);
            plan.setFoods(foods);
            
            if (!foods.isEmpty()) hasAnyFood = true;
            
            plans.add(plan);
        }

        if (!hasAnyFood) {
            Toast.makeText(getContext(), "Please add at least one food item", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mealPlanDAO.createMealPlans(plans)) {
            Toast.makeText(getContext(), "Meal Plan Assigned Successfully!", Toast.LENGTH_LONG).show();
            // Clear or navigate away?
            // Reset maybe?
        } else {
            Toast.makeText(getContext(), "Failed to save meal plan", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAllFoodsDialog() {
        if (allFoods == null || allFoods.isEmpty()) {
            Toast.makeText(getContext(), "No foods found", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] foodNames = new String[allFoods.size()];
        for (int i = 0; i < allFoods.size(); i++) {
            foodNames[i] = allFoods.get(i).toString(); // Using toString() which includes cals
        }

        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Select Food")
                .setItems(foodNames, (dialog, which) -> {
                    selectedFood = allFoods.get(which);
                    actvFoodSearch.setText(selectedFood.getName());
                    tvUnit.setText(selectedFood.getServingUnit());
                    actvFoodSearch.dismissDropDown(); // Hide autocomplete dropdown if it triggered
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

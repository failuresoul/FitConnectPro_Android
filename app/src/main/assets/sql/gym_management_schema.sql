-- ADMIN TABLES

CREATE TABLE Admins (
                        admin_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        password_hash TEXT NOT NULL,
                        full_name TEXT NOT NULL,
                        email TEXT NOT NULL UNIQUE,
                        phone TEXT,
                        role TEXT NOT NULL,
                        account_status TEXT DEFAULT 'ACTIVE',
                        created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                        last_login DATETIME
);

CREATE TABLE Admin_Logs (
                            log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                            admin_id INTEGER NOT NULL,
                            action_type TEXT NOT NULL,
                            target_table TEXT,
                            target_id INTEGER,
                            action_details TEXT,
                            timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (admin_id) REFERENCES Admins(admin_id)
);

-- TRAINER TABLES

CREATE TABLE Trainers (
                          trainer_id INTEGER PRIMARY KEY AUTOINCREMENT,
                          username TEXT UNIQUE NOT NULL,
                          password_hash TEXT NOT NULL,
                          full_name TEXT NOT NULL,
                          email TEXT UNIQUE NOT NULL,
                          phone TEXT,
                          specializations TEXT,
                          experience_years INTEGER DEFAULT 0,
                          certifications TEXT,
                          max_clients INTEGER DEFAULT 10,
                          current_clients INTEGER DEFAULT 0,
                          account_status TEXT DEFAULT 'ACTIVE',
                          salary REAL DEFAULT 0.0,
                          hired_by_admin_id INTEGER,
                          hire_date TEXT,
                          last_login TEXT,
                          FOREIGN KEY (hired_by_admin_id) REFERENCES Admins(admin_id)
);

CREATE TABLE Trainer_Applications (
                                      application_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                      full_name TEXT NOT NULL,
                                      email TEXT NOT NULL,
                                      phone TEXT,
                                      age INTEGER,
                                      education TEXT,
                                      certifications TEXT,
                                      experience_years INTEGER,
                                      specializations TEXT,
                                      cover_letter TEXT,
                                      application_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                                      status TEXT NOT NULL,
                                      reviewed_by_admin_id INTEGER,
                                      review_date DATETIME,
                                      FOREIGN KEY (reviewed_by_admin_id) REFERENCES Admins(admin_id)
);

CREATE TABLE Trainer_Member_Assignment (
                                           assignment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                           trainer_id INTEGER NOT NULL,
                                           member_id INTEGER NOT NULL,
                                           assigned_date DATE NOT NULL,
                                           end_date DATE,
                                           status TEXT NOT NULL,
                                           assigned_by_admin_id INTEGER,
                                           FOREIGN KEY (trainer_id) REFERENCES Trainers(trainer_id),
                                           FOREIGN KEY (member_id) REFERENCES Members(member_id),
                                           FOREIGN KEY (assigned_by_admin_id) REFERENCES Admins(admin_id)
);

CREATE TABLE Trainer_Workout_Plans (
                                       plan_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                       trainer_id INTEGER NOT NULL,
                                       member_id INTEGER NOT NULL,
                                       plan_date DATE NOT NULL,
                                       focus_area TEXT,
                                       total_duration INTEGER,
                                       expected_calories INTEGER,
                                       instructions TEXT,
                                       created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                                       status TEXT NOT NULL,
                                       FOREIGN KEY (trainer_id) REFERENCES Trainers(trainer_id),
                                       FOREIGN KEY (member_id) REFERENCES Members(member_id)
);

CREATE TABLE Trainer_Plan_Exercises (
                                        plan_exercise_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                        plan_id INTEGER NOT NULL,
                                        exercise_id INTEGER NOT NULL,
                                        sets INTEGER,
                                        reps INTEGER,
                                        weight REAL,
                                        rest_seconds INTEGER,
                                        trainer_notes TEXT,
                                        order_number INTEGER,
                                        FOREIGN KEY (plan_id) REFERENCES Trainer_Workout_Plans(plan_id),
                                        FOREIGN KEY (exercise_id) REFERENCES Exercises_Library(exercise_id)
);

CREATE TABLE Trainer_Daily_Goals (
                                     trainer_goal_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                     trainer_id INTEGER NOT NULL,
                                     member_id INTEGER NOT NULL,
                                     goal_date DATE NOT NULL,
                                     workout_duration INTEGER,
                                     calorie_target INTEGER,
                                     water_intake_ml INTEGER,
                                     calorie_limit INTEGER,
                                     protein_target INTEGER,
                                     carbs_target INTEGER,
                                     fats_target INTEGER,
                                     special_instructions TEXT,
                                     created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                                     FOREIGN KEY (trainer_id) REFERENCES Trainers(trainer_id),
                                     FOREIGN KEY (member_id) REFERENCES Members(member_id)
);

CREATE TABLE Trainer_Meal_Plans (
                                    meal_plan_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                    trainer_id INTEGER NOT NULL,
                                    member_id INTEGER NOT NULL,
                                    plan_date DATE NOT NULL,
                                    meal_type TEXT NOT NULL,
                                    meal_time TIME,
                                    foods TEXT,
                                    total_calories INTEGER,
                                    total_protein REAL,
                                    total_carbs REAL,
                                    total_fats REAL,
                                    preparation_instructions TEXT,
                                    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                                    FOREIGN KEY (trainer_id) REFERENCES Trainers(trainer_id),
                                    FOREIGN KEY (member_id) REFERENCES Members(member_id)
);

-- CORRECTED TRAINER SALARIES TABLE
CREATE TABLE Trainer_Salaries (
                                  salary_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                  trainer_id INTEGER NOT NULL,
                                  month INTEGER NOT NULL,
                                  year INTEGER NOT NULL,
                                  base_salary REAL NOT NULL,
                                  bonus REAL DEFAULT 0.0,
                                  deductions REAL DEFAULT 0.0,
                                  net_salary REAL NOT NULL,
                                  status TEXT DEFAULT 'PENDING' CHECK(status IN ('PENDING', 'PAID')),
                                  payment_date TEXT,
                                  processed_by_admin_id INTEGER NOT NULL,
                                  created_date TEXT NOT NULL,
                                  last_modified TEXT,
                                  FOREIGN KEY (trainer_id) REFERENCES Trainers(trainer_id) ON DELETE CASCADE,
                                  FOREIGN KEY (processed_by_admin_id) REFERENCES Admins(admin_id),
                                  UNIQUE(trainer_id, month, year)
);

CREATE TABLE Trainer_Member_Messages (
                                         message_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                         sender_id INTEGER NOT NULL,
                                         receiver_id INTEGER NOT NULL,
                                         sender_type TEXT NOT NULL,
                                         message_text TEXT NOT NULL,
                                         sent_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                                         read_status INTEGER DEFAULT 0,
                                         read_date DATETIME
);

-- MEMBER TABLES

CREATE TABLE Members (
                         member_id INTEGER PRIMARY KEY AUTOINCREMENT,
                         username TEXT NOT NULL UNIQUE,
                         password_hash TEXT NOT NULL,
                         full_name TEXT NOT NULL,
                         email TEXT NOT NULL UNIQUE,
                         phone TEXT,
                         date_of_birth DATE,
                         gender TEXT,
                         profile_photo TEXT,
                         membership_type TEXT NOT NULL,
                         membership_start DATE NOT NULL,
                         membership_end DATE NOT NULL,
                         account_status TEXT NOT NULL,
                         created_by_admin_id INTEGER,
                         created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                         last_login DATETIME,
                         FOREIGN KEY (created_by_admin_id) REFERENCES Admins(admin_id)
);

CREATE TABLE Member_Profiles (
                                 profile_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                 member_id INTEGER NOT NULL UNIQUE,
                                 height REAL,
                                 current_weight REAL,
                                 target_weight REAL,
                                 fitness_level TEXT,
                                 primary_goal TEXT,
                                 medical_notes TEXT,
                                 emergency_contact TEXT,
                                 privacy_setting TEXT,
                                 last_updated DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 FOREIGN KEY (member_id) REFERENCES Members(member_id)
);

-- FITNESS TRACKING TABLES

CREATE TABLE Goals (
                       goal_id INTEGER PRIMARY KEY AUTOINCREMENT,
                       member_id INTEGER NOT NULL,
                       goal_type TEXT NOT NULL,
                       goal_category TEXT,
                       description TEXT,
                       target_value REAL,
                       current_value REAL,
                       start_date DATE NOT NULL,
                       end_date DATE,
                       status TEXT NOT NULL,
                       achievement_date DATE,
                       FOREIGN KEY (member_id) REFERENCES Members(member_id)
);

CREATE TABLE Workouts (
                          workout_id INTEGER PRIMARY KEY AUTOINCREMENT,
                          member_id INTEGER NOT NULL,
                          workout_date DATE NOT NULL,
                          workout_time TIME,
                          workout_type TEXT,
                          total_duration INTEGER,
                          total_calories INTEGER,
                          difficulty_rating INTEGER,
                          notes TEXT,
                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (member_id) REFERENCES Members(member_id)
);

CREATE TABLE Exercises_Library (
                                   exercise_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                   exercise_name TEXT NOT NULL UNIQUE,
                                   category TEXT,
                                   muscle_group TEXT,
                                   equipment_needed TEXT,
                                   difficulty_level TEXT,
                                   instructions TEXT,
                                   video_link TEXT
);

CREATE TABLE Workout_Exercises (
                                   workout_exercise_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                   workout_id INTEGER NOT NULL,
                                   exercise_id INTEGER NOT NULL,
                                   sets INTEGER,
                                   reps INTEGER,
                                   weight REAL,
                                   duration INTEGER,
                                   order_number INTEGER,
                                   FOREIGN KEY (workout_id) REFERENCES Workouts(workout_id),
                                   FOREIGN KEY (exercise_id) REFERENCES Exercises_Library(exercise_id)
);

CREATE TABLE Meals (
                       meal_id INTEGER PRIMARY KEY AUTOINCREMENT,
                       member_id INTEGER NOT NULL,
                       meal_type TEXT NOT NULL,
                       meal_date DATE NOT NULL,
                       meal_time TIME,
                       total_calories INTEGER,
                       total_protein REAL,
                       total_carbs REAL,
                       total_fats REAL,
                       notes TEXT,
                       FOREIGN KEY (member_id) REFERENCES Members(member_id)
);

CREATE TABLE Foods_Database (
                                food_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                food_name TEXT NOT NULL,
                                serving_size TEXT,
                                calories_per_serving INTEGER,
                                protein REAL,
                                carbs REAL,
                                fats REAL,
                                category TEXT,
                                is_gym_recommended INTEGER DEFAULT 0
);

CREATE TABLE Meal_Items (
                            meal_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
                            meal_id INTEGER NOT NULL,
                            food_id INTEGER NOT NULL,
                            quantity REAL NOT NULL,
                            unit TEXT,
                            calculated_calories INTEGER,
                            FOREIGN KEY (meal_id) REFERENCES Meals(meal_id),
                            FOREIGN KEY (food_id) REFERENCES Foods_Database(food_id)
);

CREATE TABLE Water_Logs (
                            water_log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                            member_id INTEGER NOT NULL,
                            log_date DATE NOT NULL,
                            amount_ml INTEGER NOT NULL,
                            log_time TIME,
                            daily_total INTEGER,
                            FOREIGN KEY (member_id) REFERENCES Members(member_id)
);

CREATE TABLE Body_Measurements (
                                   measurement_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                   member_id INTEGER NOT NULL,
                                   measurement_date DATE NOT NULL,
                                   weight REAL,
                                   chest REAL,
                                   waist REAL,
                                   hips REAL,
                                   arms REAL,
                                   legs REAL,
                                   body_fat_percentage REAL,
                                   FOREIGN KEY (member_id) REFERENCES Members(member_id)
);

-- SOCIAL TABLES

-- Friend Requests Table (New for Phase 9)
CREATE TABLE IF NOT EXISTS Friend_Requests (
    request_id INTEGER PRIMARY KEY AUTOINCREMENT,
    sender_id INTEGER NOT NULL,
    receiver_id INTEGER NOT NULL,
    status TEXT DEFAULT 'PENDING' CHECK(status IN ('PENDING', 'ACCEPTED', 'REJECTED')),
    request_date TEXT NOT NULL,
    response_date TEXT,
    FOREIGN KEY (sender_id) REFERENCES Members(member_id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES Members(member_id) ON DELETE CASCADE,
    UNIQUE(sender_id, receiver_id)
);

CREATE TABLE Friendships (
                             friendship_id INTEGER PRIMARY KEY AUTOINCREMENT,
                             member_id_1 INTEGER NOT NULL,
                             member_id_2 INTEGER NOT NULL,
                             status TEXT NOT NULL,
                             requested_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                             accepted_date DATETIME,
                             FOREIGN KEY (member_id_1) REFERENCES Members(member_id),
                             FOREIGN KEY (member_id_2) REFERENCES Members(member_id)
);

CREATE TABLE Social_Activities (
                                   activity_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                   member_id INTEGER NOT NULL,
                                   activity_type TEXT NOT NULL,
                                   activity_description TEXT,
                                   visibility TEXT NOT NULL,
                                   timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                                   likes_count INTEGER DEFAULT 0,
                                   FOREIGN KEY (member_id) REFERENCES Members(member_id)
);

-- INDEXES

CREATE INDEX idx_admin_logs_admin_id ON Admin_Logs(admin_id);
CREATE INDEX idx_admin_logs_timestamp ON Admin_Logs(timestamp);
CREATE INDEX idx_trainers_email ON Trainers(email);
CREATE INDEX idx_trainers_status ON Trainers(account_status);
CREATE INDEX idx_trainer_applications_status ON Trainer_Applications(status);
CREATE INDEX idx_trainer_member_assignment_trainer ON Trainer_Member_Assignment(trainer_id);
CREATE INDEX idx_trainer_member_assignment_member ON Trainer_Member_Assignment(member_id);
CREATE INDEX idx_trainer_workout_plans_trainer ON Trainer_Workout_Plans(trainer_id);
CREATE INDEX idx_trainer_workout_plans_member ON Trainer_Workout_Plans(member_id);
CREATE INDEX idx_trainer_daily_goals_member ON Trainer_Daily_Goals(member_id);
CREATE INDEX idx_trainer_meal_plans_member ON Trainer_Meal_Plans(member_id);
CREATE INDEX idx_trainer_salaries_trainer ON Trainer_Salaries(trainer_id);
CREATE INDEX idx_trainer_salaries_status ON Trainer_Salaries(status);
CREATE INDEX idx_trainer_salaries_month_year ON Trainer_Salaries(month, year);
CREATE INDEX idx_trainer_messages_sender ON Trainer_Member_Messages(sender_id);
CREATE INDEX idx_trainer_messages_receiver ON Trainer_Member_Messages(receiver_id);
CREATE INDEX idx_members_email ON Members(email);
CREATE INDEX idx_members_status ON Members(account_status);
CREATE INDEX idx_goals_member ON Goals(member_id);
CREATE INDEX idx_workouts_member ON Workouts(member_id);
CREATE INDEX idx_workouts_date ON Workouts(workout_date);
CREATE INDEX idx_workout_exercises_workout ON Workout_Exercises(workout_id);
CREATE INDEX idx_meals_member ON Meals(member_id);
CREATE INDEX idx_meals_date ON Meals(meal_date);
CREATE INDEX idx_meal_items_meal ON Meal_Items(meal_id);
CREATE INDEX idx_water_logs_member ON Water_Logs(member_id);
CREATE INDEX idx_water_logs_date ON Water_Logs(log_date);
CREATE INDEX idx_body_measurements_member ON Body_Measurements(member_id);
CREATE INDEX idx_friendships_member1 ON Friendships(member_id_1);
CREATE INDEX idx_friendships_member2 ON Friendships(member_id_2);
CREATE INDEX idx_social_activities_member ON Social_Activities(member_id);

-- Add indexes for Friend_Requests
CREATE INDEX IF NOT EXISTS idx_friend_requests_receiver ON Friend_Requests(receiver_id);
CREATE INDEX IF NOT EXISTS idx_friend_requests_sender ON Friend_Requests(sender_id);
CREATE INDEX IF NOT EXISTS idx_friend_requests_status ON Friend_Requests(status);

-- SAMPLE DATA
-- Step 1: Delete all exercises
DELETE FROM Exercises_Library WHERE 1=1;

-- Step 2: Reset the autoincrement counter
DELETE FROM sqlite_sequence WHERE name='Exercises_Library';

-- Step 3: Insert exercises (IDs will start from 1)
INSERT INTO Exercises_Library (exercise_name, category, muscle_group, equipment_needed, difficulty_level, instructions) VALUES
                                                                                                                            ('Barbell Bench Press', 'Strength', 'Chest', 'Barbell, Bench', 'Intermediate', 'Lie on bench, lower bar to chest, press up'),
                                                                                                                            ('Dumbbell Bench Press', 'Strength', 'Chest', 'Dumbbells, Bench', 'Beginner', 'Lie on bench, press dumbbells up'),
                                                                                                                            ('Incline Bench Press', 'Strength', 'Chest', 'Barbell, Incline Bench', 'Intermediate', 'Press on incline bench, targets upper chest'),
                                                                                                                            ('Decline Bench Press', 'Strength', 'Chest', 'Barbell, Decline Bench', 'Intermediate', 'Press on decline bench, targets lower chest'),
                                                                                                                            ('Push-Ups', 'Bodyweight', 'Chest', 'None', 'Beginner', 'Standard push-up position, lower and press up'),
                                                                                                                            ('Dumbbell Flyes', 'Strength', 'Chest', 'Dumbbells, Bench', 'Intermediate', 'Lie on bench, arc dumbbells out and in'),
                                                                                                                            ('Cable Crossover', 'Strength', 'Chest', 'Cable Machine', 'Intermediate', 'Pull cables across body in arc motion'),
                                                                                                                            ('Dips (Chest)', 'Bodyweight', 'Chest', 'Dip Station', 'Intermediate', 'Lean forward, lower and press up'),
                                                                                                                            ('Deadlift', 'Strength', 'Back', 'Barbell', 'Advanced', 'Lift barbell from ground to standing position'),
                                                                                                                            ('Pull-Ups', 'Bodyweight', 'Back', 'Pull-up Bar', 'Intermediate', 'Hang from bar, pull body up'),
                                                                                                                            ('Barbell Row', 'Strength', 'Back', 'Barbell', 'Intermediate', 'Bent over, pull barbell to torso'),
                                                                                                                            ('Dumbbell Row', 'Strength', 'Back', 'Dumbbell, Bench', 'Beginner', 'One arm row, pull dumbbell to hip'),
                                                                                                                            ('Lat Pulldown', 'Strength', 'Back', 'Cable Machine', 'Beginner', 'Pull bar down to chest'),
                                                                                                                            ('Seated Cable Row', 'Strength', 'Back', 'Cable Machine', 'Beginner', 'Pull cable to torso'),
                                                                                                                            ('T-Bar Row', 'Strength', 'Back', 'T-Bar', 'Intermediate', 'Pull T-bar to chest'),
                                                                                                                            ('Face Pulls', 'Strength', 'Back', 'Cable Machine', 'Beginner', 'Pull rope to face level'),
                                                                                                                            ('Barbell Squat', 'Strength', 'Legs', 'Barbell, Rack', 'Intermediate', 'Lower into squat, press back up'),
                                                                                                                            ('Front Squat', 'Strength', 'Legs', 'Barbell, Rack', 'Advanced', 'Squat with bar on front shoulders'),
                                                                                                                            ('Leg Press', 'Strength', 'Legs', 'Leg Press Machine', 'Beginner', 'Push platform away with legs'),
                                                                                                                            ('Lunges', 'Strength', 'Legs', 'Dumbbells (optional)', 'Beginner', 'Step forward, lower back knee'),
                                                                                                                            ('Romanian Deadlift', 'Strength', 'Legs', 'Barbell', 'Intermediate', 'Hinge at hips, lower barbell'),
                                                                                                                            ('Leg Curl', 'Strength', 'Legs', 'Leg Curl Machine', 'Beginner', 'Curl legs up towards glutes'),
                                                                                                                            ('Leg Extension', 'Strength', 'Legs', 'Leg Extension Machine', 'Beginner', 'Extend legs from bent position'),
                                                                                                                            ('Calf Raises', 'Strength', 'Legs', 'Machine or Dumbbells', 'Beginner', 'Rise up on toes'),
                                                                                                                            ('Bulgarian Split Squat', 'Strength', 'Legs', 'Dumbbells, Bench', 'Intermediate', 'Rear foot elevated, squat down'),
                                                                                                                            ('Overhead Press', 'Strength', 'Shoulders', 'Barbell', 'Intermediate', 'Press barbell overhead'),
                                                                                                                            ('Dumbbell Shoulder Press', 'Strength', 'Shoulders', 'Dumbbells', 'Beginner', 'Press dumbbells overhead'),
                                                                                                                            ('Lateral Raises', 'Strength', 'Shoulders', 'Dumbbells', 'Beginner', 'Raise arms to sides'),
                                                                                                                            ('Front Raises', 'Strength', 'Shoulders', 'Dumbbells', 'Beginner', 'Raise arms to front'),
                                                                                                                            ('Rear Delt Flyes', 'Strength', 'Shoulders', 'Dumbbells', 'Beginner', 'Bend over, raise arms to sides'),
                                                                                                                            ('Arnold Press', 'Strength', 'Shoulders', 'Dumbbells', 'Intermediate', 'Rotating shoulder press'),
                                                                                                                            ('Upright Row', 'Strength', 'Shoulders', 'Barbell', 'Intermediate', 'Pull bar up along body'),
                                                                                                                            ('Barbell Curl', 'Strength', 'Arms', 'Barbell', 'Beginner', 'Curl barbell to shoulders'),
                                                                                                                            ('Dumbbell Curl', 'Strength', 'Arms', 'Dumbbells', 'Beginner', 'Curl dumbbells to shoulders'),
                                                                                                                            ('Hammer Curl', 'Strength', 'Arms', 'Dumbbells', 'Beginner', 'Curl with neutral grip'),
                                                                                                                            ('Tricep Dips', 'Bodyweight', 'Arms', 'Dip Station', 'Beginner', 'Lower and press up, arms focus'),
                                                                                                                            ('Tricep Pushdown', 'Strength', 'Arms', 'Cable Machine', 'Beginner', 'Push cable down, extend arms'),
                                                                                                                            ('Overhead Tricep Extension', 'Strength', 'Arms', 'Dumbbell', 'Beginner', 'Extend arms overhead'),
                                                                                                                            ('Skull Crushers', 'Strength', 'Arms', 'Barbell, Bench', 'Intermediate', 'Lower bar to forehead, extend'),
                                                                                                                            ('Cable Curl', 'Strength', 'Arms', 'Cable Machine', 'Beginner', 'Curl cable to shoulders'),
                                                                                                                            ('Plank', 'Bodyweight', 'Core', 'None', 'Beginner', 'Hold push-up position, engage core'),
                                                                                                                            ('Crunches', 'Bodyweight', 'Core', 'None', 'Beginner', 'Lift shoulders off ground'),
                                                                                                                            ('Russian Twists', 'Bodyweight', 'Core', 'None', 'Beginner', 'Rotate torso side to side'),
                                                                                                                            ('Leg Raises', 'Bodyweight', 'Core', 'None', 'Intermediate', 'Raise legs from lying position'),
                                                                                                                            ('Mountain Climbers', 'Bodyweight', 'Core', 'None', 'Intermediate', 'Alternate bringing knees to chest'),
                                                                                                                            ('Ab Wheel Rollout', 'Bodyweight', 'Core', 'Ab Wheel', 'Advanced', 'Roll wheel forward and back'),
                                                                                                                            ('Cable Woodchop', 'Strength', 'Core', 'Cable Machine', 'Intermediate', 'Rotate torso pulling cable'),
                                                                                                                            ('Hanging Knee Raises', 'Bodyweight', 'Core', 'Pull-up Bar', 'Intermediate', 'Raise knees while hanging'),
                                                                                                                            ('Treadmill Running', 'Cardio', 'Full Body', 'Treadmill', 'Beginner', 'Run at steady pace'),
                                                                                                                            ('Cycling', 'Cardio', 'Legs', 'Bike', 'Beginner', 'Pedal at steady pace'),
                                                                                                                            ('Rowing Machine', 'Cardio', 'Full Body', 'Rowing Machine', 'Beginner', 'Pull and push in rowing motion'),
                                                                                                                            ('Jump Rope', 'Cardio', 'Full Body', 'Jump Rope', 'Beginner', 'Jump continuously'),
                                                                                                                            ('Burpees', 'Cardio', 'Full Body', 'None', 'Intermediate', 'Squat, jump, push-up, repeat'),
                                                                                                                            ('Box Jumps', 'Cardio', 'Legs', 'Box', 'Intermediate', 'Jump onto elevated platform'),
                                                                                                                            ('Battle Ropes', 'Cardio', 'Full Body', 'Battle Ropes', 'Intermediate', 'Wave ropes up and down');

-- Populate Foods_Database with 100+ common foods

DELETE FROM Foods_Database WHERE 1=1;

INSERT INTO Foods_Database (food_name, serving_size, calories_per_serving, protein, carbs, fats, category, is_gym_recommended) VALUES

-- PROTEINS (Gym Recommended)
('Chicken Breast (Grilled)', '100g', 165, 31.0, 0.0, 3.6, 'Protein', 1),
('Salmon (Baked)', '100g', 206, 22.0, 0.0, 13.0, 'Protein', 1),
('Tuna (Canned in Water)', '100g', 116, 26.0, 0.0, 0.8, 'Protein', 1),
('Egg (Whole)', '1 large', 72, 6.3, 0.4, 4.8, 'Protein', 1),
('Egg White', '1 large', 17, 3.6, 0.2, 0.1, 'Protein', 1),
('Greek Yogurt (Plain)', '100g', 59, 10.0, 3.6, 0.4, 'Protein', 1),
('Cottage Cheese (Low-Fat)', '100g', 72, 12.4, 3.4, 1.0, 'Protein', 1),
('Whey Protein Powder', '1 scoop (30g)', 120, 24.0, 3.0, 1.5, 'Protein', 1),
('Turkey Breast', '100g', 135, 30.0, 0.0, 0.7, 'Protein', 1),
('Tilapia', '100g', 128, 26.0, 0.0, 2.7, 'Protein', 1),
('Shrimp', '100g', 99, 24.0, 0.2, 0.3, 'Protein', 1),
('Lean Beef (Sirloin)', '100g', 201, 27.0, 0.0, 10.0, 'Protein', 1),
('Tofu (Firm)', '100g', 144, 15.8, 3.5, 8.7, 'Protein', 1),
('Tempeh', '100g', 193, 19.0, 9.4, 11.0, 'Protein', 1),
('Edamame', '100g', 121, 11.2, 8.9, 5.2, 'Protein', 1),

-- CARBOHYDRATES (Gym Recommended)
('Brown Rice (Cooked)', '100g', 111, 2.6, 23.0, 0.9, 'Carbs', 1),
('Quinoa (Cooked)', '100g', 120, 4.4, 21.3, 1.9, 'Carbs', 1),
('Oatmeal (Cooked)', '100g', 71, 2.5, 12.0, 1.5, 'Carbs', 1),
('Sweet Potato (Baked)', '100g', 90, 2.0, 20.7, 0.2, 'Carbs', 1),
('White Potato (Baked)', '100g', 93, 2.5, 21.2, 0.1, 'Carbs', 1),
('Whole Wheat Bread', '1 slice (28g)', 69, 3.6, 11.6, 0.9, 'Carbs', 1),
('Whole Wheat Pasta (Cooked)', '100g', 124, 5.3, 26.5, 0.5, 'Carbs', 1),
('Banana', '1 medium (118g)', 105, 1.3, 27.0, 0.4, 'Carbs', 1),
('Apple', '1 medium (182g)', 95, 0.5, 25.0, 0.3, 'Carbs', 1),
('Blueberries', '100g', 57, 0.7, 14.5, 0.3, 'Carbs', 1),
('Strawberries', '100g', 32, 0.7, 7.7, 0.3, 'Carbs', 1),
('Orange', '1 medium (131g)', 62, 1.2, 15.4, 0.2, 'Carbs', 1),
('Grapes', '100g', 69, 0.7, 18.1, 0.2, 'Carbs', 1),
('Mango', '100g', 60, 0.8, 15.0, 0.4, 'Carbs', 1),
('Pineapple', '100g', 50, 0.5, 13.1, 0.1, 'Carbs', 1),

-- VEGETABLES (Gym Recommended)
('Broccoli (Cooked)', '100g', 35, 2.4, 7.2, 0.4, 'Vegetables', 1),
('Spinach (Raw)', '100g', 23, 2.9, 3.6, 0.4, 'Vegetables', 1),
('Kale (Raw)', '100g', 35, 2.9, 4.4, 1.5, 'Vegetables', 1),
('Asparagus', '100g', 20, 2.2, 3.9, 0.1, 'Vegetables', 1),
('Bell Pepper (Red)', '100g', 31, 1.0, 6.0, 0.3, 'Vegetables', 1),
('Cauliflower', '100g', 25, 1.9, 5.0, 0.3, 'Vegetables', 1),
('Zucchini', '100g', 17, 1.2, 3.1, 0.3, 'Vegetables', 1),
('Cucumber', '100g', 15, 0.7, 3.6, 0.1, 'Vegetables', 1),
('Tomato', '100g', 18, 0.9, 3.9, 0.2, 'Vegetables', 1),
('Carrots', '100g', 41, 0.9, 9.6, 0.2, 'Vegetables', 1),
('Green Beans', '100g', 31, 1.8, 7.0, 0.1, 'Vegetables', 1),
('Brussels Sprouts', '100g', 43, 3.4, 9.0, 0.3, 'Vegetables', 1),
('Lettuce (Romaine)', '100g', 17, 1.2, 3.3, 0.3, 'Vegetables', 1),
('Celery', '100g', 14, 0.7, 3.0, 0.2, 'Vegetables', 1),

-- HEALTHY FATS (Gym Recommended)
('Avocado', '100g', 160, 2.0, 8.5, 14.7, 'Fats', 1),
('Almonds', '28g (23 nuts)', 164, 6.0, 6.1, 14.2, 'Fats', 1),
('Walnuts', '28g (14 halves)', 185, 4.3, 3.9, 18.5, 'Fats', 1),
('Cashews', '28g', 157, 5.2, 8.6, 12.4, 'Fats', 1),
('Peanut Butter (Natural)', '2 tbsp (32g)', 188, 8.0, 7.0, 16.0, 'Fats', 1),
('Almond Butter', '2 tbsp (32g)', 196, 6.7, 6.0, 18.0, 'Fats', 1),
('Chia Seeds', '28g', 138, 4.7, 12.0, 8.7, 'Fats', 1),
('Flaxseeds (Ground)', '2 tbsp', 75, 2.6, 4.0, 6.0, 'Fats', 1),
('Olive Oil', '1 tbsp (14g)', 119, 0.0, 0.0, 13.5, 'Fats', 1),
('Coconut Oil', '1 tbsp (14g)', 117, 0.0, 0.0, 13.5, 'Fats', 1),
('Dark Chocolate (70-85%)', '28g', 170, 2.2, 13.0, 12.0, 'Fats', 1),

-- LEGUMES (Gym Recommended)
('Lentils (Cooked)', '100g', 116, 9.0, 20.1, 0.4, 'Legumes', 1),
('Chickpeas (Cooked)', '100g', 164, 8.9, 27.4, 2.6, 'Legumes', 1),
('Black Beans (Cooked)', '100g', 132, 8.9, 23.7, 0.5, 'Legumes', 1),
('Kidney Beans (Cooked)', '100g', 127, 8.7, 22.8, 0.5, 'Legumes', 1),
('Pinto Beans (Cooked)', '100g', 143, 9.0, 26.2, 0.7, 'Legumes', 1),

-- DAIRY
('Milk (Whole)', '1 cup (244g)', 149, 7.7, 11.7, 7.9, 'Dairy', 0),
('Milk (Skim)', '1 cup (244g)', 83, 8.3, 12.2, 0.2, 'Dairy', 1),
('Cheddar Cheese', '28g', 113, 7.0, 0.4, 9.3, 'Dairy', 0),
('Mozzarella Cheese (Part-Skim)', '28g', 72, 6.9, 0.8, 4.5, 'Dairy', 1),
('Parmesan Cheese', '28g', 111, 10.0, 0.9, 7.3, 'Dairy', 0),

-- PROCESSED/LESS HEALTHY (Not Gym Recommended)
('White Rice (Cooked)', '100g', 130, 2.7, 28.2, 0.3, 'Carbs', 0),
('White Bread', '1 slice (25g)', 67, 2.0, 13.0, 0.8, 'Carbs', 0),
('Pizza (Cheese)', '1 slice (107g)', 272, 12.2, 33.6, 9.8, 'Fast Food', 0),
('Burger (Fast Food)', '1 burger', 354, 14.6, 32.5, 17.9, 'Fast Food', 0),
('French Fries', '100g', 312, 3.4, 41.4, 14.5, 'Fast Food', 0),
('Soda (Cola)', '355ml', 140, 0.0, 39.0, 0.0, 'Beverages', 0),
('Ice Cream (Vanilla)', '100g', 207, 3.5, 23.6, 11.0, 'Desserts', 0),
('Chocolate Chip Cookie', '1 cookie (30g)', 140, 1.7, 18.0, 7.0, 'Desserts', 0),
('Potato Chips', '28g', 152, 2.0, 15.0, 10.0, 'Snacks', 0),
('Candy Bar', '1 bar (52g)', 250, 2.0, 34.0, 12.0, 'Desserts', 0),
('Donut (Glazed)', '1 donut (52g)', 192, 2.3, 22.9, 10.3, 'Desserts', 0),
('Bacon', '3 slices (24g)', 126, 9.0, 0.3, 9.9, 'Protein', 0),
('Sausage', '1 link (25g)', 96, 4.1, 0.8, 8.4, 'Protein', 0),
('Hot Dog', '1 hot dog (45g)', 151, 5.1, 1.8, 13.4, 'Fast Food', 0),

-- BEVERAGES
('Water', '1 cup (237ml)', 0, 0.0, 0.0, 0.0, 'Beverages', 1),
('Green Tea (Unsweetened)', '1 cup (237ml)', 2, 0.0, 0.0, 0.0, 'Beverages', 1),
('Black Coffee (Unsweetened)', '1 cup (237ml)', 2, 0.3, 0.0, 0.0, 'Beverages', 1),
('Orange Juice (Fresh)', '1 cup (248g)', 112, 1.7, 25.8, 0.5, 'Beverages', 0),
('Apple Juice', '1 cup (248g)', 114, 0.3, 28.0, 0.3, 'Beverages', 0),
('Sports Drink', '1 cup (240ml)', 50, 0.0, 14.0, 0.0, 'Beverages', 0),
('Protein Shake (Milk-Based)', '1 serving', 180, 20.0, 15.0, 3.0, 'Beverages', 1),

-- CONDIMENTS & EXTRAS
('Honey', '1 tbsp (21g)', 64, 0.1, 17.3, 0.0, 'Condiments', 0),
('Maple Syrup', '1 tbsp (20g)', 52, 0.0, 13.4, 0.0, 'Condiments', 0),
('Ketchup', '1 tbsp (17g)', 17, 0.2, 4.5, 0.0, 'Condiments', 0),
('Mayonnaise', '1 tbsp (13. 8g)', 94, 0.1, 0.1, 10.3, 'Condiments', 0),
('Mustard', '1 tsp (5g)', 3, 0.2, 0.3, 0.2, 'Condiments', 1),
('Salsa', '2 tbsp (32g)', 9, 0.4, 2.0, 0.1, 'Condiments', 1),
('Hummus', '2 tbsp (30g)', 70, 2.0, 6.0, 4.0, 'Condiments', 1),

-- GRAINS & CEREALS
('Granola', '100g', 471, 13.7, 64.4, 18.0, 'Carbs', 0),
('Cornflakes', '100g', 357, 7.9, 84.1, 0.4, 'Carbs', 0),
('Whole Wheat Tortilla', '1 tortilla (49g)', 130, 4.0, 22.0, 3.0, 'Carbs', 1),
('Pita Bread (Whole Wheat)', '1 pita (64g)', 170, 6.0, 35.0, 1.5, 'Carbs', 1),
('Bagel (Plain)', '1 bagel (89g)', 245, 9.0, 48.0, 1.5, 'Carbs', 0),
('English Muffin (Whole Wheat)', '1 muffin (66g)', 134, 6.0, 27.0, 1.0, 'Carbs', 1),
('Couscous (Cooked)', '100g', 112, 3.8, 23.2, 0.2, 'Carbs', 1),
('Barley (Cooked)', '100g', 123, 2.3, 28.2, 0.4, 'Carbs', 1),

-- FISH & SEAFOOD
('Cod', '100g', 82, 18.0, 0.0, 0.7, 'Protein', 1),
('Halibut', '100g', 111, 21.5, 0.0, 2.3, 'Protein', 1),
('Mackerel', '100g', 205, 18.6, 0.0, 13.9, 'Protein', 1),
('Sardines (Canned)', '100g', 208, 24.6, 0.0, 11.5, 'Protein', 1),
('Lobster', '100g', 89, 19.0, 0.5, 0.9, 'Protein', 1),
('Crab', '100g', 97, 19.4, 0.0, 1.5, 'Protein', 1),

-- MISC
('Popcorn (Air-Popped)', '100g', 387, 12.9, 77.8, 4.5, 'Snacks', 1),
('Rice Cakes', '1 cake (9g)', 35, 0.7, 7.3, 0.3, 'Snacks', 1),
('Protein Bar', '1 bar (60g)', 220, 20.0, 24.0, 7.0, 'Snacks', 1),
('Energy Bar', '1 bar (40g)', 190, 4.0, 28.0, 7.0, 'Snacks', 0);

-- Insert default admin (password: admin123)
INSERT INTO Admins (username, password_hash, full_name, email, phone, role, account_status, created_date)
VALUES ('admin', '$2a$10$xqYLkkP0RMKF0YdKhZGzAeZ. GD5YqWXHqVr3GlqN5o5yVqYqB6rqe',
        'System Administrator', 'admin@gym.com', '1234567890', 'SUPER_ADMIN', 'ACTIVE', datetime('now'));

-- Enable foreign keys
PRAGMA foreign_keys = ON;
